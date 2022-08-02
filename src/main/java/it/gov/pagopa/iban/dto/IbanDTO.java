package it.gov.pagopa.iban.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class IbanDTO {
  private String iban;
  private String checkIbanStatus;
  private String holderBank;
  private String channel;
  private String description;
}
