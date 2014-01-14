package de.flexiprovider.pqc.hbc.cmss;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import de.flexiprovider.api.MessageDigest;
import de.flexiprovider.api.SecureRandom;
import de.flexiprovider.api.Signature;
import de.flexiprovider.api.exceptions.InvalidKeyException;
import de.flexiprovider.api.exceptions.SignatureException;
import de.flexiprovider.api.keys.PrivateKey;
import de.flexiprovider.api.keys.PublicKey;
import de.flexiprovider.api.parameters.AlgorithmParameterSpec;
import de.flexiprovider.common.util.ByteUtils;
import de.flexiprovider.common.util.LittleEndianConversions;
import de.flexiprovider.core.md.SHA1;
import de.flexiprovider.core.md.SHA256;
import de.flexiprovider.core.md.SHA384;
import de.flexiprovider.core.md.SHA512;
import de.flexiprovider.core.md.swifftx.SWIFFTX224;
import de.flexiprovider.core.md.swifftx.SWIFFTX256;
import de.flexiprovider.core.md.swifftx.SWIFFTX384;
import de.flexiprovider.core.md.swifftx.SWIFFTX512;
import de.flexiprovider.pqc.hbc.FIPS_186_2_PRNG;
import de.flexiprovider.pqc.hbc.PRNG;
import de.flexiprovider.pqc.hbc.ots.LMOTS;
import de.flexiprovider.pqc.hbc.ots.OTS;
import de.flexiprovider.pqc.hbc.ots.WinternitzOTS;

public class CMSSSignature extends Signature {

    // the OID of the algorithm
    private String oid;

    // the message digest used to build the authentication trees and for the OTS
    private MessageDigest md;

    // the length of the hash function output
    private int mdLength;

    // the one-time signature scheme
    private OTS ots;

    // the RNG used for key pair generation
    private PRNG rng;

    // the private key
    private CMSSPrivateKey privKey;

    // the public key
    private CMSSPublicKey pubKey;

    // the public key bytes
    private byte[] pubKeyBytes;

    // the ByteArrayOutputStream holding the messages
    private ByteArrayOutputStream baos;

    // the main tree index
    private int indexMain;

    // the current subtree index
    private int indexSub;

    // the seeds for key pair generation
    private byte[][] seeds;

    // an array of three authentication paths for the main tree, current subtree
    // and next subtree
    private BDSAuthPath[] authPath;
    private int activeSubtree;

    // the one-time signature of the root of the current subtree
    private byte[] subtreeRootSig;

    // the one-time verification key used to verify the rootSignature of the
    // subtree
    private byte[] maintreeOTSVerificationKey;

    // the height of the authentication trees
    private int heightOfTrees;

    // the number of leafs of each tree
    private int numLeafs;

    // way to compute parent nodes
    private NodeCalc mainNc, subNc;

    private boolean useSpr;

    public static class SHA1andWinternitzOTS_1 extends CMSSSignature {

        /**
         * The OID of the algorithm
         */
        public static final String OID = CMSSKeyPairGenerator.SHA1andWinternitzOTS_1.OID;

        /**
         * Constructor.
         */
        public SHA1andWinternitzOTS_1() {
            super("1.3.6.1.4.1.8301.3.1.3.2.1", new SHA1(),
                    new WinternitzOTS(1), false);
        }
    }

    /**
     * CMSSSignature with SHA1 message digest, Winternitz OTS with parameter
     * w=2, and SHA1PRNG
     */
    public static class SHA1andWinternitzOTS_2 extends CMSSSignature {

        /**
         * The OID of the algorithm
         */
        public static final String OID = CMSSKeyPairGenerator.SHA1andWinternitzOTS_2.OID;

        public SHA1andWinternitzOTS_2() {
            super("1.3.6.1.4.1.8301.3.1.3.2.2", new SHA1(),
                    new WinternitzOTS(2), false);
        }
    }

    public static class SHA1andWinternitzOTS_3 extends CMSSSignature {

        public static final String OID = CMSSKeyPairGenerator.SHA1andWinternitzOTS_3.OID;

        /**
         * Constructor.
         */
        public SHA1andWinternitzOTS_3() {
            super("1.3.6.1.4.1.8301.3.1.3.2.3", new SHA1(),
                    new WinternitzOTS(3), false);
        }
    }


    public static class SHA1andWinternitzOTS_4 extends CMSSSignature {

        /**
         * The OID of the algorithm
         */
        public static final String OID = CMSSKeyPairGenerator.SHA1andWinternitzOTS_4.OID;

        /**
         * Constructor.
         */
        public SHA1andWinternitzOTS_4() {
            super("1.3.6.1.4.1.8301.3.1.3.2.4", new SHA1(),
                    new WinternitzOTS(4), false);
        }
    }

