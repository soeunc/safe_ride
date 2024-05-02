package com.example.safe_ride.config;

import java.security.SecureRandom;

public class SecureRandomProvider {
    private static SecureRandom instance;

    static {
        Thread initializerThread = new Thread(() -> {
            instance = new SecureRandom();
            instance.nextBytes(new byte[64]);
        });
        initializerThread.start();
    }

    public static SecureRandom getInstance() {
        try {
            while (instance == null) {
                Thread.sleep(10);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return instance;
    }
}