package stockexchange.integration;
import java.util.*;
import stockexchange.observer.Observer;
import stockexchange.observer.Subject;
import stockexchange.observer.events.*;
import stockexchange.visitor.elements.AssetGroup;
import stockexchange.visitor.operations.PortfolioValuationVisitor;

/** Each report overlays the triggering quote on immutable reference holdings. */
public final class PortfolioInspectionListener implements Observer<StockPriceChangedEvent> {
    private final Map<String, List<AssetGroup>> index;
    private final Subject<MarketEvent> reports;
    public PortfolioInspectionListener(Map<String, List<AssetGroup>> index, Subject<MarketEvent> reports) {
        var copy = new HashMap<String, List<AssetGroup>>();
        index.forEach((symbol, groups) -> copy.put(symbol, List.copyOf(groups)));
        this.index = Map.copyOf(copy);
        this.reports = reports;
    }
    public void onEvent(StockPriceChangedEvent event) {
        for (var group : index.getOrDefault(event.symbol(), List.of())) {
            var value = group.accept(new PortfolioValuationVisitor(Map.of(event.symbol(), event.newPrice())));
            reports.publish(new PortfolioReportEvent(UUID.randomUUID(), event.timestamp(), group.name(),
                event.symbol(), value, event.eventId()));
        }
    }
}
