package it.gov.pagopa.iban.event.producer;

import it.gov.pagopa.iban.dto.IbanQueueWalletDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
public class IbanProducer {
    @Value("${spring.cloud.stream.bindings.ibanQueue-out-0.binder}")
    private String binderIban;

    private final StreamBridge streamBridge;

    @Autowired
    public IbanProducer(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    public void sendIban(IbanQueueWalletDTO iban) {
        streamBridge.send("ibanQueue-out-0", binderIban, buildMessage(iban));
    }

    public static Message<IbanQueueWalletDTO> buildMessage(IbanQueueWalletDTO ibanQueueWalletDTO) {
        return MessageBuilder.withPayload(ibanQueueWalletDTO)
                .setHeader(KafkaHeaders.KEY, "%s_%s".formatted(ibanQueueWalletDTO.getUserId(), ibanQueueWalletDTO.getInitiativeId()))
                .build();
    }
}
