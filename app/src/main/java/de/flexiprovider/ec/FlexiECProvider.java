package de.flexiprovider.ec;

import de.flexiprovider.api.FlexiProvider;
import de.flexiprovider.ec.keys.ECKeyFactory;
import de.flexiprovider.ec.keys.ECKeyPairGenerator;
import de.flexiprovider.ec.parameters.ECParameters;


public class FlexiECProvider extends FlexiProvider {

    private static final String INFO = "ECDSA, ECNR, ECDH, and ECIES";


    public FlexiECProvider() {
        super("FlexiEC", 1.76, INFO);


        ECRegistry.registerAlgorithms();

        registerCommon();
        registerECIES();
    }

    private void registerCommon() {
        add(KEY_PAIR_GENERATOR, ECKeyPairGenerator.class, new String[]{"EC",
                /*"ECDSA", "ECNR",*/ "ECIES", "ECDH", ECKeyFactory.OID});
        addReverseOID(KEY_PAIR_GENERATOR, "EC", ECKeyFactory.OID);

        add(KEY_FACTORY, ECKeyFactory.class, new String[]{"EC",/* "ECDSA",
                "ECNR",*/ "ECIES", "ECDH", ECKeyFactory.OID});
        addReverseOID(KEY_FACTORY, "EC", ECKeyFactory.OID);

        add(ALG_PARAMS, ECParameters.class, new String[]{"EC",
                ECParameters.OID, ECKeyFactory.OID,/* "ECDSA", "SHA1withECDSA",
                "SHA1/ECDSA", ECDSASignature.SHA1.OID, "SHA224withECDSA",
                "SHA224/ECDSA", ECDSASignature.SHA224.OID, "SHA256withECDSA",
                "SHA256/ECDSA", ECDSASignature.SHA256.OID, "SHA384withECDSA",
                "SHA384/ECDSA", ECDSASignature.SHA384.OID, "SHA512withECDSA",
                "SHA512/ECDSA", ECDSASignature.SHA512.OID, "RawECDSA",
                "RAWECDSA", "ECNR", "SHA1withECNR", "SHA1/ECNR",*/ "ECDH"});
        addReverseOID(ALG_PARAMS, "EC", ECParameters.OID);
    }

    private void registerECIES() {
        add(CIPHER, ECIES.class, new String[]{"ECIES", "IES"});
    }

}
