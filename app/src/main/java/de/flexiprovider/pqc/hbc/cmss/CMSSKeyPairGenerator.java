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

    // //////////////////////////////////////////////////////////////////////////////

    /*
     * Inner classes providing concrete implementations of the CMSS2 key pair
     * generator with a variety of message digests and one-time signature
     * schemes.
     */

    /**
     * CMSSKeyPairGenerator with SHA1 message digest, Winternitz OTS with
     * parameter w=1, and SHA1PRNG
     */
    public static class SHA1andWinternitzOTS_1 extends CMSSKeyPairGenerator {

        /**
         * The OID of the algorithm.
         */
        public static final String OID = "1.3.6.1.4.1.8301.3.1.3.2.1";

        /**
         * Constructor.
         */
        public SHA1andWinternitzOTS_1() {
            super(OID, new SHA1(), new WinternitzOTS(1), false);
        }
    }

    /**
     * CMSSKeyPairGenerator with SHA1 message digest, Winternitz OTS with
     * parameter w=2, and SHA1PRNG
     */
    public static class SHA1andWinternitzOTS_2 extends CMSSKeyPairGenerator {

        /**
         * The OID of the algorithm.
         */
        public static final String OID = "1.3.6.1.4.1.8301.3.1.3.2.2";

        /**
         * Constructor.
         */
        public SHA1andWinternitzOTS_2() {
            super(OID, new SHA1(), new WinternitzOTS(2), false);
        }
    }

    /**
     * CMSSKeyPairGenerator with SHA1 message digest, Winternitz OTS with
     * parameter w=3, and SHA1PRNG
     */
    public static class SHA1andWinternitzOTS_3 extends CMSSKeyPairGenerator {

        /**
         * The OID of the algorithm.
         */
        public static final String OID = "1.3.6.1.4.1.8301.3.1.3.2.3";

        /**
         * Constructor.
         */
        public SHA1andWinternitzOTS_3() {
            super(OID, new SHA1(), new WinternitzOTS(3), false);
        }
    }

    /**
     * CMSSKeyPairGenerator with SHA1 message digest, Winternitz OTS with
     * parameter w=4, and SHA1PRNG
     */
    public static class SHA1andWinternitzOTS_4 extends CMSSKeyPairGenerator {

        /**
         * The OID of the algorithm.
         */
        public static final String OID = "1.3.6.1.4.1.8301.3.1.3.2.4";

        /**
         * Constructor.
         */
        public SHA1andWinternitzOTS_4() {
            super(OID, new SHA1(), new WinternitzOTS(4), false);
        }
    }

    /**
     * CMSSKeyPairGenerator with SHA256 message digest, Winternitz OTS with
     * parameter w=1, and SHA1PRNG
     */
    public static class SHA256andWinternitzOTS_1 extends CMSSKeyPairGenerator {

        /**
         * The OID of the algorithm.
         */
        public static final String OID = "1.3.6.1.4.1.8301.3.1.3.2.5";

        /**
         * Constructor.
         */
        public SHA256andWinternitzOTS_1() {
            super(OID, new SHA256(), new WinternitzOTS(1), false);
        }
    }

    /**
     * CMSSKeyPairGenerator with SHA256 message digest, Winternitz OTS with
     * parameter w=2, and SHA1PRNG
     */
    public static class SHA256andWinternitzOTS_2 extends CMSSKeyPairGenerator {

        /**
         * The OID of the algorithm.
         */
        public static final String OID = "1.3.6.1.4.1.8301.3.1.3.2.6";

        /**
         * Constructor.
         */
        public SHA256andWinternitzOTS_2() {
            super(OID, new SHA256(), new WinternitzOTS(2), false);
        }
    }

    /**
     * CMSSKeyPairGenerator with SHA256 message digest, Winternitz OTS with
     * parameter w=3, and SHA1PRNG
     */
    public static class SHA256andWinternitzOTS_3 extends CMSSKeyPairGenerator {

        /**
         * The OID of the algorithm.
         */
        public static final String OID = "1.3.6.1.4.1.8301.3.1.3.2.7";

        /**
         * Constructor.
         */
        public SHA256andWinternitzOTS_3() {
            super(OID, new SHA256(), new WinternitzOTS(3), false);
        }
    }

    /**
     * CMSSKeyPairGenerator with SHA256 message digest, Winternitz OTS with
     * parameter w=4, and SHA1PRNG
     */
    public static class SHA256andWinternitzOTS_4 extends CMSSKeyPairGenerator {

        /**
         * The OID of the algorithm.
         */
        public static final String OID = "1.3.6.1.4.1.8301.3.1.3.2.8";

        /**
         * Constructor.
         */
        public SHA256andWinternitzOTS_4() {
            super(OID, new SHA256(), new WinternitzOTS(4), false);
        }
    }

    /**
     * CMSSKeyPairGenerator with SHA384 message digest, Winternitz OTS with
     * parameter w=1, and SHA1PRNG
     */
    public static class SHA384andWinternitzOTS_1 extends CMSSKeyPairGenerator {

        /**
         * The OID of the algorithm.
         */
        public static final String OID = "1.3.6.1.4.1.8301.3.1.3.2.9";

        /**
         * Constructor.
         */
        public SHA384andWinternitzOTS_1() {
            super(OID, new SHA384(), new WinternitzOTS(1), false);
        }
    }

    /**
     * CMSSKeyPairGenerator with SHA384 message digest, Winternitz OTS with
     * parameter w=2, and SHA1PRNG
     */
    public static class SHA384andWinternitzOTS_2 extends CMSSKeyPairGenerator {

        /**
         * The OID of the algorithm.
         */
        public static final String OID = "1.3.6.1.4.1.8301.3.1.3.2.10";

        /**
         * Constructor.
         */
        public SHA384andWinternitzOTS_2() {
            super(OID, new SHA384(), new WinternitzOTS(2), false);
        }
    }

    /**
     * CMSSKeyPairGenerator with SHA384 message digest, Winternitz OTS with
     * parameter w=3, and SHA1PRNG
     */
    public static class SHA384andWinternitzOTS_3 extends CMSSKeyPairGenerator {

        /**
         * The OID of the algorithm.
         */
        public static final String OID = "1.3.6.1.4.1.8301.3.1.3.2.11";

        /**
         * Constructor.
         */
        public SHA384andWinternitzOTS_3() {
            super(OID, new SHA384(), new WinternitzOTS(3), false);
        }
    }

    /**
     * CMSSKeyPairGenerator with SHA384 message digest, Winternitz OTS with
     * parameter w=4, and SHA1PRNG
     */
    public static class SHA384andWinternitzOTS_4 extends CMSSKeyPairGenerator {

        /**
         * The OID of the algorithm.
         */
        public static final String OID = "1.3.6.1.4.1.8301.3.1.3.2.12";

        /**
         * Constructor.
         */
        public SHA384andWinternitzOTS_4() {
            super(OID, new SHA384(), new WinternitzOTS(4), false);
        }
    }

    /**
     * CMSSKeyPairGenerator with SHA512 message digest, Winternitz OTS with
     * parameter w=1, and SHA1PRNG
     */
    public static class SHA512andWinternitzOTS_1 extends CMSSKeyPairGenerator {

        /**
         * The OID of the algorithm.
         */
        public static final String OID = "1.3.6.1.4.1.8301.3.1.3.2.13";

        /**
         * Constructor.
         */
        public SHA512andWinternitzOTS_1() {
            super(OID, new SHA512(), new WinternitzOTS(1), false);
        }
    }

    /**
     * CMSSKeyPairGenerator with SHA512 message digest, Winternitz OTS with
     * parameter w=2, and SHA1PRNG
     */
    public static class SHA512andWinternitzOTS_2 extends CMSSKeyPairGenerator {

        /**
         * The OID of the algorithm.
         */
        public static final String OID = "1.3.6.1.4.1.8301.3.1.3.2.14";

        /**
         * Constructor.
         */
        public SHA512andWinternitzOTS_2() {
            super(OID, new SHA512(), new WinternitzOTS(2), false);
        }
    }

    /**
     * CMSSKeyPairGenerator with SHA512 message digest, Winternitz OTS with
     * parameter w=3, and SHA1PRNG
     */
    public static class SHA512andWinternitzOTS_3 extends CMSSKeyPairGenerator {

        /**
         * The OID of the algorithm.
         */
        public static final String OID = "1.3.6.1.4.1.8301.3.1.3.2.15";

        /**
         * Constructor.
         */
        public SHA512andWinternitzOTS_3() {
            super(OID, new SHA512(), new WinternitzOTS(3), false);
        }
    }

    /**
     * CMSSKeyPairGenerator with SHA512 message digest, Winternitz OTS with
     * parameter w=4, and SHA1PRNG
     */
    public static class SHA512andWinternitzOTS_4 extends CMSSKeyPairGenerator {

        /**
         * The OID of the algorithm.
         */
        public static final String OID = "1.3.6.1.4.1.8301.3.1.3.2.16";

        /**
         * Constructor.
         */
        public SHA512andWinternitzOTS_4() {
            super(OID, new SHA512(), new WinternitzOTS(4), false);
        }
    }

    /**
     * CMSSKeyPairGenerator with SWIFFTX224 message digest, Winternitz OTS with
     * parameter w=1, and SWIFFTX224PRNG
     */
    public static class SWIFFTX224andWinternitzOTS_1 extends
            CMSSKeyPairGenerator {

        /**
         * The OID of the algorithm.
         */
        public static final String OID = "1.3.6.1.4.1.8301.3.1.3.2.101";

        /**
         * Constructor.
         */
        public SWIFFTX224andWinternitzOTS_1() {
            super(OID, new SWIFFTX224(), new WinternitzOTS(1), false);
        }
    }

    /**
     * CMSSKeyPairGenerator with SWIFFTX224 message digest, Winternitz OTS with
     * parameter w=2, and SWIFFTX224PRNG
     */
    public static class SWIFFTX224andWinternitzOTS_2 extends
            CMSSKeyPairGenerator {

        /**
         * The OID of the algorithm.
         */
        public static final String OID = "1.3.6.1.4.1.8301.3.1.3.2.102";

        /**
         * Constructor.
         */
        public SWIFFTX224andWinternitzOTS_2() {
            super(OID, new SWIFFTX224(), new WinternitzOTS(2), false);
        }
    }

    /**
     * CMSSKeyPairGenerator with SWIFFTX224 message digest, Winternitz OTS with
     * parameter w=3, and SWIFFTX224PRNG
     */
    public static class SWIFFTX224andWinternitzOTS_3 extends
            CMSSKeyPairGenerator {

        /**
         * The OID of the algorithm.
         */
        public static final String OID = "1.3.6.1.4.1.8301.3.1.3.2.103";

        /**
         * Constructor.
         */
        public SWIFFTX224andWinternitzOTS_3() {
            super(OID, new SWIFFTX224(), new WinternitzOTS(3), false);
        }
    }

    /**
     * CMSSKeyPairGenerator with SWIFFTX224 message digest, Winternitz OTS with
     * parameter w=4, and SWIFFTX224PRNG
     */
    public static class SWIFFTX224andWinternitzOTS_4 extends
            CMSSKeyPairGenerator {

        /**
         * The OID of the algorithm.
         */
        public static final String OID = "1.3.6.1.4.1.8301.3.1.3.2.104";

        /**
         * Constructor.
         */
        public SWIFFTX224andWinternitzOTS_4() {
            super(OID, new SWIFFTX224(), new WinternitzOTS(4), false);
        }
    }

    /**
     * CMSSKeyPairGenerator with SWIFFTX256 message digest, Winternitz OTS with
     * parameter w=1, and SWIFFTX256PRNG
     */
    public static class SWIFFTX256andWinternitzOTS_1 extends
            CMSSKeyPairGenerator {

        /**
         * The OID of the algorithm.
         */
        public static final String OID = "1.3.6.1.4.1.8301.3.1.3.2.105";

        /**
         * Constructor.
         */
        public SWIFFTX256andWinternitzOTS_1() {
            super(OID, new SWIFFTX256(), new WinternitzOTS(1), false);
        }
    }

    /**
     * CMSSKeyPairGenerator with SWIFFTX256 message digest, Winternitz OTS with
     * parameter w=2, and SWIFFTX256PRNG
     */
    public static class SWIFFTX256andWinternitzOTS_2 extends
            CMSSKeyPairGenerator {

        /**
         * The OID of the algorithm.
         */
        public static final String OID = "1.3.6.1.4.1.8301.3.1.3.2.106";

        /**
         * Constructor.
         */
        public SWIFFTX256andWinternitzOTS_2() {
            super(OID, new SWIFFTX256(), new WinternitzOTS(2), false);
        }
    }

    /**
     * CMSSKeyPairGenerator with SWIFFTX256 message digest, Winternitz OTS with
     * parameter w=3, and SWIFFTX256PRNG
     */
    public static class SWIFFTX256andWinternitzOTS_3 extends
            CMSSKeyPairGenerator {

        /**
         * The OID of the algorithm.
         */
        public static final String OID = "1.3.6.1.4.1.8301.3.1.3.2.107";

        /**
         * Constructor.
         */
        public SWIFFTX256andWinternitzOTS_3() {
            super(OID, new SWIFFTX256(), new WinternitzOTS(3), false);
        }
    }

    /**
     * CMSSKeyPairGenerator with SWIFFTX256 message digest, Winternitz OTS with
     * parameter w=4, and SWIFFTX256PRNG
     */
    public static class SWIFFTX256andWinternitzOTS_4 extends
            CMSSKeyPairGenerator {

        /**
         * The OID of the algorithm.
         */
        public static final String OID = "1.3.6.1.4.1.8301.3.1.3.2.108";

        /**
         * Constructor.
         */
        public SWIFFTX256andWinternitzOTS_4() {
            super(OID, new SWIFFTX256(), new WinternitzOTS(4), false);
        }
    }

    /**
     * CMSSKeyPairGenerator with SWIFFTX384 message digest, Winternitz OTS with
     * parameter w=1, and SWIFFTX384PRNG
     */
    public static class SWIFFTX384andWinternitzOTS_1 extends
            CMSSKeyPairGenerator {

        /**
         * The OID of the algorithm.
         */
        public static final String OID = "1.3.6.1.4.1.8301.3.1.3.2.109";

        /**
         * Constructor.
         */
        public SWIFFTX384andWinternitzOTS_1() {
            super(OID, new SWIFFTX384(), new WinternitzOTS(1), false);
        }
    }

    /**
     * CMSSKeyPairGenerator with SWIFFTX384 message digest, Winternitz OTS with
     * parameter w=2, and SWIFFTX384PRNG
     */
    public static class SWIFFTX384andWinternitzOTS_2 extends
            CMSSKeyPairGenerator {

        /**
         * The OID of the algorithm.
         */
        public static final String OID = "1.3.6.1.4.1.8301.3.1.3.2.110";

        /**
         * Constructor.
         */
        public SWIFFTX384andWinternitzOTS_2() {
            super(OID, new SWIFFTX384(), new WinternitzOTS(2), false);
        }
    }

    /**
     * CMSSKeyPairGenerator with SWIFFTX384 message digest, Winternitz OTS with
     * parameter w=3, and SWIFFTX384PRNG
     */
    public static class SWIFFTX384andWinternitzOTS_3 extends
            CMSSKeyPairGenerator {

        /**
         * The OID of the algorithm.
         */
        public static final String OID = "1.3.6.1.4.1.8301.3.1.3.2.111";

        /**
         * Constructor.
         */
        public SWIFFTX384andWinternitzOTS_3() {
            super(OID, new SWIFFTX384(), new WinternitzOTS(3), false);
        }
    }

    /**
     * CMSSKeyPairGenerator with SWIFFTX384 message digest, Winternitz OTS with
     * parameter w=4, and SWIFFTX384PRNG
     */
    public static class SWIFFTX384andWinternitzOTS_4 extends
            CMSSKeyPairGenerator {

        /**
         * The OID of the algorithm.
         */
        public static final String OID = "1.3.6.1.4.1.8301.3.1.3.2.112";

        /**
         * Constructor.
         */
        public SWIFFTX384andWinternitzOTS_4() {
            super(OID, new SWIFFTX384(), new WinternitzOTS(4), false);
        }
    }

    /**
     * CMSSKeyPairGenerator with SWIFFTX512 message digest, Winternitz OTS with
     * parameter w=1, and SWIFFTX512PRNG
     */
    public static class SWIFFTX512andWinternitzOTS_1 extends
            CMSSKeyPairGenerator {

        /**
         * The OID of the algorithm.
         */
        public static final String OID = "1.3.6.1.4.1.8301.3.1.3.2.113";

        /**
         * Constructor.
         */
        public SWIFFTX512andWinternitzOTS_1() {
            super(OID, new SWIFFTX512(), new WinternitzOTS(1), false);
        }
    }

    /**
     * CMSSKeyPairGenerator with SWIFFTX512 message digest, Winternitz OTS with
     * parameter w=2, and SWIFFTX512PRNG
     */
    public static class SWIFFTX512andWinternitzOTS_2 extends
            CMSSKeyPairGenerator {

        /**
         * The OID of the algorithm.
         */
        public static final String OID = "1.3.6.1.4.1.8301.3.1.3.2.114";

        /**
         * Constructor.
         */
        public SWIFFTX512andWinternitzOTS_2() {
            super(OID, new SWIFFTX512(), new WinternitzOTS(2), false);
        }
    }

    /**
     * CMSSKeyPairGenerator with SWIFFTX512 message digest, Winternitz OTS with
     * parameter w=3, and SWIFFTX512PRNG
     */
    public static class SWIFFTX512andWinternitzOTS_3 extends
            CMSSKeyPairGenerator {

        /**
         * The OID of the algorithm.
         */
        public static final String OID = "1.3.6.1.4.1.8301.3.1.3.2.115";

        /**
         * Constructor.
         */
        public SWIFFTX512andWinternitzOTS_3() {
            super(OID, new SWIFFTX512(), new WinternitzOTS(3), false);
        }
    }

    /**
     * CMSSKeyPairGenerator with SWIFFTX512 message digest, Winternitz OTS with
     * parameter w=4, and SWIFFTX512PRNG
     */
    public static class SWIFFTX512andWinternitzOTS_4 extends
            CMSSKeyPairGenerator {

        /**
         * The OID of the algorithm.
         */
        public static final String OID = "1.3.6.1.4.1.8301.3.1.3.2.116";

        /**
         * Constructor.
         */
        public SWIFFTX512andWinternitzOTS_4() {
            super(OID, new SWIFFTX512(), new WinternitzOTS(4), false);
        }
    }

    // SPR classes


    //LM-OTS


    /**
     * CMSSKeyPairGenerator with SHA1 message digest, LMOTS OTS, and SHA1PRNG
     */
    public static class SHA1andLMOTS extends CMSSKeyPairGenerator {

        /**
         * The OID of the algorithm.
         */
        public static final String OID = "1.3.6.1.4.1.8301.3.1.3.2.117";

        /**
         * Constructor.
         */
        public SHA1andLMOTS() {
            super(OID, new SHA1(), new LMOTS(), false);
        }
    }


    /**
     * CMSSKeyPairGenerator with SHA256 message digest, LMOTS OTS, and SHA1PRNG
     */
    public static class SHA256andLMOTS extends CMSSKeyPairGenerator {

        /**
         * The OID of the algorithm.
         */
        public static final String OID = "1.3.6.1.4.1.8301.3.1.3.2.118";

        /**
         * Constructor.
         */
        public SHA256andLMOTS() {
            super(OID, new SHA256(), new LMOTS(), false);
        }
    }


    /**
     * CMSSKeyPairGenerator with SHA384 message digest, LMOTS OTS, and SHA1PRNG
     */
    public static class SHA384andLMOTS extends CMSSKeyPairGenerator {

        /**
         * The OID of the algorithm.
         */
        public static final String OID = "1.3.6.1.4.1.8301.3.1.3.2.119";

        /**
         * Constructor.
         */
        public SHA384andLMOTS() {
            super(OID, new SHA384(), new LMOTS(), false);
        }
    }

    /**
     * CMSSKeyPairGenerator with SHA512 message digest, LMOTS OTS, and SHA1PRNG
     */
    public static class SHA512andLMOTS extends CMSSKeyPairGenerator {

        /**
         * The OID of the algorithm.
         */
        public static final String OID = "1.3.6.1.4.1.8301.3.1.3.2.120";

        /**
         * Constructor.
         */
        public SHA512andLMOTS() {
            super(OID, new SHA512(), new LMOTS(), false);
        }
    }


    /**
     * CMSSKeyPairGenerator with SWIFFTX224 message digest, LMOTS OTS, and SWIFFTX224PRNG
     */
    public static class SWIFFTX224andLMOTS extends CMSSKeyPairGenerator {

        /**
         * The OID of the algorithm.
         */
        public static final String OID = "1.3.6.1.4.1.8301.3.1.3.2.121";

        /**
         * Constructor.
         */
        public SWIFFTX224andLMOTS() {
            super(OID, new SWIFFTX224(), new LMOTS(), false);
        }
    }

    /**
     * CMSSKeyPairGenerator with SWIFFTX256 message digest, LMOTS OTS, and SWIFFTX256PRNG
     */
    public static class SWIFFTX256andLMOTS extends CMSSKeyPairGenerator {

        /**
         * The OID of the algorithm.
         */
        public static final String OID = "1.3.6.1.4.1.8301.3.1.3.2.122";

        /**
         * Constructor.
         */
        public SWIFFTX256andLMOTS() {
            super(OID, new SWIFFTX256(), new LMOTS(), false);
        }
    }

    /**
     * CMSSKeyPairGenerator with SWIFFTX384 message digest, LMOTS OTS, and SWIFFTX384PRNG
     */
    public static class SWIFFTX384andLMOTS extends CMSSKeyPairGenerator {

        /**
         * The OID of the algorithm.
         */
        public static final String OID = "1.3.6.1.4.1.8301.3.1.3.2.123";

        /**
         * Constructor.
         */
        public SWIFFTX384andLMOTS() {
            super(OID, new SWIFFTX384(), new LMOTS(), false);
        }
    }

    /**
     * CMSSKeyPairGenerator with SWIFFTX512 message digest, LMOTS OTS, and SWIFFTX512PRNG
     */
    public static class SWIFFTX512andLMOTS extends CMSSKeyPairGenerator {

        /**
         * The OID of the algorithm.
         */
        public static final String OID = "1.3.6.1.4.1.8301.3.1.3.2.124";

        /**
         * Constructor.
         */
        public SWIFFTX512andLMOTS() {
            super(OID, new SWIFFTX512(), new LMOTS(), false);
        }
    }


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
