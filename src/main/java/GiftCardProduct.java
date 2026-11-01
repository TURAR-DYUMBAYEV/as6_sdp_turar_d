import java.math.BigDecimal;

/** Electronic stored value; tax is charged when redeemed, not when issued. */
class GiftCardProduct extends Product {
    GiftCardProduct(String id, String name, String category, BigDecimal price, boolean available) {
        super(id, name, category, price, available);
    }
    public <R> R accept(CatalogVisitor<R> visitor) { return visitor.visit(this); }
}
