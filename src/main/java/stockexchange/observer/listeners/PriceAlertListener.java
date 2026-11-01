package stockexchange.observer.listeners;
import java.math.BigDecimal;
import java.util.UUID;
import stockexchange.observer.*;
import stockexchange.observer.events.*;

public final class PriceAlertListener implements Observer<StockPriceChangedEvent> {
    private final Subject<MarketEvent> alerts;
    private final BigDecimal threshold;
    public PriceAlertListener(Subject<MarketEvent> alerts, BigDecimal threshold) {
        if (threshold.signum() < 0) throw new IllegalArgumentException("Negative threshold");
        this.alerts = alerts;
        this.threshold = threshold;
    }
    public void onEvent(StockPriceChangedEvent e) {
        if (e.newPrice().subtract(e.previousPrice()).abs().compareTo(e.previousPrice().multiply(threshold)) >= 0)
            alerts.publish(new MarketAlertEvent(UUID.randomUUID(), e.timestamp(), e.symbol(), "Significant price movement"));
    }
}
