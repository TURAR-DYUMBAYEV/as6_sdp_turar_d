package stockexchange.observer.listeners;
import java.math.BigDecimal;
import java.util.UUID;
import stockexchange.observer.*;
import stockexchange.observer.events.*;

public final class VolumeAlertListener implements Observer<TradingVolumeChangedEvent> {
    private final Subject<MarketEvent> alerts;
    public VolumeAlertListener(Subject<MarketEvent> alerts) { this.alerts = alerts; }
    public void onEvent(TradingVolumeChangedEvent e) {
        if (e.newVolume() > 0 && BigDecimal.valueOf(e.newVolume()).compareTo(BigDecimal.valueOf(e.previousVolume()).multiply(BigDecimal.valueOf(2))) >= 0)
            alerts.publish(new MarketAlertEvent(UUID.randomUUID(), e.timestamp(), e.symbol(), "Unusual trading volume"));
    }
}
