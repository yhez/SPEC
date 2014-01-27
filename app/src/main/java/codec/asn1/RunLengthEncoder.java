package codec.asn1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class RunLengthEncoder implements Encoder {

    public static final int INCREMENT = 256;

    private int[] stack_;
    private int tops_;

    private int[] acc_;
    private int topa_;

    public RunLengthEncoder() {
    }

    public int[] getLengthFields() {
        if (tops_ == 0)
            return new int[0];

        int[] res;

        res = new int[tops_];
        System.arraycopy(stack_, 0, res, 0, tops_);
        return res;
    }

    public void writeType(ASN1Type o) throws ASN1Exception {
        try {
            o.encode(this);
        } catch (IOException e) {
            throw new ASN1Exception("Caught IOException without I/O!");
        }
    }


    public int getHeaderLength(int tag, int len) throws ASN1Exception {
        int n;

        if (len < 0)
            throw new ASN1Exception("Length is negative!");

        n = 2;
        if (tag > 30)
            n = n + (significantBits(tag) + 6) / 7;

        if (len > 127)
            n = n + (significantBits(len) + 7) / 8;

        return n;
    }


    protected int significantBits(int n) {
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

    public void writeBoolean(ASN1Boolean t) throws ASN1Exception {
        if (t.isOptional())
            return;

        push(t, 1);
    }

    public void writeInteger(ASN1Integer t) throws ASN1Exception {
        if (t.isOptional())
            return;

        int n;

        n = t.getBigInteger().bitLength() / 8 + 1;
        push(t, n);
    }

    public void writeBitString(ASN1BitString t) throws ASN1Exception {
        if (t.isOptional())
            return;

        int n;

        if (t.isZero())
            n = 1;
        else
            n = (t.bitCount() + 7) / 8 + 1;

        push(t, n);
    }

    public void writeOctetString(ASN1OctetString t) throws ASN1Exception {
        if (t.isOptional())
            return;

        push(t, t.byteCount());
    }

    public void writeNull(ASN1Null t) throws ASN1Exception {
        if (t.isOptional())
            return;

        push(t, 0);
    }

    public void writeObjectIdentifier(ASN1ObjectIdentifier t)
            throws ASN1Exception {
        if (t.isOptional())
            return;

        int n;
        int i;
        int[] e;

        e = t.getOID();
        if (e.length < 2)
            throw new ASN1Exception("OID must have at least 2 elements!");

        for (n = 1, i = 2; i < e.length; i++)
            n = n + (significantBits(e[i]) + 6) / 7;

        push(t, n);
    }

    public void writeString(ASN1String t) throws ASN1Exception {
        if (t.isOptional())
            return;

        push(t, t.convertedLength(t.getString()));
    }

    public void writeCollection(ASN1Collection t) throws ASN1Exception {
        if (t.isOptional())
            return;

        int n;
        int p;
        int i;
        ArrayList l;
        Collection c;

        c = t.getCollection();
        if (c instanceof ArrayList)
            l = (ArrayList) c;
        else {
            l = new ArrayList(c.size());
            l.addAll(c);
        }
        try {
            for (p = sp(), i = l.size() - 1; i >= 0; i--)
                writeType((ASN1Type) l.get(i));

            n = accumulate(p);
            push(t, n);
        } catch (ClassCastException e) {
            throw new ASN1Exception("Non-ASN.1 type in collection!");
        }
    }

    public void writeTime(ASN1Time t) throws ASN1Exception {
        writeString(t);
    }

    public void writeTaggedType(ASN1TaggedType t) throws ASN1Exception {
        if (t.isOptional())
            return;

        int n;
        int p;

        p = sp();
        writeType(t.getInnerType());
        n = accumulate(p);
        push(t, n);
    }

    protected void push(ASN1Type t, int n) throws ASN1Exception {
        if (stack_ == null) {
            stack_ = new int[INCREMENT];
            tops_ = 0;
        }
        if (tops_ == stack_.length) {
            int[] stack;

            stack = new int[stack_.length + INCREMENT];
            System.arraycopy(stack_, 0, stack, 0, stack_.length);
            stack_ = stack;
        }
        if (acc_ == null) {
            acc_ = new int[INCREMENT];
            topa_ = 0;
        }
        if (topa_ == acc_.length) {
            int[] stack;

            stack = new int[acc_.length + INCREMENT];
            System.arraycopy(acc_, 0, stack, 0, acc_.length);
            acc_ = stack;
        }
        if (t.isExplicit()) {
            stack_[tops_++] = n;
            acc_[topa_++] = n + getHeaderLength(t.getTag(), n);
        } else
            acc_[topa_++] = n;
    }

    protected int sp() {
        return topa_;
    }

    protected int accumulate(int pos) {
        int n;
        int i;

        if (pos > topa_)
            throw new IllegalStateException(
                    "Internal error, bad stack pointer!");

        for (n = 0, i = pos; i < topa_; i++)
            n = n + acc_[i];

        topa_ = pos;
        return n;
    }
}
