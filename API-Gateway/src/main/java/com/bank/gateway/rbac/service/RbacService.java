package com.bank.gateway.rbac.service;

import com.bank.gateway.rbac.config.RbacConfig;
import com.bank.gateway.rbac.model.RbacPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RbacService {

    private final RbacConfig config;
    private final AntPathMatcher matcher = new AntPathMatcher();

    public boolean hasAccess(String role, String path, HttpMethod method) {

        if (role == null || config.getRoles() == null) {
            return false;
        }

        List<RbacPolicy> policies =
                config.getRoles().get(role);

        if (policies == null) {
            return false;
        }

        for (RbacPolicy policy : policies) {

            boolean pathMatch =
                    matcher.match(policy.getPath(), path);

            boolean methodMatch =
                    policy.getMethods().contains("ALL") ||
                    policy.getMethods().contains(method.name());

            if (pathMatch && methodMatch) {
                return true;
            }
        }

        return false;
    }
}