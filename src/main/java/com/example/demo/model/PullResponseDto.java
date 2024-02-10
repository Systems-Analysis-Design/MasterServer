package com.example.demo.model;

public record PullResponseDto(MessageDto messageDto, Boolean allReplicasGotMessage) {
}
