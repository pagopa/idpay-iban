package it.gov.pagopa.iban.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountHolderCheckIbanDTO {
  private String type;
  private String fiscalCode;
  private String vatCode;
  private String taxCode;
}
