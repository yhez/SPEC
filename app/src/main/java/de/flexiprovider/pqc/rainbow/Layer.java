package de.flexiprovider.pqc.rainbow;

import de.flexiprovider.api.SecureRandom;
import de.flexiprovider.pqc.rainbow.util.GF2Field;
import de.flexiprovider.pqc.rainbow.util.RainbowUtil;


public class Layer {

    private int layerIndex; // num-th layer
    private int vi; // number of vinegars in this layer
    private int viNext; // number of vinegars in next layer
    private int oi; // number of oils in this layer

    /*
     * k : index of polynomial
     *
     * i,j : indices of oil and vinegar variables
     */
    private short[/* k */][/* i */][/* j */] coeff_alpha;
    private short[/* k */][/* i */][/* j */] coeff_beta;
    private short[/* k */][/* i */] coeff_gamma;
    private short[/* k */] coeff_eta;

    /**
     * Constructor.
     *
     * @param layerIndex index of layer (from 0...u-2)
     * @param vi         number of vinegar variables of this layer
     * @param viNext     number of vinegar variables of next layer. It's the same as
     *                   (num of oils) + (num of vinegars) of this layer.
     */
    public Layer(int layerIndex, int vi, int viNext) {
        this.layerIndex = layerIndex;
        this.vi = vi;
        this.viNext = viNext;
        this.oi = viNext - vi;

        // the coefficients of all polynomials in this layer
        this.coeff_alpha = new short[this.oi][this.oi][this.vi];
        this.coeff_beta = new short[this.oi][this.vi][this.vi];
        this.coeff_gamma = new short[this.oi][this.viNext];
        this.coeff_eta = new short[this.oi];
    }

    /**
     * Constructor.
     *
     * @param layerIndex
     *            index of layer (from 0...u-2)
     * @param vi
     *            number of vinegar variables of this layer
     * @param viNext
     *            number of vinegar variables of next layer. It's the same as
     *            (num of oils) + (num of vinegars) of this layer.
     */


    /**
     * Constructor used by {@link RainbowPrivateKeySpec}.
     *
     * @param layerIndex index of layer (from 0...u-2)
     * @param vi         number of vinegar variables of this layer
     * @param viNext     number of vinegar variables of next layer. It's the same as
     *                   (num of oils) + (num of vinegars) of this layer.
     * @param coeffAlpha alpha-coefficients in the polynomials of this layer
     * @param coeffBeta  beta-coefficients in the polynomials of this layer
     * @param coeffGamma gamma-coefficients in the polynomials of this layer
     * @param coeffEta   eta-coefficients in the polynomials of this layer
     */
    public Layer(int layerIndex, byte vi, byte viNext, byte[][][] coeffAlpha,
                 byte[][][] coeffBeta, byte[][] coeffGamma, byte[] coeffEta) {

        this.layerIndex = layerIndex;
        this.vi = vi & 0xff;
        this.viNext = viNext & 0xff;
        this.oi = this.viNext - this.vi;

        // the secret coefficients of all polynomials in this layer
        this.coeff_alpha = RainbowUtil.convertArray(coeffAlpha);
        this.coeff_beta = RainbowUtil.convertArray(coeffBeta);
        this.coeff_gamma = RainbowUtil.convertArray(coeffGamma);
        this.coeff_eta = RainbowUtil.convertArray(coeffEta);
    }

    /**
     * This function generates the coefficients of all polynomials in this layer
     * at random using random generator.
     *
     * @param sr the random generator which is to be used
     */
    public void generateCoefficients(SecureRandom sr) {
        int numOfPoly = this.oi; // number of polynomials per layer

        // Alpha coeffs
        for (int k = 0; k < numOfPoly; k++) {
            for (int i = 0; i < this.oi; i++) {
                for (int j = 0; j < this.vi; j++) {
                    coeff_alpha[k][i][j] = (short) (sr.nextInt() & GF2Field.MASK);
                }
            }
        }
        // Beta coeffs
        for (int k = 0; k < numOfPoly; k++) {
            for (int i = 0; i < this.vi; i++) {
                for (int j = 0; j < this.vi; j++) {
                    coeff_beta[k][i][j] = (short) (sr.nextInt() & GF2Field.MASK);
                }
            }
        }
        // Gamma coeffs
        for (int k = 0; k < numOfPoly; k++) {
            for (int i = 0; i < this.viNext; i++) {
                coeff_gamma[k][i] = (short) (sr.nextInt() & GF2Field.MASK);
            }
        }
        // Eta
        for (int k = 0; k < numOfPoly; k++) {
            coeff_eta[k] = (short) (sr.nextInt() & GF2Field.MASK);
        }
    }

