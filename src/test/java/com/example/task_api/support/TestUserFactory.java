package com.example.task_api.support;

import java.util.concurrent.atomic.AtomicLong;

import com.example.task_api.model.AppUser;
import com.example.task_api.repository.AppUserRepository;

public final class TestUserFactory {

    private static final AtomicLong SEQUENCE = new AtomicLong();

    private TestUserFactory(){
    }

    public static AppUser saveUser(
        AppUserRepository appUserRepository
    ){
        long sequence = SEQUENCE.incrementAndGet();

        AppUser user = new AppUser(
                "test-user-" + sequence + "@example.com",
                 "test-password-hash"
        );

        return appUserRepository.saveAndFlush(user);
    }
}
