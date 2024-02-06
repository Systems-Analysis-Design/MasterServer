package com.example.demo.service;

import com.example.demo.model.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import static org.springframework.web.servlet.mvc.method.annotation.SseEmitter.event;

@Slf4j
@Component
public class ClientService {

    public static final long DEFAULT_TIMEOUT = Long.MAX_VALUE;
    private Queue<Message> messages = new LinkedList<>();
    private static Set<SseEmitter> subscribers = new HashSet<>();

    public boolean push(Message message) {
        boolean result = messages.add(message);
        broadcast(message);
        return result;
    }

    private void broadcast(Message message) {
        Set<SseEmitter> clients = Set.copyOf(subscribers);
        for (SseEmitter client: clients) {
            sendMessage(client, message);
        }
    }

    private void sendMessage(SseEmitter sseEmitter, Message message) {
        try {
            SseEmitter.SseEventBuilder eventBuilder = event()
                    .name("broadcastedMessage")
                    .id(message.getKey())
                    .data(message, MediaType.APPLICATION_JSON);
            sseEmitter.send(eventBuilder);
            log.info("- message sent successfully");
        } catch (IOException e) {
            subscribers.remove(sseEmitter);
            sseEmitter.completeWithError(e);
        }
    }

    public Message pull() {
        return messages.poll();
    }

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
        emitter.onCompletion(() -> subscribers.remove(emitter));
        emitter.onError((err) -> removeAndLogError(emitter));
        emitter.onTimeout(() -> removeAndLogError(emitter));
        subscribers.add(emitter);
        runSubscribersThread();
        log.info("subscribed successfully.");
        return emitter;
    }

    @Scheduled(fixedDelay = 200)
    private void runSubscribersThread() {
        while (!subscribers.isEmpty()) {
            if (!messages.isEmpty()){
                broadcast(pull());
                log.info("---------------------------");
            }else break;
        }
    }

    private void removeAndLogError(SseEmitter emitter) {
        log.info("Error during communication. Unregister client {}", emitter.toString());
        subscribers.remove(emitter);
    }
}
