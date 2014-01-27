/*
 * Copyright (c) 1998-2003 by The FlexiProvider Group,
 *                            Technische Universitaet Darmstadt 
 *
 * For conditions of usage and distribution please refer to the
 * file COPYING in the root directory of this package.
 *
 */
package de.flexiprovider.common.math;

import de.flexiprovider.common.exceptions.NoQuadraticResidueException;

/**
 * Class of number-theory related functions for use with integers represented as
 * <tt>int</tt>'s or <tt>FlexiBigInt</tt> objects.
 *
 * @author Ralf-P. Weinmann
 * @author Martin Dring
 */
public final class IntegerFunctions {

    private static final FlexiBigInt ZERO = FlexiBigInt.ZERO;

    private static final FlexiBigInt ONE = FlexiBigInt.ONE;

    private static final FlexiBigInt TWO = FlexiBigInt.valueOf(2);

    private static final FlexiBigInt FOUR = FlexiBigInt.valueOf(4);

    private static final int[] SMALL_PRIMES = {3, 5, 7, 11, 13, 17, 19, 23,
            29, 31, 37, 41};

    // the jacobi function uses this lookup table
    private static final int[] jacobiTable = {0, 1, 0, -1, 0, -1, 0, 1};

    private IntegerFunctions() {
        // empty
    }


    public static int jacobi(FlexiBigInt A, FlexiBigInt B) {
        FlexiBigInt a, b, v;
        long k;

        k = 1;

        // test trivial cases
        if (B.equals(ZERO)) {
            a = A.abs();
            return a.equals(ONE) ? 1 : 0;
        }

        if (!A.testBit(0) && !B.testBit(0)) {
            return 0;
        }

        a = A;
        b = B;

        if (b.signum() == -1) { // b < 0
            b = b.negate(); // b = -b
            if (a.signum() == -1) {
                k = -1;
            }
        }

        v = ZERO;
        while (!b.testBit(0)) {
            v = v.add(ONE); // v = v + 1
            b = b.divide(TWO); // b = b/2
        }

        if (v.testBit(0)) {
            k = k * jacobiTable[a.intValue() & 7];
        }

        if (a.signum() < 0) { // a < 0
            if (b.testBit(1)) {
                k = -k; // k = -k
            }
            a = a.negate(); // a = -a
        }

        // main loop
        while (a.signum() != 0) {
            v = ZERO;
            while (!a.testBit(0)) { // a is even
                v = v.add(ONE);
                a = a.divide(TWO);
            }
            if (v.testBit(0)) {
                k = k * jacobiTable[b.intValue() & 7];
            }

            if (a.compareTo(b) < 0) { // a < b
                // swap and correct intermediate result
                FlexiBigInt x = a;
                a = b;
                b = x;
                if (a.testBit(1) && b.testBit(1)) {
                    k = -k;
                }
            }
            a = a.subtract(b);
        }

        return b.equals(ONE) ? (int) k : 0;
    }


