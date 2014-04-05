package de.flexiprovider.ec.parameters;

import java.io.IOException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;

import de.flexiprovider.api.parameters.AlgorithmParameters;


public class ECParameters extends AlgorithmParameters {


    // the EC domain parameters
    private CurveParams curveParams;


    public void init(AlgorithmParameterSpec params)
            throws InvalidParameterSpecException {

        if ((params == null) || !(params instanceof CurveParams)) {
            throw new InvalidParameterSpecException("unsupported type");
        }
        curveParams = (CurveParams) params;
    }

    @Override
    public void init(byte[] encParams) {

    }


    @Override
    public void init(byte[] encParams, String format) {

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
                || !(paramSpec.isAssignableFrom(((Object)curveParams).getClass()))) {
            throw new InvalidParameterSpecException("unsupported type");
        }

        return curveParams;
    }


    public String toString() {
        return ((Object)curveParams).toString();
    }

}
