package stockexchange.observer.events;

import java.time.Instant;
import java.util.UUID;
import java.util.Objects;

public record MarketSummaryEvent(UUID eventId, Instant timestamp, long priceEvents, long volumeEvents) implements MarketEvent {
    public MarketSummaryEvent {
        Objects.requireNonNull(eventId);
        Objects.requireNonNull(timestamp);
        
        
    }
}
