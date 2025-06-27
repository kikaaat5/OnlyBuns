package com.example.OnlyBuns.service;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class UsernameBloomFilter {

    private final BloomFilter<CharSequence> bloomFilter;

    public UsernameBloomFilter(List<String> usernames) {
        this.bloomFilter = BloomFilter.create(
                Funnels.stringFunnel(StandardCharsets.UTF_8),
                10000, // broj elemenata
                0.01   // dozvoljena stopa lažnih pozitivnih (1%)
        );
        usernames.forEach(bloomFilter::put);
    }

    public boolean probablyExists(String username) {
        return bloomFilter.mightContain(username);
    }

    public void add(String username) {
        bloomFilter.put(username);
    }
}
