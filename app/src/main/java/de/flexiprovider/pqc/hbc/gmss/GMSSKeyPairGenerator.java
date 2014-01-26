package de.flexiprovider.pqc.hbc.gmss;

import java.util.Vector;

import de.flexiprovider.api.MessageDigest;
import de.flexiprovider.api.Registry;
import de.flexiprovider.api.SecureRandom;
import de.flexiprovider.api.exceptions.InvalidAlgorithmParameterException;
import de.flexiprovider.api.exceptions.NoSuchAlgorithmException;
import de.flexiprovider.api.exceptions.SignatureException;
import de.flexiprovider.api.keys.KeyPair;
import de.flexiprovider.api.keys.KeyPairGenerator;
import de.flexiprovider.api.parameters.AlgorithmParameterSpec;
import de.flexiprovider.core.CoreRegistry;


public class GMSSKeyPairGenerator extends KeyPairGenerator {
    /*
     * Inner classes providing concrete implementations of GMSSKeyPairGenerator
	 * with a variety of message digests.
	 */

    // //////////////////////////////////////////////////////////////////////////////

    /**
     * The source of randomness for OTS private key generation
     */
    private GMSSRandom gmssRandom;

    /**
     * An array of the seeds for the PRGN (for main tree, and all current
     * subtrees)
     */
    private byte[][] currentSeeds;

    /**
     * An array of seeds for the PRGN (for all subtrees after next)
     */
    private byte[][] nextNextSeeds;

    /**
     * An array of the RootSignatures
     */
    private byte[][] currentRootSigs;

    private String[] algNames = new String[2];

    /**
     * The length of the seed for the PRNG
     */
    private int mdLength;

    /**
     * the number of Layers
     */
    private int numLayer;

    /**
     * Flag indicating if the class already has been initialized
     */
    private boolean initialized = false;

    /**
     * Instance of GMSSParameterset
     */
    private GMSSParameterset gmssPS;

    /**
     * An array of the heights of the authentication trees of each layer
     */
    private int[] heightOfTrees;

    /**
     * An array of the Winternitz parameter 'w' of each layer
     */
    private int[] otsIndex;

    /**
     * The parameter K needed for the authentication path computation
     */
    private int[] K;


    public GMSSKeyPairGenerator(String mdName, String mdProvName) {
        String errorMsg;

        CoreRegistry.registerAlgorithms();
        try {
            /*
      The hash function used for the construction of the authentication trees
     */
            MessageDigest messDigestTree = Registry.getMessageDigest(mdName);
            algNames[0] = mdName;

            // set mdLength
            this.mdLength = messDigestTree.getDigestLength();
            // construct randomizer
            this.gmssRandom = new GMSSRandom(messDigestTree);

            return;
        } catch (NoSuchAlgorithmException nsae) {
            errorMsg = "message digest " + mdName + " not found in "
                    + mdProvName + " or key pair generator " + mdName
                    + " not found in " + mdProvName;
        }
        throw new RuntimeException("GMSSKeyPairGenerator error: " + errorMsg);

    }

