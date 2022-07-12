package it.gov.pagopa.iban.service;

import it.gov.pagopa.iban.dto.IbanQueueDTO;
import it.gov.pagopa.iban.model.IbanModel;
import it.gov.pagopa.iban.repository.IbanRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IbanServiceImpl implements IbanService {

    @Autowired
    private IbanRepository ibanRepository;


    public List <IbanQueueDTO> getIbanList(String userId) {
        List <IbanModel> ibanList = ibanRepository.findByUserId(userId);
        List <IbanQueueDTO> ibanQueueDTOList = new ArrayList<>();
        for (IbanModel ibanModel : ibanList) {
        IbanQueueDTO ibanQueueDTO = new IbanQueueDTO(ibanModel.getUserId(), ibanModel.getIban());
        ibanQueueDTOList.add(ibanQueueDTO);
        }
        return  ibanQueueDTOList;
    }

    public void saveIban(IbanQueueDTO iban){
        IbanModel ibanModel = new IbanModel(iban.getUserId(), iban.getIban(),null);
        ibanRepository.save(ibanModel);
    }

}
