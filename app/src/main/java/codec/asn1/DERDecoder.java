package codec.asn1;

import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import java.math.BigInteger;


public class DERDecoder extends FilterInputStream implements Decoder {
    protected int tag_;
    protected int tagclass_;
    protected int length_;
    protected boolean indefinite_;
    protected boolean primitive_;
    protected boolean skip_ = false;

    protected int pos_ = 0;

    protected int markpos_ = 0;

    protected int limit_;


    protected int[] oidbuf_ = new int[32];

    protected boolean debug_ = false;

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


    static public Class getClass(int tag) throws ASN1Exception {
        Class cls;

        if (tag < 0)
            throw new IllegalArgumentException("Tag number is negative!");

        if (tag < typeclass_.length) {
            cls = typeclass_[tag];
            if (cls != null)
                return cls;
        }
        throw new ASN1Exception("Unknown tag! (" + tag + ")");
    }

    public DERDecoder(InputStream in) {
        super(in);
    }


    protected boolean readNext() throws ASN1Exception, IOException {
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

        if (n < 0) {
            throw new ASN1Exception("Unexpected EOF, length missing!");
        }
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

        if (length_ < 0) {
            throw new ASN1Exception("Negative length: " + length_);
        }
        if (limit_ > 0) {
            m = pos_ + length_ - limit_;

            if (m > 0) {
                throw new ASN1Exception("Maximum input limit violated by " + m
                        + " octets!");
            }
        }
        if (primitive_ && indefinite_) {
            throw new ASN1Exception(
                    "Encoding can't be PRIMITIVE and INDEFINITE LENGTH!");
        }
        {
            if (debug_)
                debugHeader(j);
        }
        return true;
    }

    private void debugHeader(int offset) {
        StringBuffer sb;
        String s;
        String t;

        sb = new StringBuffer();
        sb.append("(").append(offset).append(")\t");

        switch (tagclass_) {
            case ASN1.CLASS_UNIVERSAL:
                t = "UNIVERSAL";
                break;
            case ASN1.CLASS_PRIVATE:
                t = "PRIVATE";
                break;
            case ASN1.CLASS_CONTEXT:
                t = "CONTEXT SPECIFIC";
                break;
            case ASN1.CLASS_APPLICATION:
                t = "APPLICATION";
                break;
            default:
                t = "*INTERNAL ERROR*";
        }
        if (tagclass_ == ASN1.CLASS_UNIVERSAL && tag_ < typename_.length) {
            s = typename_[tag_];
            if (s == null) {
                sb.append("[UNIVERSAL ").append(tag_).append("] ");
            } else {
                sb.append(s).append(" ");
            }
        } else {
            sb.append("[").append(t).append(" ").append(tag_).append("] ");
        }
        sb.append((primitive_) ? "PRIMITIVE " : "CONSTRUCTED ");
        sb.append("length: ");
        if (indefinite_) {
            sb.append("indefinite");
        } else {
            sb.append(length_);
        }
        System.out.println(sb.toString());
    }


    protected void match0(ASN1Type t, boolean primitive) throws ASN1Exception,
            IOException {

        if (!t.isExplicit()) {
            if (primitive != primitive_) {
                throw new ASN1Exception("PRIMTIVE vs. CONSTRUCTED mismatch!");
            }

            skipNext(false);

            return;
        }
        if (!readNext()) {
            throw new EOFException("End of stream reached!");
        }
        if (t.isType(tag_, tagclass_)) {
            if (primitive != primitive_) {
                throw new ASN1Exception("CONSTRUCTED vs. PRIMITIVE mismatch!");
            }
            return;
        }
        throw new ASN1Exception("Type mismatch!");
    }


    protected void match1(ASN1Type t) throws ASN1Exception, IOException {
        if (!t.isExplicit()) {

            skipNext(false);

            return;
        }
        if (!readNext()) {
            throw new EOFException("End of stream reached!");
        }
        if (t.isType(tag_, tagclass_)) {
            return;
        }
        throw new ASN1Exception("Type mismatch!");
    }

    protected void match2(int tag, int tagclass) throws IOException,
            ASN1Exception {
        if (!readNext()) {
            throw new EOFException("End of stream reached!");
        }
        if (tag != tag_ || tagclass != tagclass_) {
            throw new ASN1Exception("Type mismatch!");
        }
    }

    protected void skipNext(boolean skip) {
        skip_ = skip;
    }