    public static FlexiBigInt ressol(FlexiBigInt a, FlexiBigInt p)
            throws NoQuadraticResidueException {

        FlexiBigInt v;

        if (a.compareTo(ZERO) < 0) {
            a = a.add(p);
        }

        if (a.equals(ZERO)) {
            return ZERO;
        }

        if (p.equals(TWO)) {
            return a;
        }

        // p = 3 mod 4
        if (p.testBit(0) && p.testBit(1)) {
            if (jacobi(a, p) == 1) { // a quadr. residue mod p
                v = p.add(ONE); // v = p+1
                v = v.shiftRight(2); // v = v/4
                return a.modPow(v, p); // return a^v mod p
                // return --> a^((p+1)/4) mod p
            }
            throw new NoQuadraticResidueException(a, p);
        }

        long t;

        // initialization
        // compute k and s, where p = 2^s (2k+1) +1

        FlexiBigInt k = p.subtract(ONE); // k = p-1
        long s = 0;
        while (!k.testBit(0)) { // while k is even
            s++; // s = s+1
            k = k.shiftRight(1); // k = k/2
        }

        k = k.subtract(ONE); // k = k - 1
        k = k.shiftRight(1); // k = k/2

        // initial values
        FlexiBigInt r = a.modPow(k, p); // r = a^k mod p

        FlexiBigInt n = r.multiply(r).remainder(p); // n = r^2 % p
        n = n.multiply(a).remainder(p); // n = n * a % p
        r = r.multiply(a).remainder(p); // r = r * a %p

        if (n.equals(ONE)) {
            return r;
        }

        // non-quadratic residue
        FlexiBigInt z = TWO; // z = 2
        while (jacobi(z, p) == 1) {
            // while z quadratic residue
            z = z.add(ONE); // z = z + 1
        }

        v = k;
        v = v.multiply(TWO); // v = 2k
        v = v.add(ONE); // v = 2k + 1
        FlexiBigInt c = z.modPow(v, p); // c = z^v mod p

        // iteration
        while (n.compareTo(ONE) == 1) { // n > 1
            k = n; // k = n
            t = s; // t = s
            s = 0;

            while (!k.equals(ONE)) { // k != 1
                k = k.multiply(k).mod(p); // k = k^2 % p
                s++; // s = s + 1
            }

            t -= s; // t = t - s
            if (t == 0) {
                throw new NoQuadraticResidueException(a, p);
            }

            v = ONE;
            for (long i = 0; i < t - 1; i++) {
                v = v.shiftLeft(1); // v = 1 * 2^(t - 1)
            }
            c = c.modPow(v, p); // c = c^v mod p
            r = r.multiply(c).remainder(p); // r = r * c % p
            c = c.multiply(c).remainder(p); // c = c^2 % p
            n = n.multiply(c).mod(p); // n = n * c % p
        }
        return r;
    }

    /**
     * Computes the greatest common divisor of the two specified integers
     *
     * @param u - first integer
     * @param v - second integer
     * @return gcd(a, b)
     */
    public static int gcd(int u, int v) {
        return FlexiBigInt.valueOf(u).gcd(FlexiBigInt.valueOf(v)).intValue();
    }

    /**
     * Extended euclidian algorithm (computes gcd and representation).
     *
     * @param a the first integer
     * @param b the second integer
     * @return <tt>(g,u,v)</tt>, where <tt>g = gcd(abs(a),abs(b)) = ua + vb</tt>
     */
    public static int[] extGCD(int a, int b) {
        FlexiBigInt ba = FlexiBigInt.valueOf(a);
        FlexiBigInt bb = FlexiBigInt.valueOf(b);
        FlexiBigInt[] bresult = extgcd(ba, bb);
        int[] result = new int[3];
        result[0] = bresult[0].intValue();
        result[1] = bresult[1].intValue();
        result[2] = bresult[2].intValue();
        return result;
    }

    public static FlexiBigInt divideAndRound(FlexiBigInt a, FlexiBigInt b) {
        if (a.signum() < 0) {
            return divideAndRound(a.negate(), b).negate();
        }
        if (b.signum() < 0) {
            return divideAndRound(a, b.negate()).negate();
        }
        return a.shiftLeft(1).add(b).divide(b.shiftLeft(1));
    }

    public static int ceilLog256(int n) {
        if (n == 0) {
            return 1;
        }
        int m;
        if (n < 0) {
            m = -n;
        } else {
            m = n;
        }

        int d = 0;
        while (m > 0) {
            d++;
            m >>>= 8;
        }
        return d;
    }

    public static int floorLog(int a) {
        int h = 0;
        if (a <= 0) {
            return -1;
        }
        int p = a >>> 1;
        while (p > 0) {
            h++;
            p >>>= 1;
        }

        return h;
    }

    public static int order(int g, int p) {
        int b, j;

        b = g % p; // Reduce g mod p first.
        j = 1;

        // Check whether g == 0 mod p (avoiding endless loop).
        if (b == 0) {
            throw new IllegalArgumentException(g + " is not an element of Z/("
                    + p + "Z)^*; it is not meaningful to compute its order.");
        }

        // Compute the order of g mod p:
        while (b != 1) {
            b *= g;
            b %= p;
            if (b < 0) {
                b += p;
            }
            j++;
        }

        return j;
    }

    /**
     * Compute <tt>a<sup>e</sup></tt>.
     *
     * @param a the base
     * @param e the exponent
     * @return <tt>a<sup>e</sup></tt>
     */
    public static int pow(int a, int e) {
        int result = 1;
        while (e > 0) {
            if ((e & 1) == 1) {
                result *= a;
            }
            a *= a;
            e >>>= 1;
        }
        return result;
    }

