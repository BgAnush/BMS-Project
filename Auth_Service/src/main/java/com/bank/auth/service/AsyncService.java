package com.bank.auth.service;

import com.bank.auth.client.UserClient;
import com.bank.auth.dto.UserRequest;
import com.bank.auth.exception.ExternalServiceException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncService {

    private final UserClient userClient;

    @Async
    public void sendToUserService(UserRequest ur) {
        try {
            log.info("Calling User Service for profileId: {}", ur.getProfileId());

            userClient.createUser(ur);

            log.info("User Service call SUCCESS for profileId: {}", ur.getProfileId());

        } catch (Exception e) {
            log.error("User Service call FAILED for profileId: {}", ur.getProfileId(), e);
            throw new ExternalServiceException("User service call failed");
        }
    }
}