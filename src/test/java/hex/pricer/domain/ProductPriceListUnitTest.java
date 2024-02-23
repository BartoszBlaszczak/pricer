package hex.pricer.domain;

import hex.pricer.domain.ProductPriceList;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static hex.pricer.domain.DiscountType.AMOUNT_BASED;
import static hex.pricer.domain.DiscountType.PERCENTAGE_BASED;
import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.*;

class ProductPriceListUnitTest {

    @Test
    void getPriceForAmountBasedDiscount() {
        // given
        BigDecimal basePrice = BigDecimal.valueOf(50);
        var discounts = Map.of(
                10, BigDecimal.valueOf(5),
                20, BigDecimal.valueOf(15),
                30, BigDecimal.valueOf(20),
                40, BigDecimal.valueOf(25)
        );
        var priceList = new ProductPriceList(UUID.randomUUID().toString(), "test", basePrice, AMOUNT_BASED, discounts);

        Map.of(
                1, basePrice,
                5, basePrice,
                10, BigDecimal.valueOf(45),
                14, BigDecimal.valueOf(45),
                22, BigDecimal.valueOf(35),
                29, BigDecimal.valueOf(35),
                30, BigDecimal.valueOf(30),
                40, BigDecimal.valueOf(25),
                50, BigDecimal.valueOf(25)
        ).forEach((quantity, expectedPrice) ->
                // when-then
                assertEquals(expectedPrice, priceList.getPriceFor(quantity))
        );
    }

    @Test
    void getPriceForPercentageBasedDiscount() {
        // given
        BigDecimal basePrice = BigDecimal.valueOf(19.99);
        var discounts = Map.of(
                10, BigDecimal.valueOf(15),
                20, BigDecimal.valueOf(25),
                30, BigDecimal.valueOf(35),
                40, BigDecimal.valueOf(40)
        );
        var priceList = new ProductPriceList(UUID.randomUUID().toString(), "test", basePrice, PERCENTAGE_BASED, discounts);

        Map.of(
                1, basePrice,
                5, basePrice,
                10, BigDecimal.valueOf(16.99),
                14, BigDecimal.valueOf(16.99),
                22, BigDecimal.valueOf(14.99),
                29, BigDecimal.valueOf(14.99),
                30, BigDecimal.valueOf(12.99),
                40, BigDecimal.valueOf(11.99),
                50, BigDecimal.valueOf(11.99)
        ).forEach((quantity, expectedPrice) ->
                // when-then
                assertEquals(expectedPrice, priceList.getPriceFor(quantity))
        );
    }

    @Test
    void getPriceForProductWithoutDiscounts() {
        // given
        BigDecimal basePrice = BigDecimal.valueOf(9.99);
        var priceList = new ProductPriceList(UUID.randomUUID().toString(), "test", basePrice, PERCENTAGE_BASED, emptyMap());

        // when
        BigDecimal price = priceList.getPriceFor(100);

        // then
        assertEquals(basePrice, price);
    }
}