    /**
     * CMSSSignature with SHA256 message digest, Winternitz OTS with parameter
     * w=1, and SHA1PRNG
     */
    public static class SHA256andWinternitzOTS_1 extends CMSSSignature {

        /**
         * The OID of the algorithm
         */
        public static final String OID = CMSSKeyPairGenerator.SHA256andWinternitzOTS_1.OID;

        /**
         * Constructor.
         */
        public SHA256andWinternitzOTS_1() {
            super("1.3.6.1.4.1.8301.3.1.3.2.5", new SHA256(),
                    new WinternitzOTS(1), false);
        }
    }

    /**
     * CMSSSignature with SHA256 message digest, Winternitz OTS with parameter
     * w=2, and SHA1PRNG
     */
    public static class SHA256andWinternitzOTS_2 extends CMSSSignature {

        /**
         * The OID of the algorithm
         */
        public static final String OID = CMSSKeyPairGenerator.SHA256andWinternitzOTS_2.OID;

        /**
         * Constructor.
         */
        public SHA256andWinternitzOTS_2() {
            super("1.3.6.1.4.1.8301.3.1.3.2.6", new SHA256(),
                    new WinternitzOTS(2), false);
        }
    }

    /**
     * CMSSSignature with SHA256 message digest, Winternitz OTS with parameter
     * w=3, and SHA1PRNG
     */
    public static class SHA256andWinternitzOTS_3 extends CMSSSignature {

        /**
         * The OID of the algorithm
         */
        public static final String OID = CMSSKeyPairGenerator.SHA256andWinternitzOTS_3.OID;

        /**
         * Constructor.
         */
        public SHA256andWinternitzOTS_3() {
            super("1.3.6.1.4.1.8301.3.1.3.2.7", new SHA256(),
                    new WinternitzOTS(3), false);
        }
    }

    /**
     * CMSSSignature with SHA256 message digest, Winternitz OTS with parameter
     * w=4, and SHA1PRNG
     */
    public static class SHA256andWinternitzOTS_4 extends CMSSSignature {

        /**
         * The OID of the algorithm
         */
        public static final String OID = CMSSKeyPairGenerator.SHA256andWinternitzOTS_4.OID;

        /**
         * Constructor.
         */
        public SHA256andWinternitzOTS_4() {
            super("1.3.6.1.4.1.8301.3.1.3.2.8", new SHA256(),
                    new WinternitzOTS(4), false);
        }
    }

    /**
     * CMSSSignature with SHA384 message digest, Winternitz OTS with parameter
     * w=1, and SHA1PRNG
     */
    public static class SHA384andWinternitzOTS_1 extends CMSSSignature {

        /**
         * The OID of the algorithm
         */
        public static final String OID = CMSSKeyPairGenerator.SHA384andWinternitzOTS_1.OID;

        /**
         * Constructor.
         */
        public SHA384andWinternitzOTS_1() {
            super("1.3.6.1.4.1.8301.3.1.3.2.9", new SHA384(),
                    new WinternitzOTS(1), false);
        }
    }

    /**
     * CMSSSignature with SHA384 message digest, Winternitz OTS with parameter
     * w=2, and SHA1PRNG
     */
    public static class SHA384andWinternitzOTS_2 extends CMSSSignature {

        /**
         * The OID of the algorithm
         */
        public static final String OID = CMSSKeyPairGenerator.SHA384andWinternitzOTS_2.OID;

        /**
         * Constructor.
         */
        public SHA384andWinternitzOTS_2() {
            super("1.3.6.1.4.1.8301.3.1.3.2.10", new SHA384(),
                    new WinternitzOTS(2), false);
        }
    }

    /**
     * CMSSSignature with SHA384 message digest, Winternitz OTS with parameter
     * w=3, and SHA1PRNG
     */
    public static class SHA384andWinternitzOTS_3 extends CMSSSignature {

        /**
         * The OID of the algorithm
         */
        public static final String OID = CMSSKeyPairGenerator.SHA384andWinternitzOTS_3.OID;

        /**
         * Constructor.
         */
        public SHA384andWinternitzOTS_3() {
            super("1.3.6.1.4.1.8301.3.1.3.2.11", new SHA384(),
                    new WinternitzOTS(3), false);
        }
    }

    /**
     * CMSSSignature with SHA384 message digest, Winternitz OTS with parameter
     * w=4, and SHA1PRNG
     */
    public static class SHA384andWinternitzOTS_4 extends CMSSSignature {

        /**
         * The OID of the algorithm
         */
        public static final String OID = CMSSKeyPairGenerator.SHA384andWinternitzOTS_4.OID;

        /**
         * Constructor.
         */
        public SHA384andWinternitzOTS_4() {
            super("1.3.6.1.4.1.8301.3.1.3.2.12", new SHA384(),
                    new WinternitzOTS(4), false);
        }
    }

