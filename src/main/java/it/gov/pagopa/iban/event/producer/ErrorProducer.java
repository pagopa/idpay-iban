package it.gov.pagopa.iban.event.producer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Component
public class ErrorProducer {

  @Value("${spring.cloud.stream.bindings.ibanQueue-out-1.binder}")
  private String binderError;

  private final StreamBridge streamBridge;

  public ErrorProducer(StreamBridge streamBridge) {
    this.streamBridge = streamBridge;
  }

  public void sendEvent(Message<?> errorQueueDTO){
    streamBridge.send("ibanQueue-out-1",binderError, errorQueueDTO);
  }
}
