package it.gov.pagopa.iban.repository;

import it.gov.pagopa.iban.model.IbanModel;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface IbanRepository extends MongoRepository<IbanModel, String> {
    List<IbanModel> findByInitiativeIdAndUserId(String initiativeId, String userId);
    Optional<IbanModel> findByInitiativeIdAndUserIdAndEnabledTrue(String initiativeId, String userId);
}
