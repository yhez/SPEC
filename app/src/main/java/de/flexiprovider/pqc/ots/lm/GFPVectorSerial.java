package de.flexiprovider.pqc.ots.lm;

import java.util.Vector;

import de.flexiprovider.common.math.polynomials.GFP32Polynomial;
import de.flexiprovider.common.util.IntUtils;


public class GFPVectorSerial {

    // for parsing to byte array <-> vector
    private int intDimension = 4;

    private byte[] byteArray;

    public GFPVectorSerial(Vector v) {
        byteArray = parseToByteArray(v);
    }

    private byte[] append(byte[] b1, byte[] b2) {
        byte[] b = new byte[b1.length + b2.length];
        System.arraycopy(b1, 0, b, 0, b1.length);
        System.arraycopy(b2, 0, b, b1.length, b2.length);
        return b;
    }

    private byte[] arrayToByte(int[] arr) {
        byte[] b = intToByte(arr.length, 2);
        for (int i = 0; i < arr.length; i++) {
            b = append(b, intToByte(arr[i], intDimension));
        }
        return b;
    }

    // /**
    // * Method for dynamic int sizes (currently unused)
    // * @param i
    // * @return
    // */
    // private byte[] intToByte(int i) {
    // int size = (int) Math.ceil((i + 1.) / 256);
    // int sizeSize = (int) Math.ceil((size + 1.) / 256);
    // byte[] b = new byte[size + sizeSize];
    // byte[] sizeData = intToByte(size, sizeSize);
    // byte[] intData = intToByte(i, size);
    // System.arraycopy(sizeData, 0, b, 0, sizeSize);
    // System.arraycopy(intData, 0, b, sizeSize, size);
    // return b;
    // }

    public byte[] getArrayRepresentation() {
        return byteArray;
    }

    private byte[] gfpToByte(GFP32Polynomial gfp, GFP32Polynomial compare) {
        byte[] b = new byte[1];
        if (gfp.equals(compare)) {
            return b;
        }
        if (gfp.paramEqual(compare)) {
            // standard procedure
            b[0] = 0x01;
            return append(b, arrayToByte(gfp.getPoly()));
        }
        if (!IntUtils.equals(gfp.getF(), compare.getF())) {
            b[0] += 0x02;
            b = append(b, arrayToByte(gfp.getF()));
        }
        if (gfp.getP() != compare.getP()) {
            b[0] += 0x04;
            b = append(b, intToByte(gfp.getP(), intDimension));
        }
        if (!IntUtils.equals(gfp.getPoly(), compare.getPoly())) {
            b[0] += 0x01;
            b = append(b, arrayToByte(gfp.getPoly()));
        }
        return b;
    }

    private byte[] intToByte(int i, int size) {
        byte[] data = new byte[size];
        for (int j = 0; j < size; j++) {
            int shift = j << 3;
            data[size - 1 - j] = (byte) ((i & 0xff << shift) >>> shift);
        }
        return data;
    }

    private byte[] parseToByteArray(Vector v) {
        int size = v.size();
        byte[] b = new byte[]{(byte) size};
        if (size == 0) {
            return b;
        }

        // first element gets special treatment
        GFP32Polynomial gfp = (GFP32Polynomial) v.elementAt(0);
        b = append(b, arrayToByte(gfp.getF()));
        b = append(b, intToByte(gfp.getP(), intDimension));
        b = append(b, arrayToByte(gfp.getPoly()));

        for (int i = 1; i < size; i++) {
            b = append(b, gfpToByte((GFP32Polynomial) v.elementAt(i), gfp));
        }

        return b;
    }

}
