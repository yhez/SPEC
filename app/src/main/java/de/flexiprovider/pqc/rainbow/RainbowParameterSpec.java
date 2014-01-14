package de.flexiprovider.pqc.rainbow;

import de.flexiprovider.api.parameters.AlgorithmParameterSpec;


public class RainbowParameterSpec implements AlgorithmParameterSpec {

    private int[] vi;// set of vinegar vars per layer.

    public RainbowParameterSpec() {

        this.vi = new int[]{6, 12, 17, 22, 33};
    }


    public int getNumOfLayers() {
        return this.vi.length - 1;
    }

    public int[] getVi() {
        return this.vi;
    }

}
