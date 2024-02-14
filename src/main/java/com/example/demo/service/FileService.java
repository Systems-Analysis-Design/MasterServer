package com.example.demo.service;

import com.example.demo.model.StateModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

@Service
public class FileService {
    private static final int WAL_LIMIT = 10000;
    private static final String QUEUE_FILE = "queue.txt";
    private static final String WAL_FILE = "wal.txt";
    private static final String STATE_FILE = "state.txt";
    private final ObjectMapper mapper = new ObjectMapper();
    private int lineNumber = 0;

    public void push(String name) {
        try (FileWriter fw = new FileWriter(WAL_FILE, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println(name);
            lineNumber++;
        } catch (Exception e) {
            // ignore
        }
    }

    public void pull() {
        try (FileWriter fw = new FileWriter(WAL_FILE, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println("pull");
            lineNumber++;
        } catch (Exception e) {
            // ignore
        }
    }

    public Queue<String> readQueue() {
        Queue<String> queue = new LinkedList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(QUEUE_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                queue.add(line.trim());
            }
            return queue;
        } catch (Exception e) {
            return queue;
        }
    }

    public void writeQueue(Queue<String> queue) {
        try (FileWriter fw = new FileWriter(QUEUE_FILE, false);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            queue.forEach(out::println);
        } catch (Exception e) {
            // ignore
        }
    }

    public Queue<String> readWalFile(Queue<String> queue) {
        try (BufferedReader br = new BufferedReader(new FileReader(WAL_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().equals("pull")) {
                    queue.poll();
                } else if (!line.trim().isBlank()) {
                    queue.add(line.trim());
                }
            }
            return queue;
        } catch (Exception e) {
            return queue;
        }
    }

    public void checkForWalLimit(Queue<String> queue) {
        if (lineNumber == WAL_LIMIT) {
            try {
                new PrintWriter(WAL_FILE).close();
                writeQueue(queue);
                lineNumber = 0;
            } catch (Exception e) {
                // ignore
            }

        }
    }

    public void writeStateModel(StateModel stateModel) {
        try (FileWriter fw = new FileWriter(STATE_FILE, false);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println(mapper.writeValueAsString(stateModel));
        } catch (Exception e) {
            // ignore
        }
    }

    public StateModel readStateModel() {
        try {
            String state = Files.readString(Path.of(STATE_FILE));
            if (state.isBlank()) {
                return new StateModel(new ArrayList<>());
            }
            return mapper.readValue(state, StateModel.class);
        } catch (Exception e) {
            return new StateModel(new ArrayList<>());
        }
    }
}
