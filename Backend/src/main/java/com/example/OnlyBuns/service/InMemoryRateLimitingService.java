package com.example.OnlyBuns.service;

import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class InMemoryRateLimitingService {

    // Mapa za skladištenje istorije zahteva za svakog korisnika.
    // Ključ: Korisnički ID (Integer)
    // Vrednost: LinkedList vremena u milisekundama kada je korisnik izvršio akciju.
    private final Map<Integer, LinkedList<Long>> userRequestTimestamps = new ConcurrentHashMap<>();

    /**
     * Provjerava i primjenjuje ograničenje brzine za datog korisnika.
     *
     * @param userId Korisnički ID za koji se primenjuje ograničenje.
     * @param limit Maksimalan broj akcija dozvoljen u definisanom periodu.
     * @param timePeriod Vremenski period za ograničenje (npr. 1 za minutu).
     * @param timeUnit Jedinica vremena za period (npr. TimeUnit.MINUTES).
     * @return true ako je akcija dozvoljena, false ako je prekoračeno ograničenje.
     */
    public boolean allowRequest(Integer userId, int limit, long timePeriod, TimeUnit timeUnit) {
        long currentTime = System.currentTimeMillis();
        long windowStartMillis = currentTime - timeUnit.toMillis(timePeriod);

        LinkedList<Long> timestamps = userRequestTimestamps.computeIfAbsent(userId, k -> new LinkedList<>());

        synchronized (timestamps) { // Sinhronizuj na specifičnoj listi tog korisnika
            // Ukloni sve stare timestampove koji su van trenutnog vremenskog prozora
            while (!timestamps.isEmpty() && timestamps.getFirst() < windowStartMillis) {
                timestamps.removeFirst();
            }

            if (timestamps.size() < limit) {
                timestamps.addLast(currentTime);
                return true;
            } else {
                return false;
            }
        }
    }
}
