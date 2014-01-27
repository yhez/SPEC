package de.flexiprovider.ec.parameters;

import java.io.IOException;

import codec.asn1.ASN1Exception;
import codec.asn1.ASN1Integer;
import codec.asn1.ASN1ObjectIdentifier;
import codec.asn1.ASN1OctetString;
import codec.asn1.ASN1Type;
import codec.asn1.ResolverException;
import de.flexiprovider.api.Registry;
import de.flexiprovider.api.exceptions.InvalidAlgorithmParameterException;
import de.flexiprovider.api.exceptions.InvalidParameterSpecException;
import de.flexiprovider.api.parameters.AlgorithmParameterSpec;
import de.flexiprovider.api.parameters.AlgorithmParameters;
import de.flexiprovider.common.exceptions.NoSuchBasisException;
import de.flexiprovider.common.exceptions.PolynomialIsNotIrreducibleException;
import de.flexiprovider.common.math.FlexiBigInt;
import de.flexiprovider.common.math.ellipticcurves.EllipticCurve;
import de.flexiprovider.common.math.ellipticcurves.EllipticCurveGF2n;
import de.flexiprovider.common.math.ellipticcurves.EllipticCurveGFP;
import de.flexiprovider.common.math.ellipticcurves.Point;
import de.flexiprovider.common.math.ellipticcurves.PointGF2n;
import de.flexiprovider.common.math.ellipticcurves.PointGFP;
import de.flexiprovider.common.math.finitefields.GF2Polynomial;
import de.flexiprovider.common.math.finitefields.GF2nElement;
import de.flexiprovider.common.math.finitefields.GF2nONBElement;
import de.flexiprovider.common.math.finitefields.GF2nONBField;
import de.flexiprovider.common.math.finitefields.GF2nPolynomialElement;
import de.flexiprovider.common.math.finitefields.GF2nPolynomialField;
import de.flexiprovider.common.math.finitefields.GFPElement;
import de.flexiprovider.common.util.ASN1Tools;
import de.flexiprovider.ec.asn1.CharacteristicTwoField;
import de.flexiprovider.ec.asn1.Curve;
import de.flexiprovider.ec.asn1.ECDomainParameters;
import de.flexiprovider.ec.asn1.FieldId;
import de.flexiprovider.ec.asn1.Parameters;
import de.flexiprovider.ec.asn1.PpBasis;
import de.flexiprovider.ec.asn1.PrimeField;
import de.flexiprovider.ec.parameters.CurveParams.CurveParamsGF2n;
import de.flexiprovider.ec.parameters.CurveParams.CurveParamsGF2nONB;
import de.flexiprovider.ec.parameters.CurveParams.CurveParamsGF2nPentanomial;
import de.flexiprovider.ec.parameters.CurveParams.CurveParamsGF2nTrinomial;
import de.flexiprovider.ec.parameters.CurveParams.CurveParamsGFP;


public class ECParameters extends AlgorithmParameters {


    public static final String OID = "1.2.840.10045.4.1";

    // the EC domain parameters
    private CurveParams curveParams;


    public void init(AlgorithmParameterSpec params)
            throws InvalidParameterSpecException {

        if ((params == null) || !(params instanceof CurveParams)) {
            throw new InvalidParameterSpecException("unsupported type");
        }
        curveParams = (CurveParams) params;
    }


