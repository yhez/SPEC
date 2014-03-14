package zxing;

import zxing.common.BitMatrix;


public abstract class Binarizer {

  private final LuminanceSource source;

  protected Binarizer(LuminanceSource source) {
    this.source = source;
  }

  public final LuminanceSource getLuminanceSource() {
    return source;
  }


    public abstract BitMatrix getBlackMatrix() throws NotFoundException;

}
