package com.dilly.global.util.validator;

import com.dilly.global.util.TextUtil;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CustomSizeValidator implements ConstraintValidator<CustomSize, CharSequence> {

    private int min;
    private int max;

    @Override
    public void initialize(CustomSize constraintAnnotation) {
        this.min = constraintAnnotation.min();
        this.max = constraintAnnotation.max();
    }

    @Override
    public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        int length = TextUtil.countGraphemeClusters(value.toString());

        return length >= min && length <= max;
    }

}
