package com.bank.gateway.rbac.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class RbacPolicy {
    private String path;
    private List<String> methods;
}