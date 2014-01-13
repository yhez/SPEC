package de.flexiprovider.pqc.hbc.ots;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;

import de.flexiprovider.api.MessageDigest;
import de.flexiprovider.common.math.FlexiBigInt;
import de.flexiprovider.common.math.IntegerFunctions;
import de.flexiprovider.common.util.ByteUtils;
import de.flexiprovider.pqc.hbc.PRNG;

public class BiBaOTS implements OTS {

    private MessageDigest md;

    private PRNG rng;

    private int mdSize;

    private byte[][] privKeyBytes;

    private byte[] pubKeyBytes;

    private int collisionSize;

    private int numberOfBins;

    private int numberOfSeals;

    private int sealLength;

    private boolean initialized;

    public BiBaOTS() {
        collisionSize = 11;
        numberOfBins = 260;
        numberOfSeals = 1024;
        sealLength = 0; // will be initialized later
        initialized = false;
    }

    public void init(MessageDigest md, PRNG rng) {
        this.md = md;
        this.rng = rng;
        mdSize = md.getDigestLength();

        // sealLength must be (for security reasons) at least as much as the
        // length of the message digest in byzes
        if (this.sealLength < mdSize)
            this.sealLength = mdSize;
        initialized = true;
    }

    public boolean canComputeVerificationKeyFromSignature() {
        return false;
    }

    public byte[] computeVerificationKey(byte[] bytes, byte[] sigBytes) {
        return null;
    }


    public void generateKeyPair(byte[] seed) {
        generateSignatureKey(seed);
        generateVerificationKey();
    }

    public void generateSignatureKey(byte[] seed) {
        if (!initialized) {
            System.err.println("OTS has not been initialized yet");
            return;
        }
        privKeyBytes = new byte[numberOfSeals][sealLength];
        // hash table to check for equal seals
        HashSet sealTable = new HashSet();
        byte[] temp; // store key bytes temporary
        Integer arrayHash; // hashCode() of the temp key Array
        for (int i = 0; i < numberOfSeals; i++) {
            // create seals
            temp = new byte[sealLength];
            System.arraycopy(rng.nextSeed(seed), 0, temp, 0, sealLength);
            arrayHash = Integer.valueOf(ByteUtils.deepHashCode(temp));
            // if seal already exists skip it and create a new one
            if (!sealTable.add(arrayHash)) {
                i--;
            } else {
                privKeyBytes[i] = temp;
            }
        }

    }
    public void generateVerificationKey() {
        if (!initialized) {
            System.err.println("OTS has not been initialized yet");
            return;
        }
        if (privKeyBytes == null) {
            System.err.println("No private key available. "
                    + "For generating the public key there must be a "
                    + "valid private key. Please generate first the "
                    + "private key.");
            return;
        }
        pubKeyBytes = new byte[numberOfSeals * sealLength];
        for (int i = 0; i < privKeyBytes.length; i++) {
            // create public key
            System.arraycopy(md.digest(privKeyBytes[i]), 0, pubKeyBytes, i
                    * sealLength, sealLength);
        }
    }

    public int getSignatureLength() {
        // FIXME: this length cannot be guaranteed, but fixed length is needed
        // by cmss. what to do?
        return collisionSize * sealLength + 1;
    }

    public byte[] getVerificationKey() {
        return pubKeyBytes;
    }

    public int getVerificationKeyLength() {
        // if (pubKeyBytes == null)
        // return 0;
        // return pubKeyBytes.length;
        return numberOfSeals * sealLength;
    }

    public byte[] sign(byte[] message) {

        // create variables needed during calculation
        FlexiBigInt counter = new FlexiBigInt("0");
        boolean collisionFound = false;
        byte[] hash;
        byte[] signature = null;
        MessageDigest familyDigest;
        try {
            familyDigest = md.getClass().newInstance();
        } catch (InstantiationException e) {
            familyDigest = null;
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            familyDigest = null;
            e.printStackTrace();
        }

        // array for the seal hash values
        byte[][] sealHashes = new byte[privKeyBytes.length][familyDigest
                .getDigestLength()];
        int binIndex;
        Integer bin;
        Hashtable findCollisions;
        Vector vect;
        byte[] counterBytes, tempArray;

        md.reset();
        while (!collisionFound) {
            // Get the initial state of the digest with the original message

            // 1. choose hashfunction G_h out of function family G
            md.update(message);
            md.update(counter.toByteArray());
            hash = md.digest();

            // 2. calculate all the hash values for the seals (G_h(s_i))
            for (int i = 0; i < privKeyBytes.length; i++) {
                familyDigest.reset();
                familyDigest.update(hash);
                familyDigest.update(privKeyBytes[i]);
                sealHashes[i] = familyDigest.digest();
            }

            // 3. search for collisions

            // sort the hash values of the seals into the bins and check if a
            // k-way-collision was found
            findCollisions = new Hashtable();
            binIndex = -1;
            // for all hash values...
            for (int i = 0; i < privKeyBytes.length && !collisionFound; i++) {
                // calculate bin number
                bin = Integer.valueOf(calcBin(sealHashes[i]));

                // for each bin which contains seals create an vector and put
                // the current seal in it
                if (findCollisions.get(bin) != null) {
                    vect = (Vector) findCollisions.get(bin);
                    vect.addElement(Integer.valueOf(i));
                    findCollisions.put(bin, vect);
                } else {
                    // vect = new Vector<Integer>();
                    vect = new Vector();
                    vect.addElement(Integer.valueOf(i));
                    findCollisions.put(bin, vect);
                }

                // 4. if a k-way collision was found stop searching for
                // collisions -> signature is found
                if (vect.size() == collisionSize) {
                    collisionFound = true;
                    binIndex = bin.intValue();
                }
            }

            // if all seal hashes are sorted into bins and no k-way collision is
            // found increase the counter and try again
            if (!collisionFound)
                counter = counter.add(FlexiBigInt.ONE);
            else {
                // else build the signature byte array from the k-way collision
                counterBytes = counter.toByteArray();
                signature = new byte[collisionSize * sealLength
                        + counterBytes.length];
                vect = (Vector) findCollisions.get(Integer.valueOf(binIndex));
                for (int i = 0; i < collisionSize; i++) {
                    tempArray = new byte[sealLength];
                    tempArray = privKeyBytes[((Integer) vect.elementAt(i))
                            .intValue()];

                    for (int j = 0; j < sealLength; j++)
                        signature[i * sealLength + j] = tempArray[j];
                }
                // final signature of length k*seal-length bytes + bytes for
                // counter (usually bytes for counter are 1 byte)
                for (int i = 0; i < counterBytes.length; i++)
                    signature[collisionSize * sealLength + i] = counterBytes[i];
            }

        }

        return signature;

    }

