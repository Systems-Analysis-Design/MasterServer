package com.example.demo.service;

import com.example.demo.model.*;

import org.springframework.http.*;
import lombok.extern.slf4j.Slf4j;

import lombok.RequiredArgsConstructor;
import org.springframework.web.client.RestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClientService {

    private final RestTemplate restTemplate;
    public static final long DEFAULT_TIMEOUT = Long.MAX_VALUE;
    private Queue<String> messages = new LinkedList<>();
    private static Set<SseEmitter> subscribers = new HashSet<>();
    private final BrokerService brokerService;


    public boolean push(MessageDto message) {
        Broker broker = brokerService.getBroker();
        String uri = "http://" + broker.getAddress() + "/api/message/push";
        HttpHeaders headers = new HttpHeaders();

        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);

        PushRequestDto brokerPushRequest = new PushRequestDto(broker.getName(), message);
        
        HttpEntity<PushRequestDto> entity = new HttpEntity<>(brokerPushRequest, headers);
        ResponseEntity<PushResponseDto> result = restTemplate.exchange(uri, HttpMethod.POST, entity, PushResponseDto.class);
        if (result.getStatusCode().equals(HttpStatusCode.valueOf(200)))
            return messages.add(broker.getName());
        return false;
    }


    // private void broadcast(Message message) {
    //     Set<SseEmitter> clients = Set.copyOf(subscribers);
    //     for (SseEmitter client: clients) {
    //         sendMessage(client, message);
    //     }
    // }

    // private void sendMessage(SseEmitter sseEmitter, Message message) {
    //     try {
    //         SseEmitter.SseEventBuilder eventBuilder = event()
    //                 .name("broadcastedMessage")
    //                 .id(message.getKey())
    //                 .data(message, MediaType.APPLICATION_JSON);
    //         sseEmitter.send(eventBuilder);
    //         log.info("- message sent successfully");
    //     } catch (IOException e) {
    //         subscribers.remove(sseEmitter);
    //         sseEmitter.completeWithError(e);
    //     }
    // }

    public MessageDto pull() {
        String brokerName = messages.poll();
        Broker broker = brokerService.getBrokerByName(brokerName);
        String uri = "http://" + broker.getAddress() + "/api/message/pull";
        HttpHeaders headers = new HttpHeaders();

        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);

        PullRequestDto brokerPullRequest = new PullRequestDto(broker.getName());
        
        HttpEntity<PullRequestDto> entity = new HttpEntity<>(brokerPullRequest, headers);
        ResponseEntity<PullResponseDto> result = restTemplate.exchange(uri, HttpMethod.POST, entity, PullResponseDto.class);
        
        if (result.getStatusCode().equals(HttpStatusCode.valueOf(200))){
            return result.getBody().messageDto();            
        }
        return new MessageDto("null", null);
    }

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
        emitter.onCompletion(() -> subscribers.remove(emitter));
        emitter.onError((err) -> removeAndLogError(emitter));
        emitter.onTimeout(() -> removeAndLogError(emitter));
        subscribers.add(emitter);
        // runSubscribersThread();
        log.info("subscribed successfully.");
        return emitter;
    }

    // @Scheduled(fixedDelay = 200)
    // private void runSubscribersThread() {
    //     while (!subscribers.isEmpty()) {
    //         if (!messages.isEmpty()){
    //             broadcast(pull());
    //             log.info("---------------------------");
    //         }else break;
    //     }
    // }

    private void removeAndLogError(SseEmitter emitter) {
        log.info("Error during communication. Unregister client {}", emitter.toString());
        subscribers.remove(emitter);
    }
}
