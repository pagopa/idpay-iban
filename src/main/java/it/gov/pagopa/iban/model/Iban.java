package it.gov.pagopa.iban.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "enrollment_iban")
@CompoundIndex(name = "iban_unique_idx", def = "{'userId': 1, 'initiativeId': 1, 'iban': 1}}", unique = true)
public class Iban {



    @Id
    private String id;
    private String userId;
    private String initiativeId;
    private String iban;
    private String channel;
    private String status;
    private String checkIbanStatus;
    private LocalDateTime insertIbanTimestamp;
    private LocalDateTime checkIbanTimestamp;
    private boolean enabled;
}
