package it.gov.pagopa.iban.service;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonpCharacterEscapes;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import it.gov.pagopa.iban.checkiban.CheckIbanRestConnector;
import it.gov.pagopa.iban.constants.IbanConstants;
import it.gov.pagopa.iban.dto.ResponseCheckIbanDTO;
import it.gov.pagopa.iban.dto.IbanDTO;
import it.gov.pagopa.iban.dto.IbanQueueDTO;
import it.gov.pagopa.iban.model.IbanModel;
import it.gov.pagopa.iban.repository.IbanRepository;
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


    public List <IbanDTO> getIbanList(String userId) {
        List <IbanModel> ibanList = ibanRepository.findByUserId(userId);
        List <IbanDTO> ibanDTOList = new ArrayList<>();
        for (IbanModel ibanModel : ibanList) {
        IbanDTO ibanDTO = new IbanDTO(ibanModel.getIban(), ibanModel.getCheckIbanStatus(),
            ibanModel.getHolderBank());
        ibanDTOList.add(ibanDTO);
        }
        return  ibanDTOList;
    }

    public void saveIban(IbanQueueDTO iban){
        log.info("----Service----");
        ResponseCheckIbanDTO checkIbanDTO = null;
        IbanModel ibanModel= new IbanModel();
        ibanModel.setUserId(iban.getUserId());
        ibanModel.setIban(iban.getIban());
        ibanModel.setQueueDate(LocalDateTime.parse(iban.getQueueDate()));

        try {
            checkIbanDTO = checkIbanRestConnector.checkIban(iban.getIban(), "TRNFNC96R02H501I");
            if(checkIbanDTO!=null){
                log.info("Risposta checkIban:"+checkIbanDTO);
                ibanModel.setCheckIbanResponseDate(LocalDateTime.now());
                ibanModel.setCheckIbanStatus(checkIbanDTO.getStatus());
                ibanModel.setBicCode(checkIbanDTO.getPayload().getBankInfo().getBicCode());
                ibanModel.setHolderBank(checkIbanDTO.getPayload().getBankInfo().getBusinessName());
            }
        }catch(FeignException e){
            log.info("exception: "+e.getMessage());
            ObjectMapper mapper = new ObjectMapper();
            String errorCode;
            String errorDescription;
            try {
                ResponseCheckIbanDTO responseCheckIbanDTO = mapper.readValue(e.contentUTF8(),
                    ResponseCheckIbanDTO.class);
                errorCode = responseCheckIbanDTO.getErrors().get(0).getCode();
                errorDescription =responseCheckIbanDTO.getErrors().get(0).getDescription();
            }catch (JacksonException exception){
                errorCode =String.valueOf(e.status());
                errorDescription=e.contentUTF8();
            }
            ibanModel.setErrorCode(errorCode);
            ibanModel.setCheckIbanResponseDate(LocalDateTime.now());
            ibanModel.setErrorDescription(errorDescription);
            switch (e.status()) {
                case 501,502:
                    ibanModel.setCheckIbanStatus(IbanConstants.UNKNOWN_PSP);
                    break;
                default:
                    ibanModel.setCheckIbanStatus(IbanConstants.KO);
            }
        }
        ibanRepository.save(ibanModel);
    }



}
