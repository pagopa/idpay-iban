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
import it.gov.pagopa.iban.event.IbanProducer;
import it.gov.pagopa.iban.exception.IbanException;
import it.gov.pagopa.iban.model.IbanModel;
import it.gov.pagopa.iban.repository.IbanRepository;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class IbanServiceImpl implements IbanService {

  @Autowired
  private CheckIbanRestConnector checkIbanRestConnector;
  @Autowired
  private IbanRepository ibanRepository;
  @Autowired
  private DecryptRestConnector decryptRestConnector;

  @Autowired
  IbanProducer ibanProducer;

  public IbanListDTO getIbanList(String userId) {
    List<IbanModel> ibanModelList = ibanRepository.findByUserId(userId);
    List<IbanDTO> ibanDTOList = new ArrayList<>();
    IbanListDTO ibanList = new IbanListDTO();
    if (ibanModelList.isEmpty()) {
      throw new IbanException(HttpStatus.NOT_FOUND.value(),
          String.format("No iban associated with the userId %s was found", userId));
    }
    ibanModelList.forEach(iban ->
        ibanDTOList.add(new IbanDTO(iban.getIban(), iban.getCheckIbanStatus(),
            iban.getHolderBank(), iban.getChannel(), iban.getDescription()))
    );
    ibanList.setIbanList(ibanDTOList);
    return ibanList;
  }

  public void saveIban(IbanQueueDTO iban) {
    ResponseCheckIbanDTO checkIbanDTO;
    try {
      Instant start = Instant.now();
      log.debug("Calling decrypting service at: " + start);
      DecryptedCfDTO decryptedCfDTO = decryptRestConnector.getPiiByToken(iban.getUserId());
      Instant finish = Instant.now();
      long time = Duration.between(start, finish).toMillis();
      log.info(
          "Decrypting finished at: " + finish + " The decrypting service took: " + time + "ms");
      checkIbanDTO = checkIbanRestConnector.checkIban(iban.getIban(), decryptedCfDTO.getPii());
      log.info("CF di test: " + decryptedCfDTO.getPii());
      if (checkIbanDTO != null && checkIbanDTO.getPayload().getValidationStatus().equals("OK")) {
        log.info("CheckIban's answer: " + checkIbanDTO);
        this.saveOk(iban,checkIbanDTO);
      }
    } catch (FeignException e) {
      log.info("Exception: " + e.getMessage());
      ObjectMapper mapper = new ObjectMapper();
      String errorCode;
      String errorDescription;
      try {
        ResponseCheckIbanDTO responseCheckIbanDTO = mapper.readValue(e.contentUTF8(),
            ResponseCheckIbanDTO.class);
        errorCode = responseCheckIbanDTO.getErrors().get(0).getCode();
        errorDescription = responseCheckIbanDTO.getErrors().get(0).getDescription();
      } catch (JacksonException exception) {
        errorCode = String.valueOf(e.status());
        errorDescription = e.contentUTF8();
      }
      if (e.status() == 501 || e.status() == 502) {
        this.saveUnknown(iban,errorCode,errorDescription);
      }else {
        IbanQueueWalletDTO ibanQueueWalletDTO = IbanQueueWalletDTO.builder()
            .userId(iban.getUserId())
            .iban(iban.getIban())
            .status(IbanConstants.KO)
            .queueDate(LocalDateTime.now().toString())
            .build();
        ibanProducer.sendIban(ibanQueueWalletDTO);
      }
    }
  }
  private void saveOk(IbanQueueDTO iban, ResponseCheckIbanDTO checkIbanDTO){
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

  }
  private void saveUnknown(IbanQueueDTO iban, String errorCode, String errorDescription){
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
  }

  @Override
  public IbanDTO getIban(String iban, String userId) {
    IbanModel ibanModel = ibanRepository.findByIbanAndUserId(iban, userId)
        .orElseThrow(() -> new IbanException(
            HttpStatus.NOT_FOUND.value(),
            String.format("Iban for userId %s not found.",
                userId)));
    return new IbanDTO(ibanModel.getIban(), ibanModel.getCheckIbanStatus(),
        ibanModel.getHolderBank(), ibanModel.getChannel(), ibanModel.getDescription());
  }
}
