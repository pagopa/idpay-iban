package it.gov.pagopa.iban.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class IbanQueueDTO {

  private String userId;
  private String initiativeId;
  private String iban;
  private String channel;
  private String description;
  private String queueDate;

}
