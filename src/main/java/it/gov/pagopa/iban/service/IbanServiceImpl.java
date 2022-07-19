package it.gov.pagopa.iban.service;

import it.gov.pagopa.iban.checkiban.CheckIbanRestConnector;
import it.gov.pagopa.iban.dto.ResponseCheckIbanDTO;
import it.gov.pagopa.iban.dto.ErrorCheckIbanDTO;
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
        IbanModel ibanModel= new IbanModel();
        ibanModel.setUserId(iban.getUserId());
        ibanModel.setIban(iban.getIban());
        ibanModel.setQueueDate(LocalDateTime.parse(iban.getQueueDate()));
        ResponseCheckIbanDTO checkIbanDTO = checkIbanRestConnector.checkIban(iban.getIban(), "TRNFNC96R02H501I");
        if(checkIbanDTO!=null){
          log.info("Risposta checkIban:"+checkIbanDTO.toString());
          ibanModel.setCheckIbanResponseDate(LocalDateTime.now());
          ibanModel.setCheckIbanStatus(checkIbanDTO.getStatus());
          ibanModel.setBicCode(checkIbanDTO.getPayload().getBankInfo().getBicCode());
          ibanModel.setHolderBank(checkIbanDTO.getPayload().getBankInfo().getBusinessName());
        }
        ibanRepository.save(ibanModel);
    }


}
