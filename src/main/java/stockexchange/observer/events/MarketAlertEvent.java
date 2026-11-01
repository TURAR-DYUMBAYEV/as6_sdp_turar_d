package stockexchange.observer.events;

import java.time.Instant;
import java.util.UUID;
import java.util.Objects;

public record MarketAlertEvent(UUID eventId, Instant timestamp, String symbol, String message) implements MarketEvent {
    public MarketAlertEvent {
        Objects.requireNonNull(eventId);
        Objects.requireNonNull(timestamp);
        if (symbol == null || symbol.isBlank()) throw new IllegalArgumentException("Symbol required");
        java.util.Objects.requireNonNull(message);
    }
}
