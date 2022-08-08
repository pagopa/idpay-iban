package it.gov.pagopa.iban.event;

import it.gov.pagopa.iban.dto.IbanQueueWalletDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

@Component
public class IbanProducer {
  @Value("${spring.cloud.stream.bindings.IbanQueue-out-0.binder}")
  private String binderIban;
  @Autowired
  StreamBridge streamBridge;

  public void sendIban(IbanQueueWalletDTO iban){
    streamBridge.send("IbanQueue-out-0", binderIban, iban);
  }

}
