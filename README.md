# Assignment 6 Observer and Visitor Patterns

This Java 17 Maven project implements Variant 4, an online marketplace. Price and stock sources push immutable events to listeners. A concurrent listener registry uses `ConcurrentHashMap` and `CopyOnWriteArrayList`; publication uses a snapshot, so an unsubscribe affects the next publication and never causes concurrent modification errors.

The catalog is a composite with `MarketplaceCatalog` and `ProductCategory` containers and four leaf types. Visitors own traversal and use double dispatch through `CatalogElement.accept`. The project includes tax, statistics, filtering, XML export, and shipping visitors. XML is escaped manually with standard Java strings.

The low-stock integration runs tax and shipping visitors only on the affected category and stores an immutable `CategoryReport`. The demo also runs configurable concurrent producers and shuts down its `ExecutorService` gracefully.

Run with `mvn test` and start with `java -cp target/classes Main 4`. Maven/JUnit dependency setup is in `pom.xml`. The project is intentionally dependency-light and uses no runtime libraries.

## Real Visitor trade-off proof

The starting project already included shipping and subscriptions and had no Git repository. We recorded that state honestly in baseline commit c6697242b1dbef9531d12dfca83e23877c7d1b1f. No earlier development history was invented.

New operation: 51b99f651b74e4d14e8d7e244393dc36d5c30de4 adds available product value. Its exact changed files are:

- src/main/java/AvailableValueVisitor.java
- src/test/java/AvailableValueTest.java

No element class or existing visitor was changed.

New element: 6e9a366f050c95d707705b5d05544983ed459bb8 adds an electronic gift card. Its exact changed files are:

- src/main/java/GiftCardProduct.java
- src/main/java/AvailableValueVisitor.java
- src/main/java/Marketplace.java

Marketplace.java contains the generic visitor interface and the five original concrete visitors; each now explicitly handles gift cards. Gift cards have zero shipping and zero tax at issue, contribute to total product count and value, can be filtered, and have their own XML tag. These are simulated rules.

An operation is easy to add because element types stay stable. Adding an element changes the interface and every operation. Visitor is a poor choice when new product types appear frequently but operations rarely change. The project cannot claim two weeks of history: these commits were made during completion, without backdating.

## Build and run without global Maven

Open pom.xml in IntelliJ as a Maven project. Select Java 17 as the project SDK, and Bundled Maven in Maven settings. Run the Maven test goal, then run Main. Alternatively, in PowerShell from the project directory:

```powershell
& 'C:\Program Files\JetBrains\IntelliJ IDEA 2025.2.5\plugins\maven\lib\maven3\bin\mvn.cmd' clean test
java -cp target/classes Main 3
java -cp target/classes Main 4 --interactive
```

The compiler release is 17. Verification on this machine uses installed JDK 25 with Java 17 release targeting; it does not establish execution on an actual JDK 17.

## Architecture and decisions

Main delegates to Application, the composition root. Application creates the catalog and wires all subscriptions. EventSimulation owns the executor lifecycle. The catalog has marketplace, category, nested category and product levels. Original four leaf types remain supported; gift cards are an additional fifth type.

PriceUpdateSource and StockUpdateSource share EventSubject registry logic. WishlistNotifier tracks saved products; PriceDropAlert records reductions of at least ten percent; InventoryAlertListener records fewer than five items; MarketplaceAnalyticsListener counts updates; ProductCacheListener keeps the latest sequence per product. MarketplaceAggregator republishes totals to MarketplaceSummaryListener. Stock LOW_STOCK events call MarketplaceInspectionService, which looks up the category directly and stores immutable reports. Reports are printed in the demo and menu.

Push delivery supplies immutable record data immediately. No listener must query a changing source. Every update emits exactly one general change event; a qualifying update also emits a separate alert event. Subscribe aggregation to general events only to avoid counting alerts as another update.

The registry uses per-type copy-on-write lists. Each publication iterates its own snapshot. Subscription changes affect snapshots created afterward; an observer removed during an existing publication may still receive that publication. Self-unsubscription is safe but does not cancel concurrent snapshots. Callbacks run synchronously on the publisher thread and may overlap across publishers. A runtime exception is printed and other listeners continue; there is no retry. Counters use atomics, queues and caches use concurrent collections. Cache sequence comparison prevents late older updates replacing newer ones. Aggregator totals are independently read atomic counters, not a transactionally consistent global snapshot.

Catalog prices and availability are fixed reference data for reports, while live updates are stored in the cache. The demo builds the tree before producers start and never edits its structure afterward. Container child snapshots are defensive copies. Integration reads only the event category, and each operation receives a fresh visitor instance.

All accumulating visitors are single-traversal objects; create a new instance for a new report. Container visit methods own recursion. accept only calls visit(this), so traversal happens once. For example PhysicalProduct.accept selects visit(PhysicalProduct), allowing the tax operation to apply 12 percent rather than digital 20 percent, service 15 percent or subscription 10 percent. Shipping uses 2.50 per physical weight unit, zero for digital, 5.00 for service travel and 1.00 for subscription fulfillment.

EventSimulation requires at least three producer threads and waits for every Future. Normal shutdown waits for termination. Interruption requests cancellation and restores the interrupt flag; interrupted work is not promised to complete.

## Verification and defense

JUnit tests cover subscriptions, snapshots, self-removal, failure isolation, duplicate prevention, source chaining, immutable events, manually calculated tax 21.50 and shipping 11.00, statistics, filtering, nested XML, escaping, empty categories, gift-card double dispatch, subtree-only inspection, cache ordering, and mixed producers.

The stress test uses four producer threads and 1,000 events each. It checks exactly 4,000 callbacks, 4,000 unique IDs, every Future, and executor termination. No sleeps are used for synchronization.

See docs/class-diagram.puml for UML and docs/DEFENSE.md for defense preparation. Development over two weeks, variant uniqueness in the class, LMS submission, and the live defense remain student/course responsibilities.
