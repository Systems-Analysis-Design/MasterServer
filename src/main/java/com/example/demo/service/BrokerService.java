package com.example.demo.service;

import com.example.demo.model.Broker;
import com.example.demo.model.JoinResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Component
public class BrokerService {

    private final HashMap<String, Broker> brokers = new HashMap<>();
    public JoinResponse addBroker(Broker broker) {
        brokers.put(broker.getAddress(), broker);
        return new JoinResponse(broker.getName(), List.of()); // replica brokers
    }
}
