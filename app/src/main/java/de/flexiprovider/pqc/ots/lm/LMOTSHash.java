package de.flexiprovider.pqc.ots.lm;

import java.io.IOException;
import java.util.Vector;

import codec.asn1.ASN1Exception;
import de.flexiprovider.common.math.polynomials.GFP32Polynomial;


public class LMOTSHash {

    private int m;
    private Vector a;

    public LMOTSHash(byte[] encoded) {
        GFPVectorSerial serial = new GFPVectorSerial(encoded);
        a = serial.getVectorRepresentation();
        m = a.size();
    }


    public LMOTSHash(Vector a) {
        this.a = a;
        m = a.size();
    }


    public GFP32Polynomial calculatHash(Vector vec) {
        Vector intermediateResult = new Vector();

        for (int i = m; i > 0; i--) {
            intermediateResult.addElement(((GFP32Polynomial) a.elementAt(i - 1)).multiply((GFP32Polynomial) vec
                    .elementAt(i - 1)));
        }

        return elementSum(intermediateResult);
    }

    private GFP32Polynomial elementSum(Vector v) {
        int size = v.size();
        GFP32Polynomial result = (GFP32Polynomial) v.elementAt(size - 1);

        for (int j = size - 1; j > 0; j--) {
            result.addToThis((GFP32Polynomial) v.elementAt(j - 1));
        }

        return result;
    }

    // TODO: only for testing
    public Vector getA() {
        return a;
    }

    public byte[] getEncoded() throws ASN1Exception, IOException {
        GFPVectorSerial serial = new GFPVectorSerial(a);

        return serial.getArrayRepresentation();
    }
}
