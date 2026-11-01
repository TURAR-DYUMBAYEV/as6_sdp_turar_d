package stockexchange.observer.listeners;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import stockexchange.observer.Observer;
import stockexchange.observer.events.MarketSummaryEvent;

public final class MarketSummaryListener implements Observer<MarketSummaryEvent> {
    private final AtomicLong received = new AtomicLong();
    private final AtomicReference<MarketSummaryEvent> latest = new AtomicReference<>();
    public void onEvent(MarketSummaryEvent event) {
        received.incrementAndGet();
        latest.accumulateAndGet(event, (a,b) -> a == null || a.priceEvents()+a.volumeEvents() < b.priceEvents()+b.volumeEvents() ? b : a);
    }
    public long received() { return received.get(); }
    public MarketSummaryEvent latest() { return latest.get(); }
}
