package de.flexiprovider.pqc.ots.lm;

import java.util.Vector;

import de.flexiprovider.common.math.polynomials.GFP32Polynomial;
import de.flexiprovider.common.util.IntUtils;


public class GFPVectorSerial {

    // for parsing to byte array <-> vector
    private int intDimension = 4;

    private Vector gfpVector;

    private byte[] byteArray;

    public GFPVectorSerial(byte[] b) {
        byteArray = b;

        gfpVector = parseToVector(b);
    }
    public GFPVectorSerial(Vector v) {
        gfpVector = v;
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

    private int[] byteToArray(byte[] b, int size) {
        int[] arr = new int[size];
        byte[] temp = new byte[intDimension];

        for (int i = size - 1; i >= 0; i--) {
            System.arraycopy(b, i * intDimension, temp, 0, intDimension);
            arr[i] = byteToInt(temp);
        }

        return arr;
    }

    private int byteToInt(byte[] b) {
        int k = 0;
        for (int i = 0; i < b.length; i++) {
            k |= (b[b.length - 1 - i] & 0xff) << (i << 3);
        }
        return k;
    }

    public byte[] getArrayRepresentation() {
        return byteArray;
    }

    public Vector getVectorRepresentation() {
        return gfpVector;
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

    private Vector parseToVector(byte[] b)
            throws ArrayIndexOutOfBoundsException {
        Vector v = new Vector();
        int size = b[0];
        if (size == 0) {
            return v;
        }
        v.setSize(size);
        int counter;

        byte[] temp = new byte[2];
        System.arraycopy(b, 1, temp, 0, 2);
        int fLength = byteToInt(temp) * intDimension;
        temp = new byte[fLength];
        // 1 for gfp length, 2 for f length
        counter = 3;
        System.arraycopy(b, counter, temp, 0, fLength);
        counter += fLength;
        int[] refF = byteToArray(temp, fLength / intDimension);

        temp = new byte[intDimension];
        System.arraycopy(b, counter, temp, 0, intDimension);
        counter += intDimension;

        int refP = byteToInt(temp);

        temp = new byte[2];
        System.arraycopy(b, counter, temp, 0, 2);
        counter += 2;

        int polyLength = byteToInt(temp) * intDimension;
        temp = new byte[polyLength];
        System.arraycopy(b, counter, temp, 0, polyLength);
        counter += polyLength;

        int[] refPoly = byteToArray(temp, polyLength / intDimension);

        v.setElementAt(new GFP32Polynomial(refF, refP, refPoly), 0);

        for (int i = 1; i < size; i++) {
            byte id = b[counter];
            counter += 1;
            int[] f = refF;
            int[] poly = refPoly;
            int p = refP;

            if (id == 0x01) {
                // standard procedure
                temp = new byte[2];
                System.arraycopy(b, counter, temp, 0, 2);
                counter += 2;
                polyLength = byteToInt(temp) * intDimension;

                temp = new byte[polyLength];
                System.arraycopy(b, counter, temp, 0, polyLength);
                counter += polyLength;

                poly = byteToArray(temp, polyLength / intDimension);
            } else {
                if ((id & 0x02) != 0) {
                    temp = new byte[2];
                    System.arraycopy(b, counter, temp, 0, 2);
                    counter += 2;
                    fLength = byteToInt(temp) * intDimension;

                    temp = new byte[fLength];
                    System.arraycopy(b, counter, temp, 0, fLength);
                    counter += fLength;

                    f = byteToArray(temp, fLength / intDimension);
                }
                if ((id & 0x04) != 0) {
                    temp = new byte[intDimension];
                    System.arraycopy(b, counter, temp, 0, intDimension);
                    counter += intDimension;

                    p = byteToInt(temp);
                }
                if ((id & 0x01) != 0) {
                    temp = new byte[2];
                    System.arraycopy(b, counter, temp, 0, 2);
                    counter += 2;
                    polyLength = byteToInt(temp) * intDimension;

                    temp = new byte[polyLength];
                    System.arraycopy(b, counter, temp, 0, polyLength);
                    counter += polyLength;

                    poly = byteToArray(temp, polyLength / intDimension);
                }
            }
            v.setElementAt(new GFP32Polynomial(f, p, poly), i);
        }

        return v;
    }

}
