package com.example.demo.service;

import com.example.demo.model.*;

import org.springframework.http.*;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static org.springframework.web.servlet.mvc.method.annotation.SseEmitter.event;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClientService {

    private final RestTemplate restTemplate;
    public static final long DEFAULT_TIMEOUT = Long.MAX_VALUE;
    private final LoadBalancer<SseEmitter> sseEmitterLoadBalancer = new RoundRobin<>();
    private final BrokerService brokerService;
    private final Queue<String> messages = new LinkedList<>();


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
                return messages.add(broker.name());
            }
            return false;
        } catch (Exception e) {
            return sendPushForReplica(message, broker.name(), replicaBroker.address(), headers);
        }
    }

    private boolean sendPushForReplica(MessageDto message, String name, String address, HttpHeaders headers) {
        PushRequestDto brokerPushRequest = new PushRequestDto(name, message, List.of());
        HttpEntity<PushRequestDto> entity = new HttpEntity<>(brokerPushRequest, headers);
        String uri = address + "/api/push";
        try {
            ResponseEntity<Void> result = restTemplate.exchange(uri, HttpMethod.POST, entity, Void.class);
            return result.getStatusCode().equals(HttpStatusCode.valueOf(200));
        } catch (Exception e) {
            return false;
        }
    }

    public MessageDto pull() {
        String brokerName = messages.poll();
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

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
        emitter.onCompletion(() -> removeAndLog(emitter));
        emitter.onError((err) -> removeAndLog(emitter));
        emitter.onTimeout(() -> removeAndLog(emitter));
        sseEmitterLoadBalancer.addOne(emitter);
        runSubscribersThread();
        log.info("subscribed successfully.");
        return emitter;
    }

    @Scheduled(fixedDelay = 1000)
    public void runSubscribersThread() {
        if (!sseEmitterLoadBalancer.isEmpty() && !messages.isEmpty()) {
            sendMessage(sseEmitterLoadBalancer.getOne(), pull());
        }
    }

     private void sendMessage(SseEmitter sseEmitter, MessageDto message) {
         try {
             SseEmitter.SseEventBuilder eventBuilder = event().name("broadcastedMessage")
                                                              .id(message.key())
                                                              .data(message, MediaType.APPLICATION_JSON);
             sseEmitter.send(eventBuilder);
             log.info("message with key {} sent successfully", message.key());
         } catch (Exception e) {
             log.error("error in send message", e);
             removeAndLog(sseEmitter);
             sseEmitter.completeWithError(e);
         }
     }

    private void removeAndLog(SseEmitter emitter) {
        if (sseEmitterLoadBalancer.remove(emitter)) {
            log.info("Unsubscribe client {}", emitter.toString());
        }
    }
}
