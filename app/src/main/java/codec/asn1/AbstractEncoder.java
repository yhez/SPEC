package codec.asn1;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;


public abstract class AbstractEncoder extends FilterOutputStream implements
        Encoder {
    public AbstractEncoder(OutputStream out) {
        super(out);
    }

    public void writeType(ASN1Type t) throws ASN1Exception, IOException {
        t.encode(this);
    }

    protected void writeHeader(int tag, int cls, boolean prim, int len)
            throws IOException {
        int b;
        int i;

        b = cls & ASN1.CLASS_MASK;

        if (!prim) {
            b = b | ASN1.CONSTRUCTED;
        }

        if (tag > 30) {
            b = b | ASN1.TAG_MASK;
            out.write(b);
            writeBase128(tag);
        } else {
            b = b | tag;
            out.write(b);
        }
        if (len == -1) {
            out.write(0x80);
        } else {
            if (len > 127) {
                i = (significantBits(len) + 7) / 8;
                out.write(i | 0x80);
                writeBase256(len);
            } else {
                out.write(len);
            }
        }
    }


    protected void writeBase128(int n) throws IOException {
        int i;
        int j;

        i = (significantBits(n) + 6) / 7;
        j = (i - 1) * 7;

        while (i > 1) {
            out.write(((n >>> j) & 0x7f) | 0x80);
            j = j - 7;
            i--;
        }
        out.write(n & 0x7f);
    }

    protected void writeBase256(int n) throws IOException {
        int i;
        int j;

        i = (significantBits(n) + 7) / 8;
        j = (i - 1) * 8;

        while (i > 0) {
            out.write((n >>> j) & 0xff);
            j = j - 8;
            i--;
        }
    }

    protected int significantBits(int n) {
        int i;

        if (n == 0) {
            return 1;
        }

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
