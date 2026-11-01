# Stock Exchange defense guide

## Main ideas

Observer means that a source tells interested listeners about a change. A Subject stores subscriptions and publishes events. A Listener is an Observer with an onEvent method. A Subscription connects one event class to one listener. Our price and volume sources publish immutable records. Push means the event carries the values, so listeners do not ask the source again.

EventSubject keeps a copy-on-write list. A publisher sees one snapshot. A listener may unsubscribe inside its own callback. It can still be in another publisher's older snapshot. We do not lock the whole subject. Each shared listener uses atomic values or concurrent collections.

MarketEventAggregator is both Observer and Subject. It receives market events and publishes summary events. MarketSummaryListener receives them. This is a real chain.

Composite means that containers and leaves share FinancialElement. The portfolio contains accounts, accounts contain groups, and groups contain holdings. Containers hold immutable child lists.

Visitor puts an operation outside the elements. Each element calls visitor.visit(this) in accept. Double dispatch first selects the concrete accept method and then the concrete visit overload. This is why an option can use a 100-unit multiplier while a stock uses one unit.

A visitor controls traversal by visiting container children. accept does not loop over children. Statistics keeps counts while visiting many leaves, so we create a new statistics visitor for each report.

Adding DividendIncomeVisitor needed only a new visitor and a test. Adding CommodityHolding also changed FinancialVisitor and all five visitors. Visitor works well when element types stay stable.

ExecutorService owns worker threads. We submit tasks, wait for Futures and shut down the executor. On interruption we request cancellation and restore the interrupt flag. Our four-thread test expects exactly 4,000 events and 4,000 unique IDs.

The integration listener uses a symbol index. An AAPL event selects the Growth group. A valuation visitor uses the event price for AAPL and reference prices for the other holdings. The listener publishes a PortfolioReportEvent. It never visits Stable for an AAPL event.

## Explain the demo

Start stockexchange.app.Main with argument 3. Initial value is 4870. The AAPL update gives Growth value 620 and total live value 4890. Annual income is 23.50. Look for two investor alerts, one-time listener output, summary counts and terminated=true.

Use 4 --interactive for four producers and the menu. Options 8 and 9 subscribe/unsubscribe investor notifications. Option 10 demonstrates self-removal. Option 11 shows reports.

## Live coding tasks and solutions

### Add a trade-log listener

Create TradeLogListener implementing Observer<StockPriceChangedEvent>. Inject a thread-safe destination or use a ConcurrentLinkedQueue. Append the event in onEvent. Construct it in Application and subscribe it on priceBus. Test that one price event adds one log entry and a volume event adds none.

### Change the price alert threshold

Application passes new BigDecimal("0.05") to PriceAlertListener. Change it to "0.10" for ten percent. Add boundary tests: 100 to 109 must not alert; 100 to 110 must alert.

### Add a visitor that counts high-risk positions

Implement FinancialVisitor<Integer>. Container methods sum child results. Each concrete holding method returns h.quantity() when h.risk() is HIGH, otherwise zero. Implement all five leaf methods. Do not change element classes. Test stock plus option quantities with a manually computed total.

### Add a new holding type

Create CryptoHolding extending Holding and implement accept with visitor.visit(this). Add visit(CryptoHolding) to FinancialVisitor. Implement valuation, statistics, filter, JSON and dividend behavior. Add a sample and tests. Compilation shows which visitors still lack support.

### Show only low-risk instruments

Use portfolio.accept(new RiskFilterVisitor(RiskLevel.LOW)). The result contains the actual matching holdings. Print each symbol. In the demo only BOND is low risk.

### Change producer count

Pass 4 instead of 3 to Main. The count must be at least three. Keep the stress test at exactly four producers and 1,000 events each.

### Prove only a related subtree is visited

Read inspectionVisitsOnlyIndexedGroupAndPublishesUpdatedValue in IntegrationTest. The unrelated group contains an element that fails if accept is called. The test publishes an AAPL event and checks the new report value and triggering UUID.

## Questions to practice

Why not call visit(node) directly? Java chooses an overload using the declared parameter type. accept puts this in the concrete element method.

Can callbacks run at the same time? Yes. Different producers call the same listener, so ordinary mutable counters are unsafe.

Are reports a live market snapshot? No. Each report applies the triggering quote to its related reference group. Console live valuation uses the cache snapshot.

What happens when a listener throws? The configured handler logs the failure and dispatch continues to other listeners. We do not retry faulty callbacks.

What has not been verified? Execution on a real JDK 17. Tests ran on JDK 25 with Java 17 compilation targeting. No two-week history or completed course defense is claimed.