    /**
     * Generates the GMSS key pair. The public key is an instance of
     * GMSSPublicKey, the private key is an instance of GMSSPrivateKey.
     *
     * @return Key pair containing a GMSSPublicKey and a GMSSPrivateKey
     * @see de.flexiprovider.pqc.hbc.gmss.GMSSPrivateKey
     * @see de.flexiprovider.pqc.hbc.gmss.GMSSPublicKey
     */
    public KeyPair genKeyPair() {
        if (!initialized)
            initializeDefault();

        // initialize authenticationPaths and treehash instances
        byte[][][] currentAuthPaths = new byte[numLayer][][];
        byte[][][] nextAuthPaths = new byte[numLayer - 1][][];
        Treehash[][] currentTreehash = new Treehash[numLayer][];
        Treehash[][] nextTreehash = new Treehash[numLayer - 1][];

        Vector[] currentStack = new Vector[numLayer];
        Vector[] nextStack = new Vector[numLayer - 1];

        Vector[][] currentRetain = new Vector[numLayer][];
        Vector[][] nextRetain = new Vector[numLayer - 1][];

        for (int i = 0; i < numLayer; i++) {
            currentAuthPaths[i] = new byte[heightOfTrees[i]][mdLength];
            currentTreehash[i] = new Treehash[heightOfTrees[i] - K[i]];

            if (i > 0) {
                nextAuthPaths[i - 1] = new byte[heightOfTrees[i]][mdLength];
                nextTreehash[i - 1] = new Treehash[heightOfTrees[i] - K[i]];
            }

            currentStack[i] = new Vector();
            if (i > 0)
                nextStack[i - 1] = new Vector();
        }

        // initialize roots
        byte[][] currentRoots = new byte[numLayer][mdLength];
        byte[][] nextRoots = new byte[numLayer - 1][mdLength];
        // initialize seeds
        byte[][] seeds = new byte[numLayer][mdLength];
        // initialize seeds[] by copying starting-seeds of first trees of each
        // layer
        for (int i = 0; i < numLayer; i++) {
            System.arraycopy(currentSeeds[i], 0, seeds[i], 0, mdLength);
        }

        // initialize rootSigs
        currentRootSigs = new byte[numLayer - 1][mdLength];

        // -------------------------
        // -------------------------
        // --- calculation of current authpaths and current rootsigs (AUTHPATHS,
        // SIG)------
        // from bottom up to the root
        for (int h = numLayer - 1; h >= 0; h--) {
            GMSSRootCalc tree = new GMSSRootCalc(this.heightOfTrees[h],
                    this.K[h], this.algNames);
            try {
                // on lowest layer no lower root is available, so just call
                // the method with null as first parameter
                if (h == numLayer - 1)
                    tree = this.generateCurrentAuthpathAndRoot(null,
                            currentStack[h], seeds[h], h);
                else
                    // otherwise call the method with the former computed root
                    // value
                    tree = this.generateCurrentAuthpathAndRoot(
                            currentRoots[h + 1], currentStack[h], seeds[h], h);

            } catch (SignatureException e1) {
                e1.printStackTrace();
            }

            // set initial values needed for the private key construction
            for (int i = 0; i < heightOfTrees[h]; i++) {
                System.arraycopy(tree.getAuthPath()[i], 0,
                        currentAuthPaths[h][i], 0, mdLength);
            }
            currentRetain[h] = tree.getRetain();
            currentTreehash[h] = tree.getTreehash();
            System.arraycopy(tree.getRoot(), 0, currentRoots[h], 0, mdLength);
        }

        // --- calculation of next authpaths and next roots (AUTHPATHS+, ROOTS+)
        // ------
        for (int h = numLayer - 2; h >= 0; h--) {
            GMSSRootCalc tree = new GMSSRootCalc(this.heightOfTrees[h + 1],
                    this.K[h + 1], this.algNames);

            tree = this.generateNextAuthpathAndRoot(nextStack[h], seeds[h + 1],
                    h + 1);

            // set initial values needed for the private key construction
            for (int i = 0; i < heightOfTrees[h + 1]; i++) {
                System.arraycopy(tree.getAuthPath()[i], 0, nextAuthPaths[h][i],
                        0, mdLength);
            }
            nextRetain[h] = tree.getRetain();
            nextTreehash[h] = tree.getTreehash();
            System.arraycopy(tree.getRoot(), 0, nextRoots[h], 0, mdLength);

            // create seed for the Merkle tree after next (nextNextSeeds)
            // SEEDs++
            System.arraycopy(seeds[h + 1], 0, this.nextNextSeeds[h], 0,
                    mdLength);
        }
        // ------------

        // generate GMSSPublicKey
        GMSSPublicKey publicKey = new GMSSPublicKey(currentRoots[0], gmssPS);

        // generate the GMSSPrivateKey
        GMSSPrivateKey privateKey = new GMSSPrivateKey(currentSeeds,
                nextNextSeeds, currentAuthPaths, nextAuthPaths,
                currentTreehash, nextTreehash, currentStack, nextStack,
                currentRetain, nextRetain, nextRoots, currentRootSigs, gmssPS,
                algNames);

        // return the KeyPair
        return (new KeyPair(publicKey, privateKey));
    }


    private GMSSRootCalc generateCurrentAuthpathAndRoot(byte[] lowerRoot,
                                                        Vector currentStack, byte[] seed, int h) throws SignatureException {
        byte[] help;

        byte[] OTSseed;
        OTSseed = gmssRandom.nextSeed(seed);

        WinternitzOTSignature ots;

        // data structure that constructs the whole tree and stores
        // the initial values for treehash, Auth and retain
        GMSSRootCalc treeToConstruct = new GMSSRootCalc(this.heightOfTrees[h],
                this.K[h], this.algNames);

        treeToConstruct.initialize(currentStack);

        // generate the first leaf
        if (h == numLayer - 1) {
            ots = new WinternitzOTSignature(OTSseed, algNames, otsIndex[h]);
            help = ots.getPublicKey();
        } else {
            // for all layers except the lowest, generate the signature of the
            // underlying root
            // and reuse this signature to compute the first leaf of acual layer
            // more efficiently (by verifiing the signature)
            ots = new WinternitzOTSignature(OTSseed, algNames, otsIndex[h]);
            currentRootSigs[h] = ots.getSignature(lowerRoot);
            WinternitzOTSVerify otsver = new WinternitzOTSVerify(algNames,
                    otsIndex[h]);
            help = otsver.Verify(lowerRoot, currentRootSigs[h]);
        }
        // update the tree with the first leaf
        treeToConstruct.update(help);

        int seedForTreehashIndex = 3;
        int count = 0;

        // update the tree 2^(H) - 1 times, from the second to the last leaf
        for (int i = 1; i < (1 << this.heightOfTrees[h]); i++) {
            // initialize the seeds for the leaf generation with index 3 * 2^h
            if (i == seedForTreehashIndex
                    && count < this.heightOfTrees[h] - this.K[h]) {
                treeToConstruct.initializeTreehashSeed(seed, count);
                seedForTreehashIndex *= 2;
                count++;
            }

            OTSseed = gmssRandom.nextSeed(seed);
            ots = new WinternitzOTSignature(OTSseed, algNames, otsIndex[h]);
            treeToConstruct.update(ots.getPublicKey());
        }

        if (treeToConstruct.wasFinished()) {
            return treeToConstruct;
        }
        System.err.println("Baum noch nicht fertig konstruiert!!!");
        return null;
    }

