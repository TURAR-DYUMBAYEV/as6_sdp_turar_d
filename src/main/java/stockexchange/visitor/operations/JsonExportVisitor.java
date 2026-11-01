package stockexchange.visitor.operations;

import java.util.List;
import java.util.stream.Collectors;
import stockexchange.visitor.*;
import stockexchange.visitor.elements.*;

public final class JsonExportVisitor implements FinancialVisitor<String> {
    public static String quote(String text) {
        var result = new StringBuilder("\"");
        for (char c : text.toCharArray()) {
            switch (c) {
                case '"' -> result.append("\\\"");
                case '\\' -> result.append("\\\\");
                default -> {
                    if (c < 32 || Character.isSurrogate(c)) result.append(String.format("\\u%04x", (int)c));
                    else result.append(c);
                }
            }
        }
        return result.append('"').toString();
    }

    private String container(String type, String name, List<FinancialElement> children) {
        return "{\"type\":" + quote(type) + ",\"name\":" + quote(name) + ",\"children\":["
            + children.stream().map(e -> e.accept(this)).collect(Collectors.joining(",")) + "]}";
    }

    private String holding(Holding h) {
        return "{\"type\":" + quote(h.getClass().getSimpleName()) + ",\"symbol\":" + quote(h.symbol())
            + ",\"quantity\":" + h.quantity() + ",\"price\":" + h.price().toPlainString()
            + ",\"risk\":" + quote(h.risk().name()) + "}";
    }

    public String visit(ExchangePortfolio p) { return container("ExchangePortfolio", p.name(), p.children()); }
    public String visit(InvestmentAccount a) { return container("InvestmentAccount", a.name(), a.children()); }
    public String visit(AssetGroup g) { return container("AssetGroup", g.name(), g.children()); }
    public String visit(StockHolding h) { return holding(h); }
    public String visit(BondHolding h) { return holding(h); }
    public String visit(EtfHolding h) { return holding(h); }
    public String visit(OptionHolding h) { return holding(h); }
}
