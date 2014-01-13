package my;

import java.util.Arrays;

import static my.BigInteger.LONG_MASK;

public class BigDecimal extends Number implements Comparable<BigDecimal> {

    private volatile BigInteger intVal;


    private int scale;  // Note: this may have any value, so
    // calculations must be done in longs

    private transient int precision;

    private transient String stringCache;

    static final long INFLATED = Long.MIN_VALUE;

    private transient long intCompact;

    // All 18-digit base ten strings fit into a long; not all 19-digit
    // strings will
    private static final int MAX_COMPACT_DIGITS = 18;

    /* Appease the serialization gods */
    private static final long serialVersionUID = 6108874887143696463L;

    private static final ThreadLocal<StringBuilderHelper>
            threadLocalStringBuilderHelper = new ThreadLocal<StringBuilderHelper>() {
        @Override
        protected StringBuilderHelper initialValue() {
            return new StringBuilderHelper();
        }
    };

    private static final BigDecimal zeroThroughTen[] = {
            new BigDecimal(BigInteger.ZERO, 0, 0, 1),
            new BigDecimal(BigInteger.ONE, 1, 0, 1),
            new BigDecimal(BigInteger.valueOf(2), 2, 0, 1),
            new BigDecimal(BigInteger.valueOf(3), 3, 0, 1),
            new BigDecimal(BigInteger.valueOf(4), 4, 0, 1),
            new BigDecimal(BigInteger.valueOf(5), 5, 0, 1),
            new BigDecimal(BigInteger.valueOf(6), 6, 0, 1),
            new BigDecimal(BigInteger.valueOf(7), 7, 0, 1),
            new BigDecimal(BigInteger.valueOf(8), 8, 0, 1),
            new BigDecimal(BigInteger.valueOf(9), 9, 0, 1),
            new BigDecimal(BigInteger.TEN, 10, 0, 2),
    };

    // Cache of zero scaled by 0 - 15
    private static final BigDecimal[] ZERO_SCALED_BY = {
            zeroThroughTen[0],
            new BigDecimal(BigInteger.ZERO, 0, 1, 1),
            new BigDecimal(BigInteger.ZERO, 0, 2, 1),
            new BigDecimal(BigInteger.ZERO, 0, 3, 1),
            new BigDecimal(BigInteger.ZERO, 0, 4, 1),
            new BigDecimal(BigInteger.ZERO, 0, 5, 1),
            new BigDecimal(BigInteger.ZERO, 0, 6, 1),
            new BigDecimal(BigInteger.ZERO, 0, 7, 1),
            new BigDecimal(BigInteger.ZERO, 0, 8, 1),
            new BigDecimal(BigInteger.ZERO, 0, 9, 1),
            new BigDecimal(BigInteger.ZERO, 0, 10, 1),
            new BigDecimal(BigInteger.ZERO, 0, 11, 1),
            new BigDecimal(BigInteger.ZERO, 0, 12, 1),
            new BigDecimal(BigInteger.ZERO, 0, 13, 1),
            new BigDecimal(BigInteger.ZERO, 0, 14, 1),
            new BigDecimal(BigInteger.ZERO, 0, 15, 1),
    };

    // Half of Long.MIN_VALUE & Long.MAX_VALUE.
    private static final long HALF_LONG_MAX_VALUE = Long.MAX_VALUE / 2;
    private static final long HALF_LONG_MIN_VALUE = Long.MIN_VALUE / 2;
    public static final BigDecimal ZERO =
            zeroThroughTen[0];

    public static final BigDecimal ONE =
            zeroThroughTen[1];

    BigDecimal(BigInteger intVal, long val, int scale, int prec) {
        this.scale = scale;
        this.precision = prec;
        this.intCompact = val;
        this.intVal = intVal;
    }

