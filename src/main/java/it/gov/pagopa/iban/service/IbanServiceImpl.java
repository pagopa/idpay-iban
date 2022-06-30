package it.gov.pagopa.iban.service;

import it.gov.pagopa.iban.dto.IbanDTO;
import it.gov.pagopa.iban.exception.IbanException;
import it.gov.pagopa.iban.model.Iban;
import it.gov.pagopa.iban.repository.IbanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class IbanServiceImpl implements IbanService {

    @Autowired
    private IbanRepository ibanRepository;

    @Override
    public void putIban(String initiativeId, String userId, String iban, String description) {
        this.formalControl(iban);

        Iban ibanSetted = ibanRepository.findByInitiativeIdAndUserIdAndEnabledTrue(initiativeId,userId).orElse(null);
        List<Iban> ibanList = ibanRepository.findByInitiativeIdAndUserId(initiativeId,userId);
        if(ibanSetted==null || !(ibanSetted.getIbanCode().equals(iban))) {
            //pub su coda
            this.disableOldIban(ibanList);
            Iban newIban = new Iban(userId, initiativeId, iban, description);
            newIban.setInsertIbanTimestamp(LocalDateTime.now());
            newIban.setEnabled(true);
            ibanRepository.save(newIban);
        }
    }

    @Override
    public IbanDTO getIban(String initiativeId, String userId) {
        Iban ibanSetted = ibanRepository.findByInitiativeIdAndUserIdAndEnabledTrue(initiativeId,userId).orElseThrow(() -> new IbanException(HttpStatus.NOT_FOUND.value(),
                String.format("Iban for initiativeId %s and userId %s not found.", initiativeId, userId)));
        return new IbanDTO(ibanSetted.getIbanCode(), ibanSetted.getDescription(), ibanSetted.getChannel());
    }

    private void formalControl(String iban){
        String ibanMessageWrong = "The iban %s is wrong";
        Pattern pattern = Pattern.compile("^(it|IT)[0-9]{2}[A-Za-z][0-9]{10}[0-9A-Za-z]{12}$");
        Matcher matcher = pattern.matcher(iban);
        if (!matcher.find()){
            String countryIban = iban.substring(0,2).toLowerCase(Locale.ROOT);
            if(!countryIban.equals("it")){
                ibanMessageWrong = "the iban %s is not italian";
            }
            throw new IbanException(HttpStatus.BAD_REQUEST.value(),
                    String.format(ibanMessageWrong, iban));
        }
    }

    private void disableOldIban(List<Iban> ibanList) {
        if (ibanList != null && !ibanList.isEmpty()) {
            for (Iban iban : ibanList) {
                if (iban.isEnabled()) {
                    iban.setEnabled(false);
                    iban.setDeleteIbanTimestamp(LocalDateTime.now());
                    ibanRepository.save(iban);
                }
            }
        }
    }
}