    public static FlexiBigInt[] extgcd(FlexiBigInt a, FlexiBigInt b) {
        FlexiBigInt u = FlexiBigInt.ONE;
        FlexiBigInt v = FlexiBigInt.ZERO;
        FlexiBigInt d = a;
        if (b.signum() != 0) {
            FlexiBigInt v1 = FlexiBigInt.ZERO;
            FlexiBigInt v3 = b;
            while (v3.signum() != 0) {
                FlexiBigInt[] tmp = d.divideAndRemainder(v3);
                FlexiBigInt q = tmp[0];
                FlexiBigInt t3 = tmp[1];
                FlexiBigInt t1 = u.subtract(q.multiply(v1));
                u = v1;
                d = v3;
                v1 = t1;
                v3 = t3;
            }
            v = d.subtract(a.multiply(u)).divide(b);
        }
        return new FlexiBigInt[]{d, u, v};
    }

    public static long mod(long a, long m) {
        long result = a % m;
        if (result < 0) {
            result += m;
        }
        return result;
    }

    public static int modInverse(int a, int mod) {
        return FlexiBigInt.valueOf(a).modInverse(FlexiBigInt.valueOf(mod))
                .intValue();
    }

    public static long modInverse(long a, long mod) {
        return FlexiBigInt.valueOf(a).modInverse(FlexiBigInt.valueOf(mod))
                .longValue();
    }

    public static boolean isPrime(int n) {
        if (n < 2) {
            return false;
        }
        if (n == 2) {
            return true;
        }
        if ((n & 1) == 0) {
            return false;
        }
        if (n < 42) {
            for (int i = 0; i < SMALL_PRIMES.length; i++) {
                if (n == SMALL_PRIMES[i]) {
                    return true;
                }
            }
        }

        return !((n % 3 == 0) || (n % 5 == 0) || (n % 7 == 0) || (n % 11 == 0) || (n % 13 == 0) || (n % 17 == 0) || (n % 19 == 0) || (n % 23 == 0) || (n % 29 == 0) || (n % 31 == 0) || (n % 37 == 0) || (n % 41 == 0)) && FlexiBigInt.valueOf(n).isProbablePrime(20);

    }


    public static FlexiBigInt squareRoot(FlexiBigInt a) {
        int bl;
        FlexiBigInt result, remainder, b;

        if (a.compareTo(ZERO) < 0) {
            throw new ArithmeticException(
                    "cannot extract root of negative number" + a + ".");
        }

        bl = a.bitLength();
        result = ZERO;
        remainder = ZERO;

        // if the bit length is odd then extra step
        if ((bl & 1) != 0) {
            result = result.add(ONE);
            bl--;
        }

        while (bl > 0) {
            remainder = remainder.multiply(FOUR);
            remainder = remainder.add(FlexiBigInt.valueOf((a.testBit(--bl) ? 2
                    : 0)
                    + (a.testBit(--bl) ? 1 : 0)));
            b = result.multiply(FOUR).add(ONE);
            result = result.multiply(TWO);
            if (remainder.compareTo(b) != -1) {
                result = result.add(ONE);
                remainder = remainder.subtract(b);
            }
        }

        return result;
    }

    public static float intRoot(int base, int root) {
        float gNew = base / root;
        float gOld = 0;
        while (Math.abs(gOld - gNew) > 0.0001) {
            float gPow = floatPow(gNew, root);
            while (Float.isInfinite(gPow)) {
                gNew = (gNew + gOld) / 2;
                gPow = floatPow(gNew, root);
            }
            gOld = gNew;
            gNew = gOld - (gPow - base) / (root * floatPow(gOld, root - 1));
        }
        return gNew;
    }

    public static float floatLog(float param) {
        double arg = (param - 1) / (param + 1);
        double arg2 = arg;
        int counter = 1;
        float result = (float) arg;

        while (arg2 > 0.001) {
            counter += 2;
            arg2 *= arg * arg;
            result += (1. / counter) * arg2;
        }
        return 2 * result;
    }


    public static float floatPow(float f, int i) {
        float g = 1;
        for (; i > 0; i--) {
            g *= f;
        }
        return g;
    }
    public static double log(double x) {
        if (x > 0 && x < 1) {
            double d = 1 / x;
            return -log(d);
        }

        int tmp = 0;
        double tmp2 = 1;
        double d = x;

        while (d > 2) {
            d = d / 2;
            tmp += 1;
            tmp2 *= 2;
        }
        double rem = x / tmp2;
        rem = logBKM(rem);
        return tmp + rem;
    }

