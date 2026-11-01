package stockexchange.visitor;

public interface FinancialElement {
    String name();
    <R> R accept(FinancialVisitor<R> visitor);
}
