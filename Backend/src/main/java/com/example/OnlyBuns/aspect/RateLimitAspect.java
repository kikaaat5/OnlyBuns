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
 * Aspect za primenu ograničavanja brzine (rate limiting) na specifične metode.
 * U ovom slučaju, primenjuje se na 'followClient' metodu u FollowRelationService.
 */
@Aspect // Označava klasu kao Aspect
@Component // Označava da je Spring komponenta i da treba da bude skenirana
public class RateLimitAspect {

    private final InMemoryRateLimitingService rateLimitingService;

    @Autowired
    public RateLimitAspect(InMemoryRateLimitingService rateLimitingService) {
        this.rateLimitingService = rateLimitingService;
    }

    /**
     * Definira "pointcut" i "advice" za ograničavanje brzine.
     *
     * @Before: Ovaj "advice" se izvršava pre izvršenja ciljne metode.
     * "execution(* com.example.OnlyBuns.service.FollowRelationService.followClient(..))":
     * - *: bilo koja povratna vrednost.
     * - com.example.OnlyBuns.service.FollowRelationService: klasa servisa.
     * - followClient: metoda na koju se primenjuje ograničenje.
     * - (..): bilo koji broj i tip argumenata.
     *
     * JoinPoint: Daje pristup informacijama o metodi koja se presreće.
     */
    @Before("execution(* com.example.OnlyBuns.service.FollowRelationService.followClient(Integer, Integer)) && args(followerClientId, ..)")
    public void rateLimitFollowClient(JoinPoint joinPoint, Integer followerClientId) {
        int limit = 50;
        long timePeriod = 1; // 1 min
        TimeUnit timeUnit = TimeUnit.MINUTES;

        boolean allowed = rateLimitingService.allowRequest(followerClientId, limit, timePeriod, timeUnit);

        if (!allowed) {

            throw new TooManyRequestsException("Maksimum " + limit + " pracenja " + timeUnit.toMinutes(timePeriod) + " minutu.");

        }
        System.out.println("Zahtev za praćenje od korisnika " + followerClientId + " je dozvoljen.");
    }
}