    private GMSSRootCalc generateNextAuthpathAndRoot(Vector nextStack,
                                                     byte[] seed, int h) {
        byte[] OTSseed;
        WinternitzOTSignature ots;

        // data structure that constructs the whole tree and stores
        // the initial values for treehash, Auth and retain
        GMSSRootCalc treeToConstruct = new GMSSRootCalc(this.heightOfTrees[h],
                this.K[h], this.algNames);
        treeToConstruct.initialize(nextStack);

        int seedForTreehashIndex = 3;
        int count = 0;

        // update the tree 2^(H) times, from the first to the last leaf
        for (int i = 0; i < (1 << this.heightOfTrees[h]); i++) {
            // initialize the seeds for the leaf generation with index 3 * 2^h
            if (i == seedForTreehashIndex
                    && count < this.heightOfTrees[h] - this.K[h]) {
                treeToConstruct.initializeTreehashSeed(seed, count);
                seedForTreehashIndex *= 2;
                count++;
            }

            OTSseed = gmssRandom.nextSeed(seed);
            ots = new WinternitzOTSignature(OTSseed, algNames, otsIndex[h]);
            treeToConstruct.update(ots.getPublicKey());
        }

        if (treeToConstruct.wasFinished())
            return treeToConstruct;
        System.err.println("N�chster Baum noch nicht fertig konstruiert!!!");
        return null;
    }


    public void initialize(int keySize, SecureRandom secureRandom) {

        GMSSParameterSpec gps;
        if (keySize <= 10) { // create 2^10 keys
            int[] defh = {10};
            int[] defw = {3};
            int[] defk = {2};
            gps = new GMSSParameterSpec(defh.length, defh, defw, defk);
        } else if (keySize <= 20) { // create 2^20 keys
            int[] defh = {10, 10};
            int[] defw = {5, 4};
            int[] defk = {2, 2};
            gps = new GMSSParameterSpec(defh.length, defh, defw, defk);
        } else { // create 2^40 keys, keygen lasts around 80 seconds
            int[] defh = {10, 10, 10, 10};
            int[] defw = {9, 9, 9, 3};
            int[] defk = {2, 2, 2, 2};
            gps = new GMSSParameterSpec(defh.length, defh, defw, defk);
        }

        // call the initializer with the chosen parameters
        try {
            this.initialize(gps);
        } catch (InvalidAlgorithmParameterException ignored) {
        }
    }



    public void initialize(AlgorithmParameterSpec algParamSpec,
                           SecureRandom secureRandom)
            throws InvalidAlgorithmParameterException {
        this.initialize(algParamSpec);
    }

    public void initialize(AlgorithmParameterSpec algParamSpec)
            throws InvalidAlgorithmParameterException {

        if (!(algParamSpec instanceof GMSSParameterSpec)) {
            throw new InvalidAlgorithmParameterException(
                    "in GMSSKeyPairGenerator: initialize: params is not "
                            + "an instance of GMSSParameterSpec");
        }
        GMSSParameterSpec gmssParameterSpec = (GMSSParameterSpec) algParamSpec;

        // generate GMSSParameterset
        this.gmssPS = new GMSSParameterset(gmssParameterSpec.getNumOfLayers(),
                gmssParameterSpec.getHeightOfTrees(), gmssParameterSpec
                .getWinternitzParameter(), gmssParameterSpec.getK());

        this.numLayer = gmssPS.getNumOfLayers();
        this.heightOfTrees = gmssPS.getHeightOfTrees();
        this.otsIndex = gmssPS.getWinternitzParameter();
        this.K = gmssPS.getK();

        // seeds
        this.currentSeeds = new byte[numLayer][mdLength];
        this.nextNextSeeds = new byte[numLayer - 1][mdLength];

        byte[] seed;
        // construct SecureRandom for initial seed generation
        SecureRandom secRan = Registry.getSecureRandom();

        // generation of initial seeds
        for (int i = 0; i < numLayer; i++) {
            seed = secRan.generateSeed(mdLength);
            System.arraycopy(seed, 0, currentSeeds[i], 0, mdLength);
            gmssRandom.nextSeed(currentSeeds[i]);
        }

        this.initialized = true;
    }

    /**
     * This method is called by generateKeyPair() in case that no other
     * initialization method has been called by the user
     */
    private void initializeDefault() {
        int[] defh = {10, 10, 10, 10};
        int[] defw = {3, 3, 3, 3};
        int[] defk = {2, 2, 2, 2};

        GMSSParameterSpec gps = new GMSSParameterSpec(defh.length, defh, defw,
                defk);

        try {
            this.initialize(gps);
        } catch (InvalidAlgorithmParameterException ignored) {
        }
    }
}
