package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@Getter
@Setter
public class JoinResponse {
    private String name;
    private List<ReplicaBrokerDto> replicaBrokerDtoList;

}
