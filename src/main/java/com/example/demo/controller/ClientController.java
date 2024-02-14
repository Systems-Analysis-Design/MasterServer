package com.example.demo.controller;

import com.example.demo.model.MessageDto;
import com.example.demo.service.ClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @PostMapping(path = "/push")
    public boolean push(@RequestBody MessageDto message) {
        log.info("message pushed with key: " + message.key() + ", value: " + message.value());
        return clientService.push(message);
    }

    @GetMapping(path = "/pull")
    public MessageDto pull() {
        MessageDto message = clientService.pull();
        if (message == null) throw new HttpClientErrorException(HttpStatus.METHOD_NOT_ALLOWED);
        log.info("message pulled with key: " + message.key() + ", value: " + message.value());
        return message;
    }
}