package hex.pricer.domain;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public record ProductPriceListRequest(
        String name,
        BigDecimal basePrice,
        DiscountType discountType,
        Map<Integer, BigDecimal> discounts) {

    public ProductPriceList toProduct(String id) {
        return new ProductPriceList(id, name, basePrice, discountType, discounts);
    }
    public ProductPriceList toNewProduct() {
        return toProduct(UUID.randomUUID().toString());
    }
}
