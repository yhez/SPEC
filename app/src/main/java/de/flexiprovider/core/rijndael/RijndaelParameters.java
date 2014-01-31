package de.flexiprovider.core.rijndael;

import java.io.IOException;
import java.security.spec.AlgorithmParameterSpec;

import codec.asn1.ASN1Choice;
import codec.asn1.ASN1Exception;
import codec.asn1.ASN1Integer;
import codec.asn1.ASN1Null;
import codec.asn1.ASN1OctetString;
import codec.asn1.ASN1Sequence;
import codec.asn1.ASN1Type;
import de.flexiprovider.api.exceptions.InvalidParameterSpecException;
import de.flexiprovider.api.parameters.AlgorithmParameters;
import de.flexiprovider.common.mode.ModeParameterSpec;
import de.flexiprovider.common.util.ASN1Tools;
import de.flexiprovider.common.util.ByteUtils;


public class RijndaelParameters extends AlgorithmParameters {

    // the block size in bits
    private int blockSize;

    // the initialization vector
    private byte[] iv;


    public void init(AlgorithmParameterSpec params)
            throws InvalidParameterSpecException {
        if ((params == null) || !(params instanceof RijndaelParameterSpec)) {
            throw new InvalidParameterSpecException("unsupported type");
        }

        blockSize = ((RijndaelParameterSpec) params).getBlockSize();
        iv = ((RijndaelParameterSpec) params).getIV();
    }

    public void init(byte[] encParams) throws IOException {
        // build the parameters structure
        ASN1Sequence params = new ASN1Sequence(2);
        params.add(new ASN1Integer());
        ASN1Choice ivChoice = new ASN1Choice();
        // either NULL
        ivChoice.addType(new ASN1Null());
        // or an OCTET STRING
        ivChoice.addType(new ASN1OctetString());
        params.add(ivChoice);

        // decode parameters
        try {
            ASN1Tools.derDecode(encParams, params);
        } catch (ASN1Exception e) {
            throw new IOException("bad encoding");
        }

        // decode block size
        blockSize = ASN1Tools.getFlexiBigInt((ASN1Integer) params.get(0))
                .intValue();

        // decode IV
        ASN1Type ivType = ((ASN1Choice) params.get(1)).getInnerType();
        if (ivType instanceof ASN1Null) {
            iv = null;
        } else {
            iv = ((ASN1OctetString) ivType).getByteArray();
        }
    }

    public void init(byte[] encParams, String format) throws IOException {
        if (!format.equals("ASN.1")) {
            throw new IOException("unsupported format");
        }
        init(encParams);
    }

    public byte[] getEncoded() {
        ASN1Sequence params = new ASN1Sequence(2);

        // encode block size
        params.add(new ASN1Integer(blockSize));

        // encode IV
        if (iv == null) {
            // encode as NULL
            params.add(new ASN1Null());
        } else {
            // encode as OCTET STRING
            params.add(new ASN1OctetString(iv));
        }

        // return encoded parameters
        return ASN1Tools.derEncode(params);
    }

    public byte[] getEncoded(String format) throws IOException {
        if (!format.equals("ASN.1")) {
            throw new IOException("unsupported format");
        }
        return getEncoded();
    }

    public AlgorithmParameterSpec getParameterSpec(Class paramSpec)
            throws InvalidParameterSpecException {

        if (!(RijndaelParameterSpec.class.isAssignableFrom(paramSpec))) {
            throw new InvalidParameterSpecException("unsupported type");
        }

        if (iv == null) {
            return new RijndaelParameterSpec(blockSize);
        }
        return new RijndaelParameterSpec(blockSize, new ModeParameterSpec(iv));
    }

    public String toString() {
        return ("RijndaelParameters (block size " + blockSize + ")") + "(IV " + ByteUtils.toHexString(iv) + ")";
    }
}
