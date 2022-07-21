package it.gov.pagopa.iban.event;

import it.gov.pagopa.iban.dto.IbanQueueDTO;
import it.gov.pagopa.iban.service.IbanService;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j


@Configuration
public class IbanConsumer {

  @Autowired
  IbanService ibanService;

  @Bean
  public Consumer<IbanQueueDTO> consumerIban(){
    return ibanService::saveIban;
  }

}
