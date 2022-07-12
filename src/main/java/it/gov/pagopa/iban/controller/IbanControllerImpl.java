package it.gov.pagopa.iban.controller;

import it.gov.pagopa.iban.dto.IbanQueueDTO;
import it.gov.pagopa.iban.service.IbanService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IbanControllerImpl implements IbanController {

    @Autowired
    IbanService ibanService;

    @Override
    public ResponseEntity<List <IbanQueueDTO>> getIbanList(String userId) {
        List <IbanQueueDTO> ibanDTOList = ibanService.getIbanList(userId);
        return new ResponseEntity<>(ibanDTOList, HttpStatus.OK);
    }
}
