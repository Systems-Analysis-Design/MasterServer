package com.example.demo.controller;

import com.example.demo.model.Message;
import com.example.demo.service.ClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @PostMapping(path = "/push")
    public boolean push(@RequestBody Message message) {
        log.info("message pushed: " + message.getKey());
        return clientService.push(message);
    }

    @GetMapping(path = "/pull")
    public Message pull() {
        Message message = clientService.pull();
        if (message == null) throw new HttpClientErrorException(HttpStatus.METHOD_NOT_ALLOWED);
        log.info("message pulled: " + message.getKey());
        return message;
    }

    @GetMapping(path = "/subscribe")
    public SseEmitter subscribe() {
        return clientService.subscribe();
    }
}