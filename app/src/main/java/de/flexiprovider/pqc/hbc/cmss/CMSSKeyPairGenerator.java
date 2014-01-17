package de.flexiprovider.pqc.hbc.cmss;

import de.flexiprovider.api.MessageDigest;
import de.flexiprovider.api.Registry;
import de.flexiprovider.api.SecureRandom;
import de.flexiprovider.api.exceptions.InvalidAlgorithmParameterException;
import de.flexiprovider.api.keys.KeyPair;
import de.flexiprovider.api.keys.KeyPairGenerator;
import de.flexiprovider.api.parameters.AlgorithmParameterSpec;
import de.flexiprovider.common.math.IntegerFunctions;
import de.flexiprovider.common.util.ByteUtils;
import de.flexiprovider.pqc.hbc.FIPS_186_2_PRNG;
import de.flexiprovider.pqc.hbc.PRNG;
import de.flexiprovider.pqc.hbc.ots.OTS;


public class CMSSKeyPairGenerator extends KeyPairGenerator {

    // the OID string of the algorithm
    private String oidString;

    // the message digest used to build the authentication trees and for the OTS
    private MessageDigest md;

    // the output length of the message digest
    private int mdLength;

    // the one-time signature scheme
    private OTS ots;

    // the RNG used for key pair generation
    private PRNG rng;

    // the PRNG used for OTS key pair generation
    private SecureRandom sr;

    // the height of the authentication trees
    private int heightOfTrees;

    // an array of three seeds for the PRNG (main tree, current subtree, and
    // next subtree)
    private byte[][] seeds;

    // flag indicating if the key pair generator has been initialized
    private boolean initialized = false;

    private boolean useSpr;



    protected CMSSKeyPairGenerator(String oidString, MessageDigest md,
                                   OTS ots, boolean useSpr) {
        this.oidString = oidString;
        this.md = md;
        mdLength = md.getDigestLength();
        rng = new FIPS_186_2_PRNG();
        rng.initialize(md);
        ots.init(md, rng);
        this.ots = ots;
        this.useSpr = useSpr;
    }


    public void initialize(int heightOfTrees, SecureRandom secureRandom) {
        int seedSize = md.getDigestLength();
        initialize(heightOfTrees, seedSize, secureRandom);
    }


    public void initialize(AlgorithmParameterSpec params,
                           SecureRandom secureRandom)
            throws InvalidAlgorithmParameterException {

        if (!(params instanceof CMSSParameterSpec)) {
            throw new InvalidAlgorithmParameterException(
                    "Not an instance of CMSS2ParameterSpec.");
        }

        int heightOfTrees = ((CMSSParameterSpec) params).getHeightOfTrees();
        int seedSize = ((CMSSParameterSpec) params).getSeedSize();

        initialize(heightOfTrees, seedSize, secureRandom);
    }


    private void initialize(int heightOfTrees, int seedSize, SecureRandom sr) {
        if (mdLength > seedSize) {
            seedSize = mdLength;
        }
        if (sr != null) {
            this.sr = sr;
        } else if (this.sr == null) {
            this.sr = Registry.getSecureRandom();
        }

        seeds = new byte[3][];
        seeds[0] = this.sr.generateSeed(seedSize);
        seeds[1] = this.sr.generateSeed(seedSize);
        seeds[2] = new byte[seedSize];

        this.heightOfTrees = heightOfTrees;

        initialized = true;
    }


    private void initializeDefault() {
        CMSSParameterSpec defaultParams = new CMSSParameterSpec();
        initialize(defaultParams.getHeightOfTrees(), defaultParams
                .getSeedSize(), null);
    }
    public KeyPair genKeyPair() {
        if (!initialized) {
            initializeDefault();
        }
        int K = 2;
        if (heightOfTrees % 2 != 0)
            K += 1;

        BDSAuthPath[] authPath = new BDSAuthPath[3];

        byte[][][] masks = null;
        NodeCalc subNc, mainNc;

        if (useSpr) {
            int heightOfKeyTree = getKeyTreeHeight(ots);
            masks = generateMasks(md.getDigestLength(),
                    2 * (heightOfTrees + heightOfKeyTree));
            byte[][][] subMasks = new byte[masks.length / 2][][];
            System.arraycopy(masks, 0, subMasks, 0, subMasks.length);
            byte[][][] mainMasks = new byte[masks.length / 2][][];
            System.arraycopy(masks, subMasks.length, mainMasks, 0,
                    mainMasks.length);
            subNc = new SPRNodeCalc(md, subMasks, md.getDigestLength());
            mainNc = new SPRNodeCalc(md, mainMasks, md.getDigestLength());
        } else {
            subNc = new CRNodeCalc(md);
            mainNc = new CRNodeCalc(md);
        }

	/* generate the main tree */
        authPath[0] = new BDSAuthPath(heightOfTrees, K);
        authPath[0].setup(md, ots, rng, mainNc);
        byte[] maintreeRoot = authPath[0].initialize(ByteUtils.clone(seeds[0]));

	/* generate the first sub tree */
        authPath[1] = new BDSAuthPath(heightOfTrees, K);
        authPath[1].setup(md, ots, rng, subNc);
        byte[] seedSub = ByteUtils.clone(seeds[1]);
        byte[] subtreeRoot = authPath[1].initialize(seedSub);

	/* get seed for next subtree and setup initialization */
        seeds[2] = ByteUtils.clone(seedSub);
        // WARUM???
        rng.nextSeed(seeds[2]);
        //
        authPath[2] = new BDSAuthPath(heightOfTrees, K);
        authPath[2].setup(md, ots, rng, subNc);
        authPath[2].initializationSetup();

	/* sign root of first subtree */
        byte[] otsSeed = rng.nextSeed(seeds[0]);
        ots.generateSignatureKey(otsSeed);
        byte[] subtreeRootSig = ots.sign(subtreeRoot);

        byte[] maintreeOTSVerificationKey;
        if (ots.canComputeVerificationKeyFromSignature()) {
            maintreeOTSVerificationKey = null;
            authPath[0].setLeftLeaf(mainNc.getLeaf(ots.computeVerificationKey(
                    subtreeRoot, subtreeRootSig)));
        } else {
            ots.generateVerificationKey();
            maintreeOTSVerificationKey = ots.getVerificationKey();
            authPath[0].setLeftLeaf(mainNc.getLeaf(maintreeOTSVerificationKey));
        }

        CMSSPublicKey pubKey = new CMSSPublicKey(oidString, maintreeRoot,
                masks);
        CMSSPrivateKey privKey = new CMSSPrivateKey(oidString, 0, 0,
                heightOfTrees, seeds, authPath, 0, subtreeRootSig,
                maintreeOTSVerificationKey, masks);

        return new KeyPair(pubKey, privKey);
    }
    private int getKeyTreeHeight(OTS ots) {
        int t = ots.getVerificationKeyLength() / mdLength;
        return IntegerFunctions.ceilLog(t);
    }

    private byte[][][] generateMasks(int length, int height) {
        PRNG rng = new FIPS_186_2_PRNG();
        rng.initialize(md);
        byte[] seed = Registry.getSecureRandom().generateSeed(length);
        byte[][][] masks = new byte[height][2][];
        byte[] currentSeed = ByteUtils.clone(seed);
        for (int i = 0; i < masks.length; i++) {
            for (int j = 0; j < masks[i].length; j++) {
                masks[i][j] = rng.nextSeed(currentSeed);
            }
        }

        return masks;
    }
}
