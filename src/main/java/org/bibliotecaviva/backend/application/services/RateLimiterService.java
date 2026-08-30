package org.bibliotecaviva.backend.application.services;

import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {

    private final Map<String, Deque<Long>> requestLogs = new ConcurrentHashMap<>();

    public synchronized boolean isAllowed(String key, int maxRequests, long windowMillis) {
        long now = System.currentTimeMillis();
        long windowStart = now - windowMillis;

        Deque<Long> timestamps = requestLogs.computeIfAbsent(key, k -> new ArrayDeque<>());

        while (!timestamps.isEmpty() && timestamps.peekFirst() <= windowStart) {
            timestamps.pollFirst();
        }

        if (timestamps.size() < maxRequests) {
            timestamps.addLast(now);
            return true;
        }

        return false;
    }

    public synchronized long getRetryAfterSeconds(String key, long windowMillis) {
        long now = System.currentTimeMillis();
        long windowStart = now - windowMillis;
        Deque<Long> timestamps = requestLogs.get(key);
        if (timestamps == null || timestamps.isEmpty()) {
            return 1L;
        }
        Long oldest = timestamps.peekFirst();
        if (oldest == null) {
            return 1L;
        }
        long retryAfterMillis = (oldest + windowMillis) - now;
        return Math.max(1L, (retryAfterMillis + 999) / 1000);
    }

    public synchronized void cleanup() {
        long now = System.currentTimeMillis();
        long maxWindow = 3600_000L; // 1 hour
        Iterator<Map.Entry<String, Deque<Long>>> iterator = requestLogs.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Deque<Long>> entry = iterator.next();
            Deque<Long> timestamps = entry.getValue();
            while (!timestamps.isEmpty() && timestamps.peekFirst() <= now - maxWindow) {
                timestamps.pollFirst();
            }
            if (timestamps.isEmpty()) {
                iterator.remove();
            }
        }
    }

    public void clear() {
        requestLogs.clear();
    }
}
