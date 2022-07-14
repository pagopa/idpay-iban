package it.gov.pagopa.iban.service;

import it.gov.pagopa.iban.checkiban.CheckIbanRestConnector;
import it.gov.pagopa.iban.dto.CheckIbanDTO;
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
        CheckIbanDTO checkIbanDTO = checkIbanRestConnector.checkIban(iban.getIban(),iban.getUserId());
        IbanModel ibanModel = new IbanModel(iban.getUserId(),iban.getIban(), checkIbanDTO.getStatus(),checkIbanDTO.getPayload().getBankInfo().getBicCode(), checkIbanDTO.getPayload().getBankInfo().getBusinessName(),
            LocalDateTime.parse(iban.getQueueDate()));
        ibanRepository.save(ibanModel);
    }


}
