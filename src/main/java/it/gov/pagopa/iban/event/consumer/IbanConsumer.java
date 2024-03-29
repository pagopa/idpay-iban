package it.gov.pagopa.iban.event.consumer;

import it.gov.pagopa.iban.dto.IbanQueueDTO;
import it.gov.pagopa.iban.service.IbanService;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j


@Configuration
public class IbanConsumer {
  @Bean
  public Consumer<IbanQueueDTO> ibanQueue(IbanService ibanService){
    return ibanService::saveIban;
  }

}

