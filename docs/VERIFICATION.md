# Verification checklist

Run on 2 November 2026, Asia/Qyzylorda. JDK 25.0.1; Maven compiler release 17.
JUnit: 33 tests, zero failures/errors/skips. CoreTest 18; DividendTest 1; CommodityTest 1; IntegrationTest 13.

Final clean test run passed. javap reports class major version 61 (Java 17).
PowerShell ConvertFrom-Json independently parsed the console portfolio export.
The four-producer interactive smoke test handled an unknown command and invalid price, exported JSON, and exited normally.

| Requirement | Result | Evidence |
| --- | --- | --- |
| Existing project and Git repository preserved | PASS | Same project path, origin and prior commit history |
| Obsolete domain source and docs removed | PASS | Current source/docs use Stock Exchange only |
| Java 17 source/API target | PASS | pom.xml release 17 |
| Actual JDK 17 execution | FAIL | No installed JDK 17; disclosed limitation allowed by request |
| Maven without manual global installation | PASS | IntelliJ bundled Maven used |
| JUnit 5 only, no runtime libraries | PASS | pom.xml dependency scope |
| Two independent sources | PASS | StockPriceSource and TradingVolumeSource, separate injected subjects |
| At least five listeners | PASS | Seven market listeners plus inspection/report listeners |
| Own generic Observer/Subject | PASS | observer package |
| Immutable typed events | PASS | Record events with UUID, Instant, symbol and values |
| Per-type filtering and no duplicate subscription | PASS | CoreTest |
| Runtime subscribe/unsubscribe and self-removal | PASS | CoreTest, snapshot test, console demo |
| Listener also a Subject | PASS | MarketEventAggregator; chain test |
| Push/pull justified | PASS | README |
| Three-level Composite minimum | PASS | Portfolio/account/group/holding, four levels |
| At least two containers and four leaves | PASS | Three container types, five holding types |
| Generic visitor and double dispatch | PASS | FinancialVisitor<R>, option/commodity tests |
| Numeric calculation | PASS | Manual value 870; option multiplier checked |
| Stateful accumulation | PASS | Statistics instrument, quantity and per-type counters |
| Filter returns actual elements | PASS | RiskFilterVisitor identity/list assertion |
| Manual valid nested export and escaping | PASS | Exact JSON fixture and all control-character tests |
| Additional meaningful visitor | PASS | DividendIncomeVisitor, expected income 23.50 |
| Elements contain data/structure only | PASS | Immutable records, Holding fields, accept |
| Traversal choice justified | PASS | Visitor-owned recursion, README |
| Observer triggers Visitor | PASS | Price event to PortfolioInspectionListener |
| Only related subtree inspected | PASS | Indexed groups; poison element test |
| Result returns to system | PASS | PortfolioReportEvent and report listener |
| Three or more configurable producers | PASS | Main count and MarketSimulation validation |
| Thread-safe registry, no global subject lock | PASS | Copy-on-write snapshot registry |
| Thread-safe shared state | PASS | Atomic counters/CAS pair, concurrent cache and queues |
| Safe registration changes during publishing | PASS | Latch-coordinated unsubscribe test |
| Graceful shutdown and interruption | PASS | Termination result and interruption test |
| Exact four-thread stress test | PASS | 4 x 1,000, exactly 4,000 publications/deliveries/UUIDs |
| No duplicate/lost accepted stress events | PASS | Count and distinct-ID assertions; consistent summaries |
| One composition root, constructor injection | PASS | Application wires dependencies; no lazy business dependency |
| Meaningful packages and small operations | PASS | app/observer/visitor/integration/simulation |
| At least 15 meaningful tests | PASS | 33 passing |
| Genuine new-visitor commit touching no elements | PASS | aab3284, two added files only |
| Genuine new-element commit updating all visitors | PASS | 68a9d07, eight files |
| Actual hashes and changed lists documented | PASS | README |
| README, defense guide and UML | PASS | docs and README match current classes |
| Console demo | PASS | Valuation 4870; update 4890; subtree 620; income 23.50; terminated=true |
| No Observable/event bus/renamed lecture example | PASS | Standard-library stock-exchange implementation |
| No push or new repository | PASS | Existing origin retained; local commits only |
| Two weeks of development history | FAIL | Cannot create elapsed history; no backdating |
| Variant allocation, deadline, GitHub/LMS submission and defense | FAIL | External course/student actions remain unverified or pending |

FAIL entries are disclosed environment/course limitations, not failing JUnit tests. Exact stress assertions passed. Normal completion awaits every producer Future. Interrupted, not-yet-published work is canceled. Arbitrarily blocking listener code cannot be forcibly terminated by Java.

The trade-off commits are genuine new Stock Exchange changes: dividend operation then commodity element. Prior commits remain in history without rewriting.
