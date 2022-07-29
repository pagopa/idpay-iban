package it.gov.pagopa.iban.controller;

import it.gov.pagopa.iban.dto.IbanDTO;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/idpay/iban")
public interface IbanController {
    /**
     * Get Iban
     *
     * @param userId
     * @return
     */
    @GetMapping("/{userId}")
    ResponseEntity<List<IbanDTO>> getIbanList(@PathVariable("userId") String userId);
}
