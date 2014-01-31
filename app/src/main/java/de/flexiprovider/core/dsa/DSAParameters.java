package de.flexiprovider.core.dsa;

import java.io.IOException;
import java.security.spec.AlgorithmParameterSpec;

import codec.asn1.ASN1Exception;
import codec.asn1.ASN1Integer;
import codec.asn1.ASN1Sequence;
import de.flexiprovider.api.exceptions.InvalidParameterSpecException;
import de.flexiprovider.api.parameters.AlgorithmParameters;
import de.flexiprovider.common.math.FlexiBigInt;
import de.flexiprovider.common.util.ASN1Tools;


public class DSAParameters extends AlgorithmParameters {


    public static final String OID = "1.2.840.10040.4.1";

    // the prime p
    private FlexiBigInt p;

    // the subprime q
    private FlexiBigInt q;

    // the generator g
    private FlexiBigInt g;


    private static class DSAASN1Parameters extends ASN1Sequence {

        // the prime p
        private ASN1Integer p;

        // the subprime q
        private ASN1Integer q;

        // the generator g
        private ASN1Integer g;


        public DSAASN1Parameters() {
            super(3);
            p = new ASN1Integer();
            q = new ASN1Integer();
            g = new ASN1Integer();

            add(p);
            add(q);
            add(g);
        }


        public DSAASN1Parameters(FlexiBigInt p, FlexiBigInt q, FlexiBigInt g) {
            super(3);
            this.p = ASN1Tools.createInteger(p);
            this.q = ASN1Tools.createInteger(q);
            this.g = ASN1Tools.createInteger(g);

            add(this.p);
            add(this.q);
            add(this.g);
        }


        public FlexiBigInt getG() {
            return ASN1Tools.getFlexiBigInt(g);
        }


        public FlexiBigInt getP() {
            return ASN1Tools.getFlexiBigInt(p);
        }


        public FlexiBigInt getQ() {
            return ASN1Tools.getFlexiBigInt(q);
        }

    }


    public void init(AlgorithmParameterSpec params)
            throws InvalidParameterSpecException {

        if (!(params instanceof DSAParameterSpec)) {
            throw new InvalidParameterSpecException("unsupported type");
        }
        DSAParameterSpec dsaSpec = (DSAParameterSpec) params;

        p = dsaSpec.getPrimeP();
        q = dsaSpec.getPrimeQ();
        g = dsaSpec.getBaseG();
    }


    public void init(byte[] encParams) throws IOException {

        try {
            DSAASN1Parameters asn1dsaParams = new DSAASN1Parameters();
            ASN1Tools.derDecode(encParams, asn1dsaParams);

            p = asn1dsaParams.getP();
            q = asn1dsaParams.getQ();
            g = asn1dsaParams.getG();

        } catch (ASN1Exception ae) {
            throw new IOException("unable to decode parameters.");
        }
    }


    public void init(byte[] encParams, String format) throws IOException {
        if (!format.equals("ASN.1")) {
            throw new IOException("unsupported format");
        }
        init(encParams);
    }


    public byte[] getEncoded() {
        DSAASN1Parameters asn1dsaParams = new DSAASN1Parameters(p, q, g);
        return ASN1Tools.derEncode(asn1dsaParams);
    }


    public byte[] getEncoded(String format) throws IOException {
        if (!format.equals("ASN.1")) {
            throw new IOException("unsupported format");
        }
        return getEncoded();
    }


    public AlgorithmParameterSpec getParameterSpec(Class paramSpec)
            throws InvalidParameterSpecException {

        if ((paramSpec == null)
                || !paramSpec.isAssignableFrom(DSAParameterSpec.class)) {
            throw new InvalidParameterSpecException("unsupported type");
        }

        return new DSAParameterSpec(p, q, g);
    }


    public String toString() {
        return "p: 0x" + p.toString(16) + "\n" + "q: 0x" + q.toString(16)
                + "\n" + "g: 0x" + g.toString(16);

    }


    protected DSAASN1Parameters getASN1Parameters() {
        return new DSAASN1Parameters(p, q, g);
    }

}