    public void init(byte[] encParams) throws IOException {

        Parameters params = new Parameters();
        try {
            ASN1Tools.derDecode(encParams, params);
        } catch (ASN1Exception asn1e) {
            throw new IOException("ASN1Exception: " + asn1e.getMessage());
        }
        ASN1Type type = params.getInnerType();

        if (type instanceof ASN1ObjectIdentifier) {
            try {
                AlgorithmParameterSpec curveParams = Registry
                        .getAlgParamSpec(type
                                .toString());
                init(curveParams);
            } catch (InvalidAlgorithmParameterException iape) {
                throw new IOException("InvalidAlgorithmParameterException: "
                        + iape.getMessage());
            } catch (InvalidParameterSpecException e) {
                // the parameters are correct and must be accepted
                throw new RuntimeException("internal error");
            }

        } else if (type instanceof ECDomainParameters) {

            ECDomainParameters asn1params = (ECDomainParameters) type;
            ASN1Type fieldParam;
            try {
                fieldParam = asn1params.getFieldId().getField().getInnerType();
            } catch (ResolverException re) {
                throw new IOException("ResolverException: " + re.getMessage());
            }

            FlexiBigInt r = asn1params.getR();
            int k = asn1params.getK();

            if (fieldParam instanceof PrimeField) {
                FlexiBigInt p = ((PrimeField) fieldParam).getQ();
                GFPElement a = new GFPElement(asn1params.getA(), p);
                GFPElement b = new GFPElement(asn1params.getB(), p);
                PointGFP g = new PointGFP(asn1params.getG(),
                        new EllipticCurveGFP(a, b, p));

                curveParams = new CurveParamsGFP(g, r, k);

            } else if (fieldParam instanceof CharacteristicTwoField) {

                CharacteristicTwoField cFieldParams = (CharacteristicTwoField) fieldParam;
                int n = cFieldParams.getN();

                if (cFieldParams.isONB()) {
                    // ONB
                    GF2nONBField onbField = new GF2nONBField(n);
                    GF2nElement a = new GF2nONBElement(onbField, asn1params
                            .getA());
                    GF2nElement b = new GF2nONBElement(onbField, asn1params
                            .getB());
                    EllipticCurveGF2n E = new EllipticCurveGF2n(a, b, n);
                    PointGF2n g = new PointGF2n(asn1params.getG(), E);

                    curveParams = new CurveParamsGF2nONB(g, r, n, k);

                } else if (cFieldParams.isTrinomial()) {
                    // create the field polynomial
                    int tc = cFieldParams.getTrinom().getTC();
                    int[] polBytes = new int[(n + 31) >> 5];

                    polBytes[0] = 1;
                    polBytes[tc >> 5] |= 1 << (tc & 0x1f);
                    polBytes[n >> 5] |= 1 << (n & 0x1f);
                    GF2Polynomial fieldPoly = new GF2Polynomial(n + 1, polBytes);

                    GF2nPolynomialField polyField;
                    try {
                        polyField = new GF2nPolynomialField(n, fieldPoly);
                    } catch (PolynomialIsNotIrreducibleException PINIExc) {
                        throw new NoSuchBasisException(PINIExc.getMessage());
                    }

                    GF2nElement a = new GF2nPolynomialElement(polyField,
                            asn1params.getA());
                    GF2nElement b = new GF2nPolynomialElement(polyField,
                            asn1params.getB());

                    EllipticCurveGF2n E = new EllipticCurveGF2n(a, b, n);
                    PointGF2n g = new PointGF2n(asn1params.getG(), E);

                    curveParams = new CurveParamsGF2nTrinomial(g, r, n, k, tc);

                } else if (cFieldParams.isPentanomial()) {
                    // create the field polynomial
                    PpBasis pBasis = cFieldParams.getPenta();
                    int pc1 = pBasis.getPC1();
                    int pc2 = pBasis.getPC2();
                    int pc3 = pBasis.getPC3();
                    int[] polBytes = new int[(n + 31) >> 5];
                    polBytes[0] = 1;
                    polBytes[pc1 >> 5] |= 1 << (pc1 & 0x1f);
                    polBytes[pc2 >> 5] |= 1 << (pc2 & 0x1f);
                    polBytes[pc3 >> 5] |= 1 << (pc3 & 0x1f);
                    polBytes[n >> 5] |= 1 << (n & 0x1f);
                    GF2Polynomial fieldPoly = new GF2Polynomial(n + 1, polBytes);

                    GF2nPolynomialField polyField;
                    try {
                        polyField = new GF2nPolynomialField(n, fieldPoly);
                    } catch (PolynomialIsNotIrreducibleException PINIExc) {
                        throw new NoSuchBasisException(PINIExc.getMessage());
                    }

                    GF2nElement a = new GF2nPolynomialElement(polyField,
                            asn1params.getA());
                    GF2nElement b = new GF2nPolynomialElement(polyField,
                            asn1params.getB());

                    EllipticCurveGF2n E = new EllipticCurveGF2n(a, b, n);
                    PointGF2n g = new PointGF2n(asn1params.getG(), E);

                    curveParams = new CurveParamsGF2nPentanomial(g, r, n, k,
                            pc1, pc2, pc3);
                }
            }

        } else {
            throw new IOException("invalid encoding");
        }
    }


