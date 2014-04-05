package codec.asn1;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Iterator;


public class DERDecoder extends FilterInputStream implements Decoder {
    static private String[] typename_ = {null,
            "Boolean",
            "Integer",
            "BitString",
            "OctetString",
            "Null",
            "ObjectIdentifier",
            null,
            null,
            null,
            "Enumerated",
            null,
            "UTF8String",
            null,
            null,
            null,
            "Sequence",
            "Set",
            null,
            "PrintableString",
            "T61String",
            null,
            "IA5String",
            "UTCTime",
            "GeneralizedTime",
            null,
            "VisibleString",
            null,
            "UniversalString",
            null,
            "BMPString"
    };
    static private Class[] typeclass_ = getClasses();
    protected int tag_;
    protected int tagclass_;
    protected int length_;
    protected boolean indefinite_;
    protected boolean primitive_;
    protected boolean skip_ = false;
    protected int pos_ = 0;
    protected int markpos_ = 0;
    protected int[] oidbuf_ = new int[32];
    protected boolean debug_ = false;


    public DERDecoder(InputStream in) {
        super(in);
    }

    static private Class[] getClasses() {
        int n, i;
        String s;
        Class[] c;

        s = DERDecoder.class.getName();
        i = s.lastIndexOf('.');
        if (i < 0)
            s = "";
        else
            s = s.substring(0, i) + ".ASN1";

        n = typename_.length;
        c = new Class[n];

        for (i = 0; i < n; i++) {
            if (typename_[i] != null) {
                try {
                    c[i] = Class.forName(s + typename_[i]);
                } catch (ClassNotFoundException e) {
                    c[i] = null;
                }
            } else
                c[i] = null;
        }
        return c;
    }

    static public Class getClass(int tag) {
        Class cls = null;
        if (tag < typeclass_.length) {
            cls = typeclass_[tag];
        }
        return cls;
    }

    protected boolean readNext() {
        int n;
        int m;
        int j;
        j = pos_;

        if (skip_) {
            if (debug_) {
                System.out.println("(" + j + ")\tSkipping.");
            }
            skip_ = false;
            return true;
        }
        n = read();

        if (n < 0) {
            tag_ = -1;
            return false;
        }
        primitive_ = (n & ASN1.CONSTRUCTED) == 0;
        tagclass_ = n & ASN1.CLASS_MASK;

        if ((n & ASN1.TAG_LONGFORM) == ASN1.TAG_LONGFORM) {
            tag_ = readBase128();
        } else {
            tag_ = n & ASN1.TAG_MASK;
        }
        n = read();
        indefinite_ = false;
        m = n & ASN1.LENGTH_MASK;

        if ((n & ASN1.LENGTH_LONGFORM) == ASN1.LENGTH_LONGFORM) {
            if (m == 0) {
                indefinite_ = true;
            } else {
                m = readBase256(m);
            }
        }
        length_ = m;
        return true;
    }


    protected void match0(ASN1Type t) {
        if (!t.isExplicit()) {
            skipNext(false);
        }
    }


    protected void match1(ASN1Type t) {
        if (!t.isExplicit()) {
            skipNext(false);
        }
    }

    protected void skipNext(boolean skip) {
        skip_ = skip;
    }

