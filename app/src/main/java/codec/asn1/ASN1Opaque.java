package codec.asn1;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;


public class ASN1Opaque extends ASN1TaggedType {

    public ASN1Opaque() {
        super(-1, ASN1.CLASS_UNIVERSAL, new ASN1OctetString(), false);
    }

    public ASN1Opaque(byte[] code){
        super(-1, ASN1.CLASS_UNIVERSAL, new ASN1OctetString(), false);

        ByteArrayInputStream bis;
        DERDecoder dec;
            bis = new ByteArrayInputStream(code);
            dec = new DERDecoder(bis);
            decode(dec);
            dec.close();
    }


    public ASN1Opaque(int tag, int tagclass, byte[] b) {
        super(tag, tagclass, new ASN1OctetString(b.clone()), false);
    }

    public boolean isType(int tag, int tagclass) {
        if (tagclass != ASN1.CLASS_UNIVERSAL)
            return false;

        if (getTag() == -1) {
            setTag(tag);
            return true;
        }
        return super.isType(tag, tagclass);
    }


    public byte[] getEncoded() {
        ByteArrayOutputStream bos;
        DEREncoder enc;
        byte[] code;
        try {
            bos = new ByteArrayOutputStream();
            enc = new DEREncoder(bos);
            encode(enc);
            code = bos.toByteArray();
            enc.close();
            return code;
        } catch (IOException ignore) {
        }
        return null;
    }


    public void setInnerType(ASN1Type t) {
        super.setInnerType(t);
    }


    public Object clone() {
        ASN1OctetString b;
        ASN1Opaque o;

        try {
            o = (ASN1Opaque) super.clone();
            b = (ASN1OctetString) o.getInnerType();

            o.setInnerType((ASN1OctetString) b.clone());
        } catch (CloneNotSupportedException e) {

            throw new Error("Internal, clone support mismatch!");
        }
        return o;
    }

}