    /**
     * CMSSSignature with SHA512 message digest, Winternitz OTS with parameter
     * w=1, and SHA1PRNG
     */
    public static class SHA512andWinternitzOTS_1 extends CMSSSignature {

        /**
         * The OID of the algorithm
         */
        public static final String OID = CMSSKeyPairGenerator.SHA512andWinternitzOTS_1.OID;

        /**
         * Constructor.
         */
        public SHA512andWinternitzOTS_1() {
            super("1.3.6.1.4.1.8301.3.1.3.2.13", new SHA512(),
                    new WinternitzOTS(1), false);
        }
    }

    /**
     * CMSSSignature with SHA512 message digest, Winternitz OTS with parameter
     * w=2, and SHA1PRNG
     */
    public static class SHA512andWinternitzOTS_2 extends CMSSSignature {

        /**
         * The OID of the algorithm
         */
        public static final String OID = CMSSKeyPairGenerator.SHA512andWinternitzOTS_2.OID;

        /**
         * Constructor.
         */
        public SHA512andWinternitzOTS_2() {
            super("1.3.6.1.4.1.8301.3.1.3.2.14", new SHA512(),
                    new WinternitzOTS(2), false);
        }
    }

    /**
     * CMSSSignature with SHA512 message digest, Winternitz OTS with parameter
     * w=3, and SHA1PRNG
     */
    public static class SHA512andWinternitzOTS_3 extends CMSSSignature {

        /**
         * The OID of the algorithm
         */
        public static final String OID = CMSSKeyPairGenerator.SHA512andWinternitzOTS_3.OID;

        /**
         * Constructor.
         */
        public SHA512andWinternitzOTS_3() {
            super("1.3.6.1.4.1.8301.3.1.3.2.15", new SHA512(),
                    new WinternitzOTS(3), false);
        }
    }

    /**
     * CMSSSignature with SHA512 message digest, Winternitz OTS with parameter
     * w=4, and SHA1PRNG
     */
    public static class SHA512andWinternitzOTS_4 extends CMSSSignature {

        /**
         * The OID of the algorithm
         */
        public static final String OID = CMSSKeyPairGenerator.SHA512andWinternitzOTS_4.OID;

        /**
         * Constructor.
         */
        public SHA512andWinternitzOTS_4() {
            super("1.3.6.1.4.1.8301.3.1.3.2.16", new SHA512(),
                    new WinternitzOTS(4), false);
        }
    }

    /**
     * CMSSSignature with SWIFFTX224 message digest, Winternitz OTS with
     * parameter w=1, and SWIFFTX224PRNG
     */
    public static class SWIFFTX224andWinternitzOTS_1 extends CMSSSignature {

        /**
         * The OID of the algorithm
         */
        public static final String OID = CMSSKeyPairGenerator.SWIFFTX224andWinternitzOTS_1.OID;

        /**
         * Constructor.
         */
        public SWIFFTX224andWinternitzOTS_1() {
            super("1.3.6.1.4.1.8301.3.1.3.2.101", new SWIFFTX224(),
                    new WinternitzOTS(1), false);
        }
    }

    /**
     * CMSSSignature with SWIFFTX224 message digest, Winternitz OTS with
     * parameter w=2, and SWIFFTX224PRNG
     */
    public static class SWIFFTX224andWinternitzOTS_2 extends CMSSSignature {

        /**
         * The OID of the algorithm
         */
        public static final String OID = CMSSKeyPairGenerator.SWIFFTX224andWinternitzOTS_2.OID;

        /**
         * Constructor.
         */
        public SWIFFTX224andWinternitzOTS_2() {
            super("1.3.6.1.4.1.8301.3.1.3.2.102", new SWIFFTX224(),
                    new WinternitzOTS(2), false);
        }
    }

    /**
     * CMSSSignature with SWIFFTX224 message digest, Winternitz OTS with
     * parameter w=3, and SWIFFTX224PRNG
     */
    public static class SWIFFTX224andWinternitzOTS_3 extends CMSSSignature {

        /**
         * The OID of the algorithm
         */
        public static final String OID = CMSSKeyPairGenerator.SWIFFTX224andWinternitzOTS_3.OID;

        /**
         * Constructor.
         */
        public SWIFFTX224andWinternitzOTS_3() {
            super("1.3.6.1.4.1.8301.3.1.3.2.103", new SWIFFTX224(),
                    new WinternitzOTS(3), false);
        }
    }

    /**
     * CMSSSignature with SWIFFTX224 message digest, Winternitz OTS with
     * parameter w=4, and SWIFFTX224PRNG
     */
    public static class SWIFFTX224andWinternitzOTS_4 extends CMSSSignature {

        /**
         * The OID of the algorithm
         */
        public static final String OID = CMSSKeyPairGenerator.SWIFFTX224andWinternitzOTS_4.OID;

