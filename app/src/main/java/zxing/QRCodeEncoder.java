package zxing;

import android.graphics.Bitmap;
import android.graphics.Color;

import java.util.EnumMap;
import java.util.Map;

import zxing.common.BitMatrix;

final public class QRCodeEncoder {
    private static final int WHITE = 0xFFFFFFFF;
    private final int BLACK;

    private int dimension = Integer.MIN_VALUE;
    private String contents = null;
    private boolean encoded = false;

    public QRCodeEncoder(String data, int dimension) {
        this.dimension = dimension;
        //to prevent too light colors
        int red = Integer.parseInt(data.substring(data.length() - 2), 16);
        int green = Integer.parseInt(data.substring(data.length() - 4,data.length()-2), 16);
        int blue = Integer.parseInt(data.substring(data.length() - 6,data.length()-4), 16);
        int third =85;
        if(!(red<third||green<third||blue<third)){
            red-=third;
            green-=third;
            blue-=third;
        }
        if(!(red<third||green<third||blue<third)){
            red-=third;
            green-=third;
            blue-=third;
        }
        BLACK = Color.argb(0xFF,red,green,blue);
        encoded = encodeContents(data);
    }
    private boolean encodeContents(String data) {
            encodeQRCodeContents(data);
        return contents != null && contents.length() > 0;
    }

    private void encodeQRCodeContents(String data) {
        if (data != null && data.length() > 0) {
            contents = data;
        }
    }

    public Bitmap encodeAsBitmap() throws WriterException {
        if (!encoded) return null;

        Map<EncodeHintType, Object> hints = null;
        String encoding = guessAppropriateEncoding(contents);
        if (encoding != null) {
            hints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, encoding);
        }
        MultiFormatWriter writer = new MultiFormatWriter();
        BitMatrix result = writer.encode(contents, dimension, dimension, hints);
        int width = result.getWidth();
        int height = result.getHeight();
        int[] pixels = new int[width * height];
        // All are 0, or black, by default
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    private static String guessAppropriateEncoding(CharSequence contents) {
        // Very crude at the moment
        for (int i = 0; i < contents.length(); i++) {
            if (contents.charAt(i) > 0xFF) {
                return "UTF-8";
            }
        }
        return null;
    }
}
