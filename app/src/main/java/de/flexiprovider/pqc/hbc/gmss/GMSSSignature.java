package de.flexiprovider.pqc.hbc.gmss;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import de.flexiprovider.api.MessageDigest;
import de.flexiprovider.api.Registry;
import de.flexiprovider.api.SecureRandom;
import de.flexiprovider.api.Signature;
import de.flexiprovider.api.exceptions.InvalidAlgorithmParameterException;
import de.flexiprovider.api.exceptions.InvalidKeyException;
import de.flexiprovider.api.exceptions.NoSuchAlgorithmException;
import de.flexiprovider.api.exceptions.SignatureException;
import de.flexiprovider.api.keys.PrivateKey;
import de.flexiprovider.api.keys.PublicKey;
import de.flexiprovider.api.parameters.AlgorithmParameterSpec;
import de.flexiprovider.common.util.ByteUtils;
import de.flexiprovider.core.CoreRegistry;


public class GMSSSignature extends Signature {

    public static class GMSSwithSHA1 extends GMSSSignature {

        public static final String OID = GMSSKeyPairGenerator.GMSSwithSHA1.OID;

        public GMSSwithSHA1() {
            super("SHA1", "FlexiCore");
        }
    }

    public static class GMSSwithSHA224 extends GMSSSignature {


        public static final String OID = GMSSKeyPairGenerator.GMSSwithSHA224.OID;

        public GMSSwithSHA224() {
            super("SHA224", "FlexiCore");
        }
    }

    public static class GMSSwithSHA256 extends GMSSSignature {

        public static final String OID = GMSSKeyPairGenerator.GMSSwithSHA256.OID;

        public GMSSwithSHA256() {
            super("SHA256", "FlexiCore");
        }
    }

    public static class GMSSwithSHA384 extends GMSSSignature {


        public static final String OID = GMSSKeyPairGenerator.GMSSwithSHA384.OID;


        public GMSSwithSHA384() {
            super("SHA384", "FlexiCore");
        }
    }

    public static class GMSSwithSHA512 extends GMSSSignature {


        public static final String OID = GMSSKeyPairGenerator.GMSSwithSHA512.OID;

        public GMSSwithSHA512() {
            super("SHA512", "FlexiCore");
        }
    }


    private GMSSUtilities gmssUtil = new GMSSUtilities();

    private byte[] pubKeyBytes;

    private MessageDigest messDigestTrees;

    private int mdLength;


    private int numLayer;

    private MessageDigest messDigestOTS;

    private WinternitzOTSignature ots;

    private String[] algNames = new String[2];


    private int[] index;

    private byte[][][] currentAuthPaths;

    private byte[][] subtreeRootSig;

    private ByteArrayOutputStream baos;

    private GMSSParameterset gmssPS;

    private GMSSRandom gmssRandom;

    public GMSSSignature(String mdName, String mdProvName) {
        algNames[0] = mdName;
        algNames[1] = mdProvName;
        CoreRegistry.registerAlgorithms();

        // construct message digest
        try {
            messDigestTrees = Registry.getMessageDigest(mdName);
            messDigestOTS = messDigestTrees;
            mdLength = messDigestTrees.getDigestLength();
            gmssRandom = new GMSSRandom(messDigestTrees);
        } catch (NoSuchAlgorithmException nsae) {
            throw new RuntimeException("message digest " + mdName
                    + " not found in " + mdProvName + " or signature " + mdName
                    + " not found in " + mdProvName);
        }

    }

    public void update(byte data) throws SignatureException {
        baos.write(data);
    }


    public void update(byte[] data, int offset, int length)
            throws SignatureException {
        baos.write(data, offset, length);
    }

    private byte[] getData() {
        byte[] data = baos.toByteArray();

        try {
            baos.close();
        } catch (IOException ioe) {
            System.out.println("Can not close ByteArrayOutputStream");
        }
        baos.reset();
        return data;
    }

    private void initValues() {


        baos = new ByteArrayOutputStream();

    }

