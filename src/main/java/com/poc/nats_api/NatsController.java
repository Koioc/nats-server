package com.poc.nats_api;

import com.poc.nats_api.services.NatsService;
import io.nats.client.JetStreamApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class NatsController {
    private final NatsService natsService;

    @Autowired
    public NatsController(NatsService natsService) {
        this.natsService = natsService;
    }

    @GetMapping("/nats")
    public String consumir() throws IOException, InterruptedException, JetStreamApiException {
        natsService.connect();
        natsService.subscribeStream();
        natsService.consume();
        return "Mensagens consumidas!";
    }

    @PostMapping("/nats")
    public String publicar(@RequestParam String msg) throws IOException, JetStreamApiException {
        natsService.publish(msg);
        return "Mensagem publicada: " + msg;
    }
}