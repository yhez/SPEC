package de.flexiprovider.core.random;

import de.flexiprovider.api.SecureRandom;
import de.flexiprovider.common.math.FlexiBigInt;
import de.flexiprovider.common.util.SeedGenerator;


public final class BBSRandom extends SecureRandom {

    private static final int SECURITY_PARAMETER = 1024;

    private static final FlexiBigInt CONST_2 = FlexiBigInt.valueOf(2);
    private static final FlexiBigInt CONST_3 = FlexiBigInt.valueOf(3);
    private static final FlexiBigInt CONST_4 = FlexiBigInt.valueOf(4);
    private static final FlexiBigInt CONST_5 = FlexiBigInt.valueOf(5);
    private static final FlexiBigInt CONST_7 = FlexiBigInt.valueOf(7);
    private static final FlexiBigInt CONST_11 = FlexiBigInt.valueOf(11);
    private static final FlexiBigInt CONST_13 = FlexiBigInt.valueOf(13);
    private static final FlexiBigInt CONST_17 = FlexiBigInt.valueOf(17);
    private static final FlexiBigInt CONST_19 = FlexiBigInt.valueOf(19);
    private static final FlexiBigInt CONST_23 = FlexiBigInt.valueOf(23);

    private final FlexiBigInt LCG_MODULUS = new FlexiBigInt(
            "805464911080722516417453601008791849136329615762701082177197");

    private final FlexiBigInt LCG_A = new FlexiBigInt(
            "7129718823414295026033436596773772714578900265841170538826");

    private final FlexiBigInt LCG_B = new FlexiBigInt(
            "675276940120214627441986171104851609581303133674533034328917");

    private int bitsPerRound;

    private FlexiBigInt n;

    private FlexiBigInt x = null;

    private FlexiBigInt seed = null;

    private boolean isSeeded;

    private boolean parametersGenerated;


    public BBSRandom() {
        // the object is not seeded
        isSeeded = false;
        // the parameters p, q, x have not been generated yet
        parametersGenerated = false;

        // generate the object that will do seeding
        new SeedGenerator();

        // calculate the number of bits to be generated per iteration
        bitsPerRound = 0;
        for (int i = 0; i < 32; i++) {
            if ((SECURITY_PARAMETER & (1 << i)) != 0) {
                bitsPerRound = i;
            }
        }
    }

    public byte[] generateSeed(int numBytes) {
        return this.generateSeed(numBytes);
        //return seedGenerator.generateSeed(numBytes);
    }


    public void nextBytes(byte[] bytes) {

        if (bytes == null || bytes.length == 0) {
            return;
        }

        // number of bits to be generated
        int numBits = bytes.length << 3;
        // bitmask of next bit to be set
        int aktResultBit = 1;
        // generated byte
        int aktResultByte = 0;
        // bit to be copied
        int aktSourceBit = 0;
        // number of bits processed
        int aktResult = 0;

        // ensure that a Seed has been generated
        if (!isSeeded) {
            selfSeed();
        }

        // ensure that all parameters are generated
        if (!parametersGenerated) {
            generateParameters();
        }

        // clear first byte
        bytes[0] = 0;

        do {
            // if source-bit is set
            if (x.testBit(aktSourceBit++)) {
                // set corresponding bit in result-vector
                bytes[aktResultByte] += aktResultBit;
            }
            // shift left bitmask by one
            aktResultBit = aktResultBit << 1;

            // if bitsPerRound is reached
            if (aktSourceBit == bitsPerRound) {
                // reset counter,
                aktSourceBit = 0;
                // and generate new seed (sqare-mod-generator)
                x = (x.multiply(x)).mod(n);
            }

            // if a whole byte has been generated
            if (aktResultBit == 256) {
                // reset bitmask
                aktResultBit = 1;
                // and switch to next byte
                aktResultByte++;
                // if one more byte is left to do
                if (aktResultByte < bytes.length) {
                    // clear it.
                    bytes[aktResultByte] = 0;
                }
            }

            // next bit
            aktResult++;
        } // while still some bits are to be generated
        while (aktResult < numBits);
    }


    public void setSeed(byte[] newSeed) {

        if (isSeeded) { // if the object is already seeded
            // modify the seed by xor-ing the parameter to the seed,
            seed = (seed.xor(new FlexiBigInt(1, newSeed))).mod(LCG_MODULUS);
        } else { // otherwise
            // set it explicitly
            seed = (new FlexiBigInt(1, newSeed)).mod(LCG_MODULUS);
            isSeeded = true;
        }
        parametersGenerated = false;
    }

