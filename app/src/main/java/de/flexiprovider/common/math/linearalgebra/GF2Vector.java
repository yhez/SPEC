package de.flexiprovider.common.math.linearalgebra;

import de.flexiprovider.common.math.codingtheory.GF2mField;
import de.flexiprovider.common.util.IntUtils;
import de.flexiprovider.common.util.LittleEndianConversions;

public class GF2Vector extends Vector {

    /**
     * holds the elements of this vector
     */
    private int[] v;

    /**
     * Construct the zero vector of the given length.
     *
     * @param length the length of the vector
     */
    public GF2Vector(int length) {
        if (length < 0) {
            throw new ArithmeticException("Negative length.");
        }
        this.length = length;
        v = new int[(length + 31) >> 5];
    }

    public GF2Vector(int length, int[] v) {
        if (length < 0) {
            throw new ArithmeticException("negative length");
        }
        this.length = length;

        int size = (length + 31) >> 5;

        if (v.length != size) {
            throw new ArithmeticException("length mismatch");
        }

        this.v = IntUtils.clone(v);

        int r = length & 0x1f;
        if (r != 0) {
            // erase unused bits
            this.v[size - 1] &= (1 << r) - 1;
        }
    }

    public byte[] getEncoded() {
        int byteLen = (length + 7) >> 3;
        return LittleEndianConversions.toByteArray(v, byteLen);
    }

    /**
     * @return whether this is the zero vector (i.e., all elements are zero)
     */
    public boolean isZero() {
        for (int i = v.length - 1; i >= 0; i--) {
            if (v[i] != 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Set the coefficient at the given index to 1. If the index is out of
     * bounds, do nothing.
     *
     * @param index the index of the coefficient to set
     */
    public void setBit(int index) {
        if (index >= length) {
            throw new IndexOutOfBoundsException();
        }
        v[index >> 5] |= 1 << (index & 0x1f);
    }
    public Vector add(Vector other) {
        if (!(other instanceof GF2Vector)) {
            throw new ArithmeticException("vector is not defined over GF(2)");
        }

        GF2Vector otherVec = (GF2Vector) other;
        if (length != otherVec.length) {
            throw new ArithmeticException("length mismatch");
        }

        int[] vec = IntUtils.clone(((GF2Vector) other).v);

        for (int i = vec.length - 1; i >= 0; i--) {
            vec[i] ^= v[i];
        }

        return new GF2Vector(length, vec);
    }

    /**
     * Multiply this vector with a permutation.
     *
     * @param p the permutation
     * @return <tt>this*p = p*this</tt>
     */
    public Vector multiply(Permutation p) {
        int[] pVec = p.getVector();
        if (length != pVec.length) {
            throw new ArithmeticException("length mismatch");
        }

        GF2Vector result = new GF2Vector(length);

        for (int i = 0; i < pVec.length; i++) {
            int e = v[pVec[i] >> 5] & (1 << (pVec[i] & 0x1f));
            if (e != 0) {
                result.v[i >> 5] |= 1 << (i & 0x1f);
            }
        }

        return result;
    }

    /**
     * Rewrite this vector as a vector over <tt>GF(2<sup>m</sup>)</tt> with
     * <tt>t</tt> elements.
     *
     * @param field the finite field <tt>GF(2<sup>m</sup>)</tt>
     * @return the converted vector over <tt>GF(2<sup>m</sup>)</tt>
     */
    public GF2mVector toExtensionFieldVector(GF2mField field) {
        int m = field.getDegree();
        if ((length % m) != 0) {
            throw new ArithmeticException("conversion is impossible");
        }

        int t = length / m;
        int[] result = new int[t];
        int count = 0;
        for (int i = t - 1; i >= 0; i--) {
            for (int j = field.getDegree() - 1; j >= 0; j--) {
                int q = count >>> 5;
                int r = count & 0x1f;

                int e = (v[q] >>> r) & 1;
                if (e == 1) {
                    result[i] ^= 1 << j;
                }
                count++;
            }
        }
        return new GF2mVector(field, result);
    }

    public boolean equals(Object other) {

        if (!(other instanceof GF2Vector)) {
            return false;
        }
        GF2Vector otherVec = (GF2Vector) other;

        return (length == otherVec.length) && IntUtils.equals(v, otherVec.v);
    }
    public int hashCode() {
        int hash = length;
        hash = hash * 31 + v.hashCode();
        return hash;
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < length; i++) {
            if ((i != 0) && ((i & 0x1f) == 0)) {
                buf.append(' ');
            }
            int q = i >> 5;
            int r = i & 0x1f;
            int bit = v[q] & (1 << r);
            if (bit == 0) {
                buf.append('0');
            } else {
                buf.append('1');
            }
        }
        return buf.toString();
    }

}