        /**
         * Constructor.
         */
        public SWIFFTX224andWinternitzOTS_4() {
            super("1.3.6.1.4.1.8301.3.1.3.2.104", new SWIFFTX224(),
                    new WinternitzOTS(4), false);
        }
    }

    /**
     * CMSSSignature with SWIFFTX256 message digest, Winternitz OTS with
     * parameter w=1, and SWIFFTX256PRNG
     */
    public static class SWIFFTX256andWinternitzOTS_1 extends CMSSSignature {

        /**
         * The OID of the algorithm
         */
        public static final String OID = CMSSKeyPairGenerator.SWIFFTX256andWinternitzOTS_1.OID;

        /**
         * Constructor.
         */
        public SWIFFTX256andWinternitzOTS_1() {
            super("1.3.6.1.4.1.8301.3.1.3.2.105", new SWIFFTX256(),
                    new WinternitzOTS(1), false);
        }
    }

    /**
     * CMSSSignature with SWIFFTX256 message digest, Winternitz OTS with
     * parameter w=2, and SWIFFTX256PRNG
     */
    public static class SWIFFTX256andWinternitzOTS_2 extends CMSSSignature {

        /**
         * The OID of the algorithm
         */
        public static final String OID = CMSSKeyPairGenerator.SWIFFTX256andWinternitzOTS_2.OID;

        /**
         * Constructor.
         */
        public SWIFFTX256andWinternitzOTS_2() {
            super("1.3.6.1.4.1.8301.3.1.3.2.106", new SWIFFTX256(),
                    new WinternitzOTS(2), false);
        }
    }

    /**
     * CMSSSignature with SWIFFTX256 message digest, Winternitz OTS with
     * parameter w=3, and SWIFFTX256PRNG
     */
    public static class SWIFFTX256andWinternitzOTS_3 extends CMSSSignature {

        /**
         * The OID of the algorithm
         */
        public static final String OID = CMSSKeyPairGenerator.SWIFFTX256andWinternitzOTS_3.OID;

        /**
         * Constructor.
         */
        public SWIFFTX256andWinternitzOTS_3() {
            super("1.3.6.1.4.1.8301.3.1.3.2.107", new SWIFFTX256(),
                    new WinternitzOTS(3), false);
        }
    }

    /**
     * CMSSSignature with SWIFFTX256 message digest, Winternitz OTS with
     * parameter w=4, and SWIFFTX256PRNG
     */
    public static class SWIFFTX256andWinternitzOTS_4 extends CMSSSignature {

        /**
         * The OID of the algorithm
         */
        public static final String OID = CMSSKeyPairGenerator.SWIFFTX256andWinternitzOTS_4.OID;

        /**
         * Constructor.
         */
        public SWIFFTX256andWinternitzOTS_4() {
            super("1.3.6.1.4.1.8301.3.1.3.2.108", new SWIFFTX256(),
                    new WinternitzOTS(4), false);
        }
    }

    public static class SWIFFTX384andWinternitzOTS_1 extends CMSSSignature {

        public static final String OID = CMSSKeyPairGenerator.SWIFFTX384andWinternitzOTS_1.OID;

        public SWIFFTX384andWinternitzOTS_1() {
            super("1.3.6.1.4.1.8301.3.1.3.2.109", new SWIFFTX384(),
                    new WinternitzOTS(1), false);
        }
    }

    /**
     * CMSSSignature with SWIFFTX384 message digest, Winternitz OTS with
     * parameter w=2, and SWIFFTX384PRNG
     */
    public static class SWIFFTX384andWinternitzOTS_2 extends CMSSSignature {

        /**
         * The OID of the algorithm
         */
        public static final String OID = CMSSKeyPairGenerator.SWIFFTX384andWinternitzOTS_2.OID;

        /**
         * Constructor.
         */
        public SWIFFTX384andWinternitzOTS_2() {
            super("1.3.6.1.4.1.8301.3.1.3.2.110", new SWIFFTX384(),
                    new WinternitzOTS(2), false);
        }
    }

    public static class SWIFFTX384andWinternitzOTS_3 extends CMSSSignature {

        /**
         * The OID of the algorithm
         */
        public static final String OID = CMSSKeyPairGenerator.SWIFFTX384andWinternitzOTS_3.OID;

        /**
         * Constructor.
         */
        public SWIFFTX384andWinternitzOTS_3() {
            super("1.3.6.1.4.1.8301.3.1.3.2.111", new SWIFFTX384(),
                    new WinternitzOTS(3), false);
        }
    }

    public static class SWIFFTX384andWinternitzOTS_4 extends CMSSSignature {

        /**
         * The OID of the algorithm
         */
        public static final String OID = CMSSKeyPairGenerator.SWIFFTX384andWinternitzOTS_4.OID;

