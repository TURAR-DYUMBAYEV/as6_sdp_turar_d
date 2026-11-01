# Defense Notes

Observer uses push delivery: each immutable event is passed directly to subscribed listeners. Copy-on-write listener lists make concurrent publication safe and permit self-unsubscription.

Visitor uses double dispatch: the client calls `element.accept(visitor)`, and each concrete element calls the matching `visitor.visit(this)`. This keeps data classes small while making new operations easy.

The inspection service receives a low-stock event, finds its category, and runs visitors on that subtree only. Add a listener by implementing `Observer<T>` and subscribing it at the composition root. Add a visitor by implementing all `CatalogVisitor` methods. A new element is more expensive because every visitor needs a new visit method.