    public void init(byte[] encParams, String format) throws IOException {
        if (!format.equals("ASN.1")) {
            throw new IOException("unsupported format");
        }
        init(encParams);
    }


    public byte[] getEncoded() {
        ASN1ObjectIdentifier oid = curveParams.getOID();

        // If the domain parameters are named (i.e., curve params OID is not
        // null), it is sufficient to encode this OID.
        if (oid != null) {
            return ASN1Tools.derEncode(oid);
        }

        // Otherwise, an object of type ASN1ECParameters has to be
        // built and encoded.
        return ASN1Tools.derEncode(getASN1Params());
    }


    public byte[] getEncoded(String format) throws IOException {
        if (!format.equals("ASN.1")) {
            throw new IOException("Unsupported encoding format.");
        }
        return getEncoded();
    }


    public AlgorithmParameterSpec getParameterSpec(Class paramSpec)
            throws InvalidParameterSpecException {

        if ((paramSpec == null)
                || !(paramSpec.isAssignableFrom(((Object)curveParams).getClass()))) {
            throw new InvalidParameterSpecException("unsupported type");
        }

        return curveParams;
    }


    public String toString() {
        return ((Object)curveParams).toString();
    }


    public ECDomainParameters getASN1Params() {

        // create the curve E with coefficients a and b
        EllipticCurve E = curveParams.getE();
        Curve aE = new Curve(E.getA(), E.getB());

        // basepoint G
        byte[] gBytes = curveParams.getG().EC2OSP(
                Point.ENCODING_TYPE_UNCOMPRESSED);
        ASN1OctetString aG = new ASN1OctetString(gBytes);

        // The order of the basepoint.
        ASN1Integer aR = new ASN1Integer(1, curveParams.getR().toByteArray());

        // The optional cofactor of the basepoint.
        ASN1Integer aK = new ASN1Integer(curveParams.getK());

        FieldId aFieldID;

        if (curveParams instanceof CurveParamsGFP) {
            PrimeField aPF = new PrimeField(curveParams.getQ());
            aFieldID = new FieldId(aPF);

        } else if (curveParams instanceof CurveParamsGF2nONB) {
            CharacteristicTwoField aCF = new CharacteristicTwoField(
                    ((CurveParamsGF2n) curveParams).getN());
            aFieldID = new FieldId(aCF);

        } else if (curveParams instanceof CurveParamsGF2nTrinomial) {
            CurveParamsGF2nTrinomial trinomialParams = (CurveParamsGF2nTrinomial) curveParams;
            CharacteristicTwoField aCTF = new CharacteristicTwoField(
                    trinomialParams.getN(), trinomialParams.getTC());
            aFieldID = new FieldId(aCTF);

        } else if (curveParams instanceof CurveParamsGF2nPentanomial) {
            CurveParamsGF2nPentanomial pentanomialParams = (CurveParamsGF2nPentanomial) curveParams;
            CharacteristicTwoField aCPF = new CharacteristicTwoField(
                    pentanomialParams.getN(), pentanomialParams.getPC1(),
                    pentanomialParams.getPC2(), pentanomialParams.getPC3());
            aFieldID = new FieldId(aCPF);

        } else {
            aFieldID = new FieldId();
        }

        return new ECDomainParameters(aFieldID, aE, aG, aR, aK);
    }

}
