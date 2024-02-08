package com.example.demo.controller;

import com.example.demo.model.Broker;
import com.example.demo.model.HealthRequestDto;
import com.example.demo.model.JoinResponse;
import com.example.demo.service.BrokerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class BrokerController {

    private final BrokerService brokerService;

    @PostMapping(path = "/api/join")
    public JoinResponse join(@RequestBody Broker broker) {
        log.info(String.format("broker joined with address: %s", broker.getAddress()));
        broker.setName("name"); // create name for broker
        broker.setUid(UUID.randomUUID().toString());
        return brokerService.addBroker(broker);
    }

    @PostMapping(path = "/api/health")
    public void health(@RequestBody HealthRequestDto healthRequestDto) {
        log.info(String.format("health request from broker: %s with totalNumberOfMessages: %s, and totalNumberOfQueues: %s",
                               healthRequestDto.getBrokerName(),
                               healthRequestDto.getTotalNumberOfMessages(),
                               healthRequestDto.getTotalNumberOfQueues()));
    }



}
