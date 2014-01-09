package codec.x509;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.security.cert.CertificateEncodingException;
import java.util.HashSet;
import java.util.Set;

import codec.asn1.ASN1Boolean;
import codec.asn1.ASN1Exception;
import codec.asn1.ASN1ObjectIdentifier;
import codec.asn1.ASN1OctetString;
import codec.asn1.ASN1Sequence;
import codec.asn1.ASN1Type;
import codec.asn1.ConstraintException;
import codec.asn1.DERDecoder;
import codec.asn1.DEREncoder;


public class X509Extension extends ASN1Sequence implements
        java.security.cert.X509Extension, Externalizable {

    protected ASN1ObjectIdentifier extnID = null;
    protected ASN1Boolean critical = null;
    protected ASN1OctetString extnValue = null;

    public X509Extension() {

        extnID = new ASN1ObjectIdentifier();
        add(extnID);

        critical = new ASN1Boolean(false);
        critical.setOptional(true);

        add(critical);

        extnValue = new ASN1OctetString();
        add(extnValue);
    }


    public Set getCriticalExtensionOIDs() {

        HashSet res = new HashSet();

        if (isCritical())
            res.add(getOID());

        return res;
    }


    public byte[] getEncoded() throws CertificateEncodingException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DEREncoder enc = new DEREncoder(bos);

        try {
            this.encode(enc);
            bos.close();
        } catch (IOException e) {
            System.err.println("getenc Internal error: shouldn't happen!");
            e.printStackTrace();
        } catch (ASN1Exception e) {
            throw new CertificateEncodingException(e.getMessage());
        }
        return bos.toByteArray();

    }


    public byte[] getExtensionValue(String oid) {
        byte[] res = null;

        if (extnValue == null)
            return null;

        if (extnID.toString().equals(oid)
                || extnID.toString().equals("OID." + oid)) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                DEREncoder enc = new DEREncoder(baos);
                extnValue.encode(enc);
                res = baos.toByteArray();
                baos.close();
            } catch (ASN1Exception asn1e) {
                throw new IllegalStateException(
                        "Caught ASN1Exception. Internal Error. Shouldn't happen");
            } catch (IOException ioe) {
                throw new IllegalStateException(
                        "Internal Error. Shouldn't happen");
            }

        }
        return res;
    }

    public Set getNonCriticalExtensionOIDs() {

        HashSet res = new HashSet();

        if (!isCritical())
            res.add(getOID());

        return res;

    }


    public ASN1ObjectIdentifier getOID() {
        return extnID;
    }


    public Object getValue() {
        ByteArrayInputStream bis;
        DERDecoder dec;
        ASN1Type res = null;

        try {
            bis = new ByteArrayInputStream(extnValue.getByteArray());
            dec = new DERDecoder(bis);
            res = dec.readType();
            dec.close();
        } catch (IOException e) {
            System.err.println("Internal error: shouldn't happen!");
            e.printStackTrace();
        } catch (ASN1Exception e) {
            res = extnValue;
        }
        return res;

    }
    public boolean hasUnsupportedCriticalExtension() {

        if (!isCritical())
            return false;
        return false;
    }
    public boolean isCritical() {
        return !isOptional() && critical.isTrue();
    }

    public void setOID(ASN1ObjectIdentifier noid) throws ConstraintException {
        extnID.setOID(noid.getOID());
    }


    public void setValue(ASN1Type nval) throws CertificateEncodingException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            nval.encode(new DEREncoder(baos));
            extnValue.setByteArray(baos.toByteArray());
        } catch (Exception e) {
            throw new CertificateEncodingException(e.getMessage());
        }
    }
    public String toString() {
        return toString("");

    }
    public String toString(String offset) {
        String res;

        res = "Extension " + extnID.toString();

        if (critical.isTrue())
            res = res + " (CRITICAL)";
        else
            res = res + " (not critical)";

        res = res + " Value=" + getValue().toString();

        return res;
    }

}
