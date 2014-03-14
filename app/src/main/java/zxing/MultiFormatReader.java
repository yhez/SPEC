/*
 * Copyright 2007 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package zxing;


import java.util.Map;

import zxing.qrcode.QRCodeReader;

/**
 * MultiFormatReader is a convenience class and the main entry point into the library for most uses.
 * By default it attempts to decode all barcode formats that the library supports. Optionally, you
 * can provide a hints object to request different behavior, for example only decoding QR codes.
 *
 * @author Sean Owen
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class MultiFormatReader implements Reader {

  private Map<DecodeHintType,?> hints;
  private Reader[] readers;

    /**
   * Decode an image using the hints provided. Does not honor existing state.
   *
   * @param image The pixel data to decode
   * @param hints The hints to use, clearing the previous state.
   * @return The contents of the image
   * @throws NotFoundException Any errors which occurred
   */
  @Override
  public Result decode(BinaryBitmap image, Map<DecodeHintType,?> hints) throws NotFoundException {
    setHints(hints);
    return decodeInternal(image);
  }

  public Result decodeWithState(BinaryBitmap image) throws NotFoundException {
    // Make sure to set up the default state so we don't crash
    if (readers == null) {
      setHints(null);
    }
    return decodeInternal(image);
  }

  /**
   * This method adds state to the MultiFormatReader. By setting the hints once, subsequent calls
   * to decodeWithState(image) can reuse the same set of readers without reallocating memory. This
   * is important for performance in continuous scan clients.
   *
   * @param hints The set of hints to use for subsequent calls to decode(image)
   */
  public void setHints(Map<DecodeHintType,?> hints) {
    this.hints = hints;
      Reader[] r = new Reader[1];
      r[0] = new QRCodeReader();
    this.readers = r;
  }

  @Override
  public void reset() {
    if (readers != null) {
      for (Reader reader : readers) {
        reader.reset();
      }
    }
  }

  private Result decodeInternal(BinaryBitmap image) throws NotFoundException {
    if (readers != null) {
      for (Reader reader : readers) {
        try {
          return reader.decode(image, hints);
        } catch (ReaderException re) {
          // continue
        }
      }
    }
    throw NotFoundException.getNotFoundInstance();
  }

}
