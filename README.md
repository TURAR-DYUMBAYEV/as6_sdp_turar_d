# Assignment 6 Observer and Visitor Patterns

This Java 17 Maven project implements Variant 4, an online marketplace. Price and stock sources push immutable events to listeners. A concurrent listener registry uses `ConcurrentHashMap` and `CopyOnWriteArrayList`; publication uses a snapshot, so an unsubscribe affects the next publication and never causes concurrent modification errors.

The catalog is a composite with `MarketplaceCatalog` and `ProductCategory` containers and four leaf types. Visitors own traversal and use double dispatch through `CatalogElement.accept`. The project includes tax, statistics, filtering, XML export, and shipping visitors. XML is escaped manually with standard Java strings.

The low-stock integration runs tax and shipping visitors only on the affected category and stores an immutable `CategoryReport`. The demo also runs configurable concurrent producers and shuts down its `ExecutorService` gracefully.

Run with `mvn test` and start with `java -cp target/classes Main 4`. Maven/JUnit dependency setup is in `pom.xml`. The project is intentionally dependency-light and uses no runtime libraries.

For the required trade-off proof, use separate commits: first add `ShippingCostVisitor` without changing product classes, then add `SubscriptionProduct` and update visitor methods. Commit hashes and exact file lists must be recorded after Git is initialized; no hashes are invented here.
