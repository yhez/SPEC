package de.flexiprovider.common.math;

import de.flexiprovider.api.SecureRandom;
import de.flexiprovider.my.BigInteger;

public final class FlexiBigInt {

    private class JavaSecureRandom extends java.security.SecureRandom {
        JavaSecureRandom(SecureRandom flexiRand) {
            super(flexiRand, null);
        }
    }

    public BigInteger bigInt;

    public FlexiBigInt(String val) {
        bigInt = new BigInteger(val);
    }

    public FlexiBigInt(int signum, byte[] magnitude) {
        bigInt = new BigInteger(signum, magnitude);
    }

    public FlexiBigInt(String val, int radix) {
        bigInt = new BigInteger(val, radix);
    }

    public FlexiBigInt(byte[] randomData) {
        bigInt = new BigInteger(1,randomData);
    }

    public FlexiBigInt(BigInteger bigInt) {
        this.bigInt = bigInt;
    }

    public FlexiBigInt(java.math.BigInteger bigInt) {
        this.bigInt = new BigInteger(bigInt);
    }

    public static FlexiBigInt valueOf(long val) {
        return new FlexiBigInt(BigInteger.valueOf(val));
    }

    public static final FlexiBigInt ZERO = new FlexiBigInt(BigInteger.ZERO);

    public static final FlexiBigInt ONE = valueOf(1);

    public FlexiBigInt add(FlexiBigInt addend) {
        return new FlexiBigInt(bigInt.add(addend.bigInt));
    }

    public FlexiBigInt subtract(FlexiBigInt minuend) {
        return new FlexiBigInt(bigInt.subtract(minuend.bigInt));
    }

    public FlexiBigInt multiply(FlexiBigInt factor) {
        return new FlexiBigInt(bigInt.multiply(factor.bigInt));
    }

    public FlexiBigInt divide(FlexiBigInt divisor) {
        return new FlexiBigInt(bigInt.divide(divisor.bigInt));
    }

    public FlexiBigInt remainder(FlexiBigInt divisor) {
        return new FlexiBigInt(bigInt.remainder(divisor.bigInt));
    }

    public FlexiBigInt gcd(FlexiBigInt val) {
        return new FlexiBigInt(bigInt.gcd(val.bigInt));
    }

    public FlexiBigInt abs() {
        return new FlexiBigInt(bigInt.abs());
    }

    public FlexiBigInt negate() {
        return new FlexiBigInt(bigInt.negate());
    }

    public int signum() {
        return bigInt.signum();
    }

    public FlexiBigInt mod(FlexiBigInt modulus) {
        return new FlexiBigInt(bigInt.mod(modulus.bigInt));
    }

    public FlexiBigInt modPow(FlexiBigInt exponent, FlexiBigInt modulus) {
        return new FlexiBigInt(bigInt.modPow(exponent.bigInt, modulus.bigInt));
    }

    public FlexiBigInt modInverse(FlexiBigInt modulus) {
        return new FlexiBigInt(bigInt.modInverse(modulus.bigInt));
    }

    public FlexiBigInt shiftLeft(int n) {
        return new FlexiBigInt(bigInt.shiftLeft(n));
    }

    public FlexiBigInt shiftRight(int n) {
        return new FlexiBigInt(bigInt.shiftRight(n));
    }

    public boolean testBit(int n) {
        return bigInt.testBit(n);
    }

    public int bitLength() {
        return bigInt.bitLength();
    }

    public boolean isProbablePrime(int certainty) {
        return bigInt.isProbablePrime(certainty);
    }

    public int compareTo(FlexiBigInt other) {
        return bigInt.compareTo(other.bigInt);
    }

    public boolean equals(Object other) {
        return other instanceof FlexiBigInt && bigInt.equals(((FlexiBigInt) other).bigInt);
    }

    public int hashCode() {
        return bigInt.hashCode();
    }

    public String toString(int radix) {
        return bigInt.toString(radix);
    }

    public String toString() {
        return bigInt.toString();
    }

    public byte[] toByteArray() {
        return bigInt.toByteArray();
    }

    public int intValue() {
        return bigInt.intValue();
    }

}
