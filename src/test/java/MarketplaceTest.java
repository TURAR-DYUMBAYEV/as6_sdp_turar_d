import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;

class MarketplaceTest {
    final EventType type = EventType.PRICE_CHANGED;
    ProductCategory category() {
        var c = new ProductCategory("c", "Category");
        c.add(new PhysicalProduct("p", "Parcel", "c", new BigDecimal("100"), true, new BigDecimal("2")));
        c.add(new DigitalProduct("d", "Download", "c", new BigDecimal("20"), true));
        c.add(new ServiceProduct("s", "Service", "c", new BigDecimal("30"), false));
        c.add(new SubscriptionProduct("u", "Subscription", "c", new BigDecimal("10"), true));
        return c;
    }
    @Test void subscription() { var s=new EventSubject<Integer>();var n=new AtomicInteger();s.subscribe(type,n::addAndGet);s.publish(2,type);assertEquals(2,n.get()); }
    @Test void filtering() { var s=new EventSubject<Integer>();var n=new AtomicInteger();s.subscribe(type,n::addAndGet);s.publish(2,EventType.LOW_STOCK);assertEquals(0,n.get()); }
    @Test void unsubscribe() { var s=new EventSubject<Integer>();var n=new AtomicInteger();Observer<Integer> o=n::addAndGet;s.subscribe(type,o);s.unsubscribe(type,o);s.publish(2,type);assertEquals(0,n.get()); }
    @Test void selfUnsubscribe() { var s=new EventSubject<Integer>();var n=new AtomicInteger();Observer<Integer> o=new Observer<>() {public void onEvent(Integer e){n.incrementAndGet();s.unsubscribe(type,this);}};s.subscribe(type,o);s.publish(1,type);s.publish(1,type);assertEquals(1,n.get()); }
    @Test void multipleListeners() {var s=new EventSubject<Integer>();var a=new AtomicInteger();var b=new AtomicInteger();s.subscribe(type,a::addAndGet);s.subscribe(type,b::addAndGet);s.publish(3,type);assertEquals(3,a.get());assertEquals(3,b.get());}
    @Test void multipleSubjects() {var a=new EventSubject<Integer>();var b=new EventSubject<Integer>();var n=new AtomicInteger();a.subscribe(type,n::addAndGet);b.subscribe(type,n::addAndGet);a.publish(1,type);b.publish(2,type);assertEquals(3,n.get());}
    @Test void duplicateSubscriptionIgnored() {var s=new EventSubject<Integer>();var n=new AtomicInteger();Observer<Integer> o=n::addAndGet;s.subscribe(type,o);s.subscribe(type,o);s.publish(1,type);assertEquals(1,n.get());}
    @Test void failingListenerDoesNotBlockOthers() {var s=new EventSubject<Integer>();var n=new AtomicInteger();s.subscribe(type,e->{throw new IllegalStateException("expected test failure");});s.subscribe(type,n::addAndGet);s.publish(1,type);assertEquals(1,n.get());}
    @Test void subscriptionDuringDeliveryStartsNextSnapshot() {var s=new EventSubject<Integer>();var n=new AtomicInteger();Observer<Integer> late=n::addAndGet;s.subscribe(type,e->s.subscribe(type,late));s.publish(1,type);assertEquals(0,n.get());s.publish(1,type);assertEquals(1,n.get());}
    @Test void stockDeliversOnce() {var s=new StockUpdateSource();var n=new AtomicInteger();s.subscribe(EventType.STOCK_CHANGED,e->n.incrementAndGet());s.update("p",10,9,"c");s.update("p",9,1,"c");assertEquals(2,n.get());}
    @Test void aggregationChain() {var s=new PriceUpdateSource();var a=new MarketplaceAggregator();var results=new ArrayList<SummaryEvent>();s.subscribe(type,a);a.subscribe(EventType.MARKETPLACE_SUMMARY,results::add);s.update("p",BigDecimal.TEN,BigDecimal.ONE,"c");assertEquals(1,results.size());assertEquals(1,results.get(0).priceEvents());}
    @Test void immutableEvents() {assertTrue(PriceEvent.class.isRecord());var e=new PriceEvent("p",BigDecimal.TEN,BigDecimal.ONE,"c",type,1);e.newPrice().add(BigDecimal.TEN);assertEquals(BigDecimal.ONE,e.newPrice());}
    @Test void taxManualTotal() {assertEquals(0,new BigDecimal("21.5").compareTo(category().accept(new TaxCalculationVisitor())));}
    @Test void shippingManualTotal() {assertEquals(0,new BigDecimal("11").compareTo(category().accept(new ShippingCostVisitor())));}
    @Test void statisticsAccumulate() {var r=category().accept(new CatalogStatisticsVisitor());assertEquals(4,r.products());assertEquals(1,r.physical());assertEquals(1,r.digital());assertEquals(1,r.services());assertEquals(1,r.subscriptions());assertEquals(new BigDecimal("160"),r.value());}
    @Test void filterReturnsActualElements() {var c=category();var r=c.accept(new ProductFilterVisitor(p->p.available&&p.price.compareTo(new BigDecimal("50"))>0));assertEquals(List.of(c.children().get(0)),r);}
    @Test void nestedTraversal() {var root=new MarketplaceCatalog("r","Root");var outer=new ProductCategory("o","Outer");outer.add(category());root.add(outer);assertEquals(4,root.accept(new CatalogStatisticsVisitor()).products());}
    @Test void emptyContainer() {var c=new ProductCategory("c","Empty");assertEquals(BigDecimal.ZERO,c.accept(new TaxCalculationVisitor()));assertEquals(List.of(),c.accept(new ProductFilterVisitor(p->true)));assertEquals("<ProductCategory id=\"c\" name=\"Empty\"></ProductCategory>",c.accept(new XmlExportVisitor()));}
    @Test void xmlNestingAndEscaping() throws Exception {var root=new MarketplaceCatalog("r","A & <B> \"C\"");root.add(category());String xml=root.accept(new XmlExportVisitor());var doc=DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));assertEquals("A & <B> \"C\"",doc.getDocumentElement().getAttribute("name"));assertEquals("MarketplaceCatalog",doc.getElementsByTagName("ProductCategory").item(0).getParentNode().getNodeName());assertEquals(1,doc.getElementsByTagName("SubscriptionProduct").getLength());}
    @Test void giftCardDoubleDispatchAllVisitors() {var g=new GiftCardProduct("g","Gift","c",BigDecimal.TEN,true);assertEquals(BigDecimal.ZERO,g.accept(new TaxCalculationVisitor()));assertEquals(BigDecimal.ZERO,g.accept(new ShippingCostVisitor()));assertEquals(1,g.accept(new CatalogStatisticsVisitor()).products());assertEquals(List.of(g),g.accept(new ProductFilterVisitor(p->true)));assertEquals(BigDecimal.TEN,g.accept(new AvailableValueVisitor()));assertTrue(g.accept(new XmlExportVisitor()).startsWith("<GiftCardProduct"));}
    @Test void lowStockInspectsOnlyRelatedSubtree() {var unrelated=new ProductCategory("other","Other"){public <R> R accept(CatalogVisitor<R> v){fail("Unrelated category visited");return null;}};var service=new MarketplaceInspectionService(Map.of("c",category(),"other",unrelated));var source=new StockUpdateSource();source.subscribe(EventType.LOW_STOCK,service);source.update("p",10,2,"c");assertEquals(1,service.reports().size());assertEquals(4,service.reports().get(0).products());assertEquals(0,new BigDecimal("21.5").compareTo(service.reports().get(0).tax()));}
    @Test void normalStockDoesNotInspect() {var service=new MarketplaceInspectionService(Map.of("c",category()));service.onEvent(new StockEvent("p",10,9,"c",EventType.STOCK_CHANGED,1));assertTrue(service.reports().isEmpty());}
    @Test void stressExactly4000Events() throws Exception {
        var subject=new EventSubject<Integer>();var count=new AtomicInteger();var unique=ConcurrentHashMap.<Integer>newKeySet();
        subject.subscribe(type,e->{count.incrementAndGet();unique.add(e);});
        var executor=Executors.newFixedThreadPool(4);
        try {var futures=new ArrayList<Future<?>>();for(int t=0;t<4;t++){int producer=t;futures.add(executor.submit(()->{for(int i=0;i<1000;i++)subject.publish(producer*1000+i,type);}));}for(var f:futures)f.get(10,TimeUnit.SECONDS);}
        finally {executor.shutdown();if(!executor.awaitTermination(10,TimeUnit.SECONDS)){executor.shutdownNow();fail("Executor did not terminate");}}
        assertTrue(executor.isTerminated());assertEquals(4000,count.get());assertEquals(4000,unique.size());
    }
}
