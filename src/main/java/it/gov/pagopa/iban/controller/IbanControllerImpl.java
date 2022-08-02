package it.gov.pagopa.iban.controller;

import it.gov.pagopa.iban.dto.IbanDTO;
import it.gov.pagopa.iban.service.IbanService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class IbanControllerImpl implements IbanController {

    @Autowired
    IbanService ibanService;

    @Override
    public ResponseEntity<List <IbanDTO>> getIbanList(String userId) {
        List <IbanDTO> ibanDTOList = ibanService.getIbanList(userId);
        return new ResponseEntity<>(ibanDTOList, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<IbanDTO> getIban(String iban, String userId) {
        IbanDTO ibanDTO = ibanService.getIban(iban, userId);
        return new ResponseEntity<>(ibanDTO, HttpStatus.OK);
    }
}
