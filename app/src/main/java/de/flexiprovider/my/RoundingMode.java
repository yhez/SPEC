package de.flexiprovider.my;


public enum RoundingMode {


    DOWN(BigDecimal.ROUND_DOWN),


    UNNECESSARY(BigDecimal.ROUND_UNNECESSARY);

    final int oldMode;

    private RoundingMode(int oldMode) {
        this.oldMode = oldMode;
    }


}
