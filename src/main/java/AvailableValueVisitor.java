import java.math.BigDecimal;

/** Total listed value of available products. Create one instance per traversal. */
class AvailableValueVisitor extends TraversingVisitor<BigDecimal> {
    private BigDecimal total = BigDecimal.ZERO;
    protected BigDecimal result() { return total; }
    private BigDecimal add(Product product) {
        if (product.available) total = total.add(product.price);
        return total;
    }
    public BigDecimal visit(PhysicalProduct product) { return add(product); }
    public BigDecimal visit(DigitalProduct product) { return add(product); }
    public BigDecimal visit(ServiceProduct product) { return add(product); }
    public BigDecimal visit(SubscriptionProduct product) { return add(product); }
    public BigDecimal visit(GiftCardProduct product) { return add(product); }
}
