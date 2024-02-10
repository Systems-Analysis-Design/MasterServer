package com.example.demo.model;

import java.util.ArrayList;

public class RoundRobin implements BrokerLoadBalancer {
    private final ArrayList<Broker> brokers = new ArrayList<>();
    private int offset = 0;

    public Broker getOne() {
        int index = offset % brokers.size();
        return brokers.get(index++);
    }

    public boolean addOne(Broker broker) {
        return brokers.add(broker);
    }
}


