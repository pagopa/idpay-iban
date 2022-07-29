package it.gov.pagopa.iban.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BankInfoCheckIbanDTO {
  private String businessName;
  private String city;
  private String countryCode;
  private String bicCode;
  private String branchName;
}
