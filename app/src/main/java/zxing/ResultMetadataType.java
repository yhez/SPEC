package zxing;


public enum ResultMetadataType {

    /**
   * <p>2D barcode formats typically encode text, but allow for a sort of 'byte mode'
   * which is sometimes used to encode binary data. While {@link Result} makes available
   * the complete raw bytes in the barcode for these formats, it does not offer the bytes
   * from the byte segments alone.</p>
   *
   * <p>This maps to a {@link java.util.List} of byte arrays corresponding to the
   * raw bytes in the byte segments in the barcode, in order.</p>
   */
  BYTE_SEGMENTS,

  /**
   * Error correction level used, if applicable. The value type depends on the
   * format, but is typically a String.
   */
  ERROR_CORRECTION_LEVEL,

    /**
   * If the code format supports structured append and the current scanned code is part of one then the
   * sequence number is given with it.
   */
  STRUCTURED_APPEND_SEQUENCE,

  /**
   * If the code format supports structured append and the current scanned code is part of one then the
   * parity is given with it.
   */
  STRUCTURED_APPEND_PARITY,
  
}
