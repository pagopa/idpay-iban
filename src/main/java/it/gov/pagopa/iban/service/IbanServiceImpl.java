package it.gov.pagopa.iban.service;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import it.gov.pagopa.iban.checkiban.CheckIbanRestConnector;
import it.gov.pagopa.iban.constants.IbanConstants;
import it.gov.pagopa.iban.decrypt.DecryptRestConnector;
import it.gov.pagopa.iban.dto.*;
import it.gov.pagopa.iban.event.producer.ErrorProducer;
import it.gov.pagopa.iban.event.producer.IbanProducer;
import it.gov.pagopa.iban.exception.CheckIbanInvocationException;
import it.gov.pagopa.iban.exception.IbanNotFoundException;
import it.gov.pagopa.iban.model.IbanModel;
import it.gov.pagopa.iban.repository.IbanRepository;
import it.gov.pagopa.iban.utils.AuditUtilities;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class IbanServiceImpl implements IbanService {

  private final CheckIbanRestConnector checkIbanRestConnector;
  private final IbanRepository ibanRepository;
  private final DecryptRestConnector decryptRestConnector;

  private final ObjectMapper mapper;

  private final IbanProducer ibanProducer;
  private final ErrorProducer errorProducer;

  private final AuditUtilities auditUtilities;

  @Value(
      "${spring.cloud.stream.binders.kafka-iban.environment.spring.cloud.stream.kafka.binder.brokers}")
  String ibanServer;

  @Value("${spring.cloud.stream.bindings.ibanQueue-in-0.destination}")
  String ibanTopic;

  @Value(
      "${spring.cloud.stream.binders.kafka-wallet.environment.spring.cloud.stream.kafka.binder.brokers}")
  String ibanWalletServer;

  @Value("${spring.cloud.stream.bindings.ibanQueue-out-0.destination}")
  String ibanWalletTopic;

  public IbanServiceImpl(CheckIbanRestConnector checkIbanRestConnector, IbanRepository ibanRepository, DecryptRestConnector decryptRestConnector, ObjectMapper mapper, IbanProducer ibanProducer, ErrorProducer errorProducer, AuditUtilities auditUtilities) {
    this.checkIbanRestConnector = checkIbanRestConnector;
    this.ibanRepository = ibanRepository;
    this.decryptRestConnector = decryptRestConnector;
    this.mapper = mapper;
    this.ibanProducer = ibanProducer;
    this.errorProducer = errorProducer;
    this.auditUtilities = auditUtilities;
  }

  public IbanListDTO getIbanList(String userId) {
    List<IbanModel> ibanModelList = ibanRepository.findByUserId(userId);
    List<IbanDTO> ibanDTOList = new ArrayList<>();
    IbanListDTO ibanList = new IbanListDTO();
    ibanModelList.forEach(
        iban ->
            ibanDTOList.add(
                new IbanDTO(
                    iban.getIban(),
                    iban.getCheckIbanStatus(),
                    iban.getDescription(),
                    iban.getChannel(),
                    iban.getHolderBank(),
                    iban.getCheckIbanResponseDate())));
    ibanList.setIbanList(ibanDTOList);
    return ibanList;
  }

  public void saveIban(IbanQueueDTO iban) {
    long startTime = System.currentTimeMillis();
    if(IbanConstants.CHANNEL_IO.equals(iban.getChannel())){
      log.info("[SAVE_IBAN] New IBAN {} enrolled from IO: sending to CheckIban", iban.getIban());
      checkIban(iban);
      doFinally(startTime);
      return;
    }
    log.info("[SAVE_IBAN] New IBAN {} enrolled from issuer: saving", iban.getIban());
    saveIbanFromIssuer(iban);
    doFinally(startTime);
  }

  private void doFinally(long startTime){
    log.info(
        "[PERFORMANCE_LOG] [SAVE_IBAN] Time occurred to perform business logic: {} ms",
        System.currentTimeMillis() - startTime);
  }

  private void saveIbanFromIssuer(IbanQueueDTO iban) {
    IbanModel ibanModel = new IbanModel();
    ibanModel.setUserId(iban.getUserId());
    ibanModel.setIban(iban.getIban());
    ibanModel.setChannel(iban.getChannel());
    ibanModel.setDescription(iban.getDescription());
    ibanModel.setQueueDate(LocalDateTime.parse(iban.getQueueDate()));
    ibanRepository.save(ibanModel);
    auditUtilities.logEnrollIbanFromIssuer(iban.getUserId(), iban.getInitiativeId(), iban.getIban());

    this.sendIbanToWallet(iban, IbanConstants.ISSUER_NO_CHECKIBAN);
  }

  private void checkIban(IbanQueueDTO iban){
    ResponseCheckIbanDTO checkIbanDTO;
    try {
      Instant start = Instant.now();
      log.debug("[SAVE_IBAN] [CHECK_IBAN] Calling decrypting service at: " + start);
      DecryptedCfDTO decryptedCfDTO = decryptRestConnector.getPiiByToken(iban.getUserId());
      Instant finish = Instant.now();
      long time = Duration.between(start, finish).toMillis();
      log.info(
          "[SAVE_IBAN] [CHECK_IBAN] Decrypting finished at: " + finish + " The decrypting service took: " + time + "ms");
      ResponseEntity<ResponseCheckIbanDTO> responseCheckIban = checkIbanRestConnector.checkIban(iban.getIban(), decryptedCfDTO.getPii());
      String checkIbanRequestId = Objects.requireNonNull(responseCheckIban.getHeaders().get("x-request-id")).get(0);
      checkIbanDTO = responseCheckIban.getBody();
      if (checkIbanDTO != null
          && checkIbanDTO.getPayload().getValidationStatus().equals(IbanConstants.OK)) {
        log.info("[SAVE_IBAN] [CHECK_IBAN_RESULT] CheckIban OK");
        this.saveOk(iban, checkIbanDTO, checkIbanRequestId);
        auditUtilities.logCheckIbanOK(iban.getUserId(),iban.getInitiativeId(), iban.getIban(), checkIbanRequestId);
      } else {
        log.info("[SAVE_IBAN] [CHECK_IBAN_RESULT] CheckIban KO");
        auditUtilities.logCheckIbanKO(iban.getUserId(),iban.getInitiativeId(),iban.getIban(), checkIbanRequestId);
        sendIbanToWallet(iban, IbanConstants.KO);
      }
    } catch (FeignException e) {
      log.info("[SAVE_IBAN] [CHECK_IBAN] Exception: " + e.getMessage());
      log.info(e.contentUTF8());
      String errorCode = null;
      String errorDescription = null;
      try {
        ResponseCheckIbanDTO responseCheckIbanDTO =
            mapper.readValue(e.contentUTF8(), ResponseCheckIbanDTO.class);
        if (responseCheckIbanDTO == null) {
          throw new CheckIbanInvocationException(IbanConstants.ExceptionMessage.CHECKIBAN_INVOCATION_ERROR_MSG, true, e);
        }
        if (responseCheckIbanDTO.getErrors() != null) {
          errorCode = responseCheckIbanDTO.getErrors().get(0).getCode();
          errorDescription = responseCheckIbanDTO.getErrors().get(0).getDescription();
        }
      } catch (JacksonException | CheckIbanInvocationException exception) {
        errorCode = String.valueOf(e.status());
        errorDescription = e.contentUTF8();
      }
      if (e.status() == 501 || e.status() == 502) {
        log.info("[SAVE_IBAN] [CHECK_IBAN_RESULT] CheckIban UNKNOWN_PSP");
        String checkIbanRequestId = String.valueOf(e.responseHeaders().get("x-request-id").stream().toList().get(0));
        this.saveUnknown(iban, errorCode, errorDescription, checkIbanRequestId);
        auditUtilities.logCheckIbanUnknown(iban.getUserId(),iban.getInitiativeId(), iban.getIban(), checkIbanRequestId);
        return;
      } else if (e.status() == 429) {
        final MessageBuilder<?> errorMessage = MessageBuilder.withPayload(iban);
        sendToQueueError(e, errorMessage, ibanServer, ibanTopic);
        return;
      }
      sendIbanToWallet(iban, IbanConstants.KO);
    }
  }

  private void sendIbanToWallet(IbanQueueDTO iban, String status) {
    IbanQueueWalletDTO ibanQueueWalletDTO =
        IbanQueueWalletDTO.builder()
            .userId(iban.getUserId())
            .iban(iban.getIban())
            .initiativeId(iban.getInitiativeId())
            .status(status)
            .queueDate(LocalDateTime.now().toString())
            .channel(iban.getChannel())
            .build();
    try {
      ibanProducer.sendIban(ibanQueueWalletDTO);
    } catch (Exception e) {
      final MessageBuilder<?> errorMessage = MessageBuilder.withPayload(ibanQueueWalletDTO);
      sendToQueueError(e, errorMessage, ibanWalletServer, ibanWalletTopic);
    }
  }

  private void sendToQueueError(
      Exception e, MessageBuilder<?> errorMessage, String server, String topic) {

    errorMessage
        .setHeader(IbanConstants.ERROR_MSG_HEADER_SRC_TYPE, IbanConstants.KAFKA)
        .setHeader(IbanConstants.ERROR_MSG_HEADER_SRC_SERVER, server)
        .setHeader(IbanConstants.ERROR_MSG_HEADER_SRC_TOPIC, topic)
        .setHeader(IbanConstants.ERROR_MSG_HEADER_DESCRIPTION, IbanConstants.ERROR_IBAN)
        .setHeader(IbanConstants.ERROR_MSG_HEADER_RETRYABLE, true)
        .setHeader(IbanConstants.ERROR_MSG_HEADER_STACKTRACE, e.getStackTrace())
        .setHeader(IbanConstants.ERROR_MSG_HEADER_CLASS, e.getClass())
        .setHeader(IbanConstants.ERROR_MSG_HEADER_MESSAGE, e.getMessage());
    errorProducer.sendEvent(errorMessage.build());
  }

  private void saveOk(IbanQueueDTO iban, ResponseCheckIbanDTO checkIbanDTO, String requestId) {

    IbanModel ibanModel = new IbanModel();
    ibanModel.setUserId(iban.getUserId());
    ibanModel.setIban(iban.getIban());
    ibanModel.setChannel(iban.getChannel());
    ibanModel.setDescription(iban.getDescription());
    ibanModel.setQueueDate(LocalDateTime.parse(iban.getQueueDate()));
    ibanModel.setCheckIbanResponseDate(LocalDateTime.now());
    ibanModel.setCheckIbanStatus(checkIbanDTO.getPayload().getValidationStatus());
    if (checkIbanDTO.getPayload().getBankInfo() != null) {
      ibanModel.setBicCode(checkIbanDTO.getPayload().getBankInfo().getBicCode());
      ibanModel.setHolderBank(checkIbanDTO.getPayload().getBankInfo().getBusinessName());
    }
    ibanModel.setCheckIbanRequestId(requestId);
    ibanRepository.save(ibanModel);
    auditUtilities.logEnrollIban(iban.getUserId(), iban.getInitiativeId(), iban.getIban());

    this.sendIbanToWallet(iban, IbanConstants.OK);
  }

  private void saveUnknown(IbanQueueDTO iban, String errorCode, String errorDescription, String requestId) {
    IbanModel ibanModel = new IbanModel();
    ibanModel.setUserId(iban.getUserId());
    ibanModel.setIban(iban.getIban());
    ibanModel.setChannel(iban.getChannel());
    ibanModel.setDescription(iban.getDescription());
    ibanModel.setQueueDate(LocalDateTime.parse(iban.getQueueDate()));
    ibanModel.setCheckIbanResponseDate(LocalDateTime.now());
    ibanModel.setErrorCode(errorCode);
    ibanModel.setCheckIbanResponseDate(LocalDateTime.now());
    ibanModel.setErrorDescription(errorDescription);
    ibanModel.setCheckIbanStatus(IbanConstants.UNKNOWN_PSP);
    ibanModel.setCheckIbanRequestId(requestId);
    ibanRepository.save(ibanModel);
    auditUtilities.logEnrollIban(iban.getUserId(), iban.getInitiativeId(), iban.getIban());

    this.sendIbanToWallet(iban, IbanConstants.UNKNOWN_PSP);
  }

  @Override
  public IbanDTO getIban(String iban, String userId) {
    List<IbanModel> ibanModelList =
        ibanRepository
            .findByIbanAndUserId(iban, userId);
    if (ibanModelList.isEmpty()) {
      throw new IbanNotFoundException(IbanConstants.ExceptionMessage.IBAN_NOT_FOUND_MSG);
    }
    ibanModelList.sort(Comparator.comparing(IbanModel::getCheckIbanResponseDate).reversed());
    return new IbanDTO(
            ibanModelList.get(0).getIban(),
            ibanModelList.get(0).getCheckIbanStatus(),
            ibanModelList.get(0).getDescription(),
            ibanModelList.get(0).getChannel(),
            ibanModelList.get(0).getHolderBank(),
            ibanModelList.get(0).getCheckIbanResponseDate());
  }
}
