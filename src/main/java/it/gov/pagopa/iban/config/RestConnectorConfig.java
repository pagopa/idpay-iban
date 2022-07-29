package it.gov.pagopa.iban.config;

import it.gov.pagopa.iban.checkiban.CheckIbanRestClient;
import it.gov.pagopa.iban.decrypt.DecryptRest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(clients = {CheckIbanRestClient.class, DecryptRest.class})
public class RestConnectorConfig {

}
