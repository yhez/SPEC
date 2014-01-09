package zxing;

import zxing.common.BitMatrix;
import zxing.qrcode.QRCodeWriter;

import java.util.Map;


public final class MultiFormatWriter implements Writer {

  @Override
  public BitMatrix encode(String contents,
                          BarcodeFormat format,
                          int width,
                          int height) throws WriterException {
    return encode(contents, format, width, height, null);
  }

  @Override
  public BitMatrix encode(String contents,
                          BarcodeFormat format,
                          int width, int height,
                          Map<EncodeHintType,?> hints) throws WriterException {
    return new QRCodeWriter().encode(contents, format, width, height, hints);
  }

}
