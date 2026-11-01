package stockexchange.visitor.elements;

import java.math.BigDecimal;
import stockexchange.visitor.FinancialVisitor;

public final class BondHolding extends Holding {
    public BondHolding(String symbol, int quantity, BigDecimal price, RiskLevel risk) {
        super(symbol, quantity, price, risk);
    }

    public <R> R accept(FinancialVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
