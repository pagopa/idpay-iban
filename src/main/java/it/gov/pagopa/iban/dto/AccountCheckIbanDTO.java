package it.gov.pagopa.iban.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountCheckIbanDTO {
  private String value;
  private String valueType;
  private String bicCode;
}
