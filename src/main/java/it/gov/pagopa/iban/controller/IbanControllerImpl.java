package it.gov.pagopa.iban.controller;

import it.gov.pagopa.iban.dto.IbanDTO;
import it.gov.pagopa.iban.dto.IbanPutDTO;
import it.gov.pagopa.iban.service.IbanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
public class IbanControllerImpl implements IbanController {

    @Autowired
    IbanService ibanService;


    public ResponseEntity<Void> enrollmentIban(@Valid @RequestBody IbanPutDTO ibanPutDTO) {
        ibanService.putIban(ibanPutDTO.getInitiativeId(), ibanPutDTO.getUserId(), ibanPutDTO.getIban(), ibanPutDTO.getDescription());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public ResponseEntity<IbanDTO> getIban(String userId, String initiativeId) {
        IbanDTO ibanDTO = ibanService.getCurrentIban(userId, initiativeId);
        return new ResponseEntity<>(ibanDTO, HttpStatus.OK);
    }
}
