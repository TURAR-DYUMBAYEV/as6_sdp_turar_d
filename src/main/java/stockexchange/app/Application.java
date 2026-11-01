package stockexchange.app;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.*;
import stockexchange.observer.*;
import stockexchange.observer.events.*;
import stockexchange.observer.sources.*;
import stockexchange.observer.listeners.*;
import stockexchange.visitor.elements.*;
import stockexchange.integration.*;
import stockexchange.simulation.MarketSimulation;

/** The only composition root: creates dependencies and wires subscriptions. */
public final class Application {
    private Application() { }

    private static EventSubject<MarketEvent> subject() {
        return new EventSubject<>((event, exception) ->
            System.err.println("Listener failed for " + event.eventId() + ": " + exception.getMessage()));
    }

    public static ConsoleApp create() {
        var growth = new AssetGroup("Growth", List.of(
            new StockHolding("AAPL", 2, new BigDecimal("100"), RiskLevel.HIGH),
            new EtfHolding("ETF", 4, new BigDecimal("50"), RiskLevel.MEDIUM),
            new OptionHolding("CALL", 1, new BigDecimal("2"), RiskLevel.HIGH)));
        var stable = new AssetGroup("Stable", List.of(
            new BondHolding("BOND", 3, new BigDecimal("90"), RiskLevel.LOW),
            new CommodityHolding("GOLD", 2, new BigDecimal("2000"), RiskLevel.HIGH)));
        var portfolio = new ExchangePortfolio("Turar portfolio", List.of(
            new InvestmentAccount("Personal account", List.of(growth, stable))));

        var priceBus = subject();
        var volumeBus = subject();
        var alertBus = subject();
        var reportBus = subject();
        var prices = new StockPriceSource(priceBus, Clock.systemUTC());
        var volumes = new TradingVolumeSource(volumeBus, Clock.systemUTC());
        var notifications = new InvestorNotificationListener(Set.of("AAPL"));
        var analytics = new MarketAnalyticsListener();
        var cache = new MarketCacheListener();
        var aggregator = new MarketEventAggregator(subject());
        var summary = new MarketSummaryListener();
        var reports = new PortfolioReportListener();

        priceBus.subscribe(StockPriceChangedEvent.class, new PriceAlertListener(alertBus, new BigDecimal("0.05")));
        volumeBus.subscribe(TradingVolumeChangedEvent.class, new VolumeAlertListener(alertBus));
        alertBus.subscribe(MarketAlertEvent.class, notifications);
        priceBus.subscribe(StockPriceChangedEvent.class, analytics);
        volumeBus.subscribe(TradingVolumeChangedEvent.class, analytics);
        priceBus.subscribe(StockPriceChangedEvent.class, cache);
        volumeBus.subscribe(TradingVolumeChangedEvent.class, cache);
        priceBus.subscribe(StockPriceChangedEvent.class, aggregator);
        volumeBus.subscribe(TradingVolumeChangedEvent.class, aggregator);
        aggregator.subscribe(MarketSummaryEvent.class, summary);
        reportBus.subscribe(PortfolioReportEvent.class, reports);
        var index = Map.of("AAPL", List.of(growth), "ETF", List.of(growth), "CALL", List.of(growth),
            "BOND", List.of(stable), "GOLD", List.of(stable));
        priceBus.subscribe(StockPriceChangedEvent.class, new PortfolioInspectionListener(index, reportBus));

        return new ConsoleApp(portfolio, prices, volumes, priceBus, alertBus, notifications, analytics,
            cache, summary, reports, new MarketSimulation(prices, volumes));
    }
}
