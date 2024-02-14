package com.example.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.Queue;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final FileService fileService;
    @Setter
    private Queue<String> messages = new LinkedList<>();

    public boolean push(String name) {
        fileService.checkForWalLimit(messages);
        fileService.push(name);
        return messages.add(name);
    }

    public String pull() {
        fileService.checkForWalLimit(messages);
        fileService.pull();
        return messages.poll();
    }
}
