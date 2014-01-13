package de.flexiprovider.pqc.hbc.gmss;

/**
 * This class provides several methods that are required by the GMSS classes.
 *
 * @author Elena Klintsevich
 */
public class GMSSUtilities {

    /**
     * Converts a 32 bit integer into a byte array beginning at
     * <code>offset</code> (little-endian representation)
     *
     * @param value the integer to convert
     */
    public byte[] intToBytesLittleEndian(int value) {
        byte[] bytes = new byte[4];

        bytes[0] = (byte) ((value) & 0xff);
        bytes[1] = (byte) ((value >> 8) & 0xff);
        bytes[2] = (byte) ((value >> 16) & 0xff);
        bytes[3] = (byte) ((value >> 24) & 0xff);
        return bytes;
    }

    /**
     * Converts a byte array beginning at <code>offset</code> into a 32 bit
     * integer (little-endian representation)
     *
     * @param bytes  the byte array
     * @param offset the integer offset into the byte array
     * @return The resulting integer
     */
    public int bytesToIntLittleEndian(byte[] bytes, int offset) {
        return ((bytes[offset++] & 0xff)) | ((bytes[offset++] & 0xff) << 8)
                | ((bytes[offset++] & 0xff) << 16)
                | ((bytes[offset] & 0xff)) << 24;
    }

    /**
     * This method concatenates a 2-dimensional byte array into a 1-dimensional
     * byte array
     *
     * @param arraycp a 2-dimensional byte array.
     * @return 1-dimensional byte array with concatenated input array
     */
    public byte[] concatenateArray(byte[][] arraycp) {
        byte[] dest = new byte[arraycp.length * arraycp[0].length];
        int indx = 0;
        for (int i = 0; i < arraycp.length; i++) {
            System.arraycopy(arraycp[i], 0, dest, indx, arraycp[i].length);
            indx = indx + arraycp[i].length;
        }
        return dest;
    }


    /**
     * This Method calculates the actual index of the tree in level <code>height</code> from the global index byte array <code>glIndex</code>
     *
     * @param glIndex
     *            global Index byte array
     * @param height
     * 			  height of resulting tree index
     *
     * @return the actual index of the tree at level <code>height</code>
     *
     *
     */
 /*   
    public int[] calcIndex(byte[] glIndex,int[] heightOfTrees){
    	int[] index;
    	byte[]temp;
    	//index[n]=globalIndex MOD numleafs[n]
    	System.arraycopy(glIndex, 0, , temp, glIndex.length - heightOfTrees)
    	index[n]=
    	
    	
    	return index;
    }*/
}
