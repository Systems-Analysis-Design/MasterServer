package com.example.demo.controller;

import com.example.demo.model.Broker;
import com.example.demo.model.Message;
import com.example.demo.service.BrokerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class BrokerController {

    private final BrokerService brokerService;

    @PostMapping(path = "/join")
    public boolean join(@RequestBody Broker broker) {
        log.info(String.format("broker joined: %s\t| %s", broker.getUid(), broker.getAddress()));
        return brokerService.addBroker(broker);
    }

}
