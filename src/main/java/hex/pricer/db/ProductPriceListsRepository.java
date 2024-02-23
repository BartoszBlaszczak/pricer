package hex.pricer.db;

import hex.pricer.domain.ProductPriceList;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ProductPriceListsRepository extends MongoRepository<ProductPriceList, String> {
    default Optional<ProductPriceList> updateIfExists(ProductPriceList priceList) {
        return findById(priceList.id()).map(entity -> save(priceList));
    }
}