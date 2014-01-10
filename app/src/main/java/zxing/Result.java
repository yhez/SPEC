package zxing;

import java.util.EnumMap;
import java.util.Map;

public final class Result {

  private final String text;
  private final byte[] rawBytes;
  private ResultPoint[] resultPoints;
  private Map<ResultMetadataType,Object> resultMetadata;
  private final long timestamp;

  public Result(String text,
                byte[] rawBytes,
                ResultPoint[] resultPoints) {
    this(text, rawBytes, resultPoints, System.currentTimeMillis());
  }

  public Result(String text,
                byte[] rawBytes,
                ResultPoint[] resultPoints,
                long timestamp) {
    this.text = text;
    this.rawBytes = rawBytes;
    this.resultPoints = resultPoints;
    this.resultMetadata = null;
    this.timestamp = timestamp;
  }

  /**
   * @return raw text encoded by the barcode
   */
  public String getText() {
    return text;
  }

  /**
   * @return raw bytes encoded by the barcode, if applicable, otherwise {@code null}
   */
  public byte[] getRawBytes() {
    return rawBytes;
  }

  /**
   * @return points related to the barcode in the image. These are typically points
   *         identifying finder patterns or the corners of the barcode. The exact meaning is
   *         specific to the type of barcode that was decoded.
   */
  public ResultPoint[] getResultPoints() {
    return resultPoints;
  }



  /**
   * @return {@link java.util.Map} mapping {@link ResultMetadataType} keys to values. May be
   *   {@code null}. This contains optional metadata about what was detected about the barcode,
   *   like orientation.
   */
  public Map<ResultMetadataType,Object> getResultMetadata() {
    return resultMetadata;
  }

  public void putMetadata(ResultMetadataType type, Object value) {
    if (resultMetadata == null) {
      resultMetadata = new EnumMap<ResultMetadataType,Object>(ResultMetadataType.class);
    }
    resultMetadata.put(type, value);
  }

  public void putAllMetadata(Map<ResultMetadataType,Object> metadata) {
    if (metadata != null) {
      if (resultMetadata == null) {
        resultMetadata = metadata;
      } else {
        resultMetadata.putAll(metadata);
      }
    }
  }

  public void addResultPoints(ResultPoint[] newPoints) {
    ResultPoint[] oldPoints = resultPoints;
    if (oldPoints == null) {
      resultPoints = newPoints;
    } else if (newPoints != null && newPoints.length > 0) {
      ResultPoint[] allPoints = new ResultPoint[oldPoints.length + newPoints.length];
      System.arraycopy(oldPoints, 0, allPoints, 0, oldPoints.length);
      System.arraycopy(newPoints, 0, allPoints, oldPoints.length, newPoints.length);
      resultPoints = allPoints;
    }
  }

  public long getTimestamp() {
    return timestamp;
  }

  @Override
  public String toString() {
    return text;
  }

}
