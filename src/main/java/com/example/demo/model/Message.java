package com.example.demo.model;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class Message {

    private String key;
    private byte[] value;
}
