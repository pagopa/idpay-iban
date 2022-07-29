package it.gov.pagopa.iban.service;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import it.gov.pagopa.iban.checkiban.CheckIbanRestConnector;
import it.gov.pagopa.iban.constants.IbanConstants;
import it.gov.pagopa.iban.decrypt.DecryptRestConnector;
import it.gov.pagopa.iban.dto.DecryptedCfDTO;
import it.gov.pagopa.iban.dto.IbanDTO;
import it.gov.pagopa.iban.dto.IbanQueueDTO;
import it.gov.pagopa.iban.dto.ResponseCheckIbanDTO;
import it.gov.pagopa.iban.model.IbanModel;
import it.gov.pagopa.iban.repository.IbanRepository;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

  public List<IbanDTO> getIbanList(String userId) {
    List<IbanModel> ibanList = ibanRepository.findByUserId(userId);
    List<IbanDTO> ibanDTOList = new ArrayList<>();
    for (IbanModel ibanModel : ibanList) {
      IbanDTO ibanDTO = new IbanDTO(ibanModel.getIban(), ibanModel.getCheckIbanStatus(),
          ibanModel.getHolderBank());
      ibanDTOList.add(ibanDTO);
    }
    return ibanDTOList;
  }

  public void saveIban(IbanQueueDTO iban) {
    ResponseCheckIbanDTO checkIbanDTO;
    IbanModel ibanModel = new IbanModel();
    ibanModel.setUserId(iban.getUserId());
    ibanModel.setIban(iban.getIban());
    ibanModel.setQueueDate(LocalDateTime.parse(iban.getQueueDate()));

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
      if (checkIbanDTO != null) {
        log.info("CheckIban's answer: " + checkIbanDTO);
        ibanModel.setCheckIbanResponseDate(LocalDateTime.now());
        ibanModel.setCheckIbanStatus(checkIbanDTO.getStatus());
        ibanModel.setBicCode(checkIbanDTO.getPayload().getBankInfo().getBicCode());
        ibanModel.setHolderBank(checkIbanDTO.getPayload().getBankInfo().getBusinessName());
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
      ibanModel.setErrorCode(errorCode);
      ibanModel.setCheckIbanResponseDate(LocalDateTime.now());
      ibanModel.setErrorDescription(errorDescription);
      if (e.status() == 501 || e.status() == 502) {
        ibanModel.setCheckIbanStatus(IbanConstants.UNKNOWN_PSP);
      } else {
        ibanModel.setCheckIbanStatus(IbanConstants.KO);

      }
    }
    ibanRepository.save(ibanModel);
  }


}
