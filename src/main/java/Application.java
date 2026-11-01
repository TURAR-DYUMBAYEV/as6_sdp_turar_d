import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;

class Application {
  static void run(String[] args) throws Exception {
    var catalog=new MarketplaceCatalog("market","Online Marketplace");
    var electronics=new ProductCategory("electronics","Electronics");
    var books=new ProductCategory("books","Books"); var fiction=new ProductCategory("fiction","Fiction");
    electronics.add(new PhysicalProduct("phone","Phone & Case","electronics",new BigDecimal("100.00"),true,new BigDecimal("0.40")));
    electronics.add(new DigitalProduct("ebook","Clean Code <Guide>","electronics",new BigDecimal("20.00"),true));
    fiction.add(new ServiceProduct("review","Book Review","fiction",new BigDecimal("30.00"),true));
    fiction.add(new SubscriptionProduct("club","Reader Club","fiction",new BigDecimal("10.00"),true)); books.add(fiction); catalog.add(electronics); catalog.add(books);
    var prices=new PriceUpdateSource(); var stock=new StockUpdateSource(); var aggregator=new MarketplaceAggregator();
    var reports=new MarketplaceInspectionService(Map.of("electronics",electronics,"fiction",fiction));
    prices.subscribe(EventType.PRICE_CHANGED,aggregator); stock.subscribe(EventType.STOCK_CHANGED,aggregator::onStockEvent); stock.subscribe(EventType.LOW_STOCK,reports);
    var wishlist=new WishlistNotifier(Set.of("phone")); var drop=new PriceDropAlert(); var inventory=new InventoryAlertListener(); var analytics=new MarketplaceAnalyticsListener(); var cache=new ProductCacheListener();
    prices.subscribe(EventType.PRICE_CHANGED,wishlist); prices.subscribe(EventType.PRICE_DROPPED,drop); stock.subscribe(EventType.LOW_STOCK,inventory); prices.subscribe(EventType.PRICE_CHANGED,analytics); stock.subscribe(EventType.STOCK_CHANGED,analytics::onStockEvent); prices.subscribe(EventType.PRICE_CHANGED,cache); stock.subscribe(EventType.STOCK_CHANGED,cache::onStockEvent);
    var summary=new MarketplaceSummaryListener(); aggregator.subscribe(EventType.MARKETPLACE_SUMMARY,summary);
    System.out.println("===== ONLINE MARKETPLACE ====="); System.out.println(catalog.accept(new XmlExportVisitor()));
    System.out.println("Tax: "+catalog.accept(new TaxCalculationVisitor())); System.out.println("Shipping: "+catalog.accept(new ShippingCostVisitor()));
    prices.update("phone",new BigDecimal("100.00"),new BigDecimal("90.00"),"electronics"); stock.update("phone",8,3,"electronics");
    System.out.println("Summary events: "+summary.received.get()+", inspection reports: "+reports.reports());
    int threads=3; for(String arg:args) if(!arg.startsWith("--")) threads=Integer.parseInt(arg); EventSimulation.run(prices,stock,threads); System.out.println("Simulation completed with "+threads+" producer threads.");
    if(Arrays.asList(args).contains("--interactive")) {
      var input=new Scanner(System.in);
      while(true) {
        System.out.println("1 Catalog | 2 Price | 3 Stock | 4 Listeners | 5 Subscribe wishlist | 6 Unsubscribe wishlist | 7 Self-unsubscribe | 8 Statistics | 9 Tax | 10 Shipping | 11 Filter | 12 XML | 13 Inspection | 14 Concurrent simulation | 15 Reports | 0 Exit");
        if(!input.hasNextLine()) break;
        try {
          int choice=Integer.parseInt(input.nextLine());
          if(choice==0) break;
          switch(choice) {
            case 1,12 -> System.out.println(catalog.accept(new XmlExportVisitor()));
            case 2 -> {System.out.println("New phone price:");prices.update("phone",new BigDecimal("100"),new BigDecimal(input.nextLine()),"electronics");}
            case 3 -> {System.out.println("New phone stock:");stock.update("phone",10,Integer.parseInt(input.nextLine()),"electronics");}
            case 4 -> System.out.println("Price subscriptions: "+prices.listenerCount()+", stock subscriptions: "+stock.listenerCount());
            case 5 -> prices.subscribe(EventType.PRICE_CHANGED,wishlist);
            case 6 -> prices.unsubscribe(EventType.PRICE_CHANGED,wishlist);
            case 7 -> {
              Observer<PriceEvent> once=new Observer<>() {public void onEvent(PriceEvent e){System.out.println("One-time listener received "+e.productId());prices.unsubscribe(EventType.PRICE_CHANGED,this);}};
              prices.subscribe(EventType.PRICE_CHANGED,once);
              prices.update("phone",BigDecimal.TEN,BigDecimal.ONE,"electronics");
              prices.update("phone",BigDecimal.TEN,BigDecimal.ONE,"electronics");
            }
            case 8 -> System.out.println("Price events: "+analytics.prices+", stock events: "+analytics.stocks+", summary deliveries: "+summary.received);
            case 9 -> System.out.println(catalog.accept(new TaxCalculationVisitor()));
            case 10 -> System.out.println(catalog.accept(new ShippingCostVisitor()));
            case 11 -> {System.out.println("Maximum price:");var limit=new BigDecimal(input.nextLine());catalog.accept(new ProductFilterVisitor(p->p.available&&p.price.compareTo(limit)<=0)).forEach(p->System.out.println(p.id()+" "+p.name()));}
            case 13 -> {stock.update("phone",10,1,"electronics");System.out.println(reports.reports());}
            case 14 -> EventSimulation.run(prices,stock,threads);
            case 15 -> System.out.println(reports.reports());
            default -> System.out.println("Choose a listed option.");
          }
        } catch(IllegalArgumentException ex) {System.out.println("Invalid input: "+ex.getMessage());}
      }
    }
  }
}
