# Defense Notes

Observer uses push delivery: each immutable event is passed directly to subscribed listeners. Copy-on-write listener lists make concurrent publication safe and permit self-unsubscription.

Visitor uses double dispatch: the client calls `element.accept(visitor)`, and each concrete element calls the matching `visitor.visit(this)`. This keeps data classes small while making new operations easy.

The inspection service receives a low-stock event, finds its category, and runs visitors on that subtree only. Add a listener by implementing `Observer<T>` and subscribing it at the composition root. Add a visitor by implementing all `CatalogVisitor` methods. A new element is more expensive because every visitor needs a new visit method.

Change producer count with Main 4, or Main 4 --interactive. The minimum is three.

Five practice questions:

1. Why not call visitor.visit(node)? Java chooses overloaded methods from the declared argument type. accept runs on the concrete element and supplies its concrete this type.
2. Can a removed listener still run? Yes, when an already-started publication captured it in its snapshot. Later snapshots exclude it.
3. How do you know no stress events were duplicated? The test asserts both 4,000 callbacks and 4,000 unique IDs, then verifies executor termination.
4. Why does inspection avoid the root? The category map resolves the category ID directly. A test places a failing visitor entry in an unrelated category.
5. What did the trade-off commits prove? AvailableValueVisitor added an operation without changing elements. GiftCardProduct required an interface method and handlers in all six concrete visitors.
