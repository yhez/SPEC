package de.flexiprovider.api.parameters;

import java.io.IOException;

import de.flexiprovider.api.exceptions.InvalidParameterSpecException;


public abstract class AlgorithmParameters extends
        java.security.AlgorithmParametersSpi {


    protected void engineInit(java.security.spec.AlgorithmParameterSpec params)
            throws java.security.spec.InvalidParameterSpecException {

        if ((params == null) || !(params instanceof AlgorithmParameterSpec)) {
            throw new java.security.spec.InvalidParameterSpecException();
        }
        init((AlgorithmParameterSpec) params);
    }


    protected final void engineInit(byte[] params) throws IOException {
        init(params);
    }

    protected final void engineInit(byte[] params, String format)
            throws IOException {
        init(params, format);
    }

    protected final byte[] engineGetEncoded() throws IOException {
        return getEncoded();
    }

    protected final byte[] engineGetEncoded(String format) throws IOException {
        return getEncoded(format);
    }

    protected java.security.spec.AlgorithmParameterSpec engineGetParameterSpec(
            Class paramSpec)
            throws java.security.spec.InvalidParameterSpecException {
        if (!(AlgorithmParameterSpec.class.isAssignableFrom(paramSpec))) {
            throw new java.security.spec.InvalidParameterSpecException(
                    "Unsupported parameter specification.");
        }
        return getParameterSpec(paramSpec);
    }

    protected final String engineToString() {
        return toString();
    }

    public abstract void init(AlgorithmParameterSpec paramSpec)
            throws InvalidParameterSpecException;

    public abstract void init(byte[] encParams) throws IOException;


    public abstract void init(byte[] encParams, String format)
            throws IOException;


    public abstract byte[] getEncoded() throws IOException;


    public abstract byte[] getEncoded(String format) throws IOException;

    public abstract AlgorithmParameterSpec getParameterSpec(Class paramSpec)
            throws InvalidParameterSpecException;

    public abstract String toString();

}
