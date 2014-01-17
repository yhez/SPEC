
package zxing;

import java.util.Map;

import zxing.common.BitMatrix;

public interface Writer {

  BitMatrix encode(String contents, int width, int height)
      throws WriterException;

  BitMatrix encode(String contents,
                   int width,
                   int height,
                   Map<EncodeHintType, ?> hints)
      throws WriterException;

}
