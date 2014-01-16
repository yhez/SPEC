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

package zxing.common;

import zxing.NotFoundException;

/**
 * @author Sean Owen
 */
public final class DefaultGridSampler extends GridSampler {

    @Override
  public BitMatrix sampleGrid(BitMatrix image,
                              int dimensionX,
                              int dimensionY,
                              PerspectiveTransform transform) throws NotFoundException {
    if (dimensionX <= 0 || dimensionY <= 0) {
      throw NotFoundException.getNotFoundInstance();      
    }
    BitMatrix bits = new BitMatrix(dimensionX, dimensionY);
    float[] points = new float[dimensionX << 1];
    for (int y = 0; y < dimensionY; y++) {
      int max = points.length;
      float iValue = (float) y + 0.5f;
      for (int x = 0; x < max; x += 2) {
        points[x] = (float) (x >> 1) + 0.5f;
        points[x + 1] = iValue;
      }
      transform.transformPoints(points);
      // Quick check to see if points transformed to something inside the image;
      // sufficient to check the endpoints
      checkAndNudgePoints(image, points);
      try {
        for (int x = 0; x < max; x += 2) {
          if (image.get((int) points[x], (int) points[x + 1])) {
            // Black(-ish) pixel
            bits.set(x >> 1, y);
          }
        }
      } catch (ArrayIndexOutOfBoundsException aioobe) {
        throw NotFoundException.getNotFoundInstance();
      }
    }
    return bits;
  }

}
