package stockexchange.observer.events;

import java.time.Instant;
import java.util.UUID;
import java.util.Objects;

public record TradingVolumeChangedEvent(UUID eventId, Instant timestamp, String symbol, long previousVolume, long newVolume) implements MarketEvent {
    public TradingVolumeChangedEvent {
        Objects.requireNonNull(eventId);
        Objects.requireNonNull(timestamp);
        if (symbol == null || symbol.isBlank()) throw new IllegalArgumentException("Symbol required");
        if (previousVolume < 0 || newVolume < 0) throw new IllegalArgumentException("Volumes cannot be negative");
    }
}
