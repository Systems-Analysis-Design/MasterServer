package com.example.demo.model;

import java.util.ArrayList;
import java.util.List;

public class RoundRobin<T> implements LoadBalancer<T> {
    private final List<T> list = new ArrayList<>();
    private int offset = 0;

    public T getOne() {
        T t = list.get(offset % list.size());
        offset = (offset + 1) % list.size();
        return t;
    }

    public boolean addOne(T t) {
        return list.add(t);
    }

    public boolean remove(T t) {
        return list.remove(t);
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }
}


