package com.poc.nats_api.services;

import io.nats.client.*;
import io.nats.client.api.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

@Service
public class NatsService {
    private static final String NATS_URL = System.getenv("NATS_URL");
    private static final String NATS_STREAM_NAME = System.getenv("NATS_STREAM_NAME");
    private static final String NATS_SUBJECT_NAME = System.getenv("NATS_SUBJECT_NAME");
    private static final String NATS_DURABLE_NAME = System.getenv("NATS_DURABLE_NAME");
    private static final int NATS_MAX_MESSAGES = Integer.parseInt(System.getenv("NATS_MAX_MESSAGES"));
    private static final int NATS_TIMEOUT = Integer.parseInt(System.getenv("NATS_TIMEOUT"));

    private Connection natsConnection;
    private JetStream jetStream;
    private JetStreamManagement jsm;
    private JetStreamSubscription sub;

    public void connect() throws IOException, InterruptedException {
        natsConnection = Nats.connect(NATS_URL);
        jsm = natsConnection.jetStreamManagement();
    }

    public void subscribeStream() throws IOException, JetStreamApiException {
        StreamConfiguration streamConfig = StreamConfiguration.builder()
                .name(NATS_STREAM_NAME)
                .subjects(NATS_SUBJECT_NAME)
                .storageType(StorageType.File)
                .build();

        jsm.addStream(streamConfig);
        jetStream = natsConnection.jetStream();

        ConsumerConfiguration consumerConfig = ConsumerConfiguration.builder()
                .durable(NATS_DURABLE_NAME)
                .build();

        PullSubscribeOptions options = PullSubscribeOptions.builder()
                .configuration(consumerConfig)
                .build();

        sub = jetStream.subscribe(NATS_SUBJECT_NAME, options);
    }

    public void publish(String message) throws IOException, JetStreamApiException {
        jetStream.publish(NATS_SUBJECT_NAME, message.getBytes());
    }

    public void consume() {
        while (true) {
            List<Message> messages = sub.fetch(NATS_MAX_MESSAGES, Duration.ofSeconds(NATS_TIMEOUT));
            if (messages.isEmpty()) {
                break;
            }

            for (Message msg : messages) {
                String messageData = new String(msg.getData());
                System.out.println("Mensagem recebida: " + messageData);
                System.out.println("Meta: " + msg.metaData());
                System.out.println("Menssagem processada com sucesso!");
                msg.ack();
            }
        }
    }
}