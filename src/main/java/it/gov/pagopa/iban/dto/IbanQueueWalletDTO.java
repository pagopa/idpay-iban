package it.gov.pagopa.iban.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IbanQueueWalletDTO {
  private String userId;
  private String initiativeId;
  private String iban;
  private String status;
  private String queueDate;
  private String channel;
}
