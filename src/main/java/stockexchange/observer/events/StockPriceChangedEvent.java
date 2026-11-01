package stockexchange.observer.events;

import java.time.Instant;
import java.util.UUID;
import java.util.Objects;

public record StockPriceChangedEvent(UUID eventId, Instant timestamp, String symbol, java.math.BigDecimal previousPrice, java.math.BigDecimal newPrice) implements MarketEvent {
    public StockPriceChangedEvent {
        Objects.requireNonNull(eventId);
        Objects.requireNonNull(timestamp);
        if (symbol == null || symbol.isBlank()) throw new IllegalArgumentException("Symbol required");
        if (previousPrice.signum() <= 0 || newPrice.signum() <= 0) throw new IllegalArgumentException("Prices must be positive");
    }
}
