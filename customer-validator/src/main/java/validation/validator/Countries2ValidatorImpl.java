package validation.validator;

import org.springframework.stereotype.Component;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraintvalidation.SupportedValidationTarget;
import javax.validation.constraintvalidation.ValidationTarget;


@Component
@SupportedValidationTarget(ValidationTarget.PARAMETERS)
public class Countries2ValidatorImpl implements ConstraintValidator<Countries2Validator, Object[]> {

    public void initialize(Countries2Validator countries2Validator) {
    }

    public boolean isValid(Object[] values, ConstraintValidatorContext constraintValidatorContext) {
        String countryCode = (String) values[0];
        String anotherValue = (String) values[1];

        return countryCode.equals(anotherValue);
    }

}