    public void initSign(PrivateKey privateKey, SecureRandom sr)
            throws InvalidKeyException {
        if (privateKey instanceof GMSSPrivateKey) {
            messDigestTrees.reset();
            initValues();
            GMSSPrivateKey gmssPrivateKey = (GMSSPrivateKey) privateKey;

            if (gmssPrivateKey.getIndex(0) >= gmssPrivateKey.getNumLeafs(0)) {
                throw new RuntimeException(
                        "No more signatures can be generated");
            }

            // get Parameterset
            this.gmssPS = gmssPrivateKey.getParameterset();
            // get numLayer
            this.numLayer = gmssPS.getNumOfLayers();

            // get OTS Instance of lowest layer
            byte[] seed = gmssPrivateKey.getCurrentSeeds()[numLayer - 1];
            byte[] OTSSeed;
            byte[] dummy = new byte[mdLength];
            System.arraycopy(seed, 0, dummy, 0, mdLength);
            OTSSeed = gmssRandom.nextSeed(dummy); // secureRandom.nextBytes(currentSeeds[currentSeeds.length-1]);secureRandom.nextBytes(OTSseed);
            this.ots = new WinternitzOTSignature(OTSSeed, algNames, gmssPS
                    .getWinternitzParameter()[numLayer - 1]);

            byte[][][] helpCurrentAuthPaths = gmssPrivateKey
                    .getCurrentAuthPaths();
            currentAuthPaths = new byte[numLayer][][];

            // copy the main tree authentication path
            for (int j = 0; j < numLayer; j++) {
                currentAuthPaths[j] = new byte[helpCurrentAuthPaths[j].length][mdLength];
                for (int i = 0; i < helpCurrentAuthPaths[j].length; i++) {
                    System.arraycopy(helpCurrentAuthPaths[j][i], 0,
                            currentAuthPaths[j][i], 0, mdLength);
                }
            }

            // copy index
            index = new int[numLayer];
            System.arraycopy(gmssPrivateKey.getIndex(), 0, index, 0, numLayer);

            // copy subtreeRootSig
            byte[] helpSubtreeRootSig;
            subtreeRootSig = new byte[numLayer - 1][];
            for (int i = 0; i < numLayer - 1; i++) {
                helpSubtreeRootSig = gmssPrivateKey.getSubtreeRootSig(i);
                subtreeRootSig[i] = new byte[helpSubtreeRootSig.length];
                System.arraycopy(helpSubtreeRootSig, 0, subtreeRootSig[i], 0,
                        helpSubtreeRootSig.length);
            }

            // change private key for next signature
            gmssPrivateKey.nextKey(numLayer - 1);

        } else
            throw new InvalidKeyException("Key is not a GMSSPrivateKey.");
    }

    public byte[] sign() throws SignatureException {

        byte[] message;
        byte[] otsSig;
        byte[] authPathBytes;
        byte[] indexBytes;

        // --- first part of this signature
        // get the data which should be signed
        message = getData();

        otsSig = ots.getSignature(message);

        // get concatenated lowest layer tree authentication path
        authPathBytes = gmssUtil
                .concatenateArray(currentAuthPaths[numLayer - 1]);

        // put lowest layer index into a byte array
        indexBytes = gmssUtil.intToBytesLittleEndian(index[numLayer - 1]);

        // create first part of GMSS signature
        byte[] gmssSigFirstPart = new byte[indexBytes.length + otsSig.length
                + authPathBytes.length];
        System.arraycopy(indexBytes, 0, gmssSigFirstPart, 0, indexBytes.length);
        System.arraycopy(otsSig, 0, gmssSigFirstPart, indexBytes.length,
                otsSig.length);
        System.arraycopy(authPathBytes, 0, gmssSigFirstPart,
                (indexBytes.length + otsSig.length), authPathBytes.length);
        // --- end first part

        // --- next parts of the signature
        // create initial array with length 0 for iteration
        byte[] gmssSigNextPart = new byte[0];

        for (int i = numLayer - 1 - 1; i >= 0; i--) {

            // get concatenated next tree authentication path
            authPathBytes = gmssUtil.concatenateArray(currentAuthPaths[i]);

            // put next tree index into a byte array
            indexBytes = gmssUtil.intToBytesLittleEndian(index[i]);

            // create next part of GMSS signature

            // create help array and copy actual gmssSig into it
            byte[] helpGmssSig = new byte[gmssSigNextPart.length];
            System.arraycopy(gmssSigNextPart, 0, helpGmssSig, 0,
                    gmssSigNextPart.length);
            // adjust length of gmssSigNextPart for adding next part
            gmssSigNextPart = new byte[helpGmssSig.length + indexBytes.length
                    + subtreeRootSig[i].length + authPathBytes.length];

            // copy old data (help array) and new data in gmssSigNextPart
            System.arraycopy(helpGmssSig, 0, gmssSigNextPart, 0,
                    helpGmssSig.length);
            System.arraycopy(indexBytes, 0, gmssSigNextPart,
                    helpGmssSig.length, indexBytes.length);
            System.arraycopy(subtreeRootSig[i], 0, gmssSigNextPart,
                    (helpGmssSig.length + indexBytes.length),
                    subtreeRootSig[i].length);
            System
                    .arraycopy(
                            authPathBytes,
                            0,
                            gmssSigNextPart,
                            (helpGmssSig.length + indexBytes.length + subtreeRootSig[i].length),
                            authPathBytes.length);

        }
        // --- end next parts

        // concatenate the two parts of the GMSS signature
        byte[] gmssSig = new byte[gmssSigFirstPart.length
                + gmssSigNextPart.length];
        System.arraycopy(gmssSigFirstPart, 0, gmssSig, 0,
                gmssSigFirstPart.length);
        System.arraycopy(gmssSigNextPart, 0, gmssSig, gmssSigFirstPart.length,
                gmssSigNextPart.length);

        // return the GMSS signature
        return gmssSig;
    }

