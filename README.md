# Assignment 6 - Stock Exchange

Author: **Turar Dyumbayev**

Group: **SE-2535**

Variant: **3 - Stock Exchange**

A Java console simulation of prices, trading volumes and investor portfolios. Observer distributes live changes to independent listeners. Visitor adds valuation, statistics, filtering, JSON export and annual income operations to an immutable portfolio tree. All prices and rules are simulated; the application uses no market API, database or network connection.

## Build and run

Requirements: JDK 17 or later, Maven, and JUnit 5 for tests. The only project dependency is test-scoped JUnit. Compiler release is 17.

Open the existing pom.xml in IntelliJ as a Maven project. Choose **Bundled Maven**, reload the project, and run the Maven **test** goal. Run **stockexchange.app.Main**, using program arguments **4 --interactive** for four producers and the menu.

PowerShell commands from the existing project directory, using the verified IntelliJ Maven installation:

```powershell
Set-Location 'C:\Users\dumba\IdeaProjects\as6_sdp_turar_d'
& 'C:\Program Files\JetBrains\IntelliJ IDEA 2025.2.5\plugins\maven\lib\maven3\bin\mvn.cmd' clean test
java -cp target/classes stockexchange.app.Main 3
java -cp target/classes stockexchange.app.Main 4 --interactive
```

If Maven is on PATH, use **mvn clean test**. No global Maven installation is needed here. A plain run executes the demonstration and exits. The optional menu supports updates, analysis, JSON, subscription changes, one-time listeners, reports and concurrent simulation. Producer count must be at least three. Invalid menu numbers and values show a message; EOF exits.

## Project structure

- app: Main, Application composition root, ConsoleApp.
- observer: generic Observer<T>, Subject<T>, and shared EventSubject implementation.
- observer/events: immutable event records with UUID and Instant.
- observer/sources: independent StockPriceSource and TradingVolumeSource.
- observer/listeners: alerts, notifications, analytics, cache, aggregator and summary.
- visitor/elements: immutable containers and financial holdings.
- visitor/operations: five concrete financial visitors.
- integration: indexed subtree inspection and report listener.
- simulation: executor lifecycle and producer tasks.

Application creates all business dependencies and injects them through constructors. No dependency setters, service locator, lazy business initialization or framework is used. EventSubject contains the only registration and dispatch implementation.

## Observer

Each source publishes to its own injected Subject<MarketEvent>. Subscriptions use a concrete event class and a compatible typed Observer. Exact-class matching prevents unrelated delivery. Records include event ID, timestamp, symbol and old/new value when applicable.

| Listener | Responsibility |
| --- | --- |
| PriceAlertListener | Publishes an alert for absolute price changes of at least 5% |
| VolumeAlertListener | Publishes an alert when positive volume reaches twice the previous volume |
| InvestorNotificationListener | Records alerts only for watched symbols |
| MarketAnalyticsListener | Counts price and volume events |
| MarketCacheListener | Stores the latest price and volume sample per symbol |
| MarketEventAggregator | Observes both sources and publishes cumulative summaries |
| MarketSummaryListener | Counts summaries and retains the greatest cumulative total |
| PortfolioInspectionListener | Invokes a visitor on indexed groups |
| PortfolioReportListener | Stores immutable returned reports |

The real chain is source → MarketEventAggregator → MarketSummaryListener. Aggregator implements both Observer and Subject and delegates subscription mechanics to its injected output subject. Alert listeners separately publish to the investor-alert subject.

Push delivery sends the complete immutable event directly to listeners. Pull would require listeners to read a potentially changed source. Every accepted source update publishes exactly one market event; alerts, summaries and reports are separate derived events on separate subjects.

### Subscription and failure semantics

EventSubject uses CopyOnWriteArrayList and addIfAbsent for each (event class, observer) registration. Re-registering the same observer object for the same event class has no effect. Different lambda objects are distinct observers. Publishing traverses one snapshot; additions and removals affect future snapshots. A removed listener can still receive a callback from an existing snapshot. Self-unsubscription is safe, but it cannot cancel other publishers' snapshots.

Callbacks execute synchronously on the publisher thread. Different publishers may enter a listener concurrently. There is no global subject lock. A listener RuntimeException is passed to the injected failure handler; the application logs it and continues to other listeners. Failed callbacks are not retried. The injected failure handler must not throw. Successful publication is not a guarantee that a faulty callback completed its own work.

Analytics uses atomic counters. The aggregator uses one AtomicReference to an immutable count pair for consistent totals without a lock. Summary delivery can arrive out of order, so the summary listener retains the greatest total. Cache merge uses timestamp order and UUID to break equal-time ties deterministically. A cache snapshot is immutable but is not a transactionally simultaneous snapshot across all symbols. Alert/report queues are concurrent and unbounded, appropriate for this short simulation.

## Composite and Visitor

ExchangePortfolio → InvestmentAccount → AssetGroup → Holding gives four levels and three container types. Container child lists are defensive immutable copies. StockHolding, BondHolding, EtfHolding, OptionHolding and CommodityHolding contain only position data and accept dispatch. The abstract Holding holds shared fields; there is no valuation logic in elements.

Every visitor implements FinancialVisitor<R>. A concrete holding's accept method calls visitor.visit(this). The runtime holding selects its accept implementation, and the concrete type of this selects the correct overloaded visit method. Calling visit directly on a FinancialElement variable would not select the concrete holding overload.

Visitors own traversal: container visit methods visit children, while accept only dispatches. This avoids double traversal and lets the same operation start at a single AssetGroup. The immutable tree can be shared by producer threads.