    /**
     * This method plugs in the vinegar variables into the polynomials of this
     * layer and computes the coefficients of the Oil-variables as well as the
     * free coefficient in each polynomial.
     * <p/>
     * It is needed for computing the Oil variables while signing.
     *
     * @param x vinegar variables of this layer that should be plugged into
     *          the polynomials.
     * @return coeff the coefficients of Oil variables and the free coeff in the
     * polynomials of this layer.
     */
    protected short[][] plugInVinegars(short[] x) {
        // temporary variable needed for the multiplication
        short tmpMult;
        // coeff: 1st index = which polynomial, 2nd index=which variable
        short[][] coeff = new short[oi][oi + 1]; // gets returned
        // free coefficient per polynomial
        short[] sum = new short[oi];

		/*
         * evaluate the beta-part of the polynomials (it contains no oil
		 * variables)
		 */
        for (int k = 0; k < oi; k++) {
            for (int i = 0; i < vi; i++) {
                for (int j = 0; j < vi; j++) {
                    // tmp = beta * xi (plug in)
                    tmpMult = GF2Field.multElem(coeff_beta[k][i][j], x[i]);
                    // tmp = tmp * xj
                    tmpMult = GF2Field.multElem(tmpMult, x[j]);
                    // accumulate into the array for the free coefficients.
                    sum[k] = GF2Field.addElem(sum[k], tmpMult);
                }
            }
        }

		/* evaluate the alpha-part (it contains oils) */
        for (int k = 0; k < oi; k++) {
            for (int i = 0; i < oi; i++) {
                for (int j = 0; j < vi; j++) {
                    // alpha * xj (plug in)
                    tmpMult = GF2Field.multElem(coeff_alpha[k][i][j], x[j]);
                    // accumulate
                    coeff[k][i] = GF2Field.addElem(coeff[k][i], tmpMult);
                }
            }
        }
        /* evaluate the gama-part of the polynomial (containing no oils) */
        for (int k = 0; k < oi; k++) {
            for (int i = 0; i < vi; i++) {
                // gamma * xi (plug in)
                tmpMult = GF2Field.multElem(coeff_gamma[k][i], x[i]);
                // accumulate in the array for the free coefficients (per
                // polynomial).
                sum[k] = GF2Field.addElem(sum[k], tmpMult);
            }
        }
		/* evaluate the gama-part of the polynomial (but containing oils) */
        for (int k = 0; k < oi; k++) {
            for (int i = vi; i < viNext; i++) { // oils
                // accumulate the coefficients of the oil variables (per
                // polynomial).
                coeff[k][i - vi] = GF2Field.addElem(coeff_gamma[k][i],
                        coeff[k][i - vi]);
            }
        }
		/* evaluate the eta-part of the polynomial */
        for (int k = 0; k < oi; k++) {
            // accumulate in the array for the free coefficients per polynomial.
            sum[k] = GF2Field.addElem(sum[k], coeff_eta[k]);
        }

		/* put the free coefficients (sum) into the coeff-array as last column */
        for (int k = 0; k < oi; k++) {
            coeff[k][oi] = sum[k];
        }
        return coeff;
    }

    /**
     * Getter for the index of the layer.
     *
     * @return the index of the current layer.
     */
    public int getLayerIndex() {
        return layerIndex;
    }

    /**
     * Getter for the number of vinegar variables of this layer.
     *
     * @return the number of vinegar variables of this layer.
     */
    public int getVi() {
        return vi;
    }

    /**
     * Getter for the number of vinegar variables of the next layer.
     *
     * @return the number of vinegar variables of the next layer.
     */
    public int getViNext() {
        return viNext;
    }

    /**
     * Getter for the number of Oil variables of this layer.
     *
     * @return the number of oil variables of this layer.
     */
    public int getOi() {
        return oi;
    }

    /**
     * Getter for the alpha-coefficients of the polynomials in this layer.
     *
     * @return the coefficients of alpha-terms of this layer.
     */
    public short[][][] getCoeffAlpha() {
        return coeff_alpha;
    }

    /**
     * Getter for the beta-coefficients of the polynomials in this layer.
     *
     * @return the coefficients of beta-terms of this layer.
     */

    public short[][][] getCoeffBeta() {
        return coeff_beta;
    }

    /**
     * Getter for the gamma-coefficients of the polynomials in this layer.
     *
     * @return the coefficients of gamma-terms of this layer
     */
    public short[][] getCoeffGamma() {
        return coeff_gamma;
    }

    /**
     * Getter for the eta-coefficients of the polynomials in this layer.
     *
     * @return the coefficients eta of this layer
     */
    public short[] getCoeffEta() {
        return coeff_eta;
    }

    public boolean equals(Object other) {
        if (other == null || !(other instanceof Layer)) {
            return false;
        }
        Layer otherLayer = (Layer) other;

        boolean eq;
        eq = layerIndex == otherLayer.getLayerIndex();
        eq &= vi == otherLayer.getVi();
        eq &= viNext == otherLayer.getViNext();
        eq &= oi == otherLayer.getOi();
        eq &= RainbowUtil.equals(coeff_alpha, otherLayer.getCoeffAlpha());
        eq &= RainbowUtil.equals(coeff_beta, otherLayer.getCoeffBeta());
        eq &= RainbowUtil.equals(coeff_gamma, otherLayer.getCoeffGamma());
        eq &= RainbowUtil.equals(coeff_eta, otherLayer.getCoeffEta());
        return eq;
    }

}
