package stockexchange.visitor.operations;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import stockexchange.visitor.*;
import stockexchange.visitor.elements.*;

/** Immutable quote snapshot; price means unit price, option premium uses a 100-unit contract. */
public final class PortfolioValuationVisitor implements FinancialVisitor<BigDecimal> {
    private final Map<String, BigDecimal> quotes;

    public PortfolioValuationVisitor(Map<String, BigDecimal> quotes) {
        this.quotes = Map.copyOf(quotes);
    }

    private BigDecimal sum(List<FinancialElement> children) {
        return children.stream().map(e -> e.accept(this)).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal value(Holding holding, int multiplier) {
        return quotes.getOrDefault(holding.symbol(), holding.price())
            .multiply(BigDecimal.valueOf(holding.quantity())).multiply(BigDecimal.valueOf(multiplier));
    }

    public BigDecimal visit(ExchangePortfolio p) { return sum(p.children()); }
    public BigDecimal visit(InvestmentAccount a) { return sum(a.children()); }
    public BigDecimal visit(AssetGroup g) { return sum(g.children()); }
    public BigDecimal visit(StockHolding h) { return value(h, 1); }
    public BigDecimal visit(BondHolding h) { return value(h, 1); }
    public BigDecimal visit(EtfHolding h) { return value(h, 1); }
    public BigDecimal visit(OptionHolding h) { return value(h, 100); }
    public BigDecimal visit(CommodityHolding h) { return value(h, 1); }
}
