package it.gov.pagopa.iban.controller;

import it.gov.pagopa.iban.dto.IbanDTO;
import it.gov.pagopa.iban.dto.IbanListDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/idpay/iban")
public interface IbanController {
    /**
     * Get IbanList
     *
     * @param userId
     * @return
     */
    @GetMapping("/{userId}")
    ResponseEntity<IbanListDTO> getIbanList(@Valid @PathVariable("userId") String userId);


    /**
     * Get Iban
     *
     * @param iban
     * @param userId
     * @return
     */
    @GetMapping("/{iban}/{userId}")
    ResponseEntity<IbanDTO> getIban(@Valid @PathVariable("iban") String iban, @PathVariable("userId") String userId);

}
