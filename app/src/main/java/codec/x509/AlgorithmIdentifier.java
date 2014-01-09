package codec.x509;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;

import codec.InconsistentStateException;
import codec.asn1.ASN1;
import codec.asn1.ASN1Exception;
import codec.asn1.ASN1Null;
import codec.asn1.ASN1ObjectIdentifier;
import codec.asn1.ASN1Opaque;
import codec.asn1.ASN1Sequence;
import codec.asn1.ASN1Type;
import codec.asn1.DEREncoder;
import codec.util.JCA;


public class AlgorithmIdentifier extends ASN1Sequence {

    protected ASN1Opaque parameters_;

    protected ASN1ObjectIdentifier algorithm_;

    public AlgorithmIdentifier() {
        super(2);

        algorithm_ = new ASN1ObjectIdentifier();
        parameters_ = new ASN1Opaque();
        parameters_.setOptional(true);
        add(algorithm_);
        add(parameters_);
    }


    public AlgorithmIdentifier(ASN1ObjectIdentifier oid, byte[] b)
            throws ASN1Exception {
        super(2);

        if (oid == null)
            throw new NullPointerException("Need an OID!");

        algorithm_ = (ASN1ObjectIdentifier) oid.clone();

        if (b == null) {
            parameters_ = new ASN1Opaque(ASN1.TAG_NULL, ASN1.CLASS_UNIVERSAL,
                    new byte[0]);
        } else
            parameters_ = new ASN1Opaque(b);

        add(algorithm_);
        add(parameters_);
    }


    public AlgorithmIdentifier(ASN1ObjectIdentifier oid, ASN1Type params)
            throws ASN1Exception {
        super(2);

        DEREncoder enc;
        ByteArrayOutputStream bos;

        if (oid == null)
            throw new NullPointerException("Need an OID!");

        algorithm_ = (ASN1ObjectIdentifier) oid.clone();

        try {
            if (params == null || (params instanceof ASN1Null))
                parameters_ = new ASN1Opaque(ASN1.TAG_NULL,
                        ASN1.CLASS_UNIVERSAL, new byte[0]);
            else {
                bos = new ByteArrayOutputStream();
                enc = new DEREncoder(bos);
                params.encode(enc);

                parameters_ = new ASN1Opaque(bos.toByteArray());
                bos.close();
            }
            add(algorithm_);
            add(parameters_);
        } catch (IOException e) {
            throw new InconsistentStateException(
                    "Internal, caught IOException!");
        }
    }


    public AlgorithmParameters getParameters() throws NoSuchAlgorithmException,
            InvalidAlgorithmParameterException {
        AlgorithmParameters params;
        String s;

        if (parameters_.isOptional())
            return null;

        if (parameters_.getTag() == ASN1.TAG_NULL
                && parameters_.getTagClass() == ASN1.CLASS_UNIVERSAL)
            return null;

        s = JCA.getName(algorithm_.toString());

        if (s == null)
            throw new NoSuchAlgorithmException("Cannot resolve "
                    + algorithm_.toString());

        int n;

        n = s.indexOf("/");

        if (n > 0)
            s = s.substring(0, n);

        params = AlgorithmParameters.getInstance(s);

        try {
            params.init(parameters_.getEncoded());
        } catch (IOException e) {
            throw new InvalidAlgorithmParameterException(
                    "Caught IOException(\"" + e.getMessage() + "\")");
        } catch (ASN1Exception e) {
            throw new InvalidAlgorithmParameterException(
                    "Caught ASN1Exception(\"" + e.getMessage() + "\")");
        }
        return params;
    }
    public ASN1ObjectIdentifier getAlgorithmOID() {
        return algorithm_;
    }


    public String getAlgorithmName() {
        return JCA.getName(algorithm_.toString());
    }
    public String toString() {
        String s;
        String t;

        t = "X.509 AlgorithmIdentifier " + algorithm_.toString();
        s = getAlgorithmName();

        if (s != null)
            return t + " (" + s + ")";

        return t;
    }


    public boolean equals(Object o) {
        return o instanceof AlgorithmIdentifier && algorithm_.equals(((AlgorithmIdentifier) o).getAlgorithmOID());
    }

    public int hashCode() {
        return algorithm_.hashCode();
    }


    public Object clone() {
        AlgorithmIdentifier aid;

        aid = (AlgorithmIdentifier) super.clone();
        aid.clear();
        aid.algorithm_ = (ASN1ObjectIdentifier) algorithm_.clone();
        aid.parameters_ = (ASN1Opaque) parameters_.clone();

        aid.add(algorithm_);
        aid.add(parameters_);

        return aid;
    }

}
