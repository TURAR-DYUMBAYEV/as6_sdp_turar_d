package stockexchange.observer.events;

import java.time.Instant;
import java.util.UUID;
import java.util.Objects;

public record PortfolioReportEvent(UUID eventId, Instant timestamp, String groupId, String symbol, java.math.BigDecimal value, java.util.UUID triggeringEventId) implements MarketEvent {
    public PortfolioReportEvent {
        Objects.requireNonNull(eventId);
        Objects.requireNonNull(timestamp);
        if (symbol == null || symbol.isBlank()) throw new IllegalArgumentException("Symbol required");
        java.util.Objects.requireNonNull(value);
    }
}
