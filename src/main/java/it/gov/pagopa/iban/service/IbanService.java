package it.gov.pagopa.iban.service;

import it.gov.pagopa.iban.dto.IbanDTO;

public interface IbanService {
    void putIban(String initiativeId, String userId, String iban, String description);
    IbanDTO getCurrentIban(String initiativeId, String userId);
}
