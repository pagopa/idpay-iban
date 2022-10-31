package it.gov.pagopa.iban.repository;

import it.gov.pagopa.iban.model.IbanModel;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface IbanRepository extends MongoRepository<IbanModel, String> {
    List<IbanModel> findByUserId(String userId);
    Optional<IbanModel> findByIbanAndUserId(String iban, String userId);
}
