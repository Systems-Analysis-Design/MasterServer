package com.example.demo.model;

import java.io.Serial;
import java.io.Serializable;

public record Broker(String name, String address) implements Serializable {
    @Serial
    private static final long serialVersionUID = -457118333767834944L;
}
