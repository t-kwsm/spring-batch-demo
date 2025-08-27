package com.example.batch.validator;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.validator.ValidationException;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class InputDataValidator<T> implements org.springframework.batch.item.validator.Validator<T> {
    
    private final Validator validator;
    
    @Override
    public void validate(T item) throws ValidationException {
        Set<ConstraintViolation<T>> violations = validator.validate(item);
        
        if (!violations.isEmpty()) {
            String errorMessage = violations.stream()
                    .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                    .collect(Collectors.joining(", "));
            
            log.error("Validation failed for item: {} - Errors: {}", item, errorMessage);
            throw new ValidationException("Validation failed: " + errorMessage);
        }
    }
}