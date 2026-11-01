package stockexchange.visitor.elements;

import java.util.List;
import java.util.Objects;
import stockexchange.visitor.*;

public record InvestmentAccount(String name, List<FinancialElement> children) implements FinancialElement {
    public InvestmentAccount {
        Objects.requireNonNull(name);
        children = List.copyOf(children);
    }

    public <R> R accept(FinancialVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
