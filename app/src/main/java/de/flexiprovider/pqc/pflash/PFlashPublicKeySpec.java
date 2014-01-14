package de.flexiprovider.pqc.pflash;

import de.flexiprovider.api.keys.KeySpec;

public class PFlashPublicKeySpec implements KeySpec {

    private String oid;

    public PFlashPublicKeySpec(String oid) {
        this.oid = oid;
    }

    public String getOIDString() {
        return oid;
    }
}