        /**
         * Constructor.
         */
        public SWIFFTX384andWinternitzOTS_4() {
            super("1.3.6.1.4.1.8301.3.1.3.2.112", new SWIFFTX384(),
                    new WinternitzOTS(4), false);
        }
    }

    /**
     * CMSSSignature with SWIFFTX512 message digest, Winternitz OTS with
     * parameter w=1, and SWIFFTX512PRNG
     */
    public static class SWIFFTX512andWinternitzOTS_1 extends CMSSSignature {

        /**
         * The OID of the algorithm
         */
        public static final String OID = CMSSKeyPairGenerator.SWIFFTX512andWinternitzOTS_1.OID;

        /**
         * Constructor.
         */
        public SWIFFTX512andWinternitzOTS_1() {
            super("1.3.6.1.4.1.8301.3.1.3.2.113", new SWIFFTX512(),
                    new WinternitzOTS(1), false);
        }
    }

    /**
     * CMSSSignature with SWIFFTX512 message digest, Winternitz OTS with
     * parameter w=2, and SWIFFTX512PRNG
     */
    public static class SWIFFTX512andWinternitzOTS_2 extends CMSSSignature {

        /**
         * The OID of the algorithm
         */
        public static final String OID = CMSSKeyPairGenerator.SWIFFTX512andWinternitzOTS_2.OID;

        /**
         * Constructor.
         */
        public SWIFFTX512andWinternitzOTS_2() {
            super("1.3.6.1.4.1.8301.3.1.3.2.114", new SWIFFTX512(),
                    new WinternitzOTS(2), false);
        }
    }

    /**
     * CMSSSignature with SWIFFTX512 message digest, Winternitz OTS with
     * parameter w=3, and SWIFFTX512PRNG
     */
    public static class SWIFFTX512andWinternitzOTS_3 extends CMSSSignature {

        /**
         * The OID of the algorithm
         */
        public static final String OID = CMSSKeyPairGenerator.SWIFFTX512andWinternitzOTS_3.OID;

        /**
         * Constructor.
         */
        public SWIFFTX512andWinternitzOTS_3() {
            super("1.3.6.1.4.1.8301.3.1.3.2.115", new SWIFFTX512(),
                    new WinternitzOTS(3), false);
        }
    }

    /**
     * CMSSSignature with SWIFFTX512 message digest, Winternitz OTS with
     * parameter w=4, and SWIFFTX512PRNG
     */
    public static class SWIFFTX512andWinternitzOTS_4 extends CMSSSignature {

        /**
         * The OID of the algorithm
         */
        public static final String OID = CMSSKeyPairGenerator.SWIFFTX512andWinternitzOTS_4.OID;

        /**
         * Constructor.
         */
        public SWIFFTX512andWinternitzOTS_4() {
            super("1.3.6.1.4.1.8301.3.1.3.2.116", new SWIFFTX512(),
                    new WinternitzOTS(4), false);
        }
    }




    public static class SHA1andLMOTS extends CMSSSignature {

        /**
         * The OID of the algorithm
         */
        public static final String OID = CMSSKeyPairGenerator.SHA1andLMOTS.OID;

        /**
         * Constructor.
         */
        public SHA1andLMOTS() {
            super("1.3.6.1.4.1.8301.3.1.3.2.117", new SHA1(), new LMOTS(),
                    false);
        }
    }

    public static class SHA256andLMOTS extends CMSSSignature {

        /**
         * The OID of the algorithm
         */
        public static final String OID = CMSSKeyPairGenerator.SHA256andLMOTS.OID;

        /**
         * Constructor.
         */
        public SHA256andLMOTS() {
            super("1.3.6.1.4.1.8301.3.1.3.2.118", new SHA256(), new LMOTS(),
                    false);
        }
    }

    public static class SHA384andLMOTS extends CMSSSignature {

        /**
         * The OID of the algorithm
         */
        public static final String OID = CMSSKeyPairGenerator.SHA384andLMOTS.OID;

        /**
         * Constructor.
         */
        public SHA384andLMOTS() {
            super("1.3.6.1.4.1.8301.3.1.3.2.119", new SHA384(), new LMOTS(),
                    false);
        }
    }

    public static class SHA512andLMOTS extends CMSSSignature {

        /**
         * The OID of the algorithm
         */
        public static final String OID = CMSSKeyPairGenerator.SHA512andLMOTS.OID;

        /**
         * Constructor.
         */
        public SHA512andLMOTS() {
            super("1.3.6.1.4.1.8301.3.1.3.2.120", new SHA512(), new LMOTS(),
                    false);
        }
    }

    public static class SWIFFTX224andLMOTS extends CMSSSignature {

        /**
         * The OID of the algorithm
         */
        public static final String OID = CMSSKeyPairGenerator.SWIFFTX224andLMOTS.OID;

        /**
         * Constructor.
         */
        public SWIFFTX224andLMOTS() {
            super("1.3.6.1.4.1.8301.3.1.3.2.121", new SWIFFTX224(),
                    new LMOTS(), false);
        }
    }

