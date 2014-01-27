package de.flexiprovider.pqc.tss;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Vector;

import codec.asn1.ASN1Exception;
import codec.asn1.ASN1Integer;
import codec.asn1.ASN1Sequence;
import codec.asn1.ASN1SequenceOf;
import codec.asn1.DEREncoder;
import de.flexiprovider.api.Registry;
import de.flexiprovider.api.SecureRandom;
import de.flexiprovider.common.math.FlexiBigInt;
import de.flexiprovider.common.util.ASN1Tools;


public class TSSPolynomial {

    private long[] f;
    private long[] poly;
    private int degree;

    private long p;

    private SecureRandom generator;

    public TSSPolynomial(long[] f, long p, long[] poly) {
        this.f = f;
        degree = f.length - 1;
        this.p = p;
        this.poly = reduce(poly);
        generator = Registry.getSecureRandom();
    }

    public TSSPolynomial(long[] f, long p) {
        this.f = f;
        degree = f.length - 1;
        this.p = p;
        generator = Registry.getSecureRandom();
    }

    public TSSPolynomial add(TSSPolynomial gfp) {
        if (!paramEqual(gfp)) {
            return null;
        }
        long[] b = gfp.getPoly();
        long[] a = poly;
        if (a.length < b.length) {
            a = b;
            b = poly;
        }

        long[] result = new long[a.length];

        for (int i = a.length - 1; i >= 0; i--) {
            result[i] = a[i];
            if (i < b.length) {
                result[i] = (result[i] + b[i]) % p;
            }
        }

        return new TSSPolynomial(f, p, reduce(result));
    }

    public void addToThis(TSSPolynomial gfp) {
        poly = add(gfp).getPoly();
    }

    public boolean arrEqual(long[] arr1, long[] arr2) {
        if (arr1.length != arr2.length) {
            return false;
        } else {
            for (int i = arr1.length - 1; i >= 0; i--) {
                if (arr1[i] != arr2[i]) {
                    return false;
                }
            }
            return true;
        }
    }

    private long[] compress(long[] a) {
        return compress(a, p);
    }

    private long[] compress(long[] a, long p) {
        for (int i = a.length; i > 0; i--) {
            if (a[i - 1] > p / 2) {
                a[i - 1] -= p;
            }
        }
        return a;
    }

    public boolean equals(Object obj) {
        if (obj.getClass().equals(getClass())) {
            TSSPolynomial gfp = (TSSPolynomial) obj;
            return p == gfp.getP() && arrEqual(f, gfp.getF())
                    && arrEqual(mod(getPoly()), mod(gfp.getPoly()));
        }
        return false;
    }

    public TSSPolynomial generatePoly() {
        return generatePoly(0);
    }

    public TSSPolynomial generatePoly(long limit) {
        return generatePoly(limit, false);
    }

    public TSSPolynomial generatePoly(long limit, boolean negative) {
        if (limit == 0 || limit > p) {
            limit = p;
        }
        long[] resPoly = new long[degree];
        for (int i = degree; i > 0; i--) {
            if (negative) {
                resPoly[i - 1] = nextLong(limit * 2 - 1);
            } else {
                resPoly[i - 1] = nextLong(limit);
            }
        }
        if (negative) {
            return new TSSPolynomial(f, p, compress(resPoly, limit * 2 - 1));
        } else {
            return new TSSPolynomial(f, p, reduceZeros(resPoly));
        }
    }

    public long[] getCompressed() {
        return compress(poly);
    }

    public byte[] getEncoded() throws ASN1Exception, IOException {
        // TODO use ASN.1
        ASN1Sequence gfpSequence = new ASN1Sequence(3);
        ASN1Integer asn1P = ASN1Tools.createInteger(new FlexiBigInt("" + p));
        ASN1SequenceOf asn1Poly = new ASN1SequenceOf(ASN1Integer.class);
        ASN1SequenceOf asn1F = new ASN1SequenceOf(ASN1Integer.class);
        for (int i = 0; i < poly.length; i++) {
            asn1Poly
                    .add(ASN1Tools.createInteger(new FlexiBigInt("" + poly[i])));
        }
        for (int i = 0; i < f.length; i++) {
            asn1F.add(ASN1Tools.createInteger(new FlexiBigInt("" + f[i])));
        }
        gfpSequence.add(asn1F);
        gfpSequence.add(asn1P);
        gfpSequence.add(asn1Poly);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        gfpSequence.encode(new DEREncoder(baos));
        byte[] res = baos.toByteArray();
        baos.flush();
        baos.close();

        return res;
    }

