package com.example.demo.service;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import com.example.demo.model.*;

import org.springframework.http.*;

import lombok.RequiredArgsConstructor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class BrokerService {

    private final HashMap<String, Broker> brokers = new HashMap<>();
    private final BrokerLoadBalancer loadBalancer = new RoundRobin();

    public JoinResponse addBroker(Broker broker) {
        brokers.put(broker.getName(), broker);
        loadBalancer.addOne(broker);
        return new JoinResponse(broker.getName(), List.of()); // replica brokers
    }

    public Broker getBroker() {
        return loadBalancer.getOne();
    }

    public Broker getBrokerByName(String brokerName) {
        return brokers.get(brokerName);
    }
}
