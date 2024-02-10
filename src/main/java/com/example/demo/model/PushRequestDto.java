package com.example.demo.model;

public record PushRequestDto(String partition, MessageDto messageDto) {
}