    public long[] getF() {
        return f;
    }

    public long getP() {
        return p;
    }

    public long[] getPoly() {
        return poly;
    }

    private long[] mod(long[] poly) {
        for (int i = poly.length; i > 0; i--) {
            if (poly[i - 1] < 0) {
                poly[i - 1] = poly[i - 1] % p + p;
            } else {
                poly[i - 1] = poly[i - 1] % p;
            }
        }
        return poly;
    }

    public TSSPolynomial multiply(TSSPolynomial gfp) {
        if (!paramEqual(gfp)) {
            return null;
        }
        long[] a = poly;
        long[] z = gfp.getPoly();
        int degree = a.length + z.length - 1;
        long[] result = new long[degree];
        // Arrays.fill(result, 0);

        a = compress(a);
        z = compress(z);

        for (int i = a.length - 1; i >= 0; i--) {
            for (int j = z.length - 1; j >= 0; j--) {
                result[i + j] = (result[i + j] + (a[i] * z[j])) % p;
            }
        }
        result = mod(result);
        return new TSSPolynomial(f, p, reduce(result));
    }

    public Vector multiply(Vector k) {
        Vector result = new Vector();
        result.setSize(k.size());

        for (int i = k.size() - 1; i >= 0; i--) {
            TSSPolynomial next = (TSSPolynomial) k.elementAt(i);
            result.setElementAt(multiply(next), i);

        }

        return result;
    }

    private long nextLong(long limit) {
        if (limit > Integer.MAX_VALUE) {
            return (generator.nextInt() * generator.nextInt()) % limit;
        } else {
            return generator.nextInt((int) limit);
        }
    }

    public boolean paramEqual(TSSPolynomial gfp) {
        return p == gfp.getP() && arrEqual(f, gfp.getF());
    }

    public void print() {
        System.out.println("printing GFP");
        System.out.println("p: " + getP());
        System.out.println("f: " + printPoly(getF()));
        System.out.println("poly: " + printPoly(getPoly()));
    }

    private String printPoly(long[] arr) {
        String result = "{";
        for (int i = arr.length - 1; i > 0; i--) {
            result += arr[i] + ", ";
        }
        result += arr[0] + "}";
        return result;
    }

    private long[] reduce(long[] z) {
        z = reduceZeros(mod(z));
        int gradZ = z.length;
        int gradN = f.length;
        if (gradZ < gradN) {
            return z;
        } else {
            z = compress(z);
            for (int i = gradZ - gradN; i >= 0; i--) {
                long quotient = z[i + gradN - 1];
                z[i] = (z[i] - quotient) % p;
                z[i + gradN - 1] = (z[i + gradN - 1] - quotient) % p;
            }
            long[] remainder = new long[gradN - 1];
            System.arraycopy(z, 0, remainder, 0, remainder.length);
            return reduceZeros(mod(remainder));
        }
    }

    private long[] reduceZeros(long[] z) {
        int zSize = z.length;
        for (int i = z.length; i > 0; i--) {
            if (z[i - 1] == 0) {
                zSize--;
            } else {
                break;
            }
        }

        if (zSize == z.length) {
            return z;
        } else {
            long[] newZ = new long[zSize];
            System.arraycopy(z, 0, newZ, 0, zSize);
            return newZ;
        }
    }

    public TSSPolynomial subtract(TSSPolynomial gfp) {
        if (!paramEqual(gfp)) {
            return null;
        }
        long[] b = gfp.getPoly();
        long[] a = poly;

        long[] result = new long[Math.max(a.length, b.length)];

        for (int i = result.length - 1; i >= 0; i--) {
            if (i < b.length && i < a.length) {
                result[i] = (a[i] - b[i]) % p;
            } else if (i < b.length) {
                result[i] = -b[i];
            } else {
                result[i] = a[i];
            }
        }

        return new TSSPolynomial(f, p, reduce(result));
    }

    public void subtractFromThis(TSSPolynomial gfp) {
        poly = subtract(gfp).getPoly();
    }
}
