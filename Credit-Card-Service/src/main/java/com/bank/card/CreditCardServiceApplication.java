package com.bank.card; // This must match the folder path

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(exclude = { 
    SecurityAutoConfiguration.class, 
    UserDetailsServiceAutoConfiguration.class 
})
@EnableDiscoveryClient
@EnableFeignClients
public class CreditCardServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CreditCardServiceApplication.class, args);
    }
}