    /**
     * Initializes the signature algorithm for verifying a signature.
     *
     * @param publicKey the public key of the signer.
     * @throws de.flexiprovider.api.exceptions.InvalidKeyException if the public key is not an instance of GMSSPublicKey.
     */
    public void initVerify(PublicKey publicKey) throws InvalidKeyException {
        if (publicKey instanceof GMSSPublicKey) {
            messDigestTrees.reset();

            GMSSPublicKey gmssPublicKey = (GMSSPublicKey) publicKey;
            pubKeyBytes = gmssPublicKey.getPublicKeyBytes();
            gmssPS = gmssPublicKey.getParameterset();
            // get numLayer
            this.numLayer = gmssPS.getNumOfLayers();
            initValues();

        } else
            throw new InvalidKeyException("Key is not a GMSSPublicKey");
    }

    public boolean verify(byte[] signature) throws SignatureException {

        boolean success = false;
        // int halfSigLength = signature.length >>> 1;
        messDigestOTS.reset();
        WinternitzOTSVerify otsVerify;
        int otsSigLength;

        // get the message that was signed
        byte[] help = getData();

        byte[] message;
        byte[] otsSig;
        byte[] otsPublicKey;
        byte[][] authPath;
        byte[] dest;
        int nextEntry = 0;
        int index;
        // Verify signature

        // --- begin with message = 'message that was signed'
        // and then in each step message = subtree root
        for (int j = numLayer - 1; j >= 0; j--) {
            otsVerify = new WinternitzOTSVerify(algNames, gmssPS
                    .getWinternitzParameter()[j]);
            otsSigLength = otsVerify.getSignatureLength();

            message = help;
            // get the subtree index
            index = gmssUtil.bytesToIntLittleEndian(signature, nextEntry);

            // 4 is the number of bytes in integer
            nextEntry += 4;

            // get one-time signature
            otsSig = new byte[otsSigLength];
            System.arraycopy(signature, nextEntry, otsSig, 0, otsSigLength);
            nextEntry += otsSigLength;

            // compute public OTS key from the one-time signature
            otsPublicKey = otsVerify.Verify(message, otsSig);

            // test if OTSsignature is correct
            if (otsPublicKey == null) {
                System.err
                        .println("OTS Public Key is null in GMSSSignature.verify");
                return false;
            }

            // get authentication path from the signature
            authPath = new byte[gmssPS.getHeightOfTrees()[j]][mdLength];
            for (int i = 0; i < authPath.length; i++) {
                System
                        .arraycopy(signature, nextEntry, authPath[i], 0,
                                mdLength);
                nextEntry = nextEntry + mdLength;
            }

            // compute the root of the subtree from the authentication path
            help = new byte[mdLength];

            help = otsPublicKey;

            int count = 1 << authPath.length;
            count = count + index;

            for (int i = 0; i < authPath.length; i++) {
                dest = new byte[mdLength << 1];

                if ((count % 2) == 0) {
                    System.arraycopy(help, 0, dest, 0, mdLength);
                    System.arraycopy(authPath[i], 0, dest, mdLength, mdLength);
                    count = count / 2;
                } else {
                    System.arraycopy(authPath[i], 0, dest, 0, mdLength);
                    System.arraycopy(help, 0, dest, mdLength, help.length);
                    count = (count - 1) / 2;
                }
                messDigestTrees.update(dest);
                help = messDigestTrees.digest();
            }
        }

        // now help contains the root of the maintree

        // test if help is equal to the GMSS public key
        if (ByteUtils.equals(pubKeyBytes, help)) {
            success = true;
        }

        return success;
    }


    public void setParameters(AlgorithmParameterSpec algParamSpec)
            throws InvalidAlgorithmParameterException {
        if (algParamSpec == null) {
            return;
        }

        if (!(algParamSpec instanceof GMSSParameterSpec)) {
            throw new InvalidAlgorithmParameterException(
                    "in GMSSSignature: initialize: params is not "
                            + "an instance of GMSSParameterSpec");
        }
    }

}