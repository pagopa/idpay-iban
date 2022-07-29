package it.gov.pagopa.iban.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PayloadCheckIbanDTO {

  private String validationStatus;
  private AccountCheckIbanDTO account;
  private AccountHolderCheckIbanDTO accountHolder;
  private BankInfoCheckIbanDTO bankInfo;

}
