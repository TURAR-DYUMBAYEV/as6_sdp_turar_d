package stockexchange.visitor.operations;

import java.util.*;
import stockexchange.visitor.*;
import stockexchange.visitor.elements.*;

public final class RiskFilterVisitor implements FinancialVisitor<List<Holding>> {
    private final RiskLevel risk;
    public RiskFilterVisitor(RiskLevel risk) { this.risk = Objects.requireNonNull(risk); }
    private List<Holding> filter(List<FinancialElement> children) {
        return children.stream().flatMap(e -> e.accept(this).stream()).toList();
    }
    private List<Holding> match(Holding h) { return h.risk() == risk ? List.of(h) : List.of(); }
    public List<Holding> visit(ExchangePortfolio p) { return filter(p.children()); }
    public List<Holding> visit(InvestmentAccount a) { return filter(a.children()); }
    public List<Holding> visit(AssetGroup g) { return filter(g.children()); }
    public List<Holding> visit(StockHolding h) { return match(h); }
    public List<Holding> visit(BondHolding h) { return match(h); }
    public List<Holding> visit(EtfHolding h) { return match(h); }
    public List<Holding> visit(OptionHolding h) { return match(h); }
}
