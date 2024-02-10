package com.example.demo.model;

public interface BrokerLoadBalancer {
    
    public Broker getOne();
    public boolean addOne(Broker broker);
}
