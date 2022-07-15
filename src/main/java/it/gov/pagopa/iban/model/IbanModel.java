package it.gov.pagopa.iban.model;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "iban")
public class IbanModel {

    @Id
    private String id;
    private String userId;
    private String iban;
    private String checkIbanStatus;
    private String errorCode;
    private String errorDescription;
    private String bicCode;
    private String holderBank;

    private LocalDateTime queueDate;
    private LocalDateTime checkIbanResponseDate;

    public IbanModel(String userId, String iban, String checkIbanStatus, String bicCode, String holderBank, LocalDateTime queueDate) {
        this.userId = userId;
        this.iban = iban;
        this.checkIbanStatus = checkIbanStatus;
        this.bicCode = bicCode;
        this.holderBank = holderBank;
        this.queueDate = queueDate;
        this.checkIbanResponseDate = LocalDateTime.now();
    }
}
