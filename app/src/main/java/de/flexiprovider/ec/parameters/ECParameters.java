package de.flexiprovider.ec.parameters;

import java.io.IOException;
import java.security.AlgorithmParametersSpi;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;


public class ECParameters extends AlgorithmParametersSpi {


    // the EC domain parameters
    private CurveParams curveParams;


    public void init(AlgorithmParameterSpec params)
            throws InvalidParameterSpecException {

        if ((params == null) || !(params instanceof CurveParams)) {
            throw new InvalidParameterSpecException("unsupported type");
        }
        curveParams = (CurveParams) params;
    }


    public byte[] getEncoded() {
        return new byte[0];
    }


    public byte[] getEncoded(String format) throws IOException {
        if (!format.equals("ASN.1")) {
            throw new IOException("Unsupported encoding format.");
        }
        return getEncoded();
    }


    public AlgorithmParameterSpec getParameterSpec(Class paramSpec)
            throws InvalidParameterSpecException {

        if ((paramSpec == null)
                || !(paramSpec.isAssignableFrom(((Object) curveParams).getClass()))) {
            throw new InvalidParameterSpecException("unsupported type");
        }

        return curveParams;
    }


    public String toString() {
        return ((Object) curveParams).toString();
    }

    @Override
    protected void engineInit(AlgorithmParameterSpec paramSpec) throws InvalidParameterSpecException {
        init(paramSpec);
    }

    @Override
    protected void engineInit(byte[] params) throws IOException {

    }

    @Override
    protected void engineInit(byte[] params, String format) throws IOException {

    }

    @Override
    protected AlgorithmParameterSpec engineGetParameterSpec(Class paramSpec) throws InvalidParameterSpecException {
        return getParameterSpec(paramSpec);
    }

    @Override
    protected byte[] engineGetEncoded() throws IOException {
        return getEncoded();
    }

    @Override
    protected byte[] engineGetEncoded(String format) throws IOException {
        return getEncoded(format);
    }

    @Override
    protected String engineToString() {
        return null;
    }
}
