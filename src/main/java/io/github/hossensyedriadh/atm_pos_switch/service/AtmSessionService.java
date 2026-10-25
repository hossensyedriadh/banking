package io.github.hossensyedriadh.atm_pos_switch.service;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
@Service
public class AtmSessionService {
    @Value("${atm-terminal.session.idle-timeout}")
    private Duration idleTimeout;

    @Value("${atm-terminal.session.max-age}")
    private Duration maxAge;

    private final SecureRandom secureRandom = new SecureRandom();
    private final ConcurrentHashMap<String, AtmSession> sessionMap = new ConcurrentHashMap<>();

    public String create(String pan, String terminalId) {
        if (pan == null || terminalId == null) {
            log.error("Failed to create session. PAN and Terminal ID are required");
            throw new IllegalArgumentException("PAN and Terminal ID are required to create session");
        }

        byte[] tokenBytes = new byte[32];
        this.secureRandom.nextBytes(tokenBytes);

        String token = Base64.getEncoder().withoutPadding().encodeToString(tokenBytes);

        Instant now = Instant.now();

        this.sessionMap.put(token, new AtmSession(pan, terminalId, now, now));
        this.sessionMap.entrySet().removeIf(entry -> entry.getValue().isExpired(Instant.now(),
                this.maxAge, this.idleTimeout));

        return token;
    }

    public boolean validateAndTouch(String token, String pan, String terminalId) {
        if (token == null || pan == null || terminalId == null) {
            return false;
        }

        AtmSession session = this.sessionMap.get(token);

        if (session == null) {
            return false;
        }

        synchronized (session) {
            Instant now = Instant.now();

            if (session.isExpired(now, this.maxAge, this.idleTimeout)) {
                this.sessionMap.remove(token, session);
                return false;
            }

            if (!session.getPan().equals(pan) || !session.getTerminalId().equals(terminalId)) {
                return false;
            }

            session.touch(now);
            return true;
        }
    }

    public void invalidate(String token) {
        if (token != null) {
            this.sessionMap.remove(token);
        }
    }

    public boolean terminate(String token, String pan, String terminalId) {
        if (token == null || pan == null || terminalId == null) {
            return false;
        }

        AtmSession session = this.sessionMap.get(token);

        if (session == null) {
            return false;
        }

        synchronized (session) {
            Instant now = Instant.now();

            if (session.isExpired(now, this.maxAge, this.idleTimeout)) {
                this.sessionMap.remove(token, session);
                return false;
            }

            if (!session.getPan().equals(pan) || !session.getTerminalId().equals(terminalId)) {
                return false;
            }

            return this.sessionMap.remove(token, session);
        }
    }

    @Getter
    public static final class AtmSession {
        private final String pan;
        private final String terminalId;
        private final Instant createdAt;
        private final Instant initialLastActivity;
        private Instant lastActivity;

        public AtmSession(String pan, String terminalId, Instant createdAt, Instant initialLastActivity) {
            this.pan = pan;
            this.terminalId = terminalId;
            this.createdAt = createdAt;
            this.initialLastActivity = initialLastActivity;
            this.lastActivity = initialLastActivity;
        }

        private synchronized void touch(Instant now) {
            this.lastActivity = now;
        }

        private synchronized boolean isExpired(Instant now, Duration maxAge, Duration idleTimeout) {
            return !now.isBefore(createdAt.plus(maxAge)) || !now.isBefore(lastActivity.plus(idleTimeout));
        }
    }
}
