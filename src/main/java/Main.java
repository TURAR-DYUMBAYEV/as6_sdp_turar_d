import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;

public class Main {
  public static void main(String[] args) throws Exception {
    var catalog=new MarketplaceCatalog("market","Online Marketplace");
    var electronics=new ProductCategory("electronics","Electronics");
    var books=new ProductCategory("books","Books"); var fiction=new ProductCategory("fiction","Fiction");
    electronics.add(new PhysicalProduct("phone","Phone & Case","electronics",new BigDecimal("100.00"),true,new BigDecimal("0.40")));
    electronics.add(new DigitalProduct("ebook","Clean Code <Guide>","electronics",new BigDecimal("20.00"),true));
    fiction.add(new ServiceProduct("review","Book Review","fiction",new BigDecimal("30.00"),true));
    fiction.add(new SubscriptionProduct("club","Reader Club","fiction",new BigDecimal("10.00"),true)); books.add(fiction); catalog.add(electronics); catalog.add(books);
    var prices=new PriceUpdateSource(); var stock=new StockUpdateSource(); var aggregator=new MarketplaceAggregator();
    var reports=new MarketplaceInspectionService(Map.of("electronics",electronics,"fiction",fiction));
    prices.subscribe(EventType.PRICE_CHANGED,aggregator); prices.subscribe(EventType.PRICE_DROPPED,aggregator); stock.subscribe(EventType.STOCK_CHANGED,aggregator::onStockEvent); stock.subscribe(EventType.LOW_STOCK,reports);
    var summary=new AtomicSummary(); aggregator.subscribe(EventType.MARKETPLACE_SUMMARY,summary);
    System.out.println("===== ONLINE MARKETPLACE ====="); System.out.println(catalog.accept(new XmlExportVisitor()));
    System.out.println("Tax: "+catalog.accept(new TaxCalculationVisitor())); System.out.println("Shipping: "+catalog.accept(new ShippingCostVisitor()));
    prices.update("phone",new BigDecimal("100.00"),new BigDecimal("90.00"),"electronics"); stock.update("phone",8,3,"electronics");
    System.out.println("Summary events: "+summary.count+", inspection reports: "+reports.reports());
    int threads=args.length>0?Integer.parseInt(args[0]):3; runSimulation(stock,threads); System.out.println("Simulation completed with "+threads+" producer threads.");
  }
  static void runSimulation(StockUpdateSource source,int threads)throws Exception{var ex=Executors.newFixedThreadPool(threads);try{var fs=new ArrayList<Future<?>>();for(int t=0;t<threads;t++)fs.add(ex.submit(()->{for(int i=0;i<100;i++)source.update("p"+i,10,9,"electronics");}));for(var f:fs)f.get();}finally{ex.shutdown();ex.awaitTermination(5,TimeUnit.SECONDS);}}
  static class AtomicSummary implements Observer<SummaryEvent>{volatile long count;public void onEvent(SummaryEvent e){count++;}}
}
