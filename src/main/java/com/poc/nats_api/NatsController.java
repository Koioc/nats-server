// package com.poc.nats_api;

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
import io.nats.client.Dispatcher;
import io.nats.client.JetStream;
import io.nats.client.JetStreamOptions;
import io.nats.client.Nats;
import io.nats.client.api.ConsumerConfiguration;
import io.nats.client.api.ConsumerInfo;
import io.nats.client.api.StreamConfiguration;
import io.nats.client.api.StreamInfo;
import io.nats.client.api.StreamState;
import io.nats.client.impl.NatsJetStream;
import io.nats.client.impl.NatsMessage;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@RestController
public class NatsController {

    private final Connection natsConnection;
    private final JetStream jetStream;
    private static final String STREAM_NAME = "Lume";
    private static final String SUBJECT_NAME = "POC";
    private static final String DURABLE_CONSUMER_NAME = "durable_consumer";

    public NatsController() throws IOException, InterruptedException {
        // Conecta ao servidor NATS usando a variável de ambiente NATS_URL
        String natsUrl = "nats://0.0.0.0:4222";
        this.natsConnection = Nats.connect(natsUrl);
        JetStream jetStream = natsConnection.jetStream(JetStreamOptions.DEFAULT_JS_OPTIONS);

        // Step 2: Create a stream with persistence enabled (optional if already exists)
        StreamConfiguration streamConfig = StreamConfiguration.builder()
                .name(STREAM_NAME)
                .subjects(SUBJECT_NAME)
                .storageType(io.nats.client.api.StorageType.File) // or Memory if persistence is not needed
                .build();

        jetStream.addStream(streamConfig);
    }

    @GetMapping("/nats")
    public String consumir(@RequestParam String topico) {
        try {
            // // // Subscription subscription = natsConnection.subscribe("teste");
            // // Subscription subscription = natsConnection.subscribe("teste", "teste");

            // // Message msg = subscription.nextMessage(Duration.ofSeconds(120));
            // // String response = new String(msg.getQueueName());
            // // return "Mensagem recebida: " + response;

            // Subscription subscription = natsConnection.subscribe(topico);
            // Message msg = subscription.nextMessage(Duration.ofSeconds(120));
            // String response = new String(msg.getData());
            // return "Mensagem recebida: " + response;

            ConsumerConfiguration consumerConfig = ConsumerConfiguration.builder()
                .durable(DURABLE_CONSUMER_NAME)
                .ackWait(Duration.ofSeconds(30)) // Configure acknowledgment timeout
                .build();
                
            Dispatcher dispatcher = natsConnection.createDispatcher(msg -> {
                try {
                    // Step 4: Process the message
                    String messageData = new String(msg.getData());
                    System.out.println("Received message: " + messageData);

                    // Simulate message processing
                    boolean processingSuccessful = processMessage(messageData);

                    if (processingSuccessful) {
                        // Step 5: Acknowledge message only if processing was successful
                        msg.ack();
                        System.out.println("Message processed successfully and acknowledged.");
                    } else {
                        // If processing fails, the message is not acknowledged, it will be re-delivered
                        System.out.println("Message processing failed, will not acknowledge.");
                    }
                } catch (Exception e) {
                    // Log and do not acknowledge, message will be retried
                    e.printStackTrace();
                }
            });

            // Step 6: Subscribe using JetStream and consumer configuration
            jetStream.subscribe(SUBJECT_NAME, dispatcher, false, consumerConfig);

            // Keep the connection alive to receive messages
            Thread.sleep(60_000);                       
        } catch (Exception e) {
            return "Erro ao consumir mensagem: " + e.getMessage();
        }
    }

    @PostMapping("/nats")
    public String publicar(@RequestParam String topico, @RequestParam String msg) {
        try {
            natsConnection.publish(topico, msg.getBytes());
            return "OK publicada no tópico: " + topico + " a msg: " + msg;
        } catch (Exception e) {
            return "Erro ao publicada mensagem: " + e.getMessage();
        }
    }

    private static boolean processMessage(String message) {
        // Simulate message processing
        // Return false if processing fails, true if successful
        return !message.contains("fail");
    }
}
