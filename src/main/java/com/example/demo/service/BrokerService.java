package com.example.demo.service;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import com.example.demo.model.*;

import lombok.RequiredArgsConstructor;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class BrokerService {

    private final RestTemplate restTemplate;
    private final FileService fileService;
    @Setter
    private LoadBalancer<Broker> brokerLoadBalancer = new RoundRobin<>();
    @Setter
    private Map<String, Broker> brokers = new HashMap<>();
    @Setter
    private Map<Broker, Broker> replicaBrokers = new HashMap<>(); // replica of each broker
    @Setter
    private Map<Broker, Set<String>> brokerInsideReplications = new HashMap<>(); // replications inside each broker

    public JoinResponse addBroker(Broker broker) {
        brokers.put(broker.name(), broker);
        brokerInsideReplications.put(broker, new HashSet<>());
        setReplica(broker);
        brokerLoadBalancer.addOne(broker);
        fileService.writeStateModel(StateModel.fromMap(replicaBrokers));
        return new JoinResponse(broker.name());
    }

    public Broker getBroker() {
        return brokerLoadBalancer.getOne();
    }

    public Broker getBrokerByName(String brokerName) {
        return brokers.get(brokerName);
    }

    public Broker getReplicaBroker(Broker broker) {
        Broker replica = replicaBrokers.get(broker);
        return replica != null ? replica : broker;
    }

    private void setReplica(Broker broker) {
        setReplicaForBroker(broker);
        replicaBrokers.entrySet().stream().filter(x -> x.getValue() == null).forEach(x -> setReplicaForBroker(x.getKey()));
        fileService.writeStateModel(StateModel.fromMap(replicaBrokers));
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
                                                                              .filter(x -> !x.getKey().equals(broker)
                                                                                      && getBrokerHealth(x.getKey()))
                                                                              .collect(Collectors.toSet());
        for (Map.Entry<Broker, Set<String>> brokerSetEntry : entries) {
            if (brokerSetEntry.getValue().size() <= min) {
                min = brokerSetEntry.getValue().size();
                replica = brokerSetEntry.getKey();
            }
        }
        return replica;
    }

    private Boolean getBrokerHealth(Broker broker) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        String uri = broker.address() + "/api/health";
        try {
            ResponseEntity<Void> result = restTemplate.exchange(uri, HttpMethod.GET, entity, Void.class);
            return result.getStatusCode().equals(HttpStatusCode.valueOf(200));
        } catch (Exception e) {
            return false;
        }
    }
}
