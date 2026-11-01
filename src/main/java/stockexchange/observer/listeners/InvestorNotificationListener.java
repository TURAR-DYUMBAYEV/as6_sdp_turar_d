package stockexchange.observer.listeners;
import java.util.Set;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import stockexchange.observer.Observer;
import stockexchange.observer.events.MarketAlertEvent;

public final class InvestorNotificationListener implements Observer<MarketAlertEvent> {
    private final Set<String> symbols;
    private final ConcurrentLinkedQueue<MarketAlertEvent> messages = new ConcurrentLinkedQueue<>();
    public InvestorNotificationListener(Set<String> symbols) { this.symbols = Set.copyOf(symbols); }
    public void onEvent(MarketAlertEvent event) {
        if (symbols.contains(event.symbol())) messages.add(event);
    }
    public List<MarketAlertEvent> notifications() { return List.copyOf(messages); }
}
