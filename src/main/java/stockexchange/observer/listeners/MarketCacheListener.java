package stockexchange.observer.listeners;
import java.math.BigDecimal;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import stockexchange.observer.Observer;
import stockexchange.observer.events.*;

/** Latest timestamp wins; UUID breaks equal-time ties deterministically. */
public final class MarketCacheListener implements Observer<MarketEvent> {
    private final ConcurrentHashMap<String, StockPriceChangedEvent> prices = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, TradingVolumeChangedEvent> volumes = new ConcurrentHashMap<>();
    private static boolean newer(MarketEvent a, MarketEvent b) {
        int time = a.timestamp().compareTo(b.timestamp());
        return time > 0 || (time == 0 && a.eventId().compareTo(b.eventId()) > 0);
    }
    public void onEvent(MarketEvent e) {
        if (e instanceof StockPriceChangedEvent p) prices.merge(p.symbol(), p, (a,b) -> newer(a,b) ? a : b);
        if (e instanceof TradingVolumeChangedEvent v) volumes.merge(v.symbol(), v, (a,b) -> newer(a,b) ? a : b);
    }
    public Map<String, BigDecimal> priceSnapshot() {
        var result = new HashMap<String, BigDecimal>();
        prices.forEach((symbol,event) -> result.put(symbol,event.newPrice()));
        return Map.copyOf(result);
    }
    public Map<String, TradingVolumeChangedEvent> volumeSnapshot() { return Map.copyOf(volumes); }
}
