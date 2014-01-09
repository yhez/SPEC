package codec.x509;

import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.security.cert.X509CRLEntry;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import codec.asn1.ASN1Choice;
import codec.asn1.ASN1Exception;
import codec.asn1.ASN1GeneralizedTime;
import codec.asn1.ASN1Integer;
import codec.asn1.ASN1Sequence;
import codec.asn1.ASN1SequenceOf;
import codec.asn1.ASN1Time;
import codec.asn1.ASN1Type;
import codec.asn1.ASN1UTCTime;
import codec.asn1.Constraint;
import codec.asn1.ConstraintException;
import codec.asn1.DERDecoder;
import codec.asn1.DEREncoder;
import codec.asn1.Decoder;
import codec.asn1.Encoder;
import my.BigInteger;


public class CRLEntry extends X509CRLEntry implements ASN1Type, Externalizable {

    private ASN1Sequence crlEntry_;

    private ASN1Integer userCertificate_;

    private ASN1Choice revocationDate_;

    private ASN1SequenceOf crlEntryExtensions_;


    public CRLEntry() {

        crlEntry_ = new ASN1Sequence();
        userCertificate_ = new ASN1Integer();

        crlEntry_.add(userCertificate_);

        revocationDate_ = new ASN1Choice();
        revocationDate_.addType(new ASN1UTCTime());
        revocationDate_.addType(new ASN1GeneralizedTime());

        crlEntry_.add(revocationDate_);

        crlEntryExtensions_ = new ASN1SequenceOf(X509Extension.class);
        crlEntryExtensions_.setOptional(true);
        crlEntry_.add(crlEntryExtensions_);
    }
    public void setConstraint(Constraint c) {
        crlEntry_.setConstraint(c);
    }
    public Constraint getConstraint() {
        return crlEntry_.getConstraint();
    }
    public void checkConstraints() throws ConstraintException {
        crlEntry_.checkConstraints();
    }
    public void decode(Decoder dec) throws ASN1Exception, IOException {
        crlEntry_.decode(dec);
    }
    public void encode(Encoder enc) throws ASN1Exception, IOException {
        crlEntry_.encode(enc);
    }
    public Set getCriticalExtensionOIDs() {
        HashSet res = new HashSet();

        Iterator it = crlEntryExtensions_.iterator();

        while (it.hasNext()) {
            X509Extension theEx = (X509Extension) it.next();

            if (theEx.isCritical()) {
                res.add(theEx.getOID().toString());
            }
        }
        return res;
    }
    public byte[] getEncoded() throws java.security.cert.CRLException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            crlEntry_.encode(new DEREncoder(baos));
        } catch (Exception e) {
            throw new java.security.cert.CRLException(e.getMessage());
        }

        return baos.toByteArray();
    }
    public byte[] getExtensionValue(String oid) {
        byte[] res = null;

        Iterator it = crlEntryExtensions_.iterator();

        while (it.hasNext()) {
            X509Extension theEx = (X509Extension) it.next();

            if (theEx.getOID().toString().equals(oid)) {
                try {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    theEx.encode(new DEREncoder(baos));
                    res = baos.toByteArray();
                } catch (Exception ignore) {
                }
            }
        }
        return res;
    }
    public Set getNonCriticalExtensionOIDs() {
        HashSet res = new HashSet();
        Iterator it = crlEntryExtensions_.iterator();

        while (it.hasNext()) {
            X509Extension theEx = (X509Extension) it.next();

            if (!theEx.isCritical()) {
                res.add(theEx.getOID().toString());
            }
        }
        return res;
    }
    public Date getRevocationDate() {
        ASN1Time a1t = (ASN1Time) revocationDate_.getInnerType();
        return a1t.getDate();
    }
    public java.math.BigInteger getSerialNumber() {
        return BigInteger.get(userCertificate_.getBigInteger());
    }
    public int getTag() {
        return crlEntry_.getTag();
    }
    public int getTagClass() {
        return crlEntry_.getTagClass();
    }
    public Object getValue() {
        return crlEntry_.getValue();
    }
    public boolean hasExtensions() {
        return (!crlEntryExtensions_.isEmpty());
    }
    public boolean hasUnsupportedCriticalExtension() {

        Set s = getCriticalExtensionOIDs();

        Iterator it = s.iterator();

        while (it.hasNext()) {
            it.next();

        }
        return false;
    }
    public boolean isExplicit() {
        return crlEntry_.isExplicit();
    }
    public boolean isOptional() {
        return crlEntry_.isOptional();
    }
    public boolean isType(int eins, int zwei) {
        return crlEntry_.isType(eins, zwei);
    }
    public void setExplicit(boolean ex) {
        crlEntry_.setExplicit(ex);
    }
    public void setOptional(boolean opt) {
        crlEntry_.setOptional(opt);
    }
    public String toString() {
        return toString("");
    }
    public String toString(String offset) {
        String res = offset + "SNR (dec):" + getSerialNumber().toString(10);

        String date = DateFormat.getDateTimeInstance(DateFormat.FULL,
                DateFormat.FULL).format(getRevocationDate());

        res = res + " date:" + date;

        if (!crlEntryExtensions_.isEmpty()) {
            res = res + "\n" + offset + "extensions (";
            res = res + crlEntryExtensions_.size() + "):";

            for (int i = 0; i < crlEntryExtensions_.size(); i++) {
                res = res + "\n";
                res = res
                        + ((X509Extension) crlEntryExtensions_.get(i))
                        .toString(offset + " ");
            }
        }
        return res;
    }

    public void writeExternal(ObjectOutput s) throws IOException {
        byte[] res;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            encode(new DEREncoder(baos));
            res = baos.toByteArray();
            baos.close();
            s.write(res);
        } catch (ASN1Exception e) {
            throw new RuntimeException(e.toString());
        }
    }

    public void readExternal(ObjectInput s) throws IOException {
        try {
            decode(new DERDecoder((ObjectInputStream) s));
        } catch (ASN1Exception e) {
            throw new RuntimeException(e.toString());
        }
    }
}