    public BigDecimal(char[] in, int offset, int len) {
        // protect against huge length.
        if (offset + len > in.length || offset < 0)
            throw new NumberFormatException();

        int prec = 0;                 // record precision value
        int scl = 0;                  // record scale value
        long rs = 0;                  // the compact value in long
        BigInteger rb = null;         // the inflated value in BigInteger

        // use array bounds checking to handle too-long, len == 0,
        // bad offset, etc.
        try {
            // handle the sign
            boolean isneg = false;          // assume positive
            if (in[offset] == '-') {
                isneg = true;               // leading minus means negative
                offset++;
                len--;
            } else if (in[offset] == '+') { // leading + allowed
                offset++;
                len--;
            }

            // should now be at numeric part of the significand
            boolean dot = false;             // true when there is a '.'
            long exp = 0;                    // exponent
            char c;                          // current character

            boolean isCompact = (len <= MAX_COMPACT_DIGITS);
            // integer significand array & idx is the index to it. The array
            // is ONLY used when we can't use a compact representation.
            char coeff[] = isCompact ? null : new char[len];
            int idx = 0;

            for (; len > 0; offset++, len--) {
                c = in[offset];
                // have digit
                if ((c >= '0' && c <= '9') || Character.isDigit(c)) {
                    // First compact case, we need not to preserve the character
                    // and we can just compute the value in place.
                    if (isCompact) {
                        int digit = Character.digit(c, 10);
                        if (digit == 0) {
                            if (prec == 0)
                                prec = 1;
                            else if (rs != 0) {
                                rs *= 10;
                                ++prec;
                            } // else digit is a redundant leading zero
                        } else {
                            if (prec != 1 || rs != 0)
                                ++prec; // prec unchanged if preceded by 0s
                            rs = rs * 10 + digit;
                        }
                    } else { // the unscaled value is likely a BigInteger object.
                        if (c == '0' || Character.digit(c, 10) == 0) {
                            if (prec == 0) {
                                coeff[idx] = c;
                                prec = 1;
                            } else if (idx != 0) {
                                coeff[idx++] = c;
                                ++prec;
                            } // else c must be a redundant leading zero
                        } else {
                            if (prec != 1 || idx != 0)
                                ++prec; // prec unchanged if preceded by 0s
                            coeff[idx++] = c;
                        }
                    }
                    if (dot)
                        ++scl;
                    continue;
                }
                // have dot
                if (c == '.') {
                    // have dot
                    if (dot)         // two dots
                        throw new NumberFormatException();
                    dot = true;
                    continue;
                }
                // exponent expected
                if ((c != 'e') && (c != 'E'))
                    throw new NumberFormatException();
                offset++;
                c = in[offset];
                len--;
                boolean negexp = (c == '-');
                // optional sign
                if (negexp || c == '+') {
                    offset++;
                    c = in[offset];
                    len--;
                }
                if (len <= 0)    // no exponent digits
                    throw new NumberFormatException();
                // skip leading zeros in the exponent
                while (len > 10 && Character.digit(c, 10) == 0) {
                    offset++;
                    c = in[offset];
                    len--;
                }
                if (len > 10)  // too many nonzero exponent digits
                    throw new NumberFormatException();
                // c now holds first digit of exponent
                for (; ; len--) {
                    int v;
                    if (c >= '0' && c <= '9') {
                        v = c - '0';
                    } else {
                        v = Character.digit(c, 10);
                        if (v < 0)            // not a digit
                            throw new NumberFormatException();
                    }
                    exp = exp * 10 + v;
                    if (len == 1)
                        break;               // that was final character
                    offset++;
                    c = in[offset];
                }
                if (negexp)                  // apply sign
                    exp = -exp;
                // Next test is required for backwards compatibility
                if ((int) exp != exp)         // overflow
                    throw new NumberFormatException();
                break;                       // [saves a test]
            }
            // here when no characters left
            if (prec == 0)              // no digits found
                throw new NumberFormatException();

            // Adjust scale if exp is not zero.
            if (exp != 0) {                  // had significant exponent
                // Can't call checkScale which relies on proper fields value
                long adjustedScale = scl - exp;
                if (adjustedScale > Integer.MAX_VALUE ||
                        adjustedScale < Integer.MIN_VALUE)
                    throw new NumberFormatException("Scale out of range.");
                scl = (int) adjustedScale;
            }

            // Remove leading zeros from precision (digits count)
            if (isCompact) {
                rs = isneg ? -rs : rs;
            } else {
                char quick[];
                if (!isneg) {
                    quick = (coeff.length != prec) ?
                            Arrays.copyOf(coeff, prec) : coeff;
                } else {
                    quick = new char[prec + 1];
                    quick[0] = '-';
                    System.arraycopy(coeff, 0, quick, 1, prec);
                }
                rb = new BigInteger(quick);
                rs = compactValFor(rb);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new NumberFormatException();
        } catch (NegativeArraySizeException e) {
            throw new NumberFormatException();
        }
        this.scale = scl;
        this.precision = prec;
        this.intCompact = rs;
        this.intVal = (rs != INFLATED) ? null : rb;
    }






    public BigDecimal(String val) {
        this(val.toCharArray(), 0, val.length());
    }


    public BigDecimal(BigInteger val) {
        intCompact = compactValFor(val);
        intVal = (intCompact != INFLATED) ? null : val;
    }


    public BigDecimal(BigInteger unscaledVal, int scale) {
        // Negative scales are now allowed
        this(unscaledVal);
        this.scale = scale;
    }




    // Static Factory Methods


    public static BigDecimal valueOf(long unscaledVal, int scale) {
        if (scale == 0)
            return valueOf(unscaledVal);
        else if (unscaledVal == 0) {
            if (scale > 0 && scale < ZERO_SCALED_BY.length)
                return ZERO_SCALED_BY[scale];
            else
                return new BigDecimal(BigInteger.ZERO, 0, scale, 1);
        }
        return new BigDecimal(unscaledVal == INFLATED ?
                BigInteger.valueOf(unscaledVal) : null,
                unscaledVal, scale, 0);
    }


    public static BigDecimal valueOf(long val) {
        if (val >= 0 && val < zeroThroughTen.length)
            return zeroThroughTen[(int) val];
        else if (val != INFLATED)
            return new BigDecimal(null, val, 0, 0);
        return new BigDecimal(BigInteger.valueOf(val), val, 0, 0);
    }


    public static BigDecimal valueOf(double val) {
        // Reminder: a zero double returns '0.0', so we cannot fastpath
        // to use the constant ZERO.  This might be important enough to
        // justify a factory approach, a cache, or a few private
        // constants, later.
        return new BigDecimal(Double.toString(val));
    }

    // Arithmetic Operations


    public BigDecimal add(BigDecimal augend) {
        long xs = this.intCompact;
        long ys = augend.intCompact;
        BigInteger fst = (xs != INFLATED) ? null : this.intVal;
        BigInteger snd = (ys != INFLATED) ? null : augend.intVal;
        int rscale = this.scale;

        long sdiff = (long) rscale - augend.scale;
        if (sdiff != 0) {
            if (sdiff < 0) {
                int raise = checkScale(-sdiff);
                rscale = augend.scale;
                if (xs == INFLATED ||
                        (xs = longMultiplyPowerTen(xs, raise)) == INFLATED)
                    fst = bigMultiplyPowerTen(raise);
            } else {
                int raise = augend.checkScale(sdiff);
                if (ys == INFLATED ||
                        (ys = longMultiplyPowerTen(ys, raise)) == INFLATED)
                    snd = augend.bigMultiplyPowerTen(raise);
            }
        }
        if (xs != INFLATED && ys != INFLATED) {
            long sum = xs + ys;
            // See "Hacker's Delight" section 2-12 for explanation of
            // the overflow test.
            if ((((sum ^ xs) & (sum ^ ys))) >= 0L) // not overflowed
                return BigDecimal.valueOf(sum, rscale);
        }
        if (fst == null)
            fst = BigInteger.valueOf(xs);
        if (snd == null)
            snd = BigInteger.valueOf(ys);
        BigInteger sum = fst.add(snd);
        return (fst.signum == snd.signum) ?
                new BigDecimal(sum, INFLATED, rscale, 0) :
                new BigDecimal(sum, rscale);
    }


    public BigDecimal add(BigDecimal augend, MathContext mc) {
        if (mc.precision == 0)
            return add(augend);
        BigDecimal lhs = this;

        // Could optimize if values are compact
        this.inflate();
        augend.inflate();

        // If either number is zero then the other number, rounded and
        // scaled if necessary, is used as the result.
        {
            boolean lhsIsZero = lhs.signum() == 0;
            boolean augendIsZero = augend.signum() == 0;

            if (lhsIsZero || augendIsZero) {
                int preferredScale = Math.max(lhs.scale(), augend.scale());
                BigDecimal result;

                // Could use a factory for zero instead of a new object
                if (lhsIsZero && augendIsZero)
                    return new BigDecimal(BigInteger.ZERO, 0, preferredScale, 0);

                result = lhsIsZero ? doRound(augend, mc) : doRound(lhs, mc);

                if (result.scale() == preferredScale)
                    return result;
                else if (result.scale() > preferredScale) {
                    BigDecimal scaledResult =
                            new BigDecimal(result.intVal, result.intCompact,
                                    result.scale, 0);
                    scaledResult.stripZerosToMatchScale(preferredScale);
                    return scaledResult;
                } else { // result.scale < preferredScale
                    int precisionDiff = mc.precision - result.precision();
                    int scaleDiff = preferredScale - result.scale();

                    if (precisionDiff >= scaleDiff)
                        return result.setScale(preferredScale); // can achieve target scale
                    else
                        return result.setScale(result.scale() + precisionDiff);
                }
            }
        }

        long padding = (long) lhs.scale - augend.scale;
        if (padding != 0) {        // scales differ; alignment needed
            BigDecimal arg[] = preAlign(lhs, augend, padding, mc);
            matchScale(arg);
            lhs = arg[0];
            augend = arg[1];
        }

        BigDecimal d = new BigDecimal(lhs.inflate().add(augend.inflate()),
                lhs.scale);
        return doRound(d, mc);
    }


    private BigDecimal[] preAlign(BigDecimal lhs, BigDecimal augend,
                                  long padding, MathContext mc) {
        assert padding != 0;
        BigDecimal big;
        BigDecimal small;

        if (padding < 0) {     // lhs is big;   augend is small
            big = lhs;
            small = augend;
        } else {               // lhs is small; augend is big
            big = augend;
            small = lhs;
        }
        long estResultUlpScale = (long) big.scale - big.precision() + mc.precision;

        long smallHighDigitPos = (long) small.scale - small.precision() + 1;
        if (smallHighDigitPos > big.scale + 2 &&         // big and small disjoint
                smallHighDigitPos > estResultUlpScale + 2) { // small digits not visible
            small = BigDecimal.valueOf(small.signum(),
                    this.checkScale(Math.max(big.scale, estResultUlpScale) + 3));
        }

        // Since addition is symmetric, preserving input order in
        // returned operands doesn't matter
        return new BigDecimal[]{big, small};
    }


    public BigDecimal subtract(BigDecimal subtrahend) {
        return add(subtrahend.negate());
    }


    public BigDecimal subtract(BigDecimal subtrahend, MathContext mc) {
        BigDecimal nsubtrahend = subtrahend.negate();
        if (mc.precision == 0)
            return add(nsubtrahend);
        // share the special rounding code in add()
        return add(nsubtrahend, mc);
    }


    public BigDecimal multiply(BigDecimal multiplicand) {
        long x = this.intCompact;
        long y = multiplicand.intCompact;
        int productScale = checkScale((long) scale + multiplicand.scale);

        // Might be able to do a more clever check incorporating the
        // inflated check into the overflow computation.
        if (x != INFLATED && y != INFLATED) {
            /*
             * If the product is not an overflowed value, continue
             * to use the compact representation.  if either of x or y
             * is INFLATED, the product should also be regarded as
             * an overflow. Before using the overflow test suggested in
             * "Hacker's Delight" section 2-12, we perform quick checks
             * using the precision information to see whether the overflow
             * would occur since division is expensive on most CPUs.
             */
            long product = x * y;
            long prec = this.precision() + multiplicand.precision();
            if (prec < 19 || (prec < 21 && (y == 0 || product / y == x)))
                return BigDecimal.valueOf(product, productScale);
            return new BigDecimal(BigInteger.valueOf(x).multiply(y), INFLATED,
                    productScale, 0);
        }
        BigInteger rb;
        if (x == INFLATED && y == INFLATED)
            rb = this.intVal.multiply(multiplicand.intVal);
        else if (x != INFLATED)
            rb = multiplicand.intVal.multiply(x);
        else
            rb = this.intVal.multiply(y);
        return new BigDecimal(rb, INFLATED, productScale, 0);
    }


    public BigDecimal multiply(BigDecimal multiplicand, MathContext mc) {
        if (mc.precision == 0)
            return multiply(multiplicand);
        return doRound(this.multiply(multiplicand), mc);
    }


    public BigDecimal divide(BigDecimal divisor, int scale, int roundingMode) {

        if (roundingMode < ROUND_UP || roundingMode > ROUND_UNNECESSARY)
            throw new IllegalArgumentException("Invalid rounding mode");

        BigDecimal dividend = this;
        if (checkScale((long) scale + divisor.scale) > this.scale)
            dividend = this.setScale(scale + divisor.scale, ROUND_UNNECESSARY);
        else
            divisor = divisor.setScale(checkScale((long) this.scale - scale),
                    ROUND_UNNECESSARY);
        return divideAndRound(dividend.intCompact, dividend.intVal,
                divisor.intCompact, divisor.intVal,
                scale, roundingMode, scale);
    }
    private static BigDecimal divideAndRound(long ldividend, BigInteger bdividend,
                                             long ldivisor, BigInteger bdivisor,
                                             int scale, int roundingMode,
                                             int preferredScale) {
        boolean isRemainderZero;       // record remainder is zero or not
        int qsign;
        long q = 0, r = 0;
        MutableBigInteger mq = null;
        MutableBigInteger mr = null;   // store remainder
        MutableBigInteger mdivisor = null;
        boolean isLongDivision = (ldividend != INFLATED && ldivisor != INFLATED);
        if (isLongDivision) {
            q = ldividend / ldivisor;
            if (roundingMode == ROUND_DOWN && scale == preferredScale)
                return new BigDecimal(null, q, scale, 0);
            r = ldividend % ldivisor;
            isRemainderZero = (r == 0);
            qsign = ((ldividend < 0) == (ldivisor < 0)) ? 1 : -1;
        } else {
            if (bdividend == null)
                bdividend = BigInteger.valueOf(ldividend);
            // Descend into mutables for faster remainder checks
            MutableBigInteger mdividend = new MutableBigInteger(bdividend.mag);
            mq = new MutableBigInteger();
            if (ldivisor != INFLATED) {
                r = mdividend.divide(ldivisor, mq);
                isRemainderZero = (r == 0);
                qsign = (ldivisor < 0) ? -bdividend.signum : bdividend.signum;
            } else {
                mdivisor = new MutableBigInteger(bdivisor.mag);
                mr = mdividend.divide(mdivisor, mq);
                isRemainderZero = mr.isZero();
                qsign = (bdividend.signum != bdivisor.signum) ? -1 : 1;
            }
        }
        boolean increment = false;
        if (!isRemainderZero) {
            int cmpFracHalf;
            /* Round as appropriate */
            if (roundingMode == ROUND_UNNECESSARY) {  // Rounding prohibited
                throw new ArithmeticException("Rounding necessary");
            } else if (roundingMode == ROUND_UP) {      // Away from zero
                increment = true;
            } else if (roundingMode == ROUND_DOWN) {    // Towards zero
                increment = false;
            } else if (roundingMode == ROUND_CEILING) { // Towards +infinity
                increment = (qsign > 0);
            } else if (roundingMode == ROUND_FLOOR) {   // Towards -infinity
                increment = (qsign < 0);
            } else {
                if (isLongDivision || ldivisor != INFLATED) {
                    if (r <= HALF_LONG_MIN_VALUE || r > HALF_LONG_MAX_VALUE) {
                        cmpFracHalf = 1;    // 2 * r can't fit into long
                    } else {
                        cmpFracHalf = longCompareMagnitude(2 * r, ldivisor);
                    }
                } else {
                    cmpFracHalf = mr.compareHalf(mdivisor);
                }
                increment = cmpFracHalf >= 0 && (cmpFracHalf > 0 || roundingMode == ROUND_HALF_UP || roundingMode != ROUND_HALF_DOWN && (isLongDivision ? (q & 1L) != 0L : mq.isOdd()));
            }
        }
        BigDecimal res;
        if (isLongDivision)
            res = new BigDecimal(null, (increment ? q + qsign : q), scale, 0);
        else {
            if (increment)
                mq.add(MutableBigInteger.ONE);
            res = mq.toBigDecimal(qsign, scale);
        }
        if (isRemainderZero && preferredScale != scale)
            res.stripZerosToMatchScale(preferredScale);
        return res;
    }


    public BigDecimal divide(BigDecimal divisor, int scale, RoundingMode roundingMode) {
        return divide(divisor, scale, roundingMode.oldMode);
    }


    public BigDecimal divide(BigDecimal divisor, int roundingMode) {
        return this.divide(divisor, scale, roundingMode);
    }


    public BigDecimal divide(BigDecimal divisor, RoundingMode roundingMode) {
        return this.divide(divisor, scale, roundingMode.oldMode);
    }


    public BigDecimal divide(BigDecimal divisor) {
        /*
         * Handle zero cases first.
         */
        if (divisor.signum() == 0) {   // x/0
            if (this.signum() == 0)    // 0/0
                throw new ArithmeticException("Division undefined");  // NaN
            throw new ArithmeticException("Division by zero");
        }

        // Calculate preferred scale
        int preferredScale = saturateLong((long) this.scale - divisor.scale);
        if (this.signum() == 0)        // 0/y
            return (preferredScale >= 0 &&
                    preferredScale < ZERO_SCALED_BY.length) ?
                    ZERO_SCALED_BY[preferredScale] :
                    BigDecimal.valueOf(0, preferredScale);
        else {
            this.inflate();
            divisor.inflate();

            MathContext mc = new MathContext((int) Math.min(this.precision() +
                    (long) Math.ceil(10.0 * divisor.precision() / 3.0),
                    Integer.MAX_VALUE),
                    RoundingMode.UNNECESSARY);
            BigDecimal quotient;
            try {
                quotient = this.divide(divisor, mc);
            } catch (ArithmeticException e) {
                throw new ArithmeticException("Non-terminating decimal expansion; " +
                        "no exact representable decimal result.");
            }

            int quotientScale = quotient.scale();

            if (preferredScale > quotientScale)
                return quotient.setScale(preferredScale, ROUND_UNNECESSARY);

            return quotient;
        }
    }


    public BigDecimal divide(BigDecimal divisor, MathContext mc) {
        int mcp = mc.precision;
        if (mcp == 0)
            return divide(divisor);

        BigDecimal dividend = this;
        long preferredScale = (long) dividend.scale - divisor.scale;
        // Now calculate the answer.  We use the existing
        // divide-and-round method, but as this rounds to scale we have
        // to normalize the values here to achieve the desired result.
        // For x/y we first handle y=0 and x=0, and then normalize x and
        // y to give x' and y' with the following constraints:
        //   (a) 0.1 <= x' < 1
        //   (b)  x' <= y' < 10*x'
        // Dividing x'/y' with the required scale set to mc.precision then
        // will give a result in the range 0.1 to 1 rounded to exactly
        // the right number of digits (except in the case of a result of
        // 1.000... which can arise when x=y, or when rounding overflows
        // The 1.000... case will reduce properly to 1.
        if (divisor.signum() == 0) {      // x/0
            if (dividend.signum() == 0)    // 0/0
                throw new ArithmeticException("Division undefined");  // NaN
            throw new ArithmeticException("Division by zero");
        }
        if (dividend.signum() == 0)        // 0/y
            return new BigDecimal(BigInteger.ZERO, 0,
                    saturateLong(preferredScale), 1);

        // Normalize dividend & divisor so that both fall into [0.1, 0.999...]
        int xscale = dividend.precision();
        int yscale = divisor.precision();
        dividend = new BigDecimal(dividend.intVal, dividend.intCompact,
                xscale, xscale);
        divisor = new BigDecimal(divisor.intVal, divisor.intCompact,
                yscale, yscale);
        if (dividend.compareMagnitude(divisor) > 0) // satisfy constraint (b)
            yscale = divisor.scale -= 1;
        BigDecimal quotient;
        int scl = checkScale(preferredScale + yscale - xscale + mcp);
        if (checkScale((long) mcp + yscale) > xscale)
            dividend = dividend.setScale(mcp + yscale, ROUND_UNNECESSARY);
        else
            divisor = divisor.setScale(checkScale((long) xscale - mcp),
                    ROUND_UNNECESSARY);
        quotient = divideAndRound(dividend.intCompact, dividend.intVal,
                divisor.intCompact, divisor.intVal,
                scl, mc.roundingMode.oldMode,
                checkScale(preferredScale));
        // doRound, here, only affects 1000000000 case.
        quotient = doRound(quotient, mc);

        return quotient;
    }


    public BigDecimal divideToIntegralValue(BigDecimal divisor) {
        // Calculate preferred scale
        int preferredScale = saturateLong((long) this.scale - divisor.scale);
        if (this.compareMagnitude(divisor) < 0) {
            // much faster when this << divisor
            return BigDecimal.valueOf(0, preferredScale);
        }

        if (this.signum() == 0 && divisor.signum() != 0)
            return this.setScale(preferredScale, ROUND_UNNECESSARY);

        // Perform a divide with enough digits to round to a correct
        // integer value; then remove any fractional digits

        int maxDigits = (int) Math.min(this.precision() +
                (long) Math.ceil(10.0 * divisor.precision() / 3.0) +
                Math.abs((long) this.scale() - divisor.scale()) + 2,
                Integer.MAX_VALUE);
        BigDecimal quotient = this.divide(divisor, new MathContext(maxDigits,
                RoundingMode.DOWN));
        if (quotient.scale > 0) {
            quotient = quotient.setScale(0, RoundingMode.DOWN);
            quotient.stripZerosToMatchScale(preferredScale);
        }

        if (quotient.scale < preferredScale) {
            // pad with zeros if necessary
            quotient = quotient.setScale(preferredScale, ROUND_UNNECESSARY);
        }
        return quotient;
    }

    public BigDecimal divideToIntegralValue(BigDecimal divisor, MathContext mc) {
        if (mc.precision == 0 ||                        // exact result
                (this.compareMagnitude(divisor) < 0))      // zero result
            return divideToIntegralValue(divisor);

        // Calculate preferred scale
        int preferredScale = saturateLong((long) this.scale - divisor.scale);

        BigDecimal result = this.
                divide(divisor, new MathContext(mc.precision, RoundingMode.DOWN));

        if (result.scale() < 0) {

            BigDecimal product = result.multiply(divisor);
            if (this.subtract(product).compareMagnitude(divisor) >= 0) {
                throw new ArithmeticException("Division impossible");
            }
        } else if (result.scale() > 0) {

            result = result.setScale(0, RoundingMode.DOWN);
        }
        // else result.scale() == 0;

        int precisionDiff;
        if ((preferredScale > result.scale()) &&
                (precisionDiff = mc.precision - result.precision()) > 0) {
            return result.setScale(result.scale() +
                    Math.min(precisionDiff, preferredScale - result.scale));
        } else {
            result.stripZerosToMatchScale(preferredScale);
            return result;
        }
    }


    public BigDecimal remainder(BigDecimal divisor) {
        BigDecimal divrem[] = this.divideAndRemainder(divisor);
        return divrem[1];
    }



    public BigDecimal remainder(BigDecimal divisor, MathContext mc) {
        BigDecimal divrem[] = this.divideAndRemainder(divisor, mc);
        return divrem[1];
    }


    public BigDecimal[] divideAndRemainder(BigDecimal divisor) {
        // we use the identity  x = i * y + r to determine r
        BigDecimal[] result = new BigDecimal[2];

        result[0] = this.divideToIntegralValue(divisor);
        result[1] = this.subtract(result[0].multiply(divisor));
        return result;
    }


    public BigDecimal[] divideAndRemainder(BigDecimal divisor, MathContext mc) {
        if (mc.precision == 0)
            return divideAndRemainder(divisor);

        BigDecimal[] result = new BigDecimal[2];
        BigDecimal lhs = this;

        result[0] = lhs.divideToIntegralValue(divisor, mc);
        result[1] = lhs.subtract(result[0].multiply(divisor));
        return result;
    }


    public BigDecimal pow(int n) {
        if (n < 0 || n > 999999999)
            throw new ArithmeticException("Invalid operation");
        // No need to calculate pow(n) if result will over/underflow.
        // Don't attempt to support "supernormal" numbers.
        int newScale = checkScale((long) scale * n);
        this.inflate();
        return new BigDecimal(intVal.pow(n), newScale);
    }



    public BigDecimal pow(int n, MathContext mc) {
        if (mc.precision == 0)
            return pow(n);
        if (n < -999999999 || n > 999999999)
            throw new ArithmeticException("Invalid operation");
        if (n == 0)
            return ONE;                      // x**0 == 1 in X3.274
        this.inflate();
        BigDecimal lhs = this;
        MathContext workmc = mc;           // working settings
        int mag = Math.abs(n);               // magnitude of n
        if (mc.precision > 0) {

            int elength = longDigitLength(mag); // length of n in digits
            if (elength > mc.precision)        // X3.274 rule
                throw new ArithmeticException("Invalid operation");
            workmc = new MathContext(mc.precision + elength + 1,
                    mc.roundingMode);
        }
        // ready to carry out power calculation...
        BigDecimal acc = ONE;           // accumulator
        boolean seenbit = false;        // set once we've seen a 1-bit
        for (int i = 1; ; i++) {            // for each bit [top bit ignored]
            mag += mag;                 // shift left 1 bit
            if (mag < 0) {              // top bit is set
                seenbit = true;         // OK, we're off
                acc = acc.multiply(lhs, workmc); // acc=acc*x
            }
            if (i == 31)
                break;                  // that was the last bit
            if (seenbit)
                acc = acc.multiply(acc, workmc);   // acc=acc*acc [square]
            // else (!seenbit) no point in squaring ONE
        }
        // if negative n, calculate the reciprocal using working precision
        if (n < 0)                          // [hence mc.precision>0]
            acc = ONE.divide(acc, workmc);
        // round to final precision and strip zeros
        return doRound(acc, mc);
    }


    public BigDecimal abs() {
        return (signum() < 0 ? negate() : this);
    }


    public BigDecimal abs(MathContext mc) {
        return (signum() < 0 ? negate(mc) : plus(mc));
    }

    public BigDecimal negate() {
        BigDecimal result;
        if (intCompact != INFLATED)
            result = BigDecimal.valueOf(-intCompact, scale);
        else {
            result = new BigDecimal(intVal.negate(), scale);
            result.precision = precision;
        }
        return result;
    }


    public BigDecimal negate(MathContext mc) {
        return negate().plus(mc);
    }


    public BigDecimal plus(MathContext mc) {
        if (mc.precision == 0)                 // no rounding please
            return this;
        return doRound(this, mc);
    }


    public int signum() {
        return (intCompact != INFLATED) ?
                Long.signum(intCompact) :
                intVal.signum();
    }


    public int scale() {
        return scale;
    }


    public int precision() {
        int result = precision;
        if (result == 0) {
            long s = intCompact;
            if (s != INFLATED)
                result = longDigitLength(s);
            else
                result = bigDigitLength(inflate());
            precision = result;
        }
        return result;
    }


    // Rounding Modes


    public final static int ROUND_UP = 0;


    public final static int ROUND_DOWN = 1;


    public final static int ROUND_CEILING = 2;


    public final static int ROUND_FLOOR = 3;


    public final static int ROUND_HALF_UP = 4;

    /**
     * Rounding mode to round towards {@literal "nearest neighbor"}
     * unless both neighbors are equidistant, in which case round
     * down.  Behaves as for {@code ROUND_UP} if the discarded
     * fraction is {@literal >} 0.5; otherwise, behaves as for
     * {@code ROUND_DOWN}.
     */
    public final static int ROUND_HALF_DOWN = 5;

    /**
     * Rounding mode to round towards the {@literal "nearest neighbor"}
     * unless both neighbors are equidistant, in which case, round
     * towards the even neighbor.  Behaves as for
     * {@code ROUND_HALF_UP} if the digit to the left of the
     * discarded fraction is odd; behaves as for
     * {@code ROUND_HALF_DOWN} if it's even.  Note that this is the
     * rounding mode that minimizes cumulative error when applied
     * repeatedly over a sequence of calculations.
     */
    public final static int ROUND_HALF_EVEN = 6;

    /**
     * Rounding mode to assert that the requested operation has an exact
     * result, hence no rounding is necessary.  If this rounding mode is
     * specified on an operation that yields an inexact result, an
     * {@code ArithmeticException} is thrown.
     */
    public final static int ROUND_UNNECESSARY = 7;


    // Scaling/Rounding Operations


    public BigDecimal round(MathContext mc) {
        return plus(mc);
    }


    public BigDecimal setScale(int newScale, RoundingMode roundingMode) {
        return setScale(newScale, roundingMode.oldMode);
    }


    public BigDecimal setScale(int newScale, int roundingMode) {
        if (roundingMode < ROUND_UP || roundingMode > ROUND_UNNECESSARY)
            throw new IllegalArgumentException("Invalid rounding mode");

        int oldScale = this.scale;
        if (newScale == oldScale)        // easy case
            return this;
        if (this.signum() == 0)            // zero can have any scale
            return BigDecimal.valueOf(0, newScale);

        long rs = this.intCompact;
        if (newScale > oldScale) {
            int raise = checkScale((long) newScale - oldScale);
            BigInteger rb = null;
            if (rs == INFLATED ||
                    (rs = longMultiplyPowerTen(rs, raise)) == INFLATED)
                rb = bigMultiplyPowerTen(raise);
            return new BigDecimal(rb, rs, newScale,
                    (precision > 0) ? precision + raise : 0);
        } else {
            // newScale < oldScale -- drop some digits
            // Can't predict the precision due to the effect of rounding.
            int drop = checkScale((long) oldScale - newScale);
            if (drop < LONG_TEN_POWERS_TABLE.length)
                return divideAndRound(rs, this.intVal,
                        LONG_TEN_POWERS_TABLE[drop], null,
                        newScale, roundingMode, newScale);
            else
                return divideAndRound(rs, this.intVal,
                        INFLATED, bigTenToThe(drop),
                        newScale, roundingMode, newScale);
        }
    }


    public BigDecimal setScale(int newScale) {
        return setScale(newScale, ROUND_UNNECESSARY);
    }



    public int compareTo(BigDecimal val) {
        // Quick path for equal scale and non-inflated case.
        if (scale == val.scale) {
            long xs = intCompact;
            long ys = val.intCompact;
            if (xs != INFLATED && ys != INFLATED)
                return xs != ys ? ((xs > ys) ? 1 : -1) : 0;
        }
        int xsign = this.signum();
        int ysign = val.signum();
        if (xsign != ysign)
            return (xsign > ysign) ? 1 : -1;
        if (xsign == 0)
            return 0;
        int cmp = compareMagnitude(val);
        return (xsign > 0) ? cmp : -cmp;
    }

    /**
     * Version of compareTo that ignores sign.
     */
    private int compareMagnitude(BigDecimal val) {
        // Match scales, avoid unnecessary inflation
        long ys = val.intCompact;
        long xs = this.intCompact;
        if (xs == 0)
            return (ys == 0) ? 0 : -1;
        if (ys == 0)
            return 1;

        int sdiff = this.scale - val.scale;
        if (sdiff != 0) {
            // Avoid matching scales if the (adjusted) exponents differ
            int xae = this.precision() - this.scale;   // [-1]
            int yae = val.precision() - val.scale;     // [-1]
            if (xae < yae)
                return -1;
            if (xae > yae)
                return 1;
            BigInteger rb;
            if (sdiff < 0) {
                if ((xs == INFLATED ||
                        (xs = longMultiplyPowerTen(xs, -sdiff)) == INFLATED) &&
                        ys == INFLATED) {
                    rb = bigMultiplyPowerTen(-sdiff);
                    return rb.compareMagnitude(val.intVal);
                }
            } else { // sdiff > 0
                if ((ys == INFLATED ||
                        (ys = longMultiplyPowerTen(ys, sdiff)) == INFLATED) &&
                        xs == INFLATED) {
                    rb = val.bigMultiplyPowerTen(sdiff);
                    return this.intVal.compareMagnitude(rb);
                }
            }
        }
        if (xs != INFLATED)
            return (ys != INFLATED) ? longCompareMagnitude(xs, ys) : -1;
        else if (ys != INFLATED)
            return 1;
        else
            return this.intVal.compareMagnitude(val.intVal);
    }


    @Override
    public boolean equals(Object x) {
        if (!(x instanceof BigDecimal))
            return false;
        BigDecimal xDec = (BigDecimal) x;
        if (x == this)
            return true;
        if (scale != xDec.scale)
            return false;
        long s = this.intCompact;
        long xs = xDec.intCompact;
        if (s != INFLATED) {
            if (xs == INFLATED)
                xs = compactValFor(xDec.intVal);
            return xs == s;
        } else if (xs != INFLATED)
            return xs == compactValFor(this.intVal);

        return this.inflate().equals(xDec.inflate());
    }


    public BigDecimal min(BigDecimal val) {
        return (compareTo(val) <= 0 ? this : val);
    }


    public BigDecimal max(BigDecimal val) {
        return (compareTo(val) >= 0 ? this : val);
    }

    // Hash Function


    @Override
    public int hashCode() {
        if (intCompact != INFLATED) {
            long val2 = (intCompact < 0) ? -intCompact : intCompact;
            int temp = (int) (((int) (val2 >>> 32)) * 31 +
                    (val2 & LONG_MASK));
            return 31 * ((intCompact < 0) ? -temp : temp) + scale;
        } else
            return 31 * intVal.hashCode() + scale;
    }

    // Format Converters


    @Override
    public String toString() {
        String sc = stringCache;
        if (sc == null)
            stringCache = sc = layoutChars(true);
        return sc;
    }


    public BigInteger toBigInteger() {
        // force to an integer, quietly
        return this.setScale(0, ROUND_DOWN).inflate();
    }



    public long longValue() {
        return (intCompact != INFLATED && scale == 0) ?
                intCompact :
                toBigInteger().longValue();
    }


    public int intValue() {
        return (intCompact != INFLATED && scale == 0) ?
                (int) intCompact :
                toBigInteger().intValue();
    }


    public float floatValue() {
        if (scale == 0 && intCompact != INFLATED)
            return (float) intCompact;
        // Somewhat inefficient, but guaranteed to work.
        return Float.parseFloat(this.toString());
    }


    public double doubleValue() {
        if (scale == 0 && intCompact != INFLATED)
            return (double) intCompact;
        // Somewhat inefficient, but guaranteed to work.
        return Double.parseDouble(this.toString());
    }


    static class StringBuilderHelper {
        final StringBuilder sb;
        final char[] cmpCharArray; // character array to place the intCompact

        StringBuilderHelper() {
            sb = new StringBuilder();
            // All non negative longs can be made to fit into 19 character array.
            cmpCharArray = new char[19];
        }

        // Accessors.
        StringBuilder getStringBuilder() {
            sb.setLength(0);
            return sb;
        }

        char[] getCompactCharArray() {
            return cmpCharArray;
        }

        /**
         * Places characters representing the intCompact in {@code long} into
         * cmpCharArray and returns the offset to the array where the
         * representation starts.
         *
         * @param intCompact the number to put into the cmpCharArray.
         * @return offset to the array where the representation starts.
         * Note: intCompact must be greater or equal to zero.
         */
        int putIntCompact(long intCompact) {
            assert intCompact >= 0;

            long q;
            int r;
            // since we start from the least significant digit, charPos points to
            // the last character in cmpCharArray.
            int charPos = cmpCharArray.length;

            while (intCompact > Integer.MAX_VALUE) {
                q = intCompact / 100;
                r = (int) (intCompact - q * 100);
                intCompact = q;
                cmpCharArray[--charPos] = DIGIT_ONES[r];
                cmpCharArray[--charPos] = DIGIT_TENS[r];
            }

            // Get 2 digits/iteration using ints when i2 >= 100
            int q2;
            int i2 = (int) intCompact;
            while (i2 >= 100) {
                q2 = i2 / 100;
                r = i2 - q2 * 100;
                i2 = q2;
                cmpCharArray[--charPos] = DIGIT_ONES[r];
                cmpCharArray[--charPos] = DIGIT_TENS[r];
            }

            cmpCharArray[--charPos] = DIGIT_ONES[i2];
            if (i2 >= 10)
                cmpCharArray[--charPos] = DIGIT_TENS[i2];

            return charPos;
        }

        final static char[] DIGIT_TENS = {
                '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
                '1', '1', '1', '1', '1', '1', '1', '1', '1', '1',
                '2', '2', '2', '2', '2', '2', '2', '2', '2', '2',
                '3', '3', '3', '3', '3', '3', '3', '3', '3', '3',
                '4', '4', '4', '4', '4', '4', '4', '4', '4', '4',
                '5', '5', '5', '5', '5', '5', '5', '5', '5', '5',
                '6', '6', '6', '6', '6', '6', '6', '6', '6', '6',
                '7', '7', '7', '7', '7', '7', '7', '7', '7', '7',
                '8', '8', '8', '8', '8', '8', '8', '8', '8', '8',
                '9', '9', '9', '9', '9', '9', '9', '9', '9', '9',
        };

        final static char[] DIGIT_ONES = {
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        };
    }

    private String layoutChars(boolean sci) {
        if (scale == 0)                      // zero scale is trivial
            return (intCompact != INFLATED) ?
                    Long.toString(intCompact) :
                    intVal.toString();

        StringBuilderHelper sbHelper = threadLocalStringBuilderHelper.get();
        char[] coeff;
        int offset;  // offset is the starting index for coeff array
        // Get the significand as an absolute value
        if (intCompact != INFLATED) {
            offset = sbHelper.putIntCompact(Math.abs(intCompact));
            coeff = sbHelper.getCompactCharArray();
        } else {
            offset = 0;
            coeff = intVal.abs().toString().toCharArray();
        }

        StringBuilder buf = sbHelper.getStringBuilder();
        if (signum() < 0)             // prefix '-' if negative
            buf.append('-');
        int coeffLen = coeff.length - offset;
        long adjusted = -(long) scale + (coeffLen - 1);
        if ((scale >= 0) && (adjusted >= -6)) { // plain number
            int pad = scale - coeffLen;         // count of padding zeros
            if (pad >= 0) {                     // 0.xxx form
                buf.append('0');
                buf.append('.');
                for (; pad > 0; pad--) {
                    buf.append('0');
                }
                buf.append(coeff, offset, coeffLen);
            } else {                         // xx.xx form
                buf.append(coeff, offset, -pad);
                buf.append('.');
                buf.append(coeff, -pad + offset, scale);
            }
        } else { // E-notation is needed
            if (sci) {                       // Scientific notation
                buf.append(coeff[offset]);   // first character
                if (coeffLen > 1) {          // more to come
                    buf.append('.');
                    buf.append(coeff, offset + 1, coeffLen - 1);
                }
            } else {                         // Engineering notation
                int sig = (int) (adjusted % 3);
                if (sig < 0)
                    sig += 3;                // [adjusted was negative]
                adjusted -= sig;             // now a multiple of 3
                sig++;
                if (signum() == 0) {
                    switch (sig) {
                        case 1:
                            buf.append('0'); // exponent is a multiple of three
                            break;
                        case 2:
                            buf.append("0.00");
                            adjusted += 3;
                            break;
                        case 3:
                            buf.append("0.0");
                            adjusted += 3;
                            break;
                        default:
                            throw new AssertionError("Unexpected sig value " + sig);
                    }
                } else if (sig >= coeffLen) {   // significand all in integer
                    buf.append(coeff, offset, coeffLen);
                    // may need some zeros, too
                    for (int i = sig - coeffLen; i > 0; i--)
                        buf.append('0');
                } else {                     // xx.xxE form
                    buf.append(coeff, offset, sig);
                    buf.append('.');
                    buf.append(coeff, offset + sig, coeffLen - sig);
                }
            }
            if (adjusted != 0) {             // [!sci could have made 0]
                buf.append('E');
                if (adjusted > 0)            // force sign for positive
                    buf.append('+');
                buf.append(adjusted);
            }
        }
        return buf.toString();
    }

    /**
     * Return 10 to the power n, as a {@code BigInteger}.
     *
     * @param n the power of ten to be returned (>=0)
     * @return a {@code BigInteger} with the value (10<sup>n</sup>)
     */
    private static BigInteger bigTenToThe(int n) {
        if (n < 0)
            return BigInteger.ZERO;

        if (n < BIG_TEN_POWERS_TABLE_MAX) {
            BigInteger[] pows = BIG_TEN_POWERS_TABLE;
            if (n < pows.length)
                return pows[n];
            else
                return expandBigIntegerTenPowers(n);
        }
        // BigInteger.pow is slow, so make 10**n by constructing a
        // BigInteger from a character string (still not very fast)
        char tenpow[] = new char[n + 1];
        tenpow[0] = '1';
        for (int i = 1; i <= n; i++)
            tenpow[i] = '0';
        return new BigInteger(tenpow);
    }


    private static BigInteger expandBigIntegerTenPowers(int n) {
        synchronized (BigDecimal.class) {
            BigInteger[] pows = BIG_TEN_POWERS_TABLE;
            int curLen = pows.length;
            // The following comparison and the above synchronized statement is
            // to prevent multiple threads from expanding the same array.
            if (curLen <= n) {
                int newLen = curLen << 1;
                while (newLen <= n)
                    newLen <<= 1;
                pows = Arrays.copyOf(pows, newLen);
                for (int i = curLen; i < newLen; i++)
                    pows[i] = pows[i - 1].multiply(BigInteger.TEN);
                // Based on the following facts:
                // 1. pows is a private local varible;
                // 2. the following store is a volatile store.
                // the newly created array elements can be safely published.
                BIG_TEN_POWERS_TABLE = pows;
            }
            return pows[n];
        }
    }

    private static final long[] LONG_TEN_POWERS_TABLE = {
            1,                     // 0 / 10^0
            10,                    // 1 / 10^1
            100,                   // 2 / 10^2
            1000,                  // 3 / 10^3
            10000,                 // 4 / 10^4
            100000,                // 5 / 10^5
            1000000,               // 6 / 10^6
            10000000,              // 7 / 10^7
            100000000,             // 8 / 10^8
            1000000000,            // 9 / 10^9
            10000000000L,          // 10 / 10^10
            100000000000L,         // 11 / 10^11
            1000000000000L,        // 12 / 10^12
            10000000000000L,       // 13 / 10^13
            100000000000000L,      // 14 / 10^14
            1000000000000000L,     // 15 / 10^15
            10000000000000000L,    // 16 / 10^16
            100000000000000000L,   // 17 / 10^17
            1000000000000000000L   // 18 / 10^18
    };

    private static volatile BigInteger BIG_TEN_POWERS_TABLE[] = {BigInteger.ONE,
            BigInteger.valueOf(10), BigInteger.valueOf(100),
            BigInteger.valueOf(1000), BigInteger.valueOf(10000),
            BigInteger.valueOf(100000), BigInteger.valueOf(1000000),
            BigInteger.valueOf(10000000), BigInteger.valueOf(100000000),
            BigInteger.valueOf(1000000000),
            BigInteger.valueOf(10000000000L),
            BigInteger.valueOf(100000000000L),
            BigInteger.valueOf(1000000000000L),
            BigInteger.valueOf(10000000000000L),
            BigInteger.valueOf(100000000000000L),
            BigInteger.valueOf(1000000000000000L),
            BigInteger.valueOf(10000000000000000L),
            BigInteger.valueOf(100000000000000000L),
            BigInteger.valueOf(1000000000000000000L)
    };

    private static final int BIG_TEN_POWERS_TABLE_INITLEN =
            BIG_TEN_POWERS_TABLE.length;
    private static final int BIG_TEN_POWERS_TABLE_MAX =
            16 * BIG_TEN_POWERS_TABLE_INITLEN;

    private static final long THRESHOLDS_TABLE[] = {
            Long.MAX_VALUE,                     // 0
            Long.MAX_VALUE / 10L,                 // 1
            Long.MAX_VALUE / 100L,                // 2
            Long.MAX_VALUE / 1000L,               // 3
            Long.MAX_VALUE / 10000L,              // 4
            Long.MAX_VALUE / 100000L,             // 5
            Long.MAX_VALUE / 1000000L,            // 6
            Long.MAX_VALUE / 10000000L,           // 7
            Long.MAX_VALUE / 100000000L,          // 8
            Long.MAX_VALUE / 1000000000L,         // 9
            Long.MAX_VALUE / 10000000000L,        // 10
            Long.MAX_VALUE / 100000000000L,       // 11
            Long.MAX_VALUE / 1000000000000L,      // 12
            Long.MAX_VALUE / 10000000000000L,     // 13
            Long.MAX_VALUE / 100000000000000L,    // 14
            Long.MAX_VALUE / 1000000000000000L,   // 15
            Long.MAX_VALUE / 10000000000000000L,  // 16
            Long.MAX_VALUE / 100000000000000000L, // 17
            Long.MAX_VALUE / 1000000000000000000L // 18
    };

    /**
     * Compute val * 10 ^ n; return this product if it is
     * representable as a long, INFLATED otherwise.
     */
    private static long longMultiplyPowerTen(long val, int n) {
        if (val == 0 || n <= 0)
            return val;
        long[] tab = LONG_TEN_POWERS_TABLE;
        long[] bounds = THRESHOLDS_TABLE;
        if (n < tab.length && n < bounds.length) {
            long tenpower = tab[n];
            if (val == 1)
                return tenpower;
            if (Math.abs(val) <= bounds[n])
                return val * tenpower;
        }
        return INFLATED;
    }

    /**
     * Compute this * 10 ^ n.
     * Needed mainly to allow special casing to trap zero value
     */
    private BigInteger bigMultiplyPowerTen(int n) {
        if (n <= 0)
            return this.inflate();

        if (intCompact != INFLATED)
            return bigTenToThe(n).multiply(intCompact);
        else
            return intVal.multiply(bigTenToThe(n));
    }


    private BigInteger inflate() {
        if (intVal == null)
            intVal = BigInteger.valueOf(intCompact);
        return intVal;
    }


    private static void matchScale(BigDecimal[] val) {
        if (val[0].scale != val[1].scale) {
            if (val[0].scale < val[1].scale) {
                val[0] = val[0].setScale(val[1].scale, ROUND_UNNECESSARY);
            } else if (val[1].scale < val[0].scale) {
                val[1] = val[1].setScale(val[0].scale, ROUND_UNNECESSARY);
            }
        }
    }


    private void readObject(java.io.ObjectInputStream s)
            throws java.io.IOException, ClassNotFoundException {
        // Read in all fields
        s.defaultReadObject();
        // validate possibly bad fields
        if (intVal == null) {
            String message = "BigDecimal: null intVal in stream";
            throw new java.io.StreamCorruptedException(message);
            // [all values of scale are now allowed]
        }
        intCompact = compactValFor(intVal);
    }


    private void writeObject(java.io.ObjectOutputStream s)
            throws java.io.IOException {
        // Must inflate to maintain compatible serial form.
        this.inflate();

        // Write proper fields
        s.defaultWriteObject();
    }


    /**
     * Returns the length of the absolute value of a {@code long}, in decimal
     * digits.
     *
     * @param x the {@code long}
     * @return the length of the unscaled value, in deciaml digits.
     */
    private static int longDigitLength(long x) {
        /*
         * As described in "Bit Twiddling Hacks" by Sean Anderson,
         * (http://graphics.stanford.edu/~seander/bithacks.html)
         * integer log 10 of x is within 1 of
         * (1233/4096)* (1 + integer log 2 of x).
         * The fraction 1233/4096 approximates log10(2). So we first
         * do a version of log2 (a variant of Long class with
         * pre-checks and opposite directionality) and then scale and
         * check against powers table. This is a little simpler in
         * present context than the version in Hacker's Delight sec
         * 11-4.  Adding one to bit length allows comparing downward
         * from the LONG_TEN_POWERS_TABLE that we need anyway.
         */
        assert x != INFLATED;
        if (x < 0)
            x = -x;
        if (x < 10) // must screen for 0, might as well 10
            return 1;
        int n = 64; // not 63, to avoid needing to add 1 later
        int y = (int) (x >>> 32);
        if (y == 0) {
            n -= 32;
            y = (int) x;
        }
        if (y >>> 16 == 0) {
            n -= 16;
            y <<= 16;
        }
        if (y >>> 24 == 0) {
            n -= 8;
            y <<= 8;
        }
        if (y >>> 28 == 0) {
            n -= 4;
            y <<= 4;
        }
        if (y >>> 30 == 0) {
            n -= 2;
            y <<= 2;
        }
        int r = (((y >>> 31) + n) * 1233) >>> 12;
        long[] tab = LONG_TEN_POWERS_TABLE;
        // if r >= length, must have max possible digits for long
        return (r >= tab.length || x < tab[r]) ? r : r + 1;
    }


    private static int bigDigitLength(BigInteger b) {

        if (b.signum == 0)
            return 1;
        int r = (int) ((((long) b.bitLength() + 1) * 646456993) >>> 31);
        return b.compareMagnitude(bigTenToThe(r)) < 0 ? r : r + 1;
    }



    private BigDecimal stripZerosToMatchScale(long preferredScale) {
        this.inflate();
        BigInteger qr[];
        while (intVal.compareMagnitude(BigInteger.TEN) >= 0 &&
                scale > preferredScale) {
            if (intVal.testBit(0))
                break;                  // odd number cannot end in 0
            qr = intVal.divideAndRemainder(BigInteger.TEN);
            if (qr[1].signum() != 0)
                break;                  // non-0 remainder
            intVal = qr[0];
            scale = checkScale((long) scale - 1);  // could Overflow
            if (precision > 0)          // adjust precision if known
                precision--;
        }
        if (intVal != null)
            intCompact = compactValFor(intVal);
        return this;
    }

    private int checkScale(long val) {
        int asInt = (int) val;
        if (asInt != val) {
            asInt = val > Integer.MAX_VALUE ? Integer.MAX_VALUE : Integer.MIN_VALUE;
            BigInteger b;
            if (intCompact != 0 &&
                    ((b = intVal) == null || b.signum() != 0))
                throw new ArithmeticException(asInt > 0 ? "Underflow" : "Overflow");
        }
        return asInt;
    }


    private static BigDecimal doRound(BigDecimal d, MathContext mc) {
        int mcp = mc.precision;
        int drop;
        // This might (rarely) iterate to cover the 999=>1000 case
        while ((drop = d.precision() - mcp) > 0) {
            int newScale = d.checkScale((long) d.scale - drop);
            int mode = mc.roundingMode.oldMode;
            if (drop < LONG_TEN_POWERS_TABLE.length)
                d = divideAndRound(d.intCompact, d.intVal,
                        LONG_TEN_POWERS_TABLE[drop], null,
                        newScale, mode, newScale);
            else
                d = divideAndRound(d.intCompact, d.intVal,
                        INFLATED, bigTenToThe(drop),
                        newScale, mode, newScale);
        }
        return d;
    }

    /**
     * Returns the compact value for given {@code BigInteger}, or
     * INFLATED if too big. Relies on internal representation of
     * {@code BigInteger}.
     */
    private static long compactValFor(BigInteger b) {
        int[] m = b.mag;
        int len = m.length;
        if (len == 0)
            return 0;
        int d = m[0];
        if (len > 2 || (len == 2 && d < 0))
            return INFLATED;

        long u = (len == 2) ?
                (((long) m[1] & LONG_MASK) + (((long) d) << 32)) :
                (((long) d) & LONG_MASK);
        return (b.signum < 0) ? -u : u;
    }

    private static int longCompareMagnitude(long x, long y) {
        if (x < 0)
            x = -x;
        if (y < 0)
            y = -y;
        return (x < y) ? -1 : ((x == y) ? 0 : 1);
    }

    private static int saturateLong(long s) {
        int i = (int) s;
        return (s == i) ? i : (s < 0 ? Integer.MIN_VALUE : Integer.MAX_VALUE);
    }

}
