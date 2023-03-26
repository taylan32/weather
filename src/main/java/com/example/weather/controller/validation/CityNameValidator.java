package com.example.weather.controller.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;

@Component
public class CityNameValidator implements ConstraintValidator<CityNameConstraint, String> {


    @Override
    public void initialize(CityNameConstraint constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // özel karakterleri çıkart. Ankara-Istanbul --> AnkaraIstanbul
        value = value.replaceAll("[^a-zA-Z0-9]","");

        // "-  -  -" regexden sonra --> "   "
        return !StringUtils.isNumeric(value) && !StringUtils.isAllBlank(value);

    }
}
