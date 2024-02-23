package hex.pricer.domain;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.math.BigDecimal;

import static hex.pricer.domain.DiscountType.AMOUNT_BASED;
import static hex.pricer.domain.DiscountType.PERCENTAGE_BASED;

@Component
public class RegistrationFormValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return ProductPriceListRequest.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ProductPriceListRequest form = (ProductPriceListRequest) target;

        if (form.basePrice().signum() < 0) {
            errors.rejectValue("basePrice", "request.basePrice.negative");
        }

        if (form.discountType().equals(AMOUNT_BASED) && form.discounts().values().stream().anyMatch(discount -> discount.compareTo(form.basePrice()) >= 0 )) {
            errors.rejectValue("discounts", "request.discounts.amountBased.greaterThanBasePrice");
        }

        if (form.discountType().equals(PERCENTAGE_BASED) && form.discounts().values().stream().anyMatch(discount -> discount.compareTo(BigDecimal.valueOf(100)) >= 0 )) {
            errors.rejectValue("discounts", "request.discounts.percentageBased.greaterThan100");
        }

        if (form.discounts().keySet().stream().anyMatch(quantity -> quantity <= 0)) {
            errors.rejectValue("discounts", "request.discounts.quantity.lesserThan0");
        }

        if (form.discounts().values().stream().anyMatch(discount -> discount.signum() < 0)) {
            errors.rejectValue("discounts", "request.discounts.value.lesserThan0");
        }
    }
}

