package stockexchange.observer.sources;

import java.time.Clock;
import java.util.UUID;
import stockexchange.observer.Subject;
import stockexchange.observer.events.*;

public final class StockPriceSource {
    private final Subject<MarketEvent> subject;
    private final Clock clock;

    public StockPriceSource(Subject<MarketEvent> subject, Clock clock) {
        this.subject = subject;
        this.clock = clock;
    }

    public StockPriceChangedEvent update(String symbol, java.math.BigDecimal previous, java.math.BigDecimal current) {
        var event = new StockPriceChangedEvent(UUID.randomUUID(), clock.instant(), symbol, previous, current);
        subject.publish(event);
        return event;
    }
}
