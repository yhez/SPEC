package zxing;

public abstract class LuminanceSource {

  private final int width;
  private final int height;

  protected LuminanceSource(int width, int height) {
    this.width = width;
    this.height = height;
  }


  public abstract byte[] getRow(int y, byte[] row);

  public abstract byte[] getMatrix();

  /**
   * @return The width of the bitmap.
   */
  public final int getWidth() {
    return width;
  }

  /**
   * @return The height of the bitmap.
   */
  public final int getHeight() {
    return height;
  }

  /**
   * @return Whether this subclass supports cropping.
   */
  public boolean isCropSupported() {
    return false;
  }


  public LuminanceSource crop(int left, int top, int width, int height) {
    throw new UnsupportedOperationException("This luminance source does not support cropping.");
  }


  public boolean isRotateSupported() {
    return false;
  }

  /**
   * @return a wrapper of this {@code LuminanceSource} which inverts the luminances it returns -- black becomes
   *  white and vice versa, and each value becomes (255-value).
   */
  public LuminanceSource invert() {
    return new InvertedLuminanceSource(this);
  }

  /**
   * Returns a new object with rotated image data by 90 degrees counterclockwise.
   * Only callable if {@link #isRotateSupported()} is true.
   *
   * @return A rotated version of this object.
   */
  public LuminanceSource rotateCounterClockwise() {
    throw new UnsupportedOperationException("This luminance source does not support rotation by 90 degrees.");
  }

  /**
   * Returns a new object with rotated image data by 45 degrees counterclockwise.
   * Only callable if {@link #isRotateSupported()} is true.
   *
   * @return A rotated version of this object.
   */
  public LuminanceSource rotateCounterClockwise45() {
    throw new UnsupportedOperationException("This luminance source does not support rotation by 45 degrees.");
  }

  @Override
  public final String toString() {
    byte[] row = new byte[width];
    StringBuilder result = new StringBuilder(height * (width + 1));
    for (int y = 0; y < height; y++) {
      row = getRow(y, row);
      for (int x = 0; x < width; x++) {
        int luminance = row[x] & 0xFF;
        char c;
        if (luminance < 0x40) {
          c = '#';
        } else if (luminance < 0x80) {
          c = '+';
        } else if (luminance < 0xC0) {
          c = '.';
        } else {
          c = ' ';
        }
        result.append(c);
      }
      result.append('\n');
    }
    return result.toString();
  }

}
