package stockexchange.visitor.operations;

import java.math.BigDecimal;
import java.util.List;
import stockexchange.visitor.*;
import stockexchange.visitor.elements.*;

/** Simulated annual cash income: stock 2%, bond 5%, ETF 3%, options zero. */
public final class DividendIncomeVisitor implements FinancialVisitor<BigDecimal> {
    private BigDecimal sum(List<FinancialElement> children) {
        return children.stream().map(e -> e.accept(this)).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal income(Holding holding, String rate) {
        return holding.price().multiply(BigDecimal.valueOf(holding.quantity())).multiply(new BigDecimal(rate));
    }

    public BigDecimal visit(ExchangePortfolio p) { return sum(p.children()); }
    public BigDecimal visit(InvestmentAccount a) { return sum(a.children()); }
    public BigDecimal visit(AssetGroup g) { return sum(g.children()); }
    public BigDecimal visit(StockHolding h) { return income(h, "0.02"); }
    public BigDecimal visit(BondHolding h) { return income(h, "0.05"); }
    public BigDecimal visit(EtfHolding h) { return income(h, "0.03"); }
    public BigDecimal visit(OptionHolding h) { return BigDecimal.ZERO; }
}
