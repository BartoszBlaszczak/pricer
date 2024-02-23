package hex.pricer.web;

import hex.pricer.domain.ProductPriceList;
import hex.pricer.domain.ProductPriceListRequest;
import hex.pricer.db.ProductPriceListsRepository;
import hex.pricer.domain.RegistrationFormValidator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/prices")
public class PriceController {
    private final ProductPriceListsRepository repository;
    private final RegistrationFormValidator validator;

    public PriceController(ProductPriceListsRepository repository, RegistrationFormValidator validator) {
        this.repository = repository;
        this.validator = validator;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ProductPriceList> getAllPriceLists() {
        return repository.findAll();
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<?> getPrice(@PathVariable String id, @RequestParam Integer quantity) {
        return repository.findById(id)
                .map(priceList -> priceList.getPriceFor(quantity))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> add(@RequestBody ProductPriceListRequest request, BindingResult bindingResult) {
        validator.validate(request, bindingResult);

        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }

        ProductPriceList saved = repository.save(request.toNewProduct());
        return ResponseEntity.ok(saved.id());
    }

    @PutMapping(value = "/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @RequestBody ProductPriceListRequest request, BindingResult bindingResult) {
        validator.validate(request, bindingResult);

        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }

        Optional<ProductPriceList> result = repository.updateIfExists(request.toProduct(id));
        return result
                .map(document -> ResponseEntity.ok().build())
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void delete(@PathVariable String id) {
        repository.deleteById(id);
    }

}
