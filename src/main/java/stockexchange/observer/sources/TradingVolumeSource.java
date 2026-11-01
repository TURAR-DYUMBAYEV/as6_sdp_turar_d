package stockexchange.observer.sources;

import java.time.Clock;
import java.util.UUID;
import stockexchange.observer.Subject;
import stockexchange.observer.events.*;

public final class TradingVolumeSource {
    private final Subject<MarketEvent> subject;
    private final Clock clock;

    public TradingVolumeSource(Subject<MarketEvent> subject, Clock clock) {
        this.subject = subject;
        this.clock = clock;
    }

    public TradingVolumeChangedEvent update(String symbol, long previous, long current) {
        var event = new TradingVolumeChangedEvent(UUID.randomUUID(), clock.instant(), symbol, previous, current);
        subject.publish(event);
        return event;
    }
}
