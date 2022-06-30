package it.gov.pagopa.iban.dto;

import it.gov.pagopa.iban.constants.IbanConstants;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor

public class IbanPutDTO {
    @NotBlank(message = IbanConstants.ERROR_MANDATORY_FIELD)
    String userId;
    @NotBlank(message = IbanConstants.ERROR_MANDATORY_FIELD)
    String initiativeId;
    @NotBlank(message = IbanConstants.ERROR_MANDATORY_FIELD)
    String iban;
    @NotBlank(message = IbanConstants.ERROR_MANDATORY_FIELD)
    String description;
}
