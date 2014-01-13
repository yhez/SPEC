package de.flexiprovider.core.kdf;

import java.io.IOException;

import codec.asn1.ASN1Choice;
import codec.asn1.ASN1Exception;
import codec.asn1.ASN1Integer;
import codec.asn1.ASN1Null;
import codec.asn1.ASN1ObjectIdentifier;
import codec.asn1.ASN1OctetString;
import codec.asn1.ASN1Sequence;
import codec.asn1.ASN1Type;
import de.flexiprovider.api.exceptions.InvalidParameterSpecException;
import de.flexiprovider.api.parameters.AlgorithmParameterSpec;
import de.flexiprovider.api.parameters.AlgorithmParameters;
import de.flexiprovider.common.util.ASN1Tools;
import de.flexiprovider.common.util.ByteUtils;
import de.flexiprovider.pki.AlgorithmIdentifier;


public class PBKDF2Parameters extends AlgorithmParameters {


    public static final String OID = PBKDF2.OID;

    // the salt
    private byte[] salt;

    // the iteration count.
    private int iterationCount = 1000;

    // the key size
    private int keySize;


    private static class PBKDF2ASN1Params extends ASN1Sequence {

        // the salt
        private ASN1Choice salt;

        // the iteration count
        private ASN1Integer iterationCount;

        // the key size
        private ASN1Integer keySize;

        // the algorithm identifier of the PRF
        private AlgorithmIdentifier prf;

        /**
         * Construct the ASN.1 structure (used for decoding).
         */
        public PBKDF2ASN1Params() {
            super(4);
            salt = new ASN1Choice();
            salt.addType(new ASN1OctetString());
            salt.addType(new AlgorithmIdentifier());
            iterationCount = new ASN1Integer();
            keySize = new ASN1Integer();
            keySize.setOptional(true);
            prf = new AlgorithmIdentifier();
            prf.setOptional(true);

            add(salt);
            add(iterationCount);
            add(keySize);
            add(prf);
        }


        public PBKDF2ASN1Params(byte[] salt, int iterationCount, int keySize) {
            super(4);
            this.salt = new ASN1Choice();
            this.salt.setInnerType(new ASN1OctetString(salt));
            this.iterationCount = new ASN1Integer(iterationCount);
            this.keySize = new ASN1Integer(keySize);

            try {
                prf = new AlgorithmIdentifier(new ASN1ObjectIdentifier(
                        PBKDF2ParameterSpec.DEFAULT_PRF_OID), new ASN1Null());
            } catch (ASN1Exception ae) {
                throw new RuntimeException("internal error");
            }

            add(this.salt);
            add(this.iterationCount);
            add(this.keySize);
            add(prf);
        }

        public int getIterationCount() {
            return ASN1Tools.getFlexiBigInt(iterationCount).intValue();
        }

        public int getKeyLength() {
            if (keySize.isOptional()) {
                return 0;
            }
            return ASN1Tools.getFlexiBigInt(keySize).intValue();
        }

        public byte[] getSalt() {

            // exception if there is an AlgortihmIdentifier....

            ASN1Type inner = salt.getInnerType();
            if (inner != null) {
                if (inner instanceof ASN1OctetString) {
                    ASN1OctetString os = (ASN1OctetString) inner;
                    return os.getByteArray();
                }
            }
            return null;
        }
    }


    public void init(AlgorithmParameterSpec params)
            throws InvalidParameterSpecException {
        if (!(params instanceof PBKDF2ParameterSpec)) {
            throw new InvalidParameterSpecException("unsupported type");
        }
        PBKDF2ParameterSpec pbkdf2Params = (PBKDF2ParameterSpec) params;

        salt = pbkdf2Params.getSalt();
        iterationCount = pbkdf2Params.getIterationCount();
        keySize = pbkdf2Params.getKeySize();
    }


    public void init(byte[] encParams) throws IOException {
        PBKDF2ASN1Params asn1params;
        try {
            asn1params = new PBKDF2ASN1Params();
            ASN1Tools.derDecode(encParams, asn1params);
        } catch (ASN1Exception ae) {
            throw new IOException("ASN1Exception: " + ae.getMessage());
        }

        salt = asn1params.getSalt();
        iterationCount = asn1params.getIterationCount();
        keySize = asn1params.getKeyLength();
    }


    public void init(byte[] encParams, String format) throws IOException {
        if ((format != null) && !format.equals("ASN.1")) {
            throw new IOException("unsupported format");
        }
        init(encParams);
    }


    public byte[] getEncoded() throws IOException {
        PBKDF2ASN1Params asn1pbeParams = new PBKDF2ASN1Params(salt,
                iterationCount, keySize);

        try {
            return ASN1Tools.derEncode(asn1pbeParams);
        } catch (RuntimeException re) {
            throw new IOException(re.getMessage());
        }
    }


    public byte[] getEncoded(String format) throws IOException {
        if ((format != null) && !format.equals("ASN.1")) {
            throw new IOException("unsupported format");
        }
        return getEncoded();
    }


    public AlgorithmParameterSpec getParameterSpec(Class paramSpec)
            throws InvalidParameterSpecException {

        if (!paramSpec.isAssignableFrom(PBKDF2ParameterSpec.class)) {
            throw new InvalidParameterSpecException("unsupported type");
        }
        return new PBKDF2ParameterSpec(salt, iterationCount, keySize);
    }


    public String toString() {
        String result = "salt             : " + ByteUtils.toHexString(salt);
        result += "\niteration count: " + iterationCount;
        result += "\nkey size       : " + keySize;
        result += "\nprf OID        : " + PBKDF2ParameterSpec.DEFAULT_PRF_OID;
        return result;
    }

}
