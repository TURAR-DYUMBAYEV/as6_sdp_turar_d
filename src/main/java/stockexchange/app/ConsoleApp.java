package stockexchange.app;

import java.math.BigDecimal;
import java.io.PrintStream;
import java.util.*;
import stockexchange.observer.Observer;
import stockexchange.observer.Subject;
import stockexchange.observer.events.*;
import stockexchange.observer.sources.*;
import stockexchange.observer.listeners.*;
import stockexchange.visitor.elements.*;
import stockexchange.visitor.operations.*;
import stockexchange.integration.PortfolioReportListener;
import stockexchange.simulation.MarketSimulation;

public final class ConsoleApp {
    private final ExchangePortfolio portfolio;
    private final StockPriceSource prices;
    private final TradingVolumeSource volumes;
    private final Subject<MarketEvent> priceBus;
    private final Subject<MarketEvent> alerts;
    private final InvestorNotificationListener notifications;
    private final MarketAnalyticsListener analytics;
    private final MarketCacheListener cache;
    private final MarketSummaryListener summary;
    private final PortfolioReportListener reports;
    private final MarketSimulation simulation;

    public ConsoleApp(ExchangePortfolio portfolio, StockPriceSource prices, TradingVolumeSource volumes,
            Subject<MarketEvent> priceBus, Subject<MarketEvent> alerts, InvestorNotificationListener notifications,
            MarketAnalyticsListener analytics, MarketCacheListener cache, MarketSummaryListener summary,
            PortfolioReportListener reports, MarketSimulation simulation) {
        this.portfolio = portfolio;
        this.prices = prices;
        this.volumes = volumes;
        this.priceBus = priceBus;
        this.alerts = alerts;
        this.notifications = notifications;
        this.analytics = analytics;
        this.cache = cache;
        this.summary = summary;
        this.reports = reports;
        this.simulation = simulation;
    }

    public void demo(int producers, PrintStream out) throws Exception {
        out.println("=== STOCK EXCHANGE - SIMULATED DATA ===");
        out.println("Initial portfolio: " + portfolio.accept(new JsonExportVisitor()));
        out.println("Initial valuation: " + portfolio.accept(new PortfolioValuationVisitor(Map.of())));
        prices.update("AAPL", new BigDecimal("100"), new BigDecimal("110"));
        volumes.update("AAPL", 1000, 2500);
        out.println("Investor notifications: " + notifications.notifications());
        out.println("Observer chain summary: " + summary.latest());
        out.println("AAPL subtree report: " + reports.reports());
        out.println("Live quote valuation: " + portfolio.accept(new PortfolioValuationVisitor(cache.priceSnapshot())));
        out.println("Statistics: " + portfolio.accept(new PortfolioStatisticsVisitor()));
        out.println("High risk: " + portfolio.accept(new RiskFilterVisitor(RiskLevel.HIGH)).stream().map(Holding::symbol).toList());
        out.println("Annual income: " + portfolio.accept(new DividendIncomeVisitor()));
        selfUnsubscribe(out);
        int before = notifications.notifications().size();
        alerts.unsubscribe(MarketAlertEvent.class, notifications);
        prices.update("AAPL", new BigDecimal("100"), new BigDecimal("120"));
        out.println("Unsubscribed investor received extra notifications: " + (notifications.notifications().size()-before));
        alerts.subscribe(MarketAlertEvent.class, notifications);
        out.println("Concurrent simulation: " + simulation.run(producers, 100));
        out.println("Delivered price/volume events: " + analytics.priceCount() + "/" + analytics.volumeCount());
        out.println("Summary deliveries: " + summary.received());
    }

    private void selfUnsubscribe(PrintStream out) {
        var once = new Observer<StockPriceChangedEvent>() {
            public void onEvent(StockPriceChangedEvent event) {
                out.println("One-time listener: " + event.symbol());
                priceBus.unsubscribe(StockPriceChangedEvent.class, this);
            }
        };
        priceBus.subscribe(StockPriceChangedEvent.class, once);
        prices.update("AAPL", new BigDecimal("110"), new BigDecimal("111"));
        prices.update("AAPL", new BigDecimal("111"), new BigDecimal("112"));
    }

    public void interactive(int producers, Scanner input, PrintStream out) throws Exception {
        while (true) {
            out.println("1 Price | 2 Volume | 3 Valuation | 4 Statistics | 5 High risk | 6 JSON | 7 Income | 8 Subscribe | 9 Unsubscribe | 10 One-time listener | 11 Reports | 12 Simulate | 13 Notifications | 0 Exit");
            if (!input.hasNextLine()) return;
            String command = input.nextLine().trim();
            if (command.equals("0")) return;
            try {
                switch (command) {
                    case "1" -> {
                        out.println("New AAPL price:");
                        var value = new BigDecimal(input.nextLine());
                        prices.update("AAPL", cache.priceSnapshot().getOrDefault("AAPL",new BigDecimal("100")),value);
                    }
                    case "2" -> {
                        out.println("New AAPL volume:");
                        volumes.update("AAPL",1000,Long.parseLong(input.nextLine()));
                    }
                    case "3" -> out.println(portfolio.accept(new PortfolioValuationVisitor(cache.priceSnapshot())));
                    case "4" -> out.println(portfolio.accept(new PortfolioStatisticsVisitor()));
                    case "5" -> out.println(portfolio.accept(new RiskFilterVisitor(RiskLevel.HIGH)).stream().map(Holding::symbol).toList());
                    case "6" -> out.println(portfolio.accept(new JsonExportVisitor()));
                    case "7" -> out.println(portfolio.accept(new DividendIncomeVisitor()));
                    case "8" -> alerts.subscribe(MarketAlertEvent.class,notifications);
                    case "9" -> alerts.unsubscribe(MarketAlertEvent.class,notifications);
                    case "10" -> selfUnsubscribe(out);
                    case "11" -> out.println(reports.reports());
                    case "12" -> out.println(simulation.run(producers,100));
                    case "13" -> out.println(notifications.notifications());
                    default -> out.println("Choose a listed command.");
                }
            } catch (IllegalArgumentException ex) {
                out.println("Invalid input: " + ex.getMessage());
            } catch (NoSuchElementException ex) {
                return;
            }
        }
    }
}
