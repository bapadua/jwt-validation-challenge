package io.github.bapadua.jwt.lib.service;

/**
 * Interface responsável por validar números primos
 * Aplica o princípio Single Responsibility
 */
public interface PrimeNumberValidator {
    
    /**
     * Verifica se um número é primo
     * 
     * @param number número a ser verificado
     * @return true se o número é primo, false caso contrário
     */
    boolean isPrime(long number);
} 