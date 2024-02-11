package com.example.demo.model;

import java.util.List;

public record PullRequestDto(String name, List<String> replicas) {
}
