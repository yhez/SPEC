package codec.asn1;

import java.io.IOException;

public class ASN1BitString extends ASN1AbstractType {

    private static final byte[] DEFAULT_VALUE = new byte[0];

    private static final byte[] MASK = {(byte) 0x80, (byte) 0x40, (byte) 0x20,
            (byte) 0x10, (byte) 0x08, (byte) 0x04, (byte) 0x02, (byte) 0x01};

    private static final byte[] TRAIL_MASK = {(byte) 0xff, (byte) 0xfe,
            (byte) 0xfc, (byte) 0xf8, (byte) 0xf0, (byte) 0xe0, (byte) 0xc0,
            (byte) 0x80};

    private int pad_ = 0;

    private byte[] value_ = DEFAULT_VALUE;


    private boolean namedBits_ = false;

    public ASN1BitString() {
    }

    public ASN1BitString(byte[] b, int pad) {
        setBits0(b, pad);
    }

    public Object getValue() {
        return getBits();
    }

    public boolean[] getBits() {
        int n, i;
        boolean[] b;

        if (value_.length == 0) {
            return new boolean[0];
        }
        b = new boolean[(value_.length * 8) - pad_];

        for (n = 0, i = 0; i < b.length; i++) {
            b[i] = (value_[n] & MASK[i & 0x07]) != 0;
            if ((i & 0x07) == 0x07) {
                n++;
            }
        }
        return b;
    }

    public void setBits(byte[] b, int pad) throws ConstraintException {
        setBits0(b, pad);
        checkConstraints();
    }

    protected void setBits0(byte[] b, int p) {
        int n;

        if ((p < 0) || (p > 7)) {
            throw new IllegalArgumentException("Illegal pad value (" + p + ")");
        }
        namedBits_ = false;

        for (n = b.length - 1; n >= 0; n--) {
            if (b[n] != 0) {
                break;
            }
        }
        if (n < 0) {
            if (p != 0) {
                throw new IllegalArgumentException(
                        "Zero length bit strings can't have pad bits!");
            }
            value_ = DEFAULT_VALUE;
            pad_ = 0;

            return;
        }
        if ((b[b.length - 1] & ~TRAIL_MASK[p]) != 0) {
            throw new IllegalArgumentException(
                    "trailing pad bits are not zero!");
        }
        value_ = b;
        pad_ = p;
    }

    public byte[] getBytes() {
        return value_;
    }

    public int getPadCount() {
        return pad_;
    }


    public int bitCount() {
        return (value_.length * 8) - pad_;
    }


    public boolean isZero() {
        return (value_.length == 0);
    }

    public int getTag() {
        return ASN1.TAG_BITSTRING;
    }


    public void encode(Encoder enc) throws ASN1Exception, IOException {
        enc.writeBitString(this);
    }


    public void decode(Decoder dec) throws ASN1Exception, IOException {
        boolean tmp;

        tmp = namedBits_;

        try {
            dec.readBitString(this);
        } finally {
            namedBits_ = tmp;
        }

    }

    public String toString() {
        StringBuffer buf;
        boolean[] bits;
        int i;

        bits = getBits();
        buf = new StringBuffer(12 + bits.length);

        buf.append("BitString '");

        for (i = 0; i < bits.length; i++) {
            if (bits[i]) {
                buf.append("1");
            } else {
                buf.append("0");
            }
        }
        buf.append("'");

        return buf.toString();
    }
}
