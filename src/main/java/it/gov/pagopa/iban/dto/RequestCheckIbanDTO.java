package it.gov.pagopa.iban.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor

public class RequestCheckIbanDTO {
  private AccountCheckIbanDTO account;
  private AccountHolderCheckIbanDTO accountHolder;

  public RequestCheckIbanDTO() {
    this.account = new AccountCheckIbanDTO();
    this.accountHolder = new AccountHolderCheckIbanDTO();
  }
}