    private static double logBKM(double arg) {
        double ae[] = // A_e[k] = log_2 (1 + 0.5^k)
                {
                        1.0000000000000000000000000000000000000000000000000000000000000000000000000000,
                        0.5849625007211561814537389439478165087598144076924810604557526545410982276485,
                        0.3219280948873623478703194294893901758648313930245806120547563958159347765589,
                        0.1699250014423123629074778878956330175196288153849621209115053090821964552970,
                        0.0874628412503394082540660108104043540112672823448206881266090643866965081686,
                        0.0443941193584534376531019906736094674630459333742491317685543002674288465967,
                        0.0223678130284545082671320837460849094932677948156179815932199216587899627785,
                        0.0112272554232541203378805844158839407281095943600297940811823651462712311786,
                        0.0056245491938781069198591026740666017211096815383520359072957784732489771013,
                        0.0028150156070540381547362547502839489729507927389771959487826944878598909400,
                        0.0014081943928083889066101665016890524233311715793462235597709051792834906001,
                        0.0007042690112466432585379340422201964456668872087249334581924550139514213168,
                        0.0003521774803010272377989609925281744988670304302127133979341729842842377649,
                        0.0001760994864425060348637509459678580940163670081839283659942864068257522373,
                        0.0000880524301221769086378699983597183301490534085738474534831071719854721939,
                        0.0000440268868273167176441087067175806394819146645511899503059774914593663365,
                        0.0000220136113603404964890728830697555571275493801909791504158295359319433723,
                        0.0000110068476674814423006223021573490183469930819844945565597452748333526464,
                        0.0000055034343306486037230640321058826431606183125807276574241540303833251704,
                        0.0000027517197895612831123023958331509538486493412831626219340570294203116559,
                        0.0000013758605508411382010566802834037147561973553922354232704569052932922954,
                        0.0000006879304394358496786728937442939160483304056131990916985043387874690617,
                        0.0000003439652607217645360118314743718005315334062644619363447395987584138324,
                        0.0000001719826406118446361936972479533123619972434705828085978955697643547921,
                        0.0000000859913228686632156462565208266682841603921494181830811515318381744650,
                        0.0000000429956620750168703982940244684787907148132725669106053076409624949917,
                        0.0000000214978311976797556164155504126645192380395989504741781512309853438587,
                        0.0000000107489156388827085092095702361647949603617203979413516082280717515504,
                        0.0000000053744578294520620044408178949217773318785601260677517784797554422804,
                        0.0000000026872289172287079490026152352638891824761667284401180026908031182361,
                        0.0000000013436144592400232123622589569799954658536700992739887706412976115422,
                        0.0000000006718072297764289157920422846078078155859484240808550018085324187007,
                        0.0000000003359036149273187853169587152657145221968468364663464125722491530858,
                        0.0000000001679518074734354745159899223037458278711244127245990591908996412262,
                        0.0000000000839759037391617577226571237484864917411614198675604731728132152582,
                        0.0000000000419879518701918839775296677020135040214077417929807824842667285938,
                        0.0000000000209939759352486932678195559552767641474249812845414125580747434389,
                        0.0000000000104969879676625344536740142096218372850561859495065136990936290929,
                        0.0000000000052484939838408141817781356260462777942148580518406975851213868092,
                        0.0000000000026242469919227938296243586262369156865545638305682553644113887909,
                        0.0000000000013121234959619935994960031017850191710121890821178731821983105443,
                        0.0000000000006560617479811459709189576337295395590603644549624717910616347038,
                        0.0000000000003280308739906102782522178545328259781415615142931952662153623493,
                        0.0000000000001640154369953144623242936888032768768777422997704541618141646683,
                        0.0000000000000820077184976595619616930350508356401599552034612281802599177300,
                        0.0000000000000410038592488303636807330652208397742314215159774270270147020117,
                        0.0000000000000205019296244153275153381695384157073687186580546938331088730952,
                        0.0000000000000102509648122077001764119940017243502120046885379813510430378661,
                        0.0000000000000051254824061038591928917243090559919209628584150482483994782302,
                        0.0000000000000025627412030519318726172939815845367496027046030028595094737777,
                        0.0000000000000012813706015259665053515049475574143952543145124550608158430592,
                        0.0000000000000006406853007629833949364669629701200556369782295210193569318434,
                        0.0000000000000003203426503814917330334121037829290364330169106716787999052925,
                        0.0000000000000001601713251907458754080007074659337446341494733882570243497196,
                        0.0000000000000000800856625953729399268240176265844257044861248416330071223615,
                        0.0000000000000000400428312976864705191179247866966320469710511619971334577509,
                        0.0000000000000000200214156488432353984854413866994246781519154793320684126179,
                        0.0000000000000000100107078244216177339743404416874899847406043033792202127070,
                        0.0000000000000000050053539122108088756700751579281894640362199287591340285355,
                        0.0000000000000000025026769561054044400057638132352058574658089256646014899499,
                        0.0000000000000000012513384780527022205455634651853807110362316427807660551208,
                        0.0000000000000000006256692390263511104084521222346348012116229213309001913762,
                        0.0000000000000000003128346195131755552381436585278035120438976487697544916191,
                        0.0000000000000000001564173097565877776275512286165232838833090480508502328437,
                        0.0000000000000000000782086548782938888158954641464170239072244145219054734086,
                        0.0000000000000000000391043274391469444084776945327473574450334092075712154016,
                        0.0000000000000000000195521637195734722043713378812583900953755962557525252782,
                        0.0000000000000000000097760818597867361022187915943503728909029699365320287407,
                        0.0000000000000000000048880409298933680511176764606054809062553340323879609794,
                        0.0000000000000000000024440204649466840255609083961603140683286362962192177597,
                        0.0000000000000000000012220102324733420127809717395445504379645613448652614939,
                        0.0000000000000000000006110051162366710063906152551383735699323415812152114058,
                        0.0000000000000000000003055025581183355031953399739107113727036860315024588989,
                        0.0000000000000000000001527512790591677515976780735407368332862218276873443537,
                        0.0000000000000000000000763756395295838757988410584167137033767056170417508383,
                        0.0000000000000000000000381878197647919378994210346199431733717514843471513618,
                        0.0000000000000000000000190939098823959689497106436628681671067254111334889005,
                        0.0000000000000000000000095469549411979844748553534196582286585751228071408728,
                        0.0000000000000000000000047734774705989922374276846068851506055906657137209047,
                        0.0000000000000000000000023867387352994961187138442777065843718711089344045782,
                        0.0000000000000000000000011933693676497480593569226324192944532044984865894525,
                        0.0000000000000000000000005966846838248740296784614396011477934194852481410926,
                        0.0000000000000000000000002983423419124370148392307506484490384140516252814304,
                        0.0000000000000000000000001491711709562185074196153830361933046331030629430117,
                        0.0000000000000000000000000745855854781092537098076934460888486730708440475045,
                        0.0000000000000000000000000372927927390546268549038472050424734256652501673274,
                        0.0000000000000000000000000186463963695273134274519237230207489851150821191330,
                        0.0000000000000000000000000093231981847636567137259618916352525606281553180093,
                        0.0000000000000000000000000046615990923818283568629809533488457973317312233323,
                        0.0000000000000000000000000023307995461909141784314904785572277779202790023236,
                        0.0000000000000000000000000011653997730954570892157452397493151087737428485431,
                        0.0000000000000000000000000005826998865477285446078726199923328593402722606924,
                        0.0000000000000000000000000002913499432738642723039363100255852559084863397344,
                        0.0000000000000000000000000001456749716369321361519681550201473345138307215067,
                        0.0000000000000000000000000000728374858184660680759840775119123438968122488047,
                        0.0000000000000000000000000000364187429092330340379920387564158411083803465567,
                        0.0000000000000000000000000000182093714546165170189960193783228378441837282509,
                        0.0000000000000000000000000000091046857273082585094980096891901482445902524441,
                        0.0000000000000000000000000000045523428636541292547490048446022564529197237262,
                        0.0000000000000000000000000000022761714318270646273745024223029238091160103901};
        int n = 53;
        double x = 1;
        double y = 0;
        double z;
        double s = 1;
        int k;

        for (k = 0; k < n; k++) {
            z = x + x * s;
            if (z <= arg) {
                x = z;
                y += ae[k];
            }
            s *= 0.5;
        }
        return y;
    }

}
