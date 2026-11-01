package stockexchange.observer.listeners;
import java.util.concurrent.atomic.AtomicReference;
import java.util.UUID;
import stockexchange.observer.*;
import stockexchange.observer.events.*;

public final class MarketEventAggregator implements Observer<MarketEvent>, Subject<MarketEvent> {
    private record Counts(long prices, long volumes) { }
    private final AtomicReference<Counts> counts = new AtomicReference<>(new Counts(0,0));
    private final Subject<MarketEvent> output;
    public MarketEventAggregator(Subject<MarketEvent> output) { this.output = output; }
    public void onEvent(MarketEvent event) {
        if (!(event instanceof StockPriceChangedEvent) && !(event instanceof TradingVolumeChangedEvent)) return;
        var current = counts.updateAndGet(old -> event instanceof StockPriceChangedEvent
            ? new Counts(old.prices()+1, old.volumes()) : new Counts(old.prices(), old.volumes()+1));
        publish(new MarketSummaryEvent(UUID.randomUUID(), event.timestamp(), current.prices(), current.volumes()));
    }
    public <E extends MarketEvent> void subscribe(Class<E> type, Observer<? super E> observer) { output.subscribe(type,observer); }
    public <E extends MarketEvent> void unsubscribe(Class<E> type, Observer<? super E> observer) { output.unsubscribe(type,observer); }
    public void publish(MarketEvent event) { output.publish(event); }
}
