package it.gov.pagopa.iban.config;

import it.gov.pagopa.common.web.dto.ErrorDTO;
import it.gov.pagopa.iban.constants.IbanConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IbanErrorManagerConfig {
    @Bean
    ErrorDTO defaultErrorDTO() {
        return new ErrorDTO(
                IbanConstants.ExceptionCode.GENERIC_ERROR,
                "A generic error occurred"
        );
    }

    @Bean
    ErrorDTO tooManyRequestsErrorDTO() {
        return new ErrorDTO(IbanConstants.ExceptionCode.TOO_MANY_REQUESTS, "Too Many Requests");
    }

    @Bean
    ErrorDTO templateValidationErrorDTO(){
        return new ErrorDTO(IbanConstants.ExceptionCode.INVALID_REQUEST, null);
    }
}