    private ASN1Type readType() {
        if (tag_ == 0 && tagclass_ == 0) {
            return null;
        }
        if (tagclass_ != ASN1.CLASS_UNIVERSAL) {
            ASN1OctetString o;
            ASN1TaggedType t;
            primitive_ = true;
            o = new ASN1OctetString();
            t = new ASN1TaggedType(tag_, tagclass_, o, false);
            readOctetString(o);

            return t;
        }
        ASN1Type t;

        try {
            t = (ASN1Type) getClass(tag_).newInstance();

            if (t instanceof ASN1Collection) {
                readTypes((ASN1Collection) t);
            } else {
                skipNext(true);
                t.decode(this);
            }
            return t;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }


    protected void readTypes(ASN1Collection c) {
        ASN1Type o;
        int end;

        end = pos_ + length_;

        while (end > pos_) {
            o = readType();
            c.add(o);
        }
    }

    public void readBoolean(ASN1Boolean t) {
        int b;

        match0(t);
        b = read();
        if (b == 0) {
            t.setTrue(false);
        } else if (b == 0xff) {
            t.setTrue(true);
        }
    }

    public void readInteger(ASN1Integer t) {
        byte[] buf;
        match0(t);
        buf = new byte[length_];
        t.setBigInteger(new BigInteger(buf));
    }

    public void readBitString(ASN1BitString t) {
        byte[] buf;
        int pad;

        match0(t);
        pad = read();
        buf = new byte[length_ - 1];
        t.setBits(buf, pad);
    }

    public void readOctetString(ASN1OctetString t) {
        byte[] buf;

        match0(t);

        buf = new byte[length_];
        t.setByteArray(buf);
    }

    public void readNull(ASN1Null t) {
        match0(t);
    }

    public void readObjectIdentifier(ASN1ObjectIdentifier t) {
        int[] oid;
        int end;
        int n;
        int i;

        match0(t);
        end = pos_ + length_;
        n = read();
        oidbuf_[0] = n / 40;
        oidbuf_[1] = n % 40;
        i = 2;

        while (pos_ < end) {
            oidbuf_[i++] = readBase128();
        }
        oid = new int[i];

        System.arraycopy(oidbuf_, 0, oid, 0, i);
        t.setOID(oid);
    }

    public void readString(ASN1String t) {
        byte[] buf;
        match0(t);
        buf = new byte[length_];
        t.setString(t.convert(buf));
    }

    public void readCollection(ASN1Collection t) {
        Iterator i;
        ASN1Type o;
        int end;

        match0(t);

        end = pos_ + length_;
        i = t.iterator();

        if (pos_ < end) {
            while (i.hasNext()) {
                if (!readNext()) {
                    break;
                }
                skipNext(true);
                o = (ASN1Type) i.next();

                if (o.isType(tag_, tagclass_)) {
                    o.decode(this);
                    o.setOptional(false);

                    if (pos_ == end) {
                        break;
                    }
                }
            }
        }
        while (i.hasNext()) {
            i.next();
        }
    }

    public void readCollectionOf(ASN1CollectionOf t) {
        int end;
        ASN1Type o;

        match0(t);

        t.clear();
        end = pos_ + length_;

        while (pos_ < end) {

            o = t.newElement();

            o.decode(this);
        }
    }

    public void readTime(ASN1Time t) {
        readString(t);
    }


    public void readTaggedType(ASN1TaggedType t) {
        ASN1Type o;

        match1(t);

        o = t.getInnerType();
        if (t instanceof ASN1Opaque) {
            primitive_ = true;
        }
        o.decode(this);
    }


    public void readChoice(ASN1Choice t) {
        ASN1Type o;
        skipNext(true);
        o = t.getType(tag_, tagclass_);
        o.decode(this);
        t.setInnerType(o);
    }

    public int readBase128() {
        int n;
        int b;
        n = 0;
        while ((b = read()) >= 0) {
            n = (n << 7) | (b & 0x7f);

            if ((b & 0x80) == 0) {
                break;
            }
        }
        return n;
    }


    public int readBase256(int num) {
        int n, b;

        n = 0;

        while (num > 0) {
            b = read();
            n = (n << 8) + b;
            num--;
        }
        return n;
    }


    public int read() {

        try {
            int b;
            b = in.read();
            if (b >= 0) {
                pos_++;
            }
            return b;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }


    public int read(byte[] b, int off, int len) {
        try {
            int l;
            int ls;
            ls = 0;
            while (ls < len) {
                l = in.read(b, off + ls, len - ls);
                if (l < 0) {
                    break;
                }
                ls += l;
            }
            pos_ += ls;
            return ls;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int read(byte[] b) {
        try {
            int l;
            int ls;


            ls = 0;

            while (ls < b.length) {
                l = in.read(b, ls, b.length - ls);

                if (l < 0) {
                    break;
                }
                ls += l;
            }

            pos_ += ls;
            return ls;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public long skip(long n) throws IOException {
        long l;

        l = in.skip(n);
        pos_ += (int) l;
        return l;
    }

    public void mark(int readAheadLimit) {
        in.mark(readAheadLimit);
        markpos_ = pos_;
    }

    public void reset() throws IOException {
        in.reset();
        pos_ = markpos_;
    }

    public boolean markSupported() {
        return in.markSupported();
    }


    public int available() throws IOException {
        return in.available();
    }

    public void close() {
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        in = null;
    }

}
