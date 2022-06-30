package it.gov.pagopa.iban.repository;

import it.gov.pagopa.iban.model.Iban;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface IbanRepository extends MongoRepository<Iban, String> {
    List<Iban> findByInitiativeIdAndUserId(String initiativeId, String userId);
    Optional<Iban> findByInitiativeIdAndUserIdAndEnabledTrue(String initiativeId, String userId);
}
