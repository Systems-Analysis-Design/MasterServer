package com.example.demo.service;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import com.example.demo.model.*;

import lombok.RequiredArgsConstructor;

@Slf4j
@Component
@RequiredArgsConstructor
public class BrokerService {

    private final HashMap<String, Broker> brokers = new HashMap<>();
    private final LoadBalancer<Broker> brokerLoadBalancer = new RoundRobin<>();
    private final Map<Broker, Broker> replicaBrokers = new HashMap<>(); // replica of each broker
    private final Map<Broker, Set<String>> brokerInsideReplications = new HashMap<>(); // replications inside of each broker

    public JoinResponse addBroker(Broker broker) {
        brokers.put(broker.name(), broker);
        brokerInsideReplications.put(broker, new HashSet<>());
        setReplica(broker);
        brokerLoadBalancer.addOne(broker);
        return new JoinResponse(broker.name());
    }

    public Broker getBroker() {
        return brokerLoadBalancer.getOne();
    }

    public Broker getBrokerByName(String brokerName) {
        return brokers.get(brokerName);
    }

    public Broker getReplicaBroker(Broker broker) {
        return replicaBrokers.get(broker);
    }

    private void setReplica(Broker broker) {
        setReplicaForBroker(broker);
        replicaBrokers.entrySet().stream().filter(x -> x.getValue() == null).forEach(x -> setReplicaForBroker(x.getKey()));
    }

    private void setReplicaForBroker(Broker broker) {
        Broker replica = getReplicaBrokerWithMinimumLoad(broker);
        replicaBrokers.put(broker, replica);
        if (replica != null) {
            brokerInsideReplications.get(replica).add(broker.name());
        }
    }

    private Broker getReplicaBrokerWithMinimumLoad(Broker broker) {
        Broker replica = null;
        int min = Integer.MAX_VALUE;
        Set<Map.Entry<Broker, Set<String>>> entries = brokerInsideReplications.entrySet()
                                                                              .stream()
                                                                              .filter(x -> !x.getKey().equals(broker))
                                                                              .collect(Collectors.toSet());
        for (Map.Entry<Broker, Set<String>> brokerSetEntry : entries) {
            if (brokerSetEntry.getValue().size() <= min) {
                min = brokerSetEntry.getValue().size();
                replica = brokerSetEntry.getKey();
            }
        }
        return replica;
    }
}
