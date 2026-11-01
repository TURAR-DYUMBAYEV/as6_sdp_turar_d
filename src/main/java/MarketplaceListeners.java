import java.util.Set;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

class WishlistNotifier implements Observer<PriceEvent> {
    private final Set<String> saved;
    final ConcurrentLinkedQueue<String> notifications = new ConcurrentLinkedQueue<>();
    WishlistNotifier(Set<String> saved) { this.saved = Set.copyOf(saved); }
    public void onEvent(PriceEvent e) { if (saved.contains(e.productId())) notifications.add(e.productId()+" now costs "+e.newPrice()); }
}
class PriceDropAlert implements Observer<PriceEvent> {
    final ConcurrentLinkedQueue<String> alerts = new ConcurrentLinkedQueue<>();
    public void onEvent(PriceEvent e) {
        if(e.newPrice().compareTo(e.previousPrice().multiply(new java.math.BigDecimal("0.90")))<=0)
            alerts.add(e.productId()+" dropped at least 10 percent");
    }
}
class InventoryAlertListener implements Observer<StockEvent> {
    final ConcurrentLinkedQueue<String> alerts = new ConcurrentLinkedQueue<>();
    public void onEvent(StockEvent e) { if(e.newQuantity()<5) alerts.add(e.productId()+": "+e.newQuantity()+" remaining"); }
}
class MarketplaceAnalyticsListener implements Observer<PriceEvent> {
    final AtomicLong prices = new AtomicLong(), stocks = new AtomicLong();
    public void onEvent(PriceEvent e) { prices.incrementAndGet(); }
    public void onStockEvent(StockEvent e) { stocks.incrementAndGet(); }
}
class ProductCacheListener implements Observer<PriceEvent> {
    final Map<String,PriceEvent> prices = new ConcurrentHashMap<>();
    final Map<String,StockEvent> stocks = new ConcurrentHashMap<>();
    public void onEvent(PriceEvent e) { prices.merge(e.productId(),e,(a,b)->a.sequence()>b.sequence()?a:b); }
    public void onStockEvent(StockEvent e) { stocks.merge(e.productId(),e,(a,b)->a.sequence()>b.sequence()?a:b); }
}
class MarketplaceSummaryListener implements Observer<SummaryEvent> {
    final AtomicLong received = new AtomicLong();
    public void onEvent(SummaryEvent e) { received.incrementAndGet(); }
}
