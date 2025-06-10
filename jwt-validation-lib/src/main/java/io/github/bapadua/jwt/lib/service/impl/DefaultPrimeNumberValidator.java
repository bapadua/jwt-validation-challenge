package io.github.bapadua.jwt.lib.service.impl;

import org.springframework.stereotype.Component;

import io.github.bapadua.jwt.lib.service.PrimeNumberValidator;

/**
 * Implementação padrão do validador de números primos
 * Aplica o princípio Single Responsibility
 */
@Component
public class DefaultPrimeNumberValidator implements PrimeNumberValidator {
    
    @Override
    public boolean isPrime(long number) {
        if (number <= 1) {
            return false;
        }
        if (number <= 3) {
            return true;
        }
        if (number % 2 == 0 || number % 3 == 0) {
            return false;
        }
        
        for (long i = 5; i * i <= number; i += 6) {
            if (number % i == 0 || number % (i + 2) == 0) {
                return false;
            }
        }
        
        return true;
    }
} 