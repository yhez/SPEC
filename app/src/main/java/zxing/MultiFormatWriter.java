package zxing;

import java.util.Map;

import zxing.common.BitMatrix;
import zxing.qrcode.QRCodeWriter;


public final class MultiFormatWriter implements Writer {

  @Override
  public BitMatrix encode(String contents,
                          int width,
                          int height) throws WriterException {
    return encode(contents, width, height, null);
  }

  @Override
  public BitMatrix encode(String contents,
                          int width, int height,
                          Map<EncodeHintType,?> hints) throws WriterException {
    return new QRCodeWriter().encode(contents, width, height, hints);
  }

}
