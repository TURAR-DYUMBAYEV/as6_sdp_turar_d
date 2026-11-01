package stockexchange;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import stockexchange.observer.*;
import stockexchange.observer.events.*;
import stockexchange.observer.sources.*;
import stockexchange.observer.listeners.*;
import stockexchange.integration.*;
import stockexchange.simulation.MarketSimulation;
import stockexchange.visitor.*;
import stockexchange.visitor.elements.*;
import stockexchange.visitor.operations.*;
import static org.junit.jupiter.api.Assertions.*;

class IntegrationTest {
    private static final Clock CLOCK = Clock.fixed(Instant.EPOCH, ZoneOffset.UTC);

    @Test void aggregationRepublishesConsistentTotals() {
        var input=CoreTest.bus();var aggregator=new MarketEventAggregator(CoreTest.bus());
        var summary=new MarketSummaryListener();
        aggregator.subscribe(MarketSummaryEvent.class,summary);
        input.subscribe(StockPriceChangedEvent.class,aggregator);
        input.subscribe(TradingVolumeChangedEvent.class,aggregator);
        new StockPriceSource(input,CLOCK).update("AAPL",BigDecimal.TEN,BigDecimal.ONE);
        new TradingVolumeSource(input,CLOCK).update("AAPL",10,20);
        assertEquals(2,summary.received());assertEquals(1,summary.latest().priceEvents());assertEquals(1,summary.latest().volumeEvents());
    }

    @Test void inspectionVisitsOnlyIndexedGroupAndPublishesUpdatedValue() {
        var poison=new FinancialElement() {
            public String name() { return "Unrelated"; }
            public <R> R accept(FinancialVisitor<R> visitor) { fail("Unrelated group visited");return null; }
        };
        var unrelated=new AssetGroup("Other",List.of(poison));
        var reports=CoreTest.bus();var listener=new PortfolioReportListener();
        reports.subscribe(PortfolioReportEvent.class,listener);
        var inspection=new PortfolioInspectionListener(Map.of("AAPL",List.of(CoreTest.group()),"OTHER",List.of(unrelated)),reports);
        var input=CoreTest.bus();input.subscribe(StockPriceChangedEvent.class,inspection);
        var event=new StockPriceSource(input,CLOCK).update("AAPL",new BigDecimal("100"),new BigDecimal("110"));
        assertEquals(1,listener.reports().size());
        var report=listener.reports().get(0);
        assertEquals(new BigDecimal("890"),report.value());
        assertEquals(event.eventId(),report.triggeringEventId());
        assertEquals("Growth",report.groupId());
    }

    @Test void unknownSymbolProducesNoReport() {
        var reports=CoreTest.bus();var listener=new PortfolioReportListener();
        reports.subscribe(PortfolioReportEvent.class,listener);
        new PortfolioInspectionListener(Map.of(),reports).onEvent(CoreTest.event());
        assertTrue(listener.reports().isEmpty());
    }

    @Test void notificationsFilterSymbolsAndRespectUnsubscribe() {
        var alerts=CoreTest.bus();var investor=new InvestorNotificationListener(Set.of("AAPL"));
        alerts.subscribe(MarketAlertEvent.class,investor);
        var price=new PriceAlertListener(alerts,new BigDecimal(".05"));
        price.onEvent(CoreTest.event());
        price.onEvent(new StockPriceChangedEvent(UUID.randomUUID(),Instant.EPOCH,"OTHER",BigDecimal.TEN,BigDecimal.ONE));
        assertEquals(1,investor.notifications().size());
        alerts.unsubscribe(MarketAlertEvent.class,investor);price.onEvent(CoreTest.event());
        assertEquals(1,investor.notifications().size());
    }

    @Test void volumeThresholdAndPriceThreshold() {
        var alerts=CoreTest.bus();var n=new AtomicInteger();
        alerts.subscribe(MarketAlertEvent.class,e->n.incrementAndGet());
        new VolumeAlertListener(alerts).onEvent(new TradingVolumeChangedEvent(UUID.randomUUID(),Instant.EPOCH,"A",100,199));
        assertEquals(0,n.get());
        new VolumeAlertListener(alerts).onEvent(new TradingVolumeChangedEvent(UUID.randomUUID(),Instant.EPOCH,"A",100,200));
        new PriceAlertListener(alerts,new BigDecimal(".05")).onEvent(new StockPriceChangedEvent(UUID.randomUUID(),Instant.EPOCH,"A",new BigDecimal("100"),new BigDecimal("105")));
        assertEquals(2,n.get());
    }

    @Test void cacheKeepsNewerQuoteWhenDeliveryIsReordered() {
        var cache=new MarketCacheListener();
        cache.onEvent(new StockPriceChangedEvent(UUID.randomUUID(),Instant.EPOCH.plusSeconds(1),"A",BigDecimal.ONE,BigDecimal.TEN));
        cache.onEvent(new StockPriceChangedEvent(UUID.randomUUID(),Instant.EPOCH,"A",BigDecimal.TEN,BigDecimal.ONE));
        assertEquals(BigDecimal.TEN,cache.priceSnapshot().get("A"));
        assertThrows(UnsupportedOperationException.class,()->cache.priceSnapshot().clear());
    }

