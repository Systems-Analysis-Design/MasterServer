package com.example.demo.service;

import com.example.demo.model.Broker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;

@Slf4j
@Component
public class BrokerService {

    private final HashMap<String, Broker> brokers = new HashMap<>();
    public boolean addBroker(Broker broker) {
        brokers.put(broker.getName(), broker);
        return true;
    }
}
