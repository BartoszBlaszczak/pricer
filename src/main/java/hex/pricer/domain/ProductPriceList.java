package hex.pricer.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.Map;

import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_UP;

@Document("pricelists")
public record ProductPriceList(
        @Id String id,
        String name,
        BigDecimal basePrice,
        DiscountType discountType,
        Map<Integer, BigDecimal> discounts
) {
    private static final BigDecimal hundred = BigDecimal.valueOf(100);
    public BigDecimal getPriceFor(Integer quantity) {
        var discountKey = discounts.keySet().stream().filter(key -> key <= quantity).max(Integer::compareTo);
        BigDecimal discount = discountKey.map(discounts::get).orElse(ZERO);
        return switch (discountType) {
            case AMOUNT_BASED -> basePrice.subtract(discount);
            case PERCENTAGE_BASED -> {
                BigDecimal percentage = hundred.subtract(discount).divide(hundred, 2, HALF_UP);
                yield basePrice.multiply(percentage).setScale(2, HALF_UP);
            }
        };
    }
}
