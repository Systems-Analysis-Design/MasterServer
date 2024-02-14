package com.example.demo.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public record StateModel(List<MapEntry> entries) implements Serializable {
    @Serial
    private static final long serialVersionUID = -457118233767834944L;

    public static StateModel fromMap(Map<Broker, Broker> replicaBrokers) {
        List<MapEntry> mapEntries = replicaBrokers.entrySet()
                                                  .stream()
                                                  .map(x -> new MapEntry(x.getKey(), x.getValue()))
                                                  .toList();
        return new StateModel(mapEntries);
    }

    public Map<Broker, Broker> toMap() {
        Map<Broker, Broker> map = new HashMap<>();
        entries.forEach(x -> map.put(x.key(), x.value()));
        return map;
    }

    public Map<String, Broker> brokerMap() {
        return entries.stream().map(MapEntry::key).collect(Collectors.toMap(Broker::name, x -> x));
    }

    public Map<Broker, Set<String>> replicaMap() {
        Map<Broker, Set<String>> map = new HashMap<>();
        for (MapEntry entry : entries) {
            Broker value = entry.value();
            if (value == null) {
                continue;
            }
            if (map.containsKey(value)) {
                map.get(value).add(entry.key().name());
            } else {
                map.put(value, new HashSet<>());
            }
        }
        return map;
    }
}

record MapEntry(Broker key, Broker value) implements Serializable {
    @Serial
    private static final long serialVersionUID = -457118233767824944L;
}
