package com.example.demo.model;

public interface LoadBalancer<T> {
    
    T getOne();
    boolean addOne(T t);
    boolean remove(T t);
    boolean isEmpty();
}