    public static class SWIFFTX256andLMOTS extends CMSSSignature {

        public static final String OID = CMSSKeyPairGenerator.SWIFFTX256andLMOTS.OID;

        public SWIFFTX256andLMOTS() {
            super("1.3.6.1.4.1.8301.3.1.3.2.122", new SWIFFTX256(),
                    new LMOTS(), false);
        }
    }
    public static class SWIFFTX384andLMOTS extends CMSSSignature {

        /**
         * The OID of the algorithm
         */
        public static final String OID = CMSSKeyPairGenerator.SWIFFTX384andLMOTS.OID;

        /**
         * Constructor.
         */
        public SWIFFTX384andLMOTS() {
            super("1.3.6.1.4.1.8301.3.1.3.2.123", new SWIFFTX384(),
                    new LMOTS(), false);
        }
    }

    /**
     * CMSS2Signature with SWIFFTX512 message digest, LM OTS, and SWIFFTX512PRNG
     */
    public static class SWIFFTX512andLMOTS extends CMSSSignature {

        /**
         * The OID of the algorithm
         */
        public static final String OID = CMSSKeyPairGenerator.SWIFFTX512andLMOTS.OID;

        /**
         * Constructor.
         */
        public SWIFFTX512andLMOTS() {
            super("1.3.6.1.4.1.8301.3.1.3.2.124", new SWIFFTX512(),
                    new LMOTS(), false);
        }
    }
    protected CMSSSignature(String oidString, MessageDigest md, OTS ots,
                            boolean useSpr) {
        oid = oidString;
        this.md = md;
        mdLength = md.getDigestLength();
        rng = new FIPS_186_2_PRNG();
        rng.initialize(md);
        ots.init(md, rng);
        this.ots = ots;
        this.useSpr = useSpr;
    }

