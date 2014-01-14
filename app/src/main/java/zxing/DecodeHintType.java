package zxing;

public enum DecodeHintType {

  /**
   * Unspecified, application-specific hint. Maps to an unspecified {@link Object}.
   */
 OTHER(),

  /**
   * Image is a pure monochrome image of a barcode. Doesn't matter what it maps to;
   * use {@link Boolean#TRUE}.
   */
  PURE_BARCODE(),

  POSSIBLE_FORMATS(),

  /**
   * Spend more time to try to find a barcode; optimize for accuracy, not speed.
   * Doesn't matter what it maps to; use {@link Boolean#TRUE}.
   */
  TRY_HARDER(),

  /**
   * Specifies what character encoding to use when decoding, where applicable (type String)
   */
  CHARACTER_SET(),

  /**
   * Allowed lengths of encoded data -- reject anything else. Maps to an {@code int[]}.
   */
  ALLOWED_LENGTHS(),

  /**
   * Assume Code 39 codes employ a check digit. Doesn't matter what it maps to;
   * use {@link Boolean#TRUE}.
   */
  ASSUME_CODE_39_CHECK_DIGIT(),

  /**
   * Assume the barcode is being processed as a GS1 barcode, and modify behavior as needed.
   * For example this affects FNC1 handling for Code 128 (aka GS1-128). Doesn't matter what it maps to;
   * use {@link Boolean#TRUE}.
   */
  ASSUME_GS1(),

  /**
   * If true, return the start and end digits in a Codabar barcode instead of stripping them. They
   * are alpha, whereas the rest are numeric. By default, they are stripped, but this causes them
   * to not be. Doesn't matter what it maps to; use {@link Boolean#TRUE}.
   */
  RETURN_CODABAR_START_END(),

  /**
   * The caller needs to be notified via callback when a possible {@link ResultPoint}
   * is found. Maps to a {@link ResultPointCallback}.
   */
  NEED_RESULT_POINT_CALLBACK(),

  // End of enumeration values.
  ;

    DecodeHintType() {
    }

}
