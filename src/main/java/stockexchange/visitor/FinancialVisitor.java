package stockexchange.visitor;

import stockexchange.visitor.elements.*;

public interface FinancialVisitor<R> {
    R visit(ExchangePortfolio portfolio);
    R visit(InvestmentAccount account);
    R visit(AssetGroup group);
    R visit(StockHolding holding);
    R visit(BondHolding holding);
    R visit(EtfHolding holding);
    R visit(OptionHolding holding);
}