| Visitor | Result and lifecycle |
| --- | --- |
| PortfolioValuationVisitor | BigDecimal value using an immutable optional quote map; reusable |
| PortfolioStatisticsVisitor | Stateful count of instruments, quantity/contract positions and counts per type; fresh instance per traversal |
| RiskFilterVisitor | Immutable list of actual matching Holding objects; reusable |
| JsonExportVisitor | Handwritten nested JSON with escaped quotes, backslashes, control characters and surrogate code units; reusable |
| DividendIncomeVisitor | Simulated annual cash income, including bond coupon proxy; reusable |

Rules: stock and ETF value = units × unit quote; bond value = number of bonds × quoted currency price per bond (not percentage of par); option value = contracts × premium per underlying unit × 100; commodity value = units × spot price. Prices and calculations use BigDecimal. No option pricing model or bond discounting is implied.

Annual income uses original position price × quantity × assumed yield: stocks 2%, bonds 5%, ETFs 3%, options and commodities 0%. The dividend visitor ignores live quotes because it models a reference-price income estimate. No rounding is applied within calculations.

Manual test portfolio: stock 2×100=200, bonds 3×90=270, ETF 4×50=200, option 1×2×100=200. Total 870. Annual income 4+13.50+6=23.50. The demo adds gold 2×2000=4000, giving 4870 total.

## Observer-to-Visitor integration

Application builds a symbol → list of AssetGroup index once. PortfolioInspectionListener looks up only matching groups; it does not scan the root. Multiple groups for one symbol are supported. For each matching group it creates a valuation visitor with the triggering quote, then publishes PortfolioReportEvent including the triggering UUID.

Reports overlay that event's quote on immutable reference prices for the other positions. This makes each report deterministic and independent of concurrent cache changes; it is not a whole-market snapshot. For AAPL 100→110, the demo Growth group changes from 600 to 620, excluding Stable entirely. A separate console valuation uses the latest cache snapshot and produces 4890 immediately after that update. Unknown symbols produce no report. The subtree test contains an unrelated element that throws if visited.

## Concurrency

MarketSimulation uses a fixed ExecutorService with a configurable count of at least three producers. Even-numbered tasks publish price changes; odd-numbered tasks publish volumes. Tasks check interruption, and all Futures are awaited. Normal shutdown awaits termination. Interruption requests cancellation, waits for cooperative tasks and restores the interrupt flag. Work not yet published during cancellation is not promised to complete. Listeners must avoid indefinite blocking, as Java cannot forcibly terminate an uncooperative callback.

The stress test runs four producer tasks, each publishing exactly 1,000 events: two price and two volume producers. One dedicated counting listener receives both event classes from their independent subjects. Assertions check 4,000 publications, 4,000 callbacks, 4,000 unique UUIDs, 2,000 price events, 2,000 volume events, 4,000 summaries, consistent final totals and executor termination. Futures and latches replace sleeps.

## Genuine Visitor trade-off commits

Core rewrite: **07f2424313c5f9a4886c13b7ee4d3d66036840ec**.

**New operation: aab3284a562f32fb7237eb6a92136f87f650507a**

Exact changed files:

- src/main/java/stockexchange/visitor/operations/DividendIncomeVisitor.java
- src/test/java/stockexchange/DividendTest.java

No financial element, existing visitor or visitor interface changed. The new operation worked through the existing double-dispatch contract. All 19 tests passed at this stage.

**New element: 68a9d07dd06a4feb04c9fd7154a3489b4286f04b**

Exact changed files:

- src/main/java/stockexchange/visitor/FinancialVisitor.java
- src/main/java/stockexchange/visitor/elements/CommodityHolding.java
- src/main/java/stockexchange/visitor/operations/DividendIncomeVisitor.java
- src/main/java/stockexchange/visitor/operations/JsonExportVisitor.java
- src/main/java/stockexchange/visitor/operations/PortfolioStatisticsVisitor.java
- src/main/java/stockexchange/visitor/operations/PortfolioValuationVisitor.java
- src/main/java/stockexchange/visitor/operations/RiskFilterVisitor.java
- src/test/java/stockexchange/CommodityTest.java

The new leaf required a new visitor overload and explicit support in every existing operation. All 20 tests passed at this stage. This is why Visitor is useful when elements stay stable and operations grow. It is a poor choice if new instrument types appear frequently while operations rarely change.

Integration and concurrency commit: **3bc5fa43507356bdd554fac736ac4ae2e795d434**. Existing Git history was preserved. Commits were created during this work, not backdated; the project does not claim two weeks of development.

## Verification and limitations

**33 tests passed, 0 failures, 0 errors, 0 skipped.** CoreTest 18, DividendTest 1, CommodityTest 1, IntegrationTest 13. The normal console demo completed and the 4,000-event stress test passed. Tests also cover exact nested JSON, every JSON control character, subscription snapshots, alert thresholds, cache ordering, interruption and subtree-only reports.

Verification used JDK **25.0.1**, compiling with **release 17**, and IntelliJ's bundled Maven. Actual JDK 17 execution remains unverified because no JDK 17 installation was found. The repository uses no third-party runtime libraries. Simulated rules are educational, not financial advice or market pricing models.

See [verification checklist](docs/VERIFICATION.md), [defense guide](docs/DEFENSE.md) and [PlantUML source](docs/class-diagram.puml). Variant allocation, the course deadline, LMS submission and live defense remain course/student responsibilities.

## Existing GitHub repository

The configured origin is https://github.com/TURAR-DYUMBAYEV/as6_sdp_turar_d.git and the working branch is main. No push was performed. After reviewing locally, push normally:
```powershell
Set-Location 'C:\Users\dumba\IdeaProjects\as6_sdp_turar_d'
git status
git push -u origin main
```
If the remote rejects a non-fast-forward update, inspect and integrate remote changes; do not force-push.
