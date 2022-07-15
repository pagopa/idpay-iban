package it.gov.pagopa.iban.service;

import it.gov.pagopa.iban.checkiban.CheckIbanRestConnector;
import it.gov.pagopa.iban.dto.CheckIbanDTO;
import it.gov.pagopa.iban.dto.ErrorCheckIbanDTO;
import it.gov.pagopa.iban.dto.IbanDTO;
import it.gov.pagopa.iban.dto.IbanQueueDTO;
import it.gov.pagopa.iban.model.IbanModel;
import it.gov.pagopa.iban.repository.IbanRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        IbanModel ibanModel= new IbanModel();
        ibanModel.setUserId(iban.getUserId());
        ibanModel.setIban(iban.getIban());
        ibanModel.setQueueDate(LocalDateTime.parse(iban.getQueueDate()));
        ibanModel.setCheckIbanResponseDate(LocalDateTime.now());
        CheckIbanDTO checkIbanDTO = new CheckIbanDTO();
        if(iban.getUserId().equals("test_user_test")) {
                checkIbanDTO = checkIbanRestConnector.checkIban(iban.getIban(),
                    iban.getUserId());
            }else if(iban.getUserId().equals("test_user_test_unknow_checkiban")) {
            checkIbanDTO.setStatus("UNKNOW");
            List<ErrorCheckIbanDTO> errorCheckIbanDTOS = new ArrayList<>();
            errorCheckIbanDTOS.add(
              new ErrorCheckIbanDTO("PGPA-0017", "PSP Not Present in Routing Subsystem", "account.value"));
            checkIbanDTO.setErrors(errorCheckIbanDTOS);
        } else {
                checkIbanDTO.setStatus("KO");
                List<ErrorCheckIbanDTO> errorCheckIbanDTOS = new ArrayList<>();
                errorCheckIbanDTOS.add(
                    new ErrorCheckIbanDTO("PGPA-0008", "Invalid IBAN code", "account.value"));
                checkIbanDTO.setErrors(errorCheckIbanDTOS);
        }
        if(checkIbanDTO.getStatus().equals("OK")) {
                ibanModel.setCheckIbanStatus(checkIbanDTO.getStatus());
                ibanModel.setBicCode(checkIbanDTO.getPayload().getBankInfo().getBicCode());
                ibanModel.setHolderBank(checkIbanDTO.getPayload().getBankInfo().getBusinessName());
        }else{
                ibanModel.setCheckIbanStatus(checkIbanDTO.getStatus());
                ibanModel.setErrorCode(checkIbanDTO.getErrors().get(0).getCode());
                ibanModel.setErrorDescription(checkIbanDTO.getErrors().get(0).getDescription());
            }
        ibanRepository.save(ibanModel);
    }


}
