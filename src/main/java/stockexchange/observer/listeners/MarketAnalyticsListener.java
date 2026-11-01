package stockexchange.observer.listeners;
import java.util.concurrent.atomic.AtomicLong;
import stockexchange.observer.Observer;
import stockexchange.observer.events.*;

public final class MarketAnalyticsListener implements Observer<MarketEvent> {
    private final AtomicLong prices = new AtomicLong();
    private final AtomicLong volumes = new AtomicLong();
    public void onEvent(MarketEvent event) {
        if (event instanceof StockPriceChangedEvent) prices.incrementAndGet();
        if (event instanceof TradingVolumeChangedEvent) volumes.incrementAndGet();
    }
    public long priceCount() { return prices.get(); }
    public long volumeCount() { return volumes.get(); }
}
