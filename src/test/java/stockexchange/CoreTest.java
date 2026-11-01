package stockexchange;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import stockexchange.observer.*;
import stockexchange.observer.events.*;
import stockexchange.visitor.elements.*;
import stockexchange.visitor.operations.*;
import static org.junit.jupiter.api.Assertions.*;

class CoreTest {
    static AssetGroup group() {
        return new AssetGroup("Growth", List.of(
            new StockHolding("AAPL", 2, new BigDecimal("100"), RiskLevel.HIGH),
            new BondHolding("BOND", 3, new BigDecimal("90"), RiskLevel.LOW),
            new EtfHolding("ETF", 4, new BigDecimal("50"), RiskLevel.MEDIUM),
            new OptionHolding("CALL", 1, new BigDecimal("2"), RiskLevel.HIGH)));
    }
    static StockPriceChangedEvent event() {
        return new StockPriceChangedEvent(UUID.randomUUID(), Instant.EPOCH, "AAPL", BigDecimal.TEN, BigDecimal.ONE);
    }
    static EventSubject<MarketEvent> bus() { return new EventSubject<>((e, ex) -> { throw ex; }); }

    @Test void subscriptionDelivers() {
        var bus=bus(); var count=new AtomicInteger();
        bus.subscribe(StockPriceChangedEvent.class,e->count.incrementAndGet());
        bus.publish(event()); assertEquals(1,count.get());
    }
    @Test void unsubscriptionStopsDelivery() {
        var bus=bus(); var count=new AtomicInteger();
        stockexchange.observer.Observer<StockPriceChangedEvent> listener=e->count.incrementAndGet();
        bus.subscribe(StockPriceChangedEvent.class,listener);
        bus.unsubscribe(StockPriceChangedEvent.class,listener);
        bus.publish(event()); assertEquals(0,count.get());
    }
    @Test void typeFiltering() {
        var bus=bus(); var count=new AtomicInteger();
        bus.subscribe(TradingVolumeChangedEvent.class,e->count.incrementAndGet());
        bus.publish(event()); assertEquals(0,count.get());
    }
    @Test void duplicateSubscriptionIgnored() {
        var bus=bus(); var count=new AtomicInteger();
        stockexchange.observer.Observer<StockPriceChangedEvent> listener=e->count.incrementAndGet();
        bus.subscribe(StockPriceChangedEvent.class,listener); bus.subscribe(StockPriceChangedEvent.class,listener);
        bus.publish(event()); assertEquals(1,count.get());
    }
    @Test void selfUnsubscription() {
        var bus=bus(); var count=new AtomicInteger();
        var listener=new stockexchange.observer.Observer<StockPriceChangedEvent>() {
            public void onEvent(StockPriceChangedEvent e) {count.incrementAndGet();bus.unsubscribe(StockPriceChangedEvent.class,this);}
        };
        bus.subscribe(StockPriceChangedEvent.class,listener);bus.publish(event());bus.publish(event());
        assertEquals(1,count.get());
    }
    @Test void multipleSubjects() {
        var first=bus(); var second=bus(); var count=new AtomicInteger();
        stockexchange.observer.Observer<StockPriceChangedEvent> listener=e->count.incrementAndGet();
        first.subscribe(StockPriceChangedEvent.class,listener);second.subscribe(StockPriceChangedEvent.class,listener);
        first.publish(event());second.publish(event());assertEquals(2,count.get());
    }
    @Test void snapshotSubscription() {
        var bus=bus();var count=new AtomicInteger();
        stockexchange.observer.Observer<StockPriceChangedEvent> late=e->count.incrementAndGet();
        bus.subscribe(StockPriceChangedEvent.class,e->bus.subscribe(StockPriceChangedEvent.class,late));
        bus.publish(event());assertEquals(0,count.get());bus.publish(event());assertEquals(1,count.get());
    }
    @Test void listenerFailureDoesNotSkipOthers() {
        var failures=new AtomicInteger();var count=new AtomicInteger();
        var bus=new EventSubject<MarketEvent>((e,ex)->failures.incrementAndGet());
        bus.subscribe(StockPriceChangedEvent.class,e->{throw new IllegalStateException("test");});
        bus.subscribe(StockPriceChangedEvent.class,e->count.incrementAndGet());
        bus.publish(event());assertEquals(1,failures.get());assertEquals(1,count.get());
    }
    @Test void immutableEvent() { assertTrue(StockPriceChangedEvent.class.isRecord());assertEquals(BigDecimal.ONE,event().newPrice()); }
    @Test void invalidPriceRejected() {assertThrows(IllegalArgumentException.class,()->new StockPriceChangedEvent(UUID.randomUUID(),Instant.EPOCH,"A",BigDecimal.ZERO,BigDecimal.ONE));}
    @Test void valuationManual870() {assertEquals(new BigDecimal("870"),group().accept(new PortfolioValuationVisitor(Map.of())));}
    @Test void optionDoubleDispatchUsesMultiplier() {assertEquals(new BigDecimal("200"),group().children().get(3).accept(new PortfolioValuationVisitor(Map.of())));}
    @Test void statisticsCountEachPositionOnce() {
        var s=group().accept(new PortfolioStatisticsVisitor());
        assertEquals(4,s.instruments());assertEquals(10,s.positions());
        assertEquals(Map.of("StockHolding",1,"BondHolding",1,"EtfHolding",1,"OptionHolding",1),s.countsByType());
    }
    @Test void riskFilterReturnsActualElements() {
        var g=group();assertEquals(List.of(g.children().get(0),g.children().get(3)),g.accept(new RiskFilterVisitor(RiskLevel.HIGH)));
    }
    @Test void completeNestedTraversal() {
        var portfolio=new ExchangePortfolio("P",List.of(new InvestmentAccount("A",List.of(group()))));
        assertEquals(new BigDecimal("870"),portfolio.accept(new PortfolioValuationVisitor(Map.of())));
        assertEquals(4,portfolio.accept(new PortfolioStatisticsVisitor()).instruments());
    }
    @Test void emptyContainerJson() {assertEquals("{\"type\":\"AssetGroup\",\"name\":\"Empty\",\"children\":[]}",new AssetGroup("Empty",List.of()).accept(new JsonExportVisitor()));}
    @Test void quotesOverrideOnlyMatchingSymbol() {assertEquals(new BigDecimal("890"),group().accept(new PortfolioValuationVisitor(Map.of("AAPL",new BigDecimal("110")))));}
    @Test void childrenDefensivelyCopied() {
        var children=new ArrayList<stockexchange.visitor.FinancialElement>();children.addAll(group().children());
        var g=new AssetGroup("G",children);children.clear();assertEquals(4,g.children().size());
        assertThrows(UnsupportedOperationException.class,()->g.children().clear());
    }
}
