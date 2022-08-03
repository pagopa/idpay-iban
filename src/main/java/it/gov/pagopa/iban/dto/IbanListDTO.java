package it.gov.pagopa.iban.dto;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class IbanListDTO {
  List<IbanDTO> ibanList;

}
