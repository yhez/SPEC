package de.flexiprovider.ec.parameters;

import java.security.spec.AlgorithmParameterSpec;

import de.flexiprovider.common.math.FlexiBigInt;
import de.flexiprovider.common.math.ellipticcurves.EllipticCurve;
import de.flexiprovider.common.math.ellipticcurves.Point;
import de.flexiprovider.common.math.finitefields.GFPElement;
import de.flexiprovider.common.util.ByteUtils;


public class CurveParamsGFP implements AlgorithmParameterSpec {
    FlexiBigInt q;
    EllipticCurve E;
    Point g;
    private FlexiBigInt r;
    private int k;


    public FlexiBigInt getQ() {
        return q;
    }

    public EllipticCurve getE() {
        return E;
    }

    public Point getG() {
        return (Point) g.clone();
    }

    public FlexiBigInt getR() {
        return r;
    }

    public int getK() {
        return k;
    }

    public int hashCode() {
        int oidHashCode = 0;
        int qHashCode = 0;
        int eHashCode = 0;
        int gHashCode = 0;
        int rHashCode = 0;
        int kHashCode = k;
        if (q != null) {
            qHashCode = q.hashCode();
        }
        if (E != null) {
            eHashCode = E.hashCode();
        }
        if (g != null) {
            gHashCode = g.hashCode();
        }
        if (r != null) {
            rHashCode = r.hashCode();
        }
        return oidHashCode + qHashCode + eHashCode + gHashCode + rHashCode + kHashCode;
    }

    public CurveParamsGFP() {
        // order of basepoint G
        String s = "AADD9DB8DBE9C48B3FD4E6AE33C9FC07CB308DB3B3C9D20ED6639CCA70330870553E5C414CA92619418661197FAC10471DB1D381085DDADDB58796829CA90069";
        this.r = new FlexiBigInt(s, 16);
        //cofactor k
        this.k = Integer.valueOf("1", 16);
        // prime p
        s = "AADD9DB8DBE9C48B3FD4E6AE33C9FC07CB308DB3B3C9D20ED6639CCA703308717D4D9B009BC66842AECDA12AE6A380E62881FF2F2D82C68528AA6056583A48F3";
        this.q = new FlexiBigInt(s, 16);
        // curve coefficient a
        s = "7830A3318B603B89E2327145AC234CC594CBDD8D3DF91610A83441CAEA9863BC2DED5D5AA8253AA10A2EF1C98B9AC8B57F1117A72BF2C7B9E7C1AC4D77FC94CA";
        byte[] encA = ByteUtils.fromHexString(s);
        GFPElement mA = new GFPElement(encA, this.q);
        // curve coefficient b
        s = "3DF91610A83441CAEA9863BC2DED5D5AA8253AA10A2EF1C98B9AC8B57F1117A72BF2C7B9E7C1AC4D77FC94CADC083E67984050B75EBAE5DD2809BD638016F723";
        byte[] encB = ByteUtils.fromHexString(s);
        GFPElement mB = new GFPElement(encB, this.q);
        E = new EllipticCurve(mA, mB, this.q);
        // basepoint G
        s = "0481AEE4BDD82ED9645A21322E9C4C6A9385ED9F70B5D916C1B43B62EEF4D0098EFF3B1F78E2D0D48D50D1687B93B97D5F7C6D5047406A5E688B352209BCB9F8227DDE385D566332ECC0EABFA9CF7822FDF209F70024A57B1AA000C55B881F8111B2DCDE494A5F485E5BCA4BD88A2763AED1CA2B2FA8F0540678CD1E0F3AD80892";
        byte[] encG = ByteUtils.fromHexString(s);
        this.g = new Point(encG, E);
    }

    public boolean equals(Object other) {
        if ((other == null) || !(other instanceof CurveParamsGFP)) {
            return false;
        }

        CurveParamsGFP otherParams = (CurveParamsGFP) other;
        return q.equals(otherParams.q)
                && E.equals(otherParams.E) && g.equals(otherParams.g)
                && r.equals(otherParams.r) && (k == otherParams.k);
    }
}

