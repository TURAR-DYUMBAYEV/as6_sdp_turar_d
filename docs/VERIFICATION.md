# Official assignment verification

Executed with IntelliJ bundled Maven and installed JDK 25, compiling for release 17.
27 JUnit tests: 0 failures, 0 errors, 0 skipped.
AvailableValueTest: 1; MarketplaceTest: 23; SimulationTest: 3.
Stress: exactly 4,000 deliveries and 4,000 unique IDs, 4 threads x 1,000.
Standalone Main 3 demo completed successfully with category report and executor shutdown.

| Official requirement | Status | Evidence |
| --- | --- | --- |
| Java 17 language/API target | PASS | Maven compiler release 17 |
| Actual JDK 17 runtime verification | FAIL | Only JDK 25 available; tests ran there |
| JUnit 5, no other application libraries | PASS | pom.xml |
| At least two sources and five listeners | PASS | Two sources, eight wired listener roles |
| Own generic Subject and Observer | PASS | Marketplace.java |
| Immutable typed events | PASS | Records; immutable event test |
| Event-type subscriptions and runtime changes | PASS | Filtering and subscription tests; menu |
| Removal inside callback | PASS | Self-unsubscribe test and menu option 7 |
| Observer that republishes aggregates | PASS | MarketplaceAggregator and chain test |
| Push or pull justified | PASS | README push decision |
| Composite with three levels, two containers, four leaves | PASS | Nested sample catalog; five leaf classes |
| Generic Visitor and double dispatch | PASS | CatalogVisitor and gift-card dispatch tests |
| Numeric visitor | PASS | Tax and shipping manual expected totals |
| Stateful visitor | PASS | CatalogStatisticsVisitor and tests |
| Filter returns elements | PASS | Identity-preserving filter test |
| Manual nested XML export | PASS | XML parsed in test; nesting and escaping asserted |
| Traversal ownership justified | PASS | Visitor-owned recursion documented |
| Elements contain data and structure only | PASS | accept and structural methods; operations in visitors |
| Observer invokes visitor on related subtree | PASS | Integration test rejects unrelated traversal |
| Visitor result returns as report/event | PASS | Immutable CategoryReport registry and console output |
| At least three configurable producer threads | PASS | EventSimulation and CLI count |
| Thread-safe registry without whole-subject lock | PASS | Concurrent map and copy-on-write lists |
| Shared listener state thread-safe | PASS | Atomic counters and concurrent maps/queues |
| Graceful normal shutdown, no lost accepted events | PASS | Futures, termination checks, exact stress and mixed-source tests |
| One composition root | PASS | Application wires demo |
| Constructor-only dependency provision, no lazy business initialization | PASS | Inspection map and wishlist configuration use constructors; no setters/service locator |
| Clear responsibilities and no duplicate registry | PASS | Shared EventSubject; separate listener, visitor, simulation roles |
| Separate genuine new-operation and new-element commits | PASS | 51b99f6 and 6e9a366 |
| README actual changed files and trade-off cost | PASS | Full hashes and exact file lists |
| At least 15 deterministic tests | PASS | 27 executed |
| Exact 4 x 1,000 stress test | PASS | 4,000 callbacks and distinct IDs |
| README design decisions and UML | PASS | README and class-diagram.puml |
| Meaningful history over two weeks | FAIL | Genuine commits created now; elapsed history cannot be fabricated |
| No Observable, event bus, renamed lecture example | PASS | Marketplace-specific standard-library implementation |
| Individual variant uniqueness | FAIL | Requires course/group confirmation |
| GitHub link and LMS report submission | FAIL | No push authorized; student submission pending |
| Deadline and live defense | FAIL | Course deadline and defense cannot be certified by code tests |

PASS describes the implementation and evidence above, not a guarantee of instructor grading.
