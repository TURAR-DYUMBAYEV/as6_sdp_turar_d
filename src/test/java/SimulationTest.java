import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class SimulationTest {
    @Test void allMixedProducerEventsFinishBeforeReturn() throws Exception {
        var prices=new PriceUpdateSource();var stock=new StockUpdateSource();
        var p=new AtomicInteger();var s=new AtomicInteger();
        prices.subscribe(EventType.PRICE_CHANGED,e->p.incrementAndGet());
        stock.subscribe(EventType.STOCK_CHANGED,e->s.incrementAndGet());
        EventSimulation.run(prices,stock,3);
        assertEquals(300,p.get());assertEquals(300,s.get());
    }
    @Test void rejectsTooFewProducers() {
        assertThrows(IllegalArgumentException.class,()->EventSimulation.run(new PriceUpdateSource(),new StockUpdateSource(),2));
    }
    @Test void cacheRejectsLateOlderUpdate() {
        var cache=new ProductCacheListener();
        cache.onEvent(new PriceEvent("p",java.math.BigDecimal.TEN,java.math.BigDecimal.ONE,"c",EventType.PRICE_CHANGED,2));
        cache.onEvent(new PriceEvent("p",java.math.BigDecimal.ONE,java.math.BigDecimal.TEN,"c",EventType.PRICE_CHANGED,1));
        assertEquals(java.math.BigDecimal.ONE,cache.prices.get("p").newPrice());
    }
}
