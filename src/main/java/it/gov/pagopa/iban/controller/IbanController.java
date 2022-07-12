package it.gov.pagopa.iban.controller;

import it.gov.pagopa.iban.dto.IbanQueueDTO;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    ResponseEntity<List<IbanQueueDTO>> getIbanList(@PathVariable("userId") String userId);
}
