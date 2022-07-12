package it.gov.pagopa.iban.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@NoArgsConstructor
@Document(collection = "iban")
public class IbanModel {

    @Id
    private String id;
    private String userId;
    private String iban;
    private String checkIbanStatus;

    public IbanModel(String userId, String iban, String checkIbanStatus) {
        this.userId = userId;
        this.iban = iban;
        this.checkIbanStatus = checkIbanStatus;
    }
}