    public boolean verify(byte[] mBytes, byte[] sBytes, byte[] pBytes) {
        // Check the parameters
        if (mBytes == null || sBytes == null || pBytes == null
                & pubKeyBytes == null) {
            return false;
        }
        // get the verification key
        pubKeyBytes = pBytes == null ? pubKeyBytes : pBytes;

        MessageDigest sealDigest = null;
        MessageDigest familyDigest = null;
        try {
            familyDigest = md.getClass().newInstance();
            sealDigest = md.getClass().newInstance();
        } catch (InstantiationException e) {

            e.printStackTrace();
        } catch (IllegalAccessException e) {

            e.printStackTrace();
        }

        // extract the single seals from signature array
        byte[][] seals = new byte[collisionSize][sealLength];
        for (int i = 0; i < collisionSize; i++)
            System.arraycopy(sBytes, i * sealLength, seals[i], 0, sealLength);

        // check if all seals are distinct
        for (int i = 0; i < collisionSize; i++)
            for (int j = i + 1; j < collisionSize; j++)
                if (ByteUtils.equals(seals[i], seals[j])) {
                    System.out.println("Two seals in the signature are the "
                            + "same.");
                    return false;
                }

        // check if all seals are authentic (seal hash compared with public key)
        // must be the same algorithm which the seals were hashed with
        byte[] currentSealHash = new byte[sealDigest.getDigestLength()];
        for (int i = 0; i < collisionSize; i++) {
            currentSealHash = sealDigest.digest(seals[i]);

            if (!isPartOfKey(currentSealHash)) {
                System.out.println("The hash value of a seal is not contained"
                        + " in the public key.");
                return false;
            }
        }

        // calculate hash value of the message and counter
        byte[] hash;
        md.update(mBytes);
        // counter
        int counterLength = sBytes.length - collisionSize * sealLength;
        byte[] counter = new byte[counterLength];
        for (int i = 0; i < counterLength; i++)
            counter[i] = sBytes[collisionSize * sealLength + i];
        md.update(counter);
        hash = md.digest();

        // calculate all the hash values for the seals (G_h(s_i))
        byte[] sealValue = new byte[familyDigest.getDigestLength()];
        int bin = 0;
        // for each seal in the signature...
        for (int i = 0; i < collisionSize; i++) {
            // calculate the hash value
            familyDigest.reset();
            familyDigest.update(hash);
            familyDigest.update(seals[i]);
            sealValue = familyDigest.digest();
            // first seal appoints correct bin, all other seals must map into
            // the same bin
            if (i == 0)
                bin = calcBin(sealValue);
            else if (bin != calcBin(sealValue)) {
                System.out.println("The seals are not mapped into the"
                        + " same bin.");
                return false;
            }
        }

        // signature passed all tests and is correct
        // System.out.println("Verification passed");
        return true;
    }


    private int calcBin(byte[] seal) {
        int binbytes = (int) Math
                .ceil(Math.log(numberOfBins) / Math.log(2) / 8);
        FlexiBigInt binNumber = IntegerFunctions.octetsToInteger(seal,
                seal.length - binbytes, binbytes);
        return binNumber.intValue() % numberOfBins;
    }

    private boolean isPartOfKey(byte[] sealHash) {
        byte[] temp = new byte[mdSize];
        for (int i = 0; i < pubKeyBytes.length; i += mdSize) {
            System.arraycopy(pubKeyBytes, i, temp, 0, mdSize);
            if (ByteUtils.equals(sealHash, temp))
                return true;
        }
        return false;
    }

}
