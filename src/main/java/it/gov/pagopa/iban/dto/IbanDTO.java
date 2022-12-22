package it.gov.pagopa.iban.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class IbanDTO {
  private String iban;
  private String checkIbanStatus;
  private String holderBank;
  private String channel;
  private String description;
}
