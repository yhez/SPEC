package de.flexiprovider.my;



class BitSieve {

    private long bits[];

    private int length;

    private static BitSieve smallSieve = new BitSieve();

    private BitSieve() {
        length = 150 * 64;
        bits = new long[(unitIndex(length - 1) + 1)];

        // Mark 1 as composite
        set(0);
        int nextIndex = 1;
        int nextPrime = 3;

        // Find primes and remove their multiples from sieve
        do {
            sieveSingle(length, nextIndex + nextPrime, nextPrime);
            nextIndex = sieveSearch(length, nextIndex + 1);
            nextPrime = 2 * nextIndex + 1;
        } while ((nextIndex > 0) && (nextPrime < length));
    }

    BitSieve(BigInteger base, int searchLen) {

        bits = new long[(unitIndex(searchLen - 1) + 1)];
        length = searchLen;
        int start = 0;

        int step = smallSieve.sieveSearch(smallSieve.length, start);
        int convertedStep = (step * 2) + 1;

        // Construct the large sieve at an even offset specified by base
        MutableBigInteger b = new MutableBigInteger(base);
        MutableBigInteger q = new MutableBigInteger();
        do {
            // Calculate base mod convertedStep
            start = b.divideOneWord(convertedStep, q);

            // Take each multiple of step out of sieve
            start = convertedStep - start;
            if (start % 2 == 0)
                start += convertedStep;
            sieveSingle(searchLen, (start - 1) / 2, convertedStep);

            // Find next prime from small sieve
            step = smallSieve.sieveSearch(smallSieve.length, step + 1);
            convertedStep = (step * 2) + 1;
        } while (step > 0);
    }

    /**
     * Given a bit index return unit index containing it.
     */
    private static int unitIndex(int bitIndex) {
        return bitIndex >>> 6;
    }

    /**
     * Return a unit that masks the specified bit in its unit.
     */
    private static long bit(int bitIndex) {
        return 1L << (bitIndex & ((1 << 6) - 1));
    }

    /**
     * Get the value of the bit at the specified index.
     */
    private boolean get(int bitIndex) {
        int unitIndex = unitIndex(bitIndex);
        return ((bits[unitIndex] & bit(bitIndex)) != 0);
    }

    /**
     * Set the bit at the specified index.
     */
    private void set(int bitIndex) {
        int unitIndex = unitIndex(bitIndex);
        bits[unitIndex] |= bit(bitIndex);
    }

    private int sieveSearch(int limit, int start) {
        if (start >= limit)
            return -1;

        int index = start;
        do {
            if (!get(index))
                return index;
            index++;
        } while (index < limit - 1);
        return -1;
    }


    private void sieveSingle(int limit, int start, int step) {
        while (start < limit) {
            set(start);
            start += step;
        }
    }


    BigInteger retrieve(BigInteger initValue, int certainty, java.util.Random random) {
        // Examine the sieve one long at a time to find possible primes
        int offset = 1;
        for (int i = 0; i < bits.length; i++) {
            long nextLong = ~bits[i];
            for (int j = 0; j < 64; j++) {
                if ((nextLong & 1) == 1) {
                    BigInteger candidate = initValue.add(
                            BigInteger.valueOf(offset));
                    if (candidate.primeToCertainty(certainty, random))
                        return candidate;
                }
                nextLong >>>= 1;
                offset += 2;
            }
        }
        return null;
    }
}
