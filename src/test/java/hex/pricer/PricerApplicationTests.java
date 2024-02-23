package hex.pricer;

import hex.pricer.domain.ProductPriceList;
import hex.pricer.domain.ProductPriceListRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

import static hex.pricer.domain.DiscountType.AMOUNT_BASED;
import static hex.pricer.domain.DiscountType.PERCENTAGE_BASED;
import static java.math.BigDecimal.ONE;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PricerApplicationTests {
    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    private String applicationUrl() {
        return "http://localhost:" + port + "/prices";
    }

    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:latest");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        mongoDBContainer.start();
        registry.add("spring.data.mongodb.port", () -> mongoDBContainer.getMappedPort(27017));
    }

    @Test
    public void CRUDIntegration() {
        // add a new price list
        var addProductPriceListRequest = new ProductPriceListRequest("product", BigDecimal.valueOf(100), PERCENTAGE_BASED, emptyMap());
        ResponseEntity<String> addProductResponse = restTemplate.postForEntity(applicationUrl(), addProductPriceListRequest, String.class);
        String id = addProductResponse.getBody();

        // update the price list
        var updateProductPriceListRequest = new ProductPriceListRequest("product", BigDecimal.valueOf(50), PERCENTAGE_BASED, emptyMap());
        restTemplate.put(applicationUrl() + "/" + id, updateProductPriceListRequest);

        // check that the price list exists and have proper properties
        ResponseEntity<ProductPriceList[]> allPriceListsResponse = restTemplate.getForEntity(applicationUrl(), ProductPriceList[].class);

        assertThat(allPriceListsResponse.getBody()).anySatisfy(priceList -> {
            assertThat(priceList.id()).isEqualTo(id);
            assertThat(priceList.name()).isEqualTo(addProductPriceListRequest.name());
            assertThat(priceList.basePrice()).isEqualTo(updateProductPriceListRequest.basePrice());
            assertThat(priceList.discountType()).isEqualTo(addProductPriceListRequest.discountType());
        });

        // delete the price list
        restTemplate.delete(applicationUrl() + "/" + id);
        ResponseEntity<ProductPriceList[]> newAllPriceListsResponse = restTemplate.getForEntity(applicationUrl(), ProductPriceList[].class);
        assertThat(newAllPriceListsResponse.getBody()).noneSatisfy(priceList -> assertThat(priceList.id()).isEqualTo(id));
    }

    private final List<ProductPriceListRequest> incorrectProductPriceListRequests = List.of(
            new ProductPriceListRequest("price list with discount equal base price", ONE, AMOUNT_BASED, Map.of(1, ONE)),
            new ProductPriceListRequest("price list with discount higher than base price", ONE, AMOUNT_BASED, Map.of(1, ONE.add(ONE))),
            new ProductPriceListRequest("price list with negative amount discount", ONE, AMOUNT_BASED, Map.of(1, BigDecimal.valueOf(-1))),
            new ProductPriceListRequest("price list with non positive base price", BigDecimal.valueOf(-1), AMOUNT_BASED, emptyMap()),
            new ProductPriceListRequest("price list with non positive base price", BigDecimal.valueOf(-1), PERCENTAGE_BASED, emptyMap()),
            new ProductPriceListRequest("price list with negative percentage discount", ONE, PERCENTAGE_BASED, Map.of(1, BigDecimal.valueOf(-1))),
            new ProductPriceListRequest("price list with percentage discount higher than 100", ONE, PERCENTAGE_BASED, Map.of(1, BigDecimal.valueOf(101)))
    );

    @Test
    public void validationAtSavingNewEntity() {
        incorrectProductPriceListRequests.forEach(incorrectRequest -> {
            ResponseEntity<String> addProductResponse = restTemplate.postForEntity(applicationUrl(), incorrectRequest, String.class);
            assertThat(addProductResponse.getStatusCode().is4xxClientError()).isTrue();
        });
    }

    @Test
    public void validationAtUpdatingNewEntity() {
        // given
        var addProductPriceListRequest = new ProductPriceListRequest("product", BigDecimal.valueOf(100), PERCENTAGE_BASED, emptyMap());
        String id = restTemplate.postForEntity(applicationUrl(), addProductPriceListRequest, String.class).getBody();

        incorrectProductPriceListRequests.forEach(incorrectRequest -> {
            // when
            ResponseEntity<Void> response = restTemplate.exchange(applicationUrl() + "/" + id, HttpMethod.PUT, new HttpEntity<>(incorrectRequest), Void.class);
            // then
            assertThat(response.getStatusCode().is4xxClientError()).isTrue();
        });
    }

    @Test
    public void gettingPriceForGivenProductAndItsQuantity() {
        // given
        var discounts = Map.of(10, BigDecimal.valueOf(5), 20, BigDecimal.valueOf(50));
        var addProductPriceListRequest = new ProductPriceListRequest("product", BigDecimal.valueOf(100), PERCENTAGE_BASED, discounts);
        String id = restTemplate.postForEntity(applicationUrl(), addProductPriceListRequest, String.class).getBody();

        int desiredQuantity = 15;
        BigDecimal expectedPrice = BigDecimal.valueOf(95).setScale(2, RoundingMode.HALF_UP);

        // when
        var url = "%s/%s?quantity=%d".formatted(applicationUrl(), id, desiredQuantity);
        ResponseEntity<BigDecimal> priceResponse = restTemplate.getForEntity(url, BigDecimal.class);

        // then
        assertThat(priceResponse.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(priceResponse.getBody()).isEqualTo(expectedPrice);
    }
}
