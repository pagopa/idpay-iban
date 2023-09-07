package it.gov.pagopa.iban.service;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import it.gov.pagopa.iban.checkiban.CheckIbanRestConnector;
import it.gov.pagopa.iban.constants.IbanConstants;
import it.gov.pagopa.iban.decrypt.DecryptRestConnector;
import it.gov.pagopa.iban.dto.DecryptedCfDTO;
import it.gov.pagopa.iban.dto.IbanDTO;
import it.gov.pagopa.iban.dto.IbanListDTO;
import it.gov.pagopa.iban.dto.IbanQueueDTO;
import it.gov.pagopa.iban.dto.IbanQueueWalletDTO;
import it.gov.pagopa.iban.dto.ResponseCheckIbanDTO;
import it.gov.pagopa.iban.event.producer.ErrorProducer;
import it.gov.pagopa.iban.event.producer.IbanProducer;
import it.gov.pagopa.iban.exception.IbanException;
import it.gov.pagopa.iban.model.IbanModel;
import it.gov.pagopa.iban.repository.IbanRepository;
import it.gov.pagopa.iban.utils.AuditUtilities;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class IbanServiceImpl implements IbanService {

  @Autowired private CheckIbanRestConnector checkIbanRestConnector;
  @Autowired private IbanRepository ibanRepository;
  @Autowired private DecryptRestConnector decryptRestConnector;

  @Autowired private ObjectMapper mapper;

  @Autowired IbanProducer ibanProducer;
  @Autowired ErrorProducer errorProducer;
  @Autowired
  AuditUtilities auditUtilities;

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
      performanceLog(startTime, "SAVE_IBAN");
      return;
    }
    log.info("[SAVE_IBAN] New IBAN {} enrolled from issuer: saving", iban.getIban());
    saveIbanFromIssuer(iban);
    performanceLog(startTime, "SAVE_IBAN");
  }

  private void performanceLog(long startTime, String flowName){
    log.info(
        "[PERFORMANCE_LOG] [{}] Time occurred to perform business logic: {} ms",
        flowName,
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
      long startTimeDecrypt = System.currentTimeMillis();
      DecryptedCfDTO decryptedCfDTO = decryptRestConnector.getPiiByToken(iban.getUserId());
      performanceLog(startTimeDecrypt, "SAVE_IBAN_DECRYPT");

      long startTimeCheckIban = System.currentTimeMillis();
      ResponseEntity<ResponseCheckIbanDTO> responseCheckIban = checkIbanRestConnector.checkIban(iban.getIban(), decryptedCfDTO.getPii());
      performanceLog(startTimeCheckIban, "SAVE_IBAN_CHECKIBAN");

      String checkIbanRequestId = Objects.requireNonNull(responseCheckIban.getHeaders().get("x-request-id")).get(0);
      checkIbanDTO = responseCheckIban.getBody();

      if (checkIbanDTO != null
          && checkIbanDTO.getPayload().getValidationStatus().equals(IbanConstants.OK)) {
        log.info("[SAVE_IBAN] [CHECK_IBAN_RESULT] CheckIban OK, statusCode: {}", responseCheckIban.getStatusCode().value());
        this.saveOk(iban, checkIbanDTO, checkIbanRequestId);
        auditUtilities.logCheckIbanOK(iban.getUserId(),iban.getInitiativeId(), iban.getIban(), checkIbanRequestId);
      } else {
        log.info("[SAVE_IBAN] [CHECK_IBAN_RESULT] CheckIban KO, statusCode: {}", responseCheckIban.getStatusCode().value());
        auditUtilities.logCheckIbanKO(iban.getUserId(),iban.getInitiativeId(),iban.getIban(), checkIbanRequestId);
        sendIbanToWallet(iban, IbanConstants.KO);
      }
    } catch (FeignException e) {
      log.info("[SAVE_IBAN] [CHECK_IBAN] Exception: {}, statusCode: {}", e.getMessage(), e.status());
      log.info(e.contentUTF8());
      String errorCode = null;
      String errorDescription = null;
      try {
        ResponseCheckIbanDTO responseCheckIbanDTO =
            mapper.readValue(e.contentUTF8(), ResponseCheckIbanDTO.class);
        if (responseCheckIbanDTO == null) {
          throw new IbanException(e.status(), e.contentUTF8());
        }
        if (responseCheckIbanDTO.getErrors() != null) {
          errorCode = responseCheckIbanDTO.getErrors().get(0).getCode();
          errorDescription = responseCheckIbanDTO.getErrors().get(0).getDescription();
        }
      } catch (JacksonException | IbanException exception) {
        errorCode = String.valueOf(e.status());
        errorDescription = e.contentUTF8();
      }
      if (e.status() == 501 || e.status() == 502) {
        log.info("[SAVE_IBAN] [CHECK_IBAN_RESULT] CheckIban UNKNOWN_PSP");
        String checkIbanRequestId = String.valueOf(e.responseHeaders().get("x-request-id").stream().toList().get(0));
        this.saveUnknown(iban, errorCode, errorDescription, checkIbanRequestId);
        auditUtilities.logCheckIbanUnknown(iban.getUserId(),iban.getInitiativeId(), iban.getIban(), checkIbanRequestId);
        return;
      }

      final MessageBuilder<?> errorMessage = MessageBuilder.withPayload(iban);
      sendToQueueError(e, errorMessage, ibanServer, ibanTopic);
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
      throw new IbanException(HttpStatus.NOT_FOUND.value(), "Iban not found.");
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
