package stockexchange;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.*;
import stockexchange.visitor.elements.*;
import stockexchange.visitor.operations.*;
import static org.junit.jupiter.api.Assertions.*;

class CommodityTest {
    @Test void allOperationsSupportNewElement() {
        var gold=new CommodityHolding("GOLD",2,new BigDecimal("2000"),RiskLevel.HIGH);
        assertEquals(new BigDecimal("4000"),gold.accept(new PortfolioValuationVisitor(Map.of())));
        assertEquals(BigDecimal.ZERO,gold.accept(new DividendIncomeVisitor()));
        assertEquals(List.of(gold),gold.accept(new RiskFilterVisitor(RiskLevel.HIGH)));
        assertEquals(Map.of("CommodityHolding",1),gold.accept(new PortfolioStatisticsVisitor()).countsByType());
        assertEquals("{\"type\":\"CommodityHolding\",\"symbol\":\"GOLD\",\"quantity\":2,\"price\":2000,\"risk\":\"HIGH\"}",gold.accept(new JsonExportVisitor()));
    }
}
