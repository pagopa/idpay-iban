package it.gov.pagopa.iban.dto;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor

public class IbanDTO {
    String iban;
    String description;
    String channel;
}
