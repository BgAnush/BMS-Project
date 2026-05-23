package com.bank.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(exclude = { 
	    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class, 
	    org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class 
	})
@EnableFeignClients(basePackages = "com.bank.user.client")
public class UserServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserServiceApplication.class, args);
	}

}
