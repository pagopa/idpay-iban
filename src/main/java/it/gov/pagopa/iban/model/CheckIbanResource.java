package it.gov.pagopa.iban.model;

import java.util.List;

import lombok.Data;

@Data
public class CheckIbanResource {
  private String status;
  private List errors;
  private Payload payload;

  @Data
  public class Payload{
    private String validationStatus;

  }
}
