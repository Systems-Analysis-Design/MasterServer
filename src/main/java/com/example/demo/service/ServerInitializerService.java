package com.example.demo.service;

import com.example.demo.model.Broker;
import com.example.demo.model.RoundRobin;
import com.example.demo.model.StateModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Map;
import java.util.Queue;

@Component
@Slf4j
public class ServerInitializerService implements ApplicationRunner, ApplicationContextAware {

    private ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    @Override
    public void run(ApplicationArguments applicationArguments) {
        FileService fileService = (FileService) context.getBean("fileService");
        BrokerService brokerService = (BrokerService) context.getBean("brokerService");
        MessageService messageService = (MessageService) context.getBean("messageService");

        Queue<String> queue = fileService.readWalFile(fileService.readQueue());
        messageService.setMessages(queue);

        StateModel stateModel = fileService.readStateModel();
        brokerService.setReplicaBrokers(stateModel.toMap());
        Map<String, Broker> brokers = stateModel.brokerMap();
        brokerService.setBrokers(brokers);
        brokerService.setBrokerInsideReplications(stateModel.replicaMap());
        brokerService.setBrokerLoadBalancer(new RoundRobin<>(new ArrayList<>(brokers.values().stream().toList())));
    }
}
