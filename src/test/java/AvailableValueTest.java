import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class AvailableValueTest {
    @Test void excludesUnavailableProducts() {
        var category = new ProductCategory("c", "Category");
        category.add(new DigitalProduct("a", "Available", "c", new BigDecimal("20"), true));
        category.add(new DigitalProduct("b", "Unavailable", "c", new BigDecimal("50"), false));
        assertEquals(new BigDecimal("20"), category.accept(new AvailableValueVisitor()));
    }
}
