package stockexchange;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import stockexchange.visitor.operations.DividendIncomeVisitor;
import static org.junit.jupiter.api.Assertions.*;

class DividendTest {
    @Test void annualIncomeManualTotal2350() {
        // 200 * .02 + 270 * .05 + 200 * .03 + 0 = 23.50
        assertEquals(0, new BigDecimal("23.50").compareTo(CoreTest.group().accept(new DividendIncomeVisitor())));
    }
}
