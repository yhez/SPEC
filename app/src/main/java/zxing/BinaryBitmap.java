package zxing;

import zxing.common.BitMatrix;

public final class BinaryBitmap {

  private final Binarizer binarizer;
  private BitMatrix matrix;

  public BinaryBitmap(Binarizer binarizer) {
    if (binarizer == null) {
      throw new IllegalArgumentException("Binarizer must be non-null.");
    }
    this.binarizer = binarizer;
  }

  public int getWidth() {
    return binarizer.getWidth();
  }

  public int getHeight() {
    return binarizer.getHeight();
  }


    public BitMatrix getBlackMatrix() throws NotFoundException {
    // The matrix is created on demand the first time it is requested, then cached. There are two
    // reasons for this:
    // 1. This work will never be done if the caller only installs 1D Reader objects, or if a
    //    1D Reader finds a barcode before the 2D Readers run.
    // 2. This work will only be done once even if the caller installs multiple 2D Readers.
    if (matrix == null) {
      matrix = binarizer.getBlackMatrix();
    }
    return matrix;
  }


    @Override
  public String toString() {
    try {
      return getBlackMatrix().toString();
    } catch (NotFoundException e) {
      return "";
    }
  }

}
