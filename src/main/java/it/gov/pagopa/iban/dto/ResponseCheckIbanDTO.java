package it.gov.pagopa.iban.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseCheckIbanDTO {
  private String status;
  private List<ErrorCheckIbanDTO> errors;
  private PayloadCheckIbanDTO payload;

}