    private void generateParameters() {
        byte[] buf;
        // bitlength of p
        int pBL;
        // bitlength of q
        int qBL;
        int add;

        // determine bit lengths of p and q
        pBL = SECURITY_PARAMETER >> 1;
        qBL = 1 + pBL;

        // generate prime p

        // create p-2 bits via lcg
        buf = lcg(pBL - 2);
        // create a FlexiBigInt p
        FlexiBigInt p = new FlexiBigInt(1, buf);
        // shift left: the LSB is set explicitly
        p = p.shiftLeft(1);
        // ensure that it is a p-bit Number
        p = p.setBit(pBL - 1);
        // and that it is odd
        p = p.setBit(0);

        // ensure that it is congruent 3 mod 4
        if (((p.mod(CONST_4)).compareTo(CONST_3)) != 0) {
            p = p.add(CONST_2);
        }

        // test for small factors
        int zmod3 = p.mod(CONST_3).intValue();
        int zmod5 = p.mod(CONST_5).intValue();
        int zmod7 = p.mod(CONST_7).intValue();
        int zmod11 = p.mod(CONST_11).intValue();
        int zmod13 = p.mod(CONST_13).intValue();
        int zmod17 = p.mod(CONST_17).intValue();
        int zmod19 = p.mod(CONST_19).intValue();
        int zmod23 = p.mod(CONST_23).intValue();

        // repeat until p is prime
        int CERTAINTY = 10;
        while (!p.isProbablePrime(CERTAINTY)) {
            add = 0;
            // add 4 while small factors exist:
            do {
                // this is cheaper than modifying the FlexiBigInt directly (if
                // the loop is passed often)
                add += 4;
                // this is cheaper than using the probabilistic primality test
                // in de.flexiprovider.common.math.FlexiBigInt
                zmod3 = (zmod3 + 4) % 3;
                zmod5 = (zmod5 + 4) % 5;
                zmod7 = (zmod7 + 4) % 7;
                zmod11 = (zmod11 + 4) % 11;
                zmod13 = (zmod13 + 4) % 13;
                zmod17 = (zmod17 + 4) % 17;
                zmod19 = (zmod19 + 4) % 19;
                zmod23 = (zmod23 + 4) % 23;
            } while ((zmod3 == 0) || (zmod5 == 0) || (zmod7 == 0)
                    || (zmod11 == 0) || (zmod13 == 0) || (zmod17 == 0)
                    || (zmod19 == 0) || (zmod23 == 0));
            // change FlexiBigInt accordingly
            p = p.add(FlexiBigInt.valueOf(add));
        }

        // generate prime q

        FlexiBigInt q;
        do {
            // create q-2 bits via lcg
            buf = lcg(qBL - 2);
            // and create a FlexiBigInt q
            q = new FlexiBigInt(1, buf);
            // shift left: bit 0 is set explicitly
            q = q.shiftLeft(1);
            // make it odd
            q = q.setBit(0);
            // ensure that it has q bits
            q = q.setBit(qBL - 1);
            if ((q.mod(CONST_4)).compareTo(CONST_3) != 0) {
                q = q.add(CONST_2);
            }
            zmod3 = q.mod(CONST_3).intValue();
            zmod5 = q.mod(CONST_5).intValue();
            zmod7 = q.mod(CONST_7).intValue();
            zmod11 = q.mod(CONST_11).intValue();
            zmod13 = q.mod(CONST_13).intValue();
            zmod17 = q.mod(CONST_17).intValue();
            zmod19 = q.mod(CONST_19).intValue();
            zmod23 = q.mod(CONST_23).intValue();
            while (!q.isProbablePrime(CERTAINTY)) {
                add = 0;
                do {
                    add += 4;
                    zmod3 = (zmod3 + 4) % 3;
                    zmod5 = (zmod5 + 4) % 5;
                    zmod7 = (zmod7 + 4) % 7;
                    zmod11 = (zmod11 + 4) % 11;
                    zmod13 = (zmod13 + 4) % 13;
                    zmod17 = (zmod17 + 4) % 17;
                    zmod19 = (zmod19 + 4) % 19;
                    zmod23 = (zmod23 + 4) % 23;
                } while (zmod3 == 0 || zmod5 == 0 || zmod7 == 0 || zmod11 == 0
                        || zmod13 == 0 || zmod17 == 0 || zmod19 == 0
                        || zmod23 == 0);
                q = q.add(FlexiBigInt.valueOf(add));
            }
            n = p.multiply(q);

        }
        while (q.compareTo(p) == 0);
        do {
            buf = lcg(n.bitLength());
            x = (new FlexiBigInt(1, buf)).mod(n);
        } while ((x.compareTo(FlexiBigInt.ZERO) == 0) || (x.compareTo(p) == 0)
                || (x.compareTo(q) == 0));

        parametersGenerated = true;
    }


    private byte[] lcg(int bitLength) {
        int i, j;
        byte[] result = new byte[(bitLength + 7) >> 3];
        int leftBits = bitLength & 7;
        for (i = result.length - 1; i >= (result.length - (bitLength >> 3)); i--) {
            result[i] = 0;
            for (j = 1; j < 256; j <<= 1) {
                if (seed.testBit(0)) {
                    result[i] |= j;
                }
                seed = (LCG_A.add(LCG_B.multiply(seed))).mod(LCG_MODULUS);
            }
        }
        if (i == 0) {
            for (j = 0; j < leftBits; j++) {
                if (seed.testBit(0)) {
                    result[i] |= 1 << j;
                } else {
                    result[i] &= 255 - (1 << j);
                }
                seed = (LCG_A.add(LCG_B.multiply(seed))).mod(LCG_MODULUS);
            }
            for (j = leftBits; j < 8; j++) {
                result[i] &= 255 - (1 << j);
            }
        }
        return result;
    }

    private void selfSeed() {
        int LCG_BYTE_SIZE = 25;
        seed = new FlexiBigInt(1, this.generateSeed(LCG_BYTE_SIZE));
        isSeeded = true;
    }

}