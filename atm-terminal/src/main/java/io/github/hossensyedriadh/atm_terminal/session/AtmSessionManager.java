package io.github.hossensyedriadh.atm_terminal.session;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class AtmSessionManager {
    private final AtomicBoolean occupied = new AtomicBoolean(false);

    public SessionLease tryAcquire() {
        if (!this.occupied.compareAndSet(false, true)) {
            return null;
        }

        return new SessionLease();
    }

    public boolean isOccupied() {
        return this.occupied.get();
    }

    public final class SessionLease implements AutoCloseable {
        private final AtomicBoolean released = new AtomicBoolean(false);

        private SessionLease() {
        }

        @Override
        public void close() {
            if (released.compareAndSet(false, true)) {
                occupied.set(false);
            }
        }
    }
}