    public ASN1Type readType() throws ASN1Exception, IOException {
        if (!readNext()) {
            throw new EOFException("End of encoding reached!");
        }
        if (tag_ == 0 && tagclass_ == 0) {
            if (length_ != 0) {
                throw new ASN1Exception("EOC with non-zero length!");
            }
            return null;
        }
        if (tagclass_ != ASN1.CLASS_UNIVERSAL) {
            ASN1OctetString o;
            ASN1TaggedType t;


            if (indefinite_) {
                throw new ASN1Exception(
                        "The decoder encountered a non-UNIVERSAL "
                                + "type with INDEFINITE LENGTH encoding. "
                                + "There is not sufficient information to "
                                + "determine the actual length of this "
                                + "type. Please try again by providing the "
                                + "appropriate template structure to the "
                                + "decoder.");
            }
            primitive_ = true;

            o = new ASN1OctetString();
            t = new ASN1TaggedType(tag_, tagclass_, o, false);

            readOctetString(o);

            return t;
        }
        ASN1Type t;

        try {
            t = (ASN1Type) getClass(tag_).newInstance();
        } catch (InstantiationException e) {
            throw new ASN1Exception("Internal error, can't instantiate type!");
        } catch (IllegalAccessException e) {
            throw new ASN1Exception("Internal error, can't access type!");
        }

        if (t instanceof ASN1Collection) {
            if (primitive_) {
                throw new ASN1Exception("Collections cannot be PRIMITIVE!");
            }
            readTypes((ASN1Collection) t);
        } else {
            skipNext(true);
            t.decode(this);
        }
        return t;
    }


    protected void readTypes(ASN1Collection c) throws ASN1Exception,
            IOException {
        ASN1Type o;
        int end;

        end = pos_ + length_;

        while (end > pos_) {
            o = readType();

            if (o == null) {
                throw new ASN1Exception(
                        "EOC cannot be component of a collection!");
            }
            c.add(o);
        }
        if (end < pos_) {
            throw new ASN1Exception("Length short by " + (pos_ - end)
                    + " octets!");
        }
    }

    public void readBoolean(ASN1Boolean t) throws ASN1Exception, IOException {
        int b;

        match0(t, true);
        b = read();

        if (b < 0) {
            throw new ASN1Exception("Unexpected EOF!");
        }
        if (b == 0) {
            t.setTrue(false);
        } else if (b == 0xff) {
            t.setTrue(true);
        } else {
            throw new ASN1Exception("Bad ASN.1 Boolean encoding!");
        }
    }

    public void readInteger(ASN1Integer t) throws ASN1Exception, IOException {
        byte[] buf;

        match0(t, true);

        buf = new byte[length_];

        if (read(buf) < buf.length) {
            throw new ASN1Exception("Unexpected EOF!");
        }
        t.setBigInteger(new BigInteger(buf));
    }

    public void readBitString(ASN1BitString t) throws ASN1Exception,
            IOException {
        byte[] buf;
        int pad;

        match0(t, true);

        if (length_ < 1) {
            throw new ASN1Exception("Length is zero, no initial octet!");
        }
        pad = read();

        if (pad < 0) {
            throw new ASN1Exception("Unexpected EOF!");
        }
        buf = new byte[length_ - 1];

        if (buf.length > 0 && read(buf) < buf.length) {
            throw new ASN1Exception("Unexpected EOF!");
        }
        t.setBits(buf, pad);
    }

    public void readOctetString(ASN1OctetString t) throws ASN1Exception,
            IOException {
        byte[] buf;

        match0(t, true);

        buf = new byte[length_];

        if (length_ > 0) {
            if (read(buf) < buf.length) {
                throw new ASN1Exception("Unexpected EOF!");
            }
        }
        t.setByteArray(buf);
    }

    public void readNull(ASN1Null t) throws ASN1Exception, IOException {
        match0(t, true);

        if (length_ != 0 || indefinite_) {
            throw new ASN1Exception("ASN.1 Null has bad length!");
        }
    }

    public void readObjectIdentifier(ASN1ObjectIdentifier t)
            throws ASN1Exception, IOException {
        int[] oid;
        int end;
        int n;
        int i;

        match0(t, true);

        if (length_ < 1) {
            throw new ASN1Exception("OID with not contents octets!");
        }
        end = pos_ + length_;
        n = read();

        if (n < 0 || n > 119) {
            throw new ASN1Exception("OID contents octet[0] must be [0,119]!");
        }
        oidbuf_[0] = n / 40;
        oidbuf_[1] = n % 40;
        i = 2;

        try {
            while (pos_ < end) {
                oidbuf_[i++] = readBase128();
            }
            if (pos_ != end) {
                throw new ASN1Exception("Bad length!");
            }
            oid = new int[i];

            System.arraycopy(oidbuf_, 0, oid, 0, i);
            t.setOID(oid);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ASN1Exception("Can't handle more than " + oidbuf_.length
                    + " OID elements!");
        }
    }

