package com.example.OnlyBuns.aspect;

import com.example.OnlyBuns.exception.TooManyRequestsException;
import com.example.OnlyBuns.service.InMemoryRateLimitingService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Aspect za primjenu ograničavanja brzine (rate limiting) na specifične metode.
 * U ovom slučaju, primjenjuje se na 'followClient' metodu u FollowRelationService.
 */
@Aspect
@Component
public class RateLimitAspect {

    private final InMemoryRateLimitingService rateLimitingService;

    @Autowired
    public RateLimitAspect(InMemoryRateLimitingService rateLimitingService) {
        this.rateLimitingService = rateLimitingService;
    }


    @Before("execution(* com.example.OnlyBuns.service.FollowRelationService.followClient(Integer, Integer)) && args(followerClientId, ..)")
    public void rateLimitFollowClient(JoinPoint joinPoint, Integer followerClientId) {
        int limit = 5; //samo za test, po specifikaciji je 50
        long timePeriod = 1; // 1 min
        TimeUnit timeUnit = TimeUnit.MINUTES;

        boolean allowed = rateLimitingService.allowRequest(followerClientId, limit, timePeriod, timeUnit);

        if (!allowed) {

            throw new TooManyRequestsException("Maksimum " + limit + " pracenja " + timeUnit.toMinutes(timePeriod) + " minutu.");

        }
        System.out.println("Zahtev za praćenje od korisnika " + followerClientId + " je dozvoljen.");
    }
}
