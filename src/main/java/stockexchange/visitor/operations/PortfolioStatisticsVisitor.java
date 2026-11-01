package stockexchange.visitor.operations;

import java.util.*;
import stockexchange.visitor.*;
import stockexchange.visitor.elements.*;

/** Single traversal accumulator. Use a fresh instance for every report. */
public final class PortfolioStatisticsVisitor implements FinancialVisitor<PortfolioStatisticsVisitor.Statistics> {
    public record Statistics(int instruments, long positions, Map<String, Integer> countsByType) {
        public Statistics { countsByType = Map.copyOf(countsByType); }
    }

    private int instruments;
    private long positions;
    private final Map<String, Integer> counts = new TreeMap<>();

    private Statistics result() { return new Statistics(instruments, positions, counts); }
    private Statistics traverse(List<FinancialElement> children) {
        children.forEach(e -> e.accept(this));
        return result();
    }
    private Statistics count(Holding holding) {
        instruments++;
        positions += holding.quantity();
        counts.merge(holding.getClass().getSimpleName(), 1, Integer::sum);
        return result();
    }

    public Statistics visit(ExchangePortfolio p) { return traverse(p.children()); }
    public Statistics visit(InvestmentAccount a) { return traverse(a.children()); }
    public Statistics visit(AssetGroup g) { return traverse(g.children()); }
    public Statistics visit(StockHolding h) { return count(h); }
    public Statistics visit(BondHolding h) { return count(h); }
    public Statistics visit(EtfHolding h) { return count(h); }
    public Statistics visit(OptionHolding h) { return count(h); }
    public Statistics visit(CommodityHolding h) { return count(h); }
}