    public void readString(ASN1String t) throws ASN1Exception, IOException {
        byte[] buf;

        match0(t, true);

        buf = new byte[length_];

        if (read(buf) < buf.length) {
            throw new ASN1Exception("Unexpected EOF!");
        }
        t.setString(t.convert(buf));
    }

    public void readCollection(ASN1Collection t) throws ASN1Exception,
            IOException {
        Iterator i;
        ASN1Type o;
        int end;
        int n;

        match0(t, false);

        end = pos_ + length_;
        i = t.iterator();
        n = 0;

        if (pos_ < end) {
            while (i.hasNext()) {
                if (!readNext()) {
                    break;
                }
                skipNext(true);
                o = (ASN1Type) i.next();
                n++;

                if (o.isType(tag_, tagclass_)) {
                    o.decode(this);
                    o.setOptional(false);

                    if (pos_ == end) {
                        break;
                    }
                    if (pos_ > end) {
                        throw new ASN1Exception("Length short by "
                                + (pos_ - end) + " octets!");
                    }
                } else {
                    if (!o.isOptional()) {
                        throw new ASN1Exception("ASN.1 type mismatch!"
                                + "\nExpected: " + o.getClass().getName()
                                + "\nIn      : " + ((Object)t).getClass().getName()
                                + "\nAt index: " + (n - 1) + "\nGot tag : "
                                + tag_ + " and class: " + tagclass_);
                    }
                }
            }
        }
        while (i.hasNext()) {
            o = (ASN1Type) i.next();
            n++;

            if (!o.isOptional()) {
                throw new ASN1Exception("ASN.1 type missing!" + "\nExpected: "
                        + o.getClass().getName() + "\nIn      : "
                        + ((Object)t).getClass().getName() + "\nAt index: " + (n - 1));
            }
        }
        if (pos_ < end) {
            throw new ASN1Exception("Bad length, " + (end - pos_)
                    + " contents octets left!");
        }
    }

    public void readCollectionOf(ASN1CollectionOf t) throws ASN1Exception,
            IOException {
        int end;
        ASN1Type o;

        match0(t, false);

        t.clear();
        end = pos_ + length_;

        while (pos_ < end) {
            try {
                o = t.newElement();
            } catch (IllegalStateException e) {
                throw new ASN1Exception("Cannot create new element! ");
            }
            o.decode(this);
        }
        if (pos_ != end) {
            throw new ASN1Exception("Bad length!");
        }
    }

    public void readTime(ASN1Time t) throws ASN1Exception, IOException {
        readString(t);
    }


    public void readTaggedType(ASN1TaggedType t) throws ASN1Exception,
            IOException {
        ASN1Type o;

        match1(t);

        o = t.getInnerType();
        if (o.isExplicit() && primitive_) {
            throw new ASN1Exception("PRIMITIVE vs. CONSTRUCTED mismatch!");
        }
        if (t instanceof ASN1Opaque) {
            if (indefinite_) {
                throw new ASN1Exception(
                        "Cannot decode indefinite length encodings "
                                + "with ASN1Opaque type!");
            }
            primitive_ = true;
        }
        o.decode(this);
    }


    public void readChoice(ASN1Choice t) throws ASN1Exception, IOException {
        ASN1Type o;

        if (!readNext())
            throw new IOException("Unexpected EOF!");

        skipNext(true);

        o = t.getType(tag_, tagclass_);
        if (o == null)
            throw new ASN1Exception("Type mismatch!");

        o.decode(this);
        t.setInnerType(o);
    }


    public int readBase128() throws ASN1Exception, IOException {
        int n;
        int b;

        n = 0;

        while ((b = read()) >= 0) {
            n = (n << 7) | (b & 0x7f);

            if ((b & 0x80) == 0) {
                break;
            }
        }
        if (b < 0) {
            throw new ASN1Exception("Unexpected EOF, base 128 octet missing!");
        }
        return n;
    }


    public int readBase256(int num) throws ASN1Exception, IOException {
        int n, b;

        n = 0;

        while (num > 0) {
            b = read();

            if (b < 0) {
                throw new ASN1Exception(
                        "Unexpected EOF, base 256 octet missing!");
            }
            n = (n << 8) + b;
            num--;
        }
        return n;
    }


    public int read() throws IOException {
        int b;

        b = in.read();

        if (b >= 0) {
            pos_++;
        }
        return b;
    }


    public int read(byte[] b, int off, int len) throws IOException {
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
    }

    public int read(byte[] b) throws IOException {
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

    public void close() throws IOException {
        in.close();
        in = null;
    }

}
