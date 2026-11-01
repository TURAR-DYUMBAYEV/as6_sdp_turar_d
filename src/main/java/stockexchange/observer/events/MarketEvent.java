package stockexchange.observer.events;

import java.time.Instant;
import java.util.UUID;

public interface MarketEvent {
    UUID eventId();
    Instant timestamp();
}
