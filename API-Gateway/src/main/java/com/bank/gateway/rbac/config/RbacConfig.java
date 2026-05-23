package com.bank.gateway.rbac.config;

import com.bank.gateway.rbac.model.RbacPolicy;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "rbac")
@Data
public class RbacConfig {
    private Map<String, List<RbacPolicy>> roles;
}