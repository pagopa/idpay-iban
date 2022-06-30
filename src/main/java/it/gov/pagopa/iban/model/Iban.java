package it.gov.pagopa.iban.model;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@Document(collection = "enrollment_iban")
@CompoundIndex(name = "iban_unique_idx", def = "{'userId': 1, 'initiativeId': 1, 'iban': 1}}", unique = true)
public class Iban {

    public Iban(String userId, String initiativeId, String iban, String description) {
        this.userId = userId;
        this.initiativeId = initiativeId;
        this.ibanCode = iban;
        this.channel = "APP-IO";
        this.description = description;
        this.holderBank = "Unicredit";
    }

    @Id
    private String id;
    private String userId;
    private String initiativeId;
    private String ibanCode;
    private String channel;
    private String description;
    private String holderBank;
    private boolean enabled;
    private String checkIbanStatus;
    private LocalDateTime insertIbanTimestamp;
    private LocalDateTime deleteIbanTimestamp;
    private LocalDateTime checkIbanTimestamp;
}
