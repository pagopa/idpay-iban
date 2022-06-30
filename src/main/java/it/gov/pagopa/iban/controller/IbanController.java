package it.gov.pagopa.iban.controller;

import it.gov.pagopa.iban.dto.IbanDTO;
import it.gov.pagopa.iban.dto.IbanPutDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/idpay/iban")
public interface IbanController {
    /**
     * Enrollment Iban
     *
     * @param body
     * @return
     */
    @PutMapping("/enroll")
    ResponseEntity<Void> enrollmentIban(@RequestBody IbanPutDTO body);
    @GetMapping("/{userId}/{initiativeId}")
    ResponseEntity<IbanDTO> getIban(@PathVariable("userId") String userId, @PathVariable("initiativeId") String initiativeId);
}
