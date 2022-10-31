package it.gov.pagopa.iban.event.producer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Component
public class ErrorProducer {

  @Value("${spring.cloud.stream.bindings.IbanQueue-out-1.binder}")
  private String binderError;
  
  @Autowired
  StreamBridge streamBridge;

  public void sendEvent(Message<?> errorQueueDTO){
    streamBridge.send("IbanQueue-out-1",binderError, errorQueueDTO);
  }
}
