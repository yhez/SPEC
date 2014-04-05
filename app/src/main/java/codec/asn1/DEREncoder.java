package codec.asn1;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

public class DEREncoder extends AbstractEncoder {
    private int[] stack_;
    private int sp_;


    public DEREncoder(OutputStream out) {
        super(out);
    }

    public boolean isStrict() {
        return false;
    }


    protected void writeHeader(ASN1Type t, boolean primitive){
        int length;

        if (!t.isExplicit()) {
            return;
        }
        if (stack_ == null || sp_ == 0) {
            RunLengthEncoder enc;

            enc = new RunLengthEncoder();
            enc.writeType(t);
            stack_ = enc.getLengthFields();
            sp_ = stack_.length;
        }
        length = stack_[--sp_];

        writeHeader(t.getTag(), t.getTagClass(), primitive, length);
    }

    public void writeBoolean(ASN1Boolean t){
        if (t.isOptional())
            return;

        writeHeader(t, true);
        try {
            write(t.isTrue() ? 0xff : 0x00);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeInteger(ASN1Integer t){
        if (t.isOptional())
            return;

        writeHeader(t, true);
        try {
            write(t.getBigInteger().toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void writeBitString(ASN1BitString t){
        if (t.isOptional()) {
            return;
        }
        writeHeader(t, true);
        try {
            write(t.getPadCount());
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!t.isZero()) {
            try {
                write(t.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void writeOctetString(ASN1OctetString t) {
        if (t.isOptional())
            return;

        writeHeader(t, true);
        try {
            write(t.getByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeNull(ASN1Null t) {
        if (t.isOptional())
            return;

        writeHeader(t, true);
    }

    public void writeObjectIdentifier(ASN1ObjectIdentifier t) {
        if (t.isOptional())
            return;

        writeHeader(t, true);

        int i;
        int[] e;

        e = t.getOID();

        try {
            write(e[0] * 40 + e[1]);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        for (i = 2; i < e.length; i++)
            try {
                writeBase128(e[i]);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
    }

    public void writeString(ASN1String t){
        if (t.isOptional())
            return;

        writeHeader(t, true);
        try {
            write(t.convert(t.getString()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeCollection(ASN1Collection t){
        if (t.isOptional())
            return;

        Iterator i;
        Collection c;
        ArrayList h;
        writeHeader(t, false);

        c = t.getCollection();

        if (isStrict() && t instanceof ASN1SetOf) {
            writeStrictSetOf((ASN1SetOf) t);
            return;
        }
        if (isStrict() && t instanceof ASN1Set) {
            h = new ArrayList(c.size());

            h.addAll(c);
            Collections.sort(h, new ASN1TagComparator());
            c = h;
        }
            for (i = c.iterator(); i.hasNext(); )
                writeType((ASN1Type) i.next());

    }

    protected void writeStrictSetOf(ASN1SetOf t){
        ByteArrayOutputStream bos;
        OutputStream old;
        Collection c;
        ArrayList res;
        Iterator i;
        byte[] buf;

        c = t.getCollection();
        res = new ArrayList(c.size());
        bos = new ByteArrayOutputStream();

        old = super.out;
        super.out = bos;

        try {
            for (i = c.iterator(); i.hasNext(); ) {
                writeType((ASN1Type) i.next());

                if (bos.size() > 0) {
                    res.add(bos.toByteArray());
                    bos.reset();
                }
            }
        } finally {
            super.out = old;
        }
        Collections.sort(res, new DERCodeComparator());

        for (i = res.iterator(); i.hasNext(); ) {
            buf = (byte[]) i.next();

            write(buf, 0, buf.length);
        }
    }

    public void writeTime(ASN1Time t){
        writeString(t);
    }

    public void writeTaggedType(ASN1TaggedType t){
        if (t.isOptional())
            return;

        boolean primitive;
        ASN1Type o;
        int tag;

        o = t.getInnerType();

        if (!o.isExplicit()) {
            if (t instanceof ASN1Opaque)
                tag = t.getTag();
            else
                tag = o.getTag();

            int constructed_ = ((1 << ASN1.TAG_SEQUENCE) | (1 << ASN1.TAG_SET) | (1 << ASN1.TAG_REAL));
            primitive = ((constructed_ & (1 << tag)) == 0);
        } else
            primitive = false;

        writeHeader(t, primitive);
        writeType(t.getInnerType());
    }

    public void write(byte[] b) throws IOException {
        out.write(b);
    }

    public void write(byte[] b, int off, int len) {
        try {
            out.write(b, off, len);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeType(ASN1Type t){
        if (!t.isOptional())
            t.encode(this);
    }


    private void writeHeader(int tag, int cls, boolean prim, int len){
        int b, i;

        b = cls & ASN1.CLASS_MASK;

        if (!prim)
            b = b | ASN1.CONSTRUCTED;

        if (tag > 30) {
            b = b | ASN1.TAG_MASK;
            try {
                out.write(b);
                writeBase128(tag);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            b = b | tag;
            try {
                out.write(b);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (len == -1)
            try {
                out.write(0x80);
            } catch (IOException e) {
                e.printStackTrace();
            }
        else {
            if (len > 127) {
                i = (significantBits(len) + 7) / 8;
                try {
                    out.write(i | 0x80);
                    writeBase256(len);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else
                try {
                    out.write(len);
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    private void writeBase128(int n) throws IOException {
        int i, j;

        i = (significantBits(n) + 6) / 7;
        j = (i - 1) * 7;

        while (i > 1) {
            out.write(((n >>> j) & 0x7f) | 0x80);
            j = j - 7;
            i--;
        }
        out.write(n & 0x7f);
    }

    private void writeBase256(int n) throws IOException {
        int i, j;

        i = (significantBits(n) + 7) / 8;
        j = (i - 1) * 8;

        while (i > 0) {
            out.write((n >>> j) & 0xff);
            j = j - 8;
            i--;
        }
    }

    private int significantBits(int n) {
        int i;

        if (n == 0)
            return 1;

        i = 0;
        while (n > 255) {
            n = n >>> 8;
            i += 8;
        }
        while (n > 0) {
            n = n >>> 1;
            i++;
        }
        return i;
    }
}
