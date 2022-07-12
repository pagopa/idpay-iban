package it.gov.pagopa.iban.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class IbanQueueDTO {

  private String userId;
  private String iban;

}
