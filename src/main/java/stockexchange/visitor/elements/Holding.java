package stockexchange.visitor.elements;

import java.math.BigDecimal;
import java.util.Objects;
import stockexchange.visitor.FinancialElement;

/** Immutable position data. No financial operations live here. */
public abstract class Holding implements FinancialElement {
    private final String symbol;
    private final int quantity;
    private final BigDecimal price;
    private final RiskLevel risk;

    protected Holding(String symbol, int quantity, BigDecimal price, RiskLevel risk) {
        if (symbol == null || symbol.isBlank() || quantity < 0 || price.signum() < 0)
            throw new IllegalArgumentException("Invalid position data");
        this.symbol = symbol;
        this.quantity = quantity;
        this.price = price;
        this.risk = Objects.requireNonNull(risk);
    }

    public String symbol() { return symbol; }
    public String name() { return symbol; }
    public int quantity() { return quantity; }
    public BigDecimal price() { return price; }
    public RiskLevel risk() { return risk; }
}
