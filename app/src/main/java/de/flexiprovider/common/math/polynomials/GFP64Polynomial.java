package de.flexiprovider.common.math.polynomials;

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


public class GFP64Polynomial {

    private long[] f;
    private long[] poly;

    private long p;



    public GFP64Polynomial(long[] f, long p, long[] poly) {
        this.f = f;
        this.p = p;
        this.poly = reduce(poly);
        SecureRandom generator = Registry.getSecureRandom();
    }


    public GFP64Polynomial add(GFP64Polynomial gfp) {
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

        return new GFP64Polynomial(f, p, reduce(result));
    }
    public void addToThis(GFP64Polynomial gfp) {
        poly = add(gfp).getPoly();
    }

    public boolean arrEqual(long[] arr1, long[] arr2) {
        if (arr1.length != arr2.length) {
            return false;
        }
        for (int i = arr1.length - 1; i >= 0; i--) {
            if (arr1[i] != arr2[i]) {
                return false;
            }
        }
        return true;
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

    public long[] getCompressed() {
        return compress(poly);
    }

    public boolean equals(Object obj) {
        if (obj.getClass().equals(getClass())) {
            GFP64Polynomial gfp = (GFP64Polynomial) obj;
            return p == gfp.getP() && arrEqual(f, gfp.getF())
                    && arrEqual(poly, gfp.getPoly());
        }
        return false;
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

    public GFP64Polynomial multiply(GFP64Polynomial gfp) {
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
                long l = a[i];
                long k = z[j];
                k = l * k % p;
                result[i + j] = ((int) k + result[i + j]) % p;
            }
        }

        return new GFP64Polynomial(f, p, reduce(result));
    }


    public Vector multiply(Vector k) {
        Vector result = new Vector();
        result.setSize(k.size());

        for (int i = k.size() - 1; i >= 0; i--) {
            GFP64Polynomial next = (GFP64Polynomial) k.elementAt(i);
            result.setElementAt(multiply(next), i);

        }

        return result;
    }

    public boolean paramEqual(GFP64Polynomial gfp) {
        return p == gfp.getP() && arrEqual(f, gfp.getF());
    }

    public void print() {
        System.out.println("printing GFP");
        System.out.println("p: " + getP());
        System.out.println("f: " + printPoly(getF()));
        System.out.println("poly: " + printPoly(mod(getPoly())));
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
        if (z.length < f.length) {
            return z;
        }
        int exp = z.length - f.length;
        long v = (-z[z.length - 1] + p) % p;
        int zSize = z.length - 1;
        long[] newZ = new long[zSize];
        for (int i = zSize; i > 0; i--) {
            if (i <= exp) {
                newZ[i - 1] = z[i - 1];
            } else {
                FlexiBigInt l = new FlexiBigInt("" + f[i - exp - 1]);
                l.multiply(new FlexiBigInt("" + v))
                        .mod(new FlexiBigInt("" + p));
                newZ[i - 1] = (l.longValue() + z[i - 1]) % p;
            }
        }
        return reduce(newZ);
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
        }
        long[] newZ = new long[zSize];
        System.arraycopy(z, 0, newZ, 0, zSize);
        return newZ;
    }

    public GFP64Polynomial subtract(GFP64Polynomial gfp) {
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

        return new GFP64Polynomial(f, p, reduce(result));
    }

    public void subtractFromThis(GFP64Polynomial gfp) {
        poly = subtract(gfp).getPoly();
    }
}
