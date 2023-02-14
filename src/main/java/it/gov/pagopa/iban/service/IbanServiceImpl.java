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
import it.gov.pagopa.iban.utils.Utilities;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
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
  @Autowired Utilities utilities;

  @Value(
      "${spring.cloud.stream.binders.kafka-iban.environment.spring.cloud.stream.kafka.binder.brokers}")
  String ibanServer;

  @Value("${spring.cloud.stream.bindings.IbanQueue-in-0.destination}")
  String ibanTopic;

  @Value(
      "${spring.cloud.stream.binders.kafka-wallet.environment.spring.cloud.stream.kafka.binder.brokers}")
  String ibanWalletServer;

  @Value("${spring.cloud.stream.bindings.IbanQueue-out-0.destination}")
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
      log.info("[SAVE_IBAN] New IBAN enrolled from IO: sending to CheckIban");
      checkIban(iban);
      doFinally(startTime);
      return;
    }
    log.info("[SAVE_IBAN] New IBAN enrolled from issuer: saving");
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
    utilities.logEnrollIbanFromIssuer(iban.getUserId(), iban.getInitiativeId(), iban.getIban());

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
      checkIbanDTO = checkIbanRestConnector.checkIban(iban.getIban(), decryptedCfDTO.getPii());
      if (checkIbanDTO != null
          && checkIbanDTO.getPayload().getValidationStatus().equals(IbanConstants.OK)) {
        log.info("[SAVE_IBAN] [CHECK_IBAN] CheckIban OK");
        this.saveOk(iban, checkIbanDTO);
        utilities.logCheckIbanOK(iban.getUserId(),iban.getInitiativeId(), iban.getIban());
      } else {
        log.info("[SAVE_IBAN] [CHECK_IBAN] CheckIban KO");
        utilities.logCheckIbanKO(iban.getUserId(),iban.getInitiativeId(),iban.getIban());
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
        log.info("[SAVE_IBAN] [CHECK_IBAN] CheckIban UNKNOWN_PSP");
        this.saveUnknown(iban, errorCode, errorDescription);
        utilities.logCheckIbanUnknown(iban.getUserId(),iban.getInitiativeId(), iban.getIban());
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

  private void saveOk(IbanQueueDTO iban, ResponseCheckIbanDTO checkIbanDTO) {
    IbanModel ibanModel = new IbanModel();
    ibanModel.setUserId(iban.getUserId());
    ibanModel.setIban(iban.getIban());
    ibanModel.setChannel(iban.getChannel());
    ibanModel.setDescription(iban.getDescription());
    ibanModel.setQueueDate(LocalDateTime.parse(iban.getQueueDate()));
    ibanModel.setCheckIbanResponseDate(LocalDateTime.now());
    ibanModel.setCheckIbanStatus(checkIbanDTO.getPayload().getValidationStatus());
    ibanModel.setBicCode(checkIbanDTO.getPayload().getBankInfo().getBicCode());
    ibanModel.setHolderBank(checkIbanDTO.getPayload().getBankInfo().getBusinessName());
    ibanRepository.save(ibanModel);
    utilities.logEnrollIban(iban.getUserId(), iban.getInitiativeId(), iban.getIban());

    this.sendIbanToWallet(iban, IbanConstants.OK);
  }

  private void saveUnknown(IbanQueueDTO iban, String errorCode, String errorDescription) {
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
    ibanRepository.save(ibanModel);
    utilities.logEnrollIban(iban.getUserId(), iban.getInitiativeId(), iban.getIban());

    this.sendIbanToWallet(iban, IbanConstants.UNKNOWN_PSP);
  }

  @Override
  public IbanDTO getIban(String iban, String userId) {
    IbanModel ibanModel =
        ibanRepository
            .findByIbanAndUserId(iban, userId)
            .orElseThrow(() -> new IbanException(HttpStatus.NOT_FOUND.value(), "Iban not found."));
    return new IbanDTO(
            ibanModel.getIban(),
            ibanModel.getCheckIbanStatus(),
            ibanModel.getDescription(),
            ibanModel.getChannel(),
            ibanModel.getHolderBank(),
            ibanModel.getCheckIbanResponseDate());
  }
}
