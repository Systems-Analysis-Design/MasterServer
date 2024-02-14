package com.example.demo.service;

import com.example.demo.model.Broker;
import com.example.demo.model.MessageDto;
import com.example.demo.model.PullRequestDto;
import com.example.demo.model.PushRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClientService {

    private final RestTemplate restTemplate;
    private final BrokerService brokerService;
    private final MessageService messageService;

    public boolean push(MessageDto message) {
        Broker broker = brokerService.getBroker();
        String uri = broker.address() + "/api/push";
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        Broker replicaBroker = brokerService.getReplicaBroker(broker);
        List<String> replicas = List.of(replicaBroker.address());
        PushRequestDto brokerPushRequest = new PushRequestDto(broker.name(), message, replicas);
        HttpEntity<PushRequestDto> entity = new HttpEntity<>(brokerPushRequest, headers);
        try {
            ResponseEntity<Void> result = restTemplate.exchange(uri, HttpMethod.POST, entity, Void.class);
            if (result.getStatusCode().equals(HttpStatusCode.valueOf(200))) {
                return messageService.push(broker.name());
            }
            return false;
        } catch (Exception e) {
            return sendPushForReplica(message, broker, replicaBroker, headers);
        }
    }

    private boolean sendPushForReplica(MessageDto message, Broker broker, Broker replica, HttpHeaders headers) {
        PushRequestDto brokerPushRequest = new PushRequestDto(broker.name(), message, List.of());
        HttpEntity<PushRequestDto> entity = new HttpEntity<>(brokerPushRequest, headers);
        String uri = replica.address() + "/api/push";
        try {
            ResponseEntity<Void> result = restTemplate.exchange(uri, HttpMethod.POST, entity, Void.class);
            if (result.getStatusCode().equals(HttpStatusCode.valueOf(200))) {
                return messageService.push(broker.name());
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public MessageDto pull() {
        String brokerName = messageService.pull();
        if (brokerName == null) {
            return new MessageDto("null", null);
        }
        Broker broker = brokerService.getBrokerByName(brokerName);
        String uri = broker.address() + "/api/pull";
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        Broker replicaBroker = brokerService.getReplicaBroker(broker);
        List<String> replicas = List.of(replicaBroker.address());
        PullRequestDto brokerPullRequest = new PullRequestDto(broker.name(), replicas);
        HttpEntity<PullRequestDto> entity = new HttpEntity<>(brokerPullRequest, headers);
        try {
            ResponseEntity<MessageDto> result = restTemplate.exchange(uri, HttpMethod.POST, entity, MessageDto.class);
            if (result.getStatusCode().equals(HttpStatusCode.valueOf(200)) && result.getBody() != null){
                return result.getBody();
            }
            return new MessageDto("null", null);
        } catch (Exception e) {
            return sendPullForReplica(broker.name(), replicaBroker.address(), headers);
        }
    }

    private MessageDto sendPullForReplica(String name, String address, HttpHeaders headers) {
        PullRequestDto brokerPullRequest = new PullRequestDto(name, List.of());
        HttpEntity<PullRequestDto> entity = new HttpEntity<>(brokerPullRequest, headers);
        String uri = address + "/api/pull";
        try {
            ResponseEntity<MessageDto> result = restTemplate.exchange(uri, HttpMethod.POST, entity, MessageDto.class);
            if (result.getStatusCode().equals(HttpStatusCode.valueOf(200)) && result.getBody() != null){
                return result.getBody();
            }
            return new MessageDto("null", null);
        } catch (Exception e) {
            return new MessageDto("null", null);
        }
    }
}
