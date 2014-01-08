package codec.asn1;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;


public class BERDecoder extends DERDecoder {


    public BERDecoder(InputStream in) {
        super(in);
    }


    protected void readTypes(ASN1Collection c) throws ASN1Exception,
            IOException {
        if (indefinite_) {
            ASN1Type o;

            while ((o = readType()) != null) {
                c.add(o);
            }
        } else {
            super.readTypes(c);
        }
    }

    public void readBitString(ASN1BitString t) throws ASN1Exception,
            IOException {
        match1(t);
        skipNext(true);

        if (primitive_) {
            super.readBitString(t);
            return;
        }

        ByteArrayOutputStream bos;
        ASN1SequenceOf seq;
        ASN1BitString v;
        Iterator i;
        byte[] buf;
        int pad;
        int n;

        seq = new ASN1SequenceOf(ASN1BitString.class);
        tag_ = ASN1.TAG_SEQUENCE;
        tagclass_ = ASN1.CLASS_UNIVERSAL;

        seq.decode(this);

        pad = 0;
        bos = new ByteArrayOutputStream();
        try {
            for (i = seq.iterator(); i.hasNext(); ) {
                v = (ASN1BitString) i.next();
                bos.write(v.getBytes());

                n = pad;
                pad = v.getPadCount();

                if (pad != 0 && n != 0) {
                    throw new ASN1Exception(
                            "Pad count mismatch in BIT STRING segment!");
                }
            }
            buf = bos.toByteArray();
            bos.close();

            t.setBits(buf, pad);
        } catch (ClassCastException e) {
            throw new ASN1Exception(
                    "Type mismatch in BER encoded BIT STRING segment!");
        }
    }

    public void readOctetString(ASN1OctetString t) throws ASN1Exception,
            IOException {
        match1(t);


        skipNext(true);

        if (primitive_) {
            super.readOctetString(t);
            return;
        }
        ByteArrayOutputStream bos;
        ASN1SequenceOf seq;
        ASN1OctetString v;
        Iterator i;
        byte[] buf;

        seq = new ASN1SequenceOf(ASN1OctetString.class);
        tag_ = ASN1.TAG_SEQUENCE;
        tagclass_ = ASN1.CLASS_UNIVERSAL;
        seq.decode(this);

        bos = new ByteArrayOutputStream();
        try {
            for (i = seq.iterator(); i.hasNext(); ) {
                v = (ASN1OctetString) i.next();
                bos.write(v.getByteArray());
            }
            buf = bos.toByteArray();
            bos.close();
        } catch (ClassCastException e) {
            throw new ASN1Exception(
                    "Type mismatch in BER encoded OCTET STRING segment!");
        }
        t.setByteArray(buf);
    }

    public void readString(ASN1String t) throws ASN1Exception, IOException {
        match1(t);
        skipNext(true);

        if (primitive_) {
            super.readString(t);
            return;
        }
        ASN1OctetString v;

        v = new ASN1OctetString();
        tag_ = ASN1.TAG_OCTETSTRING;
        tagclass_ = ASN1.CLASS_UNIVERSAL;

        v.decode(this);
        t.setString(t.convert(v.getByteArray()));
    }

    public void readCollection(ASN1Collection t) throws ASN1Exception,
            IOException {
        Iterator i;
        ASN1Type o;
        boolean vlen;
        int end;
        int n;

        match0(t, false);

        end = pos_ + length_;
        vlen = indefinite_;
        i = t.iterator();
        n = 0;


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

                if (vlen) {
                    continue;
                }
                if (pos_ == end) {
                    break;
                }
                if (pos_ > end) {
                    throw new ASN1Exception("Length short by " + (pos_ - end)
                            + " octets!");
                }
            } else {
                if (!o.isOptional()) {
                    throw new ASN1Exception("ASN.1 type mismatch!"
                            + "\nExpected: " + o.getClass().getName()
                            + "\nIn      : " + t.getClass().getName()
                            + "\nAt index: " + (n - 1) + "\nGot tag : " + tag_
                            + " and class: " + tagclass_);
                }
            }
        }
        while (i.hasNext()) {
            o = (ASN1Type) i.next();
            n++;

            if (!o.isOptional()) {
                throw new ASN1Exception("ASN.1 type missing!" + "\nExpected: "
                        + o.getClass().getName() + "\nIn      : "
                        + t.getClass().getName() + "\nAt index: " + (n - 1));
            }
        }
        if (vlen) {
            match2(ASN1.TAG_EOC, ASN1.CLASS_UNIVERSAL);
        } else {
            if (pos_ < end) {
                throw new ASN1Exception("Bad length, " + (end - pos_)
                        + " contents octets left!");
            }
        }
    }

    public void readCollectionOf(ASN1CollectionOf t) throws ASN1Exception,
            IOException {
        ASN1Type o;
        boolean vlen;
        int end;

        match0(t, false);

        t.clear();

        vlen = indefinite_;
        end = pos_ + length_;

        while (true) {
            if (!vlen) {
                if (pos_ == end) {
                    return;
                }
                if (pos_ > end) {
                    throw new ASN1Exception("Read " + (pos_ - end)
                            + " octets too much!");
                }
            }
            if (!readNext()) {
                if (vlen) {
                    throw new ASN1Exception("EOC missing at EOF!");
                }
                throw new ASN1Exception("Bad length!");
            }
            if (vlen && (tag_ == ASN1.TAG_EOC)
                    && (tagclass_ == ASN1.CLASS_UNIVERSAL)) {
                return;
            }
            try {
                skipNext(true);
                o = t.newElement();
                o.decode(this);
            } catch (IllegalStateException e) {
                throw new ASN1Exception("Cannot create new element! ");
            }
        }
    }

    public void readTaggedType(ASN1TaggedType t) throws ASN1Exception,
            IOException {
        ASN1Type o;
        boolean vlen;

        match1(t);

        vlen = indefinite_;
        o = t.getInnerType();

        if (o.isExplicit() && primitive_) {
            throw new ASN1Exception("PRIMITIVE vs. CONSTRUCTED mismatch!");
        }
        if (t instanceof ASN1Opaque) {
            if (vlen) {
                throw new ASN1Exception(
                        "Cannot decode indefinite length encodings "
                                + "with ASN1Opaque type!");
            }
            primitive_ = true;
        }
        o.decode(this);

        if (vlen && o.isExplicit()) {

            match2(ASN1.TAG_EOC, ASN1.CLASS_UNIVERSAL);
        }
    }
}
