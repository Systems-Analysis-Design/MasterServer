package com.example.demo.model;

import lombok.Getter;

@Getter
public class HealthRequestDto {
    private String brokerName;
    private long totalNumberOfMessages;
    private int totalNumberOfQueues;

}