    @Test void stressExactly4000DeliveriesAndUniqueEventIds() throws Exception {
        var price=CoreTest.bus();var volume=CoreTest.bus();
        var count=new AtomicInteger();var ids=ConcurrentHashMap.<UUID>newKeySet();
        stockexchange.observer.Observer<MarketEvent> counter=e->{count.incrementAndGet();ids.add(e.eventId());};
        price.subscribe(StockPriceChangedEvent.class,counter);
        volume.subscribe(TradingVolumeChangedEvent.class,counter);
        var analytics=new MarketAnalyticsListener();
        price.subscribe(StockPriceChangedEvent.class,analytics);volume.subscribe(TradingVolumeChangedEvent.class,analytics);
        var aggregator=new MarketEventAggregator(CoreTest.bus());var summaries=new MarketSummaryListener();
        aggregator.subscribe(MarketSummaryEvent.class,summaries);
        price.subscribe(StockPriceChangedEvent.class,aggregator);volume.subscribe(TradingVolumeChangedEvent.class,aggregator);
        var simulation=new MarketSimulation(new StockPriceSource(price,CLOCK),new TradingVolumeSource(volume,CLOCK));
        var result=simulation.run(4,1000);
        assertEquals(4000,result.published());assertTrue(result.terminated());
        assertEquals(4000,count.get());assertEquals(4000,ids.size());
        assertEquals(2000,analytics.priceCount());assertEquals(2000,analytics.volumeCount());
        assertEquals(4000,summaries.received());
        assertEquals(2000,summaries.latest().priceEvents());assertEquals(2000,summaries.latest().volumeEvents());
    }

    @Test void threeProducerSimulationTerminates() throws Exception {
        var result=new MarketSimulation(new StockPriceSource(CoreTest.bus(),CLOCK),new TradingVolumeSource(CoreTest.bus(),CLOCK)).run(3,20);
        assertEquals(60,result.published());assertTrue(result.terminated());
    }

    @Test void rejectsFewerThanThreeProducers() {
        var simulation=new MarketSimulation(new StockPriceSource(CoreTest.bus(),CLOCK),new TradingVolumeSource(CoreTest.bus(),CLOCK));
        assertThrows(IllegalArgumentException.class,()->simulation.run(2,100));
    }

    @Test void concurrentUnsubscribeKeepsExistingSnapshot() throws Exception {
        var bus=CoreTest.bus();var entered=new CountDownLatch(1);var release=new CountDownLatch(1);var count=new AtomicInteger();
        bus.subscribe(StockPriceChangedEvent.class,e->{
            entered.countDown();
            try {if(!release.await(5,TimeUnit.SECONDS)) throw new IllegalStateException("Release timed out");}
            catch(InterruptedException ex){Thread.currentThread().interrupt();throw new IllegalStateException(ex);}
        });
        stockexchange.observer.Observer<StockPriceChangedEvent> late=e->count.incrementAndGet();
        bus.subscribe(StockPriceChangedEvent.class,late);
        var executor=Executors.newSingleThreadExecutor();
        try {
            var future=executor.submit(()->bus.publish(CoreTest.event()));
            assertTrue(entered.await(5,TimeUnit.SECONDS));
            bus.unsubscribe(StockPriceChangedEvent.class,late);
            release.countDown();future.get(5,TimeUnit.SECONDS);
            bus.publish(CoreTest.event());assertEquals(1,count.get());
        } finally {release.countDown();executor.shutdownNow();assertTrue(executor.awaitTermination(5,TimeUnit.SECONDS));}
    }

    @Test void jsonFullNestedStructure() {
        var p=new ExchangePortfolio("P",List.of(new InvestmentAccount("A",List.of(
            new AssetGroup("G",List.of(new StockHolding("X",1,BigDecimal.TEN,RiskLevel.HIGH)))))));
        String expected="{\"type\":\"ExchangePortfolio\",\"name\":\"P\",\"children\":[{\"type\":\"InvestmentAccount\",\"name\":\"A\",\"children\":[{\"type\":\"AssetGroup\",\"name\":\"G\",\"children\":[{\"type\":\"StockHolding\",\"symbol\":\"X\",\"quantity\":1,\"price\":10,\"risk\":\"HIGH\"}]}]}]}";
        assertEquals(expected,p.accept(new JsonExportVisitor()));
    }

    @Test void jsonEscapesAllControlCharactersQuotesAndBackslashes() {
        assertEquals("\"a\\\"b\\\\c\\u000a\\u0009\\u000d\\u0000\"",JsonExportVisitor.quote("a\"b\\c\n\t\r\u0000"));
        for(int i=0;i<32;i++) assertEquals("\""+String.format("\\u%04x",i)+"\"",JsonExportVisitor.quote(String.valueOf((char)i)));
    }

    @Test void simulationInterruptionRestoresFlagAndStopsProducers() throws Exception {
        var started=new CountDownLatch(1);var blocker=new CountDownLatch(1);
        var restored=new AtomicBoolean();var error=new AtomicReference<Throwable>();
        var bus=CoreTest.bus();
        bus.subscribe(StockPriceChangedEvent.class,e->{
            started.countDown();
            try {blocker.await();} catch(InterruptedException ex){Thread.currentThread().interrupt();}
        });
        var simulation=new MarketSimulation(new StockPriceSource(bus,CLOCK),new TradingVolumeSource(CoreTest.bus(),CLOCK));
        var controller=new Thread(()->{
            try {simulation.run(3,1000);error.set(new AssertionError("Expected interruption"));}
            catch(InterruptedException ex){restored.set(Thread.currentThread().isInterrupted());}
            catch(Throwable ex){error.set(ex);}
        });
        controller.start();
        try {
            assertTrue(started.await(5,TimeUnit.SECONDS));controller.interrupt();controller.join(10000);
            assertFalse(controller.isAlive());assertNull(error.get());assertTrue(restored.get());
        } finally {blocker.countDown();controller.interrupt();controller.join(10000);}
    }
}
