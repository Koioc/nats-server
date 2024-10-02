package com.poc.nats_api;

// import io.nats.client.Connection;
// import io.nats.client.Nats;
// import io.nats.client.Subscription;
// import io.nats.client.Message;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestParam;
// import org.springframework.web.bind.annotation.RestController;

// import java.io.IOException;
// import java.time.Duration;

import io.nats.client.Connection;
import io.nats.client.JetStream;
import io.nats.client.JetStreamApiException;
import io.nats.client.JetStreamManagement;
import io.nats.client.JetStreamSubscription;
import io.nats.client.Message;
import io.nats.client.Nats;
import io.nats.client.PullSubscribeOptions;
import io.nats.client.api.ConsumerConfiguration;
import io.nats.client.api.PublishAck;
import io.nats.client.api.StorageType;
import io.nats.client.api.StreamConfiguration;
import io.nats.client.api.StreamInfo;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NatsController {

    private final Connection natsConnection;
    private final JetStream jetStream;
    private final JetStreamManagement jsm;
    private final JetStreamSubscription sub;

    private static final String STREAM_NAME = "lume";
    private static final String SUBJECT_NAME = "poc";
    private static final int DURATION_SECONDS = 10;
    private static final int MAX_MESSAGES = 10;
    private static final String DURABLE_CONSUMER_NAME = "lume-consumer";

    public NatsController() throws IOException, InterruptedException, JetStreamApiException {
        // Conectar ao servidor NATS
        natsConnection = Nats.connect("nats://0.0.0.0:4222");

        // Gerenciamento de JetStream
        jsm = natsConnection.jetStreamManagement();

        // Configuração do Stream
        StreamConfiguration streamConfig = StreamConfiguration.builder()
            .name(STREAM_NAME)
            .subjects(SUBJECT_NAME)
            .storageType(StorageType.Memory)
            .build();

        // Adicionar o Stream
        StreamInfo streamInfo = jsm.addStream(streamConfig);
        jetStream = natsConnection.jetStream();

        // Fechar a conexão
        // natsConnection.close();

        ConsumerConfiguration consumerConfig = ConsumerConfiguration.builder()
        .durable(DURABLE_CONSUMER_NAME)
        .build();

        PullSubscribeOptions options = PullSubscribeOptions.builder()
            .configuration(consumerConfig)
            .build();

        sub = jetStream.subscribe(SUBJECT_NAME, options);
    }

    @GetMapping("/nats")
    public String consumir() throws JetStreamApiException, IOException, InterruptedException {
        while (true) {
            List<Message> messages = sub.fetch(MAX_MESSAGES, Duration.ofSeconds(DURATION_SECONDS));
            if (messages.isEmpty()) {
                break;
            }

            for (Message msg : messages) {
                // Processar a mensagem
                String messageData = new String(msg.getData());
                System.out.println("Mensagem recebida: " + messageData);
                System.out.println("Meta: " + msg.metaData());

                // Simular processamento da mensagem
                boolean processingSuccessful = processMessage(messageData);

                if (processingSuccessful) {
                    System.out.println("Menssagem processada com sucesso!");
                    msg.ack();
                } else {
                    // Se o processamento falhar, a mensagem não será confirmada e será reentregue
                    System.out.println("Não foi possível processar a mensagem!");
                }

                System.out.println("--------------------------------");
            }
        }

        return "Mensagens consumidas!";
    }

    @PostMapping("/nats")
    public String publicar(@RequestParam String msg) throws JetStreamApiException, IOException {
        PublishAck ack = jetStream.publish(SUBJECT_NAME, msg.getBytes());
        return "Mensagem publicada: " + ack.getSeqno();
    }

    private static boolean processMessage(String message) {
        return !message.contains("erro");
    }
}