    public void initSign(PrivateKey key, SecureRandom random)
            throws InvalidKeyException {

        // reset the signature object
        reset();

        if (!(key instanceof CMSSPrivateKey)) {
            throw new InvalidKeyException("unsupported type");
        }
        privKey = (CMSSPrivateKey) key;

        // check if OID stored in the key matches algorithm OID
        if (!privKey.getOIDString().equals(oid)) {
            throw new InvalidKeyException("invalid key for this signature");
        }

        md.reset();
        baos = new ByteArrayOutputStream();

        // obtain required parameters from private key

        heightOfTrees = privKey.getHeightOfTrees();
        numLeafs = privKey.getNumLeafs();

        indexMain = privKey.getIndexMain();
        indexSub = privKey.getIndexSub();
        seeds = privKey.getSeeds();
        authPath = privKey.getAuthPath();
        activeSubtree = privKey.getActiveSubtree();
        subtreeRootSig = privKey.getSubtreeRootSig();
        maintreeOTSVerificationKey = privKey.getMaintreeOTSVerificationKey();

        if (useSpr) {
            if (privKey.getMasks() == null) {
                throw new IllegalArgumentException(
                        "Masks must not be null if SPR is in use.");
            }
            byte[][][] masks = privKey.getMasks();
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

        if (heightOfTrees % 2 != 0)
            ;
        authPath[0].setup(md, ots, rng, mainNc);
        authPath[1].setup(md, ots, rng, subNc);
        authPath[2].setup(md, ots, rng, subNc);

    }

    public void initVerify(PublicKey key) throws InvalidKeyException {

        // reset the signature object
        reset();

        if (!(key instanceof CMSSPublicKey)) {
            throw new InvalidKeyException("unsupported type");
        }
        pubKey = (CMSSPublicKey) key;

        // check if OID stored in the key matches algorithm OID
        if (!pubKey.getOIDString().equals(oid)) {
            throw new InvalidKeyException("invalid key for this signature");
        }

        pubKeyBytes = pubKey.getKeyBytes();

        md.reset();
        baos = new ByteArrayOutputStream();

        if (pubKey.getMasks() != null) {
            byte[][][] masks = pubKey.getMasks();
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
    }

    public void setParameters(AlgorithmParameterSpec params) {
        // parameters are not used
    }

    public void update(byte data) {
        baos.write(data);
    }
    public void update(byte[] data, int offset, int length) {
        baos.write(data, offset, length);
    }
    public byte[] sign() throws SignatureException {

        // check if last signature has been generated
        if (indexMain >= numLeafs) {
            throw new SignatureException(
                    "No more signatures can be generated with this key.");
        }
        /* first part of the signature */

        // obtain the message
        byte[] message = getData();

        // generate the new subtree seed and one-time signature of the message
        byte[] otsSeed = rng.nextSeed(seeds[1]);
        ots.generateSignatureKey(otsSeed);
        byte[] otsSig = ots.sign(message);

        // if the current subtree node is a left node, store it for the next
        // authentication path
        if (!ots.canComputeVerificationKeyFromSignature()) {
            ots.generateVerificationKey();
        }

        if ((indexSub & 1) == 0) {
            if (!ots.canComputeVerificationKeyFromSignature()) {
                authPath[1 + activeSubtree].setLeftLeaf(subNc.getLeaf(ots
                        .getVerificationKey()));
            } else {
                authPath[1 + activeSubtree].setLeftLeaf(subNc.getLeaf(ots
                        .computeVerificationKey(message, otsSig)));
            }
        }

		/* subtree part of signature */
        // convert subtree index into a byte array
        byte[] indexBytes = LittleEndianConversions.I2OSP(indexSub);

        // get concatenated subtree authentication path
        byte[] authPathBytes = ByteUtils
                .concatenate(authPath[1 + activeSubtree].getAuthPath());

        // concatenate index, otsSig, maybe OTSPubKey and authPathBytes
        byte[] firstHalf = ByteUtils.concatenate(indexBytes, otsSig);
        if (!ots.canComputeVerificationKeyFromSignature())
            firstHalf = ByteUtils.concatenate(firstHalf, ots
                    .getVerificationKey());
        firstHalf = ByteUtils.concatenate(firstHalf, authPathBytes);

		/* maintree part of signature */
        // convert main tree index into a byte array
        indexBytes = LittleEndianConversions.I2OSP(indexMain);

        // get concatenated main tree authentication path
        authPathBytes = ByteUtils.concatenate(authPath[0].getAuthPath());

        // concatenate index, subtreeRootSig, and authPathBytes
        byte[] secondHalf = ByteUtils.concatenate(indexBytes, subtreeRootSig);
        if (!ots.canComputeVerificationKeyFromSignature())
            secondHalf = ByteUtils.concatenate(secondHalf,
                    maintreeOTSVerificationKey);
        secondHalf = ByteUtils.concatenate(secondHalf, authPathBytes);

        // change private key for next signature and reset signature
        if (indexSub < numLeafs - 1 || indexMain < numLeafs - 1) {
            nextKey();
            privKey.update(indexMain, indexSub, seeds, authPath, activeSubtree,
                    subtreeRootSig, maintreeOTSVerificationKey);
        } else
            privKey.update(numLeafs, numLeafs, null, null, 0, null, null);

        // concatenate the two halves of the CMSS2 signature and return
        return ByteUtils.concatenate(firstHalf, secondHalf);
    }

    public boolean verify(byte[] sigBytes) {
        int otsSigLength = ots.getSignatureLength();
        int otsPubKeyLength = ots.getVerificationKeyLength();

        if (ots.canComputeVerificationKeyFromSignature())
            heightOfTrees = (sigBytes.length / 2 - otsSigLength - 4) / mdLength;
        else
            heightOfTrees = (sigBytes.length / 2 - otsSigLength
                    - otsPubKeyLength - 4)
                    / mdLength;

		/* first part */

        // obtain the message
        byte[] message = getData();

        // get the subtree index
        int index = LittleEndianConversions.OS2IP(sigBytes, 0);

        // 4 is the number of bytes in integer
        int nextEntry = 4;

        // get one-time signature of the message
        byte[] otsSig = new byte[otsSigLength];
        System.arraycopy(sigBytes, nextEntry, otsSig, 0, otsSigLength);
        nextEntry += otsSigLength;

        // get one-time verification key from signature
        byte[] otsPubKey;
        if (ots.canComputeVerificationKeyFromSignature()) {
            // if one-time verification key can be computed from signature and
            // message, e.g. Winternitz
            otsPubKey = ots.computeVerificationKey(message, otsSig);
            if (otsPubKey == null)
                return false;
        } else {
            // else, e.g. LM-OTS
            otsPubKey = new byte[otsPubKeyLength];
            // System.out.println(otsSig.length);
            System
                    .arraycopy(sigBytes, nextEntry, otsPubKey, 0,
                            otsPubKeyLength);
            nextEntry += otsPubKeyLength;
            if (!ots.verify(message, otsSig, otsPubKey))
                return false;
        }

        // get authentication path from the signature
        byte[][] authPath = new byte[heightOfTrees][mdLength];
        for (int i = 0; i < heightOfTrees; i++, nextEntry += mdLength) {
            System.arraycopy(sigBytes, nextEntry, authPath[i], 0, mdLength);
        }

        // compute the subtree root from the authentication path
        byte[] help = subNc.getLeaf(otsPubKey);
        for (int i = 0; i < heightOfTrees; i++, index /= 2) {
            if ((index & 1) == 0) {
                help = subNc.computeParent(help, authPath[i], i);
            } else {
                help = subNc.computeParent(authPath[i], help, i);
            }
        }

        // now help contains the root of the subtree
        byte[] subtreeRoot = help;

		/* second part */

        // get the main tree index
        index = LittleEndianConversions.OS2IP(sigBytes, nextEntry);
        nextEntry += 4;

        // get one-time signature
        otsSig = new byte[otsSigLength];
        System.arraycopy(sigBytes, nextEntry, otsSig, 0, otsSigLength);
        nextEntry += otsSigLength;

        // get one-time verification key from signature
        if (ots.canComputeVerificationKeyFromSignature()) {
            // if one-time verification key can be computed from signature and
            // message, e.g. Winternitz
            otsPubKey = ots.computeVerificationKey(subtreeRoot, otsSig);
            if (otsPubKey == null)
                return false;
        } else {
            // else, e.g. LM-OTS
            otsPubKey = new byte[otsPubKeyLength];
            System
                    .arraycopy(sigBytes, nextEntry, otsPubKey, 0,
                            otsPubKeyLength);
            nextEntry += otsPubKeyLength;
            if (!ots.verify(subtreeRoot, otsSig, otsPubKey))
                return false;
        }

        // get authentication path from the signature
        for (int i = 0; i < heightOfTrees; i++, nextEntry += mdLength) {
            System.arraycopy(sigBytes, nextEntry, authPath[i], 0, mdLength);
        }

        // compute the main tree root from the authentication path
        help = mainNc.getLeaf(otsPubKey);
        for (int i = 0; i < heightOfTrees; i++, index /= 2) {
            if ((index & 1) == 0) {
                help = mainNc.computeParent(help, authPath[i], i);
            } else {
                help = mainNc.computeParent(authPath[i], help, i);
            }
        }

        // now help contains the main tree root
        byte[] maintreeRoot = help;

        // check whether the computed main tree root is equal to the
        // public key
        return ByteUtils.equals(maintreeRoot, pubKeyBytes);
    }

    /**
     * @return the data contained in the ByteArrayOutputStream. Closes the
     * stream.
     */
    private byte[] getData() {
        byte[] data = baos.toByteArray();

        try {
            baos.close();
        } catch (IOException ioe) {
            System.err.println("Can not close ByteArrayOutputStream");
        }
        baos.reset();
        return data;
    }

    /**
     * This method updates the CMSS2 private key for the next signature
     */
    private void nextKey() {
        if (indexSub == numLeafs - 1) {
			/* switch to next subtree */
            nextTree();
        } else {
			/* process current subtree */

            authPath[1 + activeSubtree].update(indexSub);
			/* process next subtree, if there is one */
            if (indexMain < numLeafs - 1) {
                authPath[2 - activeSubtree].initializationUpdate(indexSub,
                        seeds[2]);

            }

			/* update indexSub */
            indexSub++;
        }
    }

    /**
     * Switch to next subtree if the current one is depleted
     */
    private void nextTree() {
		/* process next subtree and get root of next subtree */

		/* update auth path of main tree and indexMain */
        authPath[0].update(indexMain);
        indexMain++;

		/* complete construction of next subtree */
        authPath[2 - activeSubtree].initializationUpdate(indexSub, seeds[2]);
        byte[] subtreeRoot = authPath[2 - activeSubtree]
                .initializationFinalize();

		/* sign root of next subtree */
        byte[] otsSeed = rng.nextSeed(seeds[0]);
        ots.generateSignatureKey(otsSeed);
        subtreeRootSig = ots.sign(subtreeRoot);

        if (!ots.canComputeVerificationKeyFromSignature()) {
            ots.generateVerificationKey();
            maintreeOTSVerificationKey = ots.getVerificationKey();
        }

        // if the current subtree node is a left node, store it for the next
        // authentication path
        if ((indexMain & 1) == 0) {
            if (!ots.canComputeVerificationKeyFromSignature()) {
                authPath[0].setLeftLeaf(mainNc
                        .getLeaf(maintreeOTSVerificationKey));
            } else {
                authPath[0].setLeftLeaf(mainNc.getLeaf(ots
                        .computeVerificationKey(subtreeRoot, subtreeRootSig)));
            }
        }

		/* update indexSub */
        indexSub = 0;

		/* copy authentication path from next tree to current tree */
        rng.nextSeed(seeds[1]);
        // authPath[1].copy(authPath[2]);

		/* initialize authentication path computation for next subtree */
        rng.nextSeed(seeds[2]);
        authPath[1 + activeSubtree].initializationSetup();

        activeSubtree = 1 - activeSubtree;

    }

    /**
     * Reset the internal state of the signature.
     */
    private void reset() {
        privKey = null;
        pubKey = null;
        pubKeyBytes = null;
        baos = null;
        indexMain = 0;
        indexSub = 0;
        seeds = null;
        authPath = null;
        subtreeRootSig = null;
        heightOfTrees = 0;
        numLeafs = 0;
    }

}
