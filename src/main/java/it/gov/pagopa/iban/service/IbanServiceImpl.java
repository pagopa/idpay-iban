package it.gov.pagopa.iban.service;

import it.gov.pagopa.iban.dto.IbanDTO;
import it.gov.pagopa.iban.exception.IbanException;
import it.gov.pagopa.iban.model.IbanModel;
import it.gov.pagopa.iban.repository.IbanRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.iban4j.CountryCode;
import org.iban4j.Iban;
import org.iban4j.IbanUtil;
import org.iban4j.UnsupportedCountryException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class IbanServiceImpl implements IbanService {

    @Autowired
    private IbanRepository ibanRepository;

    @Override
    public void putIban(String initiativeId, String userId, String iban, String description) {
        iban = iban.toUpperCase();
        this.formalControl(iban);
        IbanModel ibanSetted = ibanRepository.findByInitiativeIdAndUserIdAndEnabledTrue(initiativeId,userId).orElse(null);
        List<IbanModel> ibanList = ibanRepository.findByInitiativeIdAndUserId(initiativeId,userId);
        if(ibanSetted==null || !(ibanSetted.getIbanCode().equals(iban))) {
            //pub su coda
            this.disableOldIban(ibanList);
            IbanModel newIban = new IbanModel(userId, initiativeId, iban, description);
            newIban.setInsertIbanTimestamp(LocalDateTime.now());
            newIban.setEnabled(true);
            ibanRepository.save(newIban);
        }
    }

    @Override
    public IbanDTO getIban(String initiativeId, String userId) {
        IbanModel ibanSetted = ibanRepository.findByInitiativeIdAndUserIdAndEnabledTrue(initiativeId,userId).orElseThrow(() -> new IbanException(HttpStatus.NOT_FOUND.value(),
                String.format("Iban for initiativeId %s and userId %s not found.", initiativeId, userId)));
        return new IbanDTO(ibanSetted.getIbanCode(), ibanSetted.getDescription(), ibanSetted.getHolderBank(), ibanSetted.getChannel());
    }

    private void formalControl(String iban){
        Iban ibanValidator = Iban.valueOf(iban);
            IbanUtil.validate(iban);
            if(!ibanValidator.getCountryCode().equals(CountryCode.IT)){
                throw new UnsupportedCountryException(iban+" Iban is not italian");
            }
    }

    private void disableOldIban(List<IbanModel> ibanList) {
        if (ibanList != null && !ibanList.isEmpty()) {
            for (IbanModel iban : ibanList) {
                if (iban.isEnabled()) {
                    iban.setEnabled(false);
                    iban.setDeleteIbanTimestamp(LocalDateTime.now());
                    ibanRepository.save(iban);
                }
            }
        }
    }
}
