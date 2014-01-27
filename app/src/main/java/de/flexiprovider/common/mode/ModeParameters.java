package de.flexiprovider.common.mode;

import java.io.IOException;

import javax.crypto.spec.IvParameterSpec;

import codec.asn1.ASN1Exception;
import codec.asn1.ASN1OctetString;
import de.flexiprovider.api.exceptions.InvalidParameterSpecException;
import de.flexiprovider.api.parameters.AlgorithmParameterSpec;
import de.flexiprovider.api.parameters.AlgorithmParameters;
import de.flexiprovider.common.util.ASN1Tools;
import de.flexiprovider.common.util.ByteUtils;


public class ModeParameters extends AlgorithmParameters {

    private byte[] iv;


    protected void engineInit(java.security.spec.AlgorithmParameterSpec params)
            throws java.security.spec.InvalidParameterSpecException {

        if (params == null) {
            throw new java.security.spec.InvalidParameterSpecException();
        }
        if (!(params instanceof AlgorithmParameterSpec)) {
            if (params instanceof IvParameterSpec) {
                iv = ((IvParameterSpec) params).getIV();
                return;
            }
            throw new java.security.spec.InvalidParameterSpecException();
        }

        init((AlgorithmParameterSpec) params);
    }


    protected java.security.spec.AlgorithmParameterSpec engineGetParameterSpec(
            Class paramSpec)
            throws java.security.spec.InvalidParameterSpecException {

        if (!(AlgorithmParameterSpec.class.isAssignableFrom(paramSpec))) {
            if (paramSpec == IvParameterSpec.class) {
                return getParameterSpec(ModeParameterSpec.class);
            }
            throw new java.security.spec.InvalidParameterSpecException(
                    "Unsupported parameter specification.");
        }

        return getParameterSpec(paramSpec);
    }


    public final void init(AlgorithmParameterSpec paramSpec)
            throws InvalidParameterSpecException {

        if (paramSpec == null) {
            throw new InvalidParameterSpecException("Null parameters.");
        }

        if (!(paramSpec instanceof ModeParameterSpec)) {
            throw new InvalidParameterSpecException(
                    "Unsupported parameter specification.");
        }
        iv = ((ModeParameterSpec) paramSpec).getIV();
    }


    public final void init(byte[] encParams) throws IOException {
        ASN1OctetString asn1IV = new ASN1OctetString();
        try {
            ASN1Tools.derDecode(encParams, asn1IV);
        } catch (ASN1Exception e) {
            throw new IOException("Illegal encoding.");
        }
        iv = asn1IV.getByteArray();
    }


    public final void init(byte[] params, String format) throws IOException {
        if (!(format == "ASN.1")) {
            throw new IOException("Unsupported encoding format.");
        }
        init(params);
    }


    public final byte[] getEncoded() {
        return ASN1Tools.derEncode(new ASN1OctetString(iv));
    }


    public final byte[] getEncoded(String format) throws IOException {
        if (!(format == "ASN.1")) {
            throw new IOException("Unsupported encoding format.");
        }
        return getEncoded();
    }


    public final AlgorithmParameterSpec getParameterSpec(Class paramSpec)
            throws InvalidParameterSpecException {
        if (!(paramSpec.isAssignableFrom(ModeParameterSpec.class))) {
            throw new InvalidParameterSpecException(
                    "Unsupported parameter specification.");
        }
        return new ModeParameterSpec(iv);
    }


    public final String toString() {
        return "IV: " + ByteUtils.toHexString(iv);
    }

}
