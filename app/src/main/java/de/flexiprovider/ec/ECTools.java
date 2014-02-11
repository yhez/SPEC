package de.flexiprovider.ec;

import java.security.InvalidKeyException;

import de.flexiprovider.common.math.ellipticcurves.Point;
import de.flexiprovider.ec.keys.ECPublicKey;

public final class ECTools {


    private ECTools() {
        // empty
    }


    public static boolean isValidPublicKey(ECPublicKey ecPubKey)
            throws InvalidKeyException {
        Point q = ecPubKey.getW();
        return !q.isZero() && q.onCurve();
    }

}
