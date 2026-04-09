package org.bibliotecaviva.backend.application.services;

import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service

//TODO: scheduler ou redis pra limpar tokens expirados da blacklist
public class TokenBlackListService {
    private final Set<String> blacklist = ConcurrentHashMap.newKeySet();

    public void invalidate(String token) {
        blacklist.add(token);
    }
    public boolean isBlacklisted(String token) {
        return blacklist.contains(token);
    }
}
