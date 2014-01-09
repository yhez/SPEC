package specular.systems.scanqr;

import android.content.Intent;

import zxing.BarcodeFormat;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.regex.Pattern;

final class DecodeFormatManager {

    private static final Pattern COMMA_PATTERN = Pattern.compile(",");

    static final Collection<BarcodeFormat> QR_CODE_FORMATS = EnumSet.of(BarcodeFormat.QR_CODE);
    static final Collection<BarcodeFormat> DATA_MATRIX_FORMATS = EnumSet.of(BarcodeFormat.DATA_MATRIX);

    private DecodeFormatManager() {
    }

    static Collection<BarcodeFormat> parseDecodeFormats(Intent intent) {
        List<String> scanFormats = null;
        String scanFormatsString = intent.getStringExtra(Intents.Scan.FORMATS);
        if (scanFormatsString != null) {
            scanFormats = Arrays.asList(COMMA_PATTERN.split(scanFormatsString));
        }
        return parseDecodeFormats(scanFormats, intent.getStringExtra(Intents.Scan.MODE));
    }

    private static Collection<BarcodeFormat> parseDecodeFormats(Iterable<String> scanFormats,
                                                                String decodeMode) {
        if (scanFormats != null) {
            Collection<BarcodeFormat> formats = EnumSet.noneOf(BarcodeFormat.class);
            try {
                for (String format : scanFormats) {
                    formats.add(BarcodeFormat.valueOf(format));
                }
                return formats;
            } catch (IllegalArgumentException iae) {
                iae.printStackTrace();
            }
        }
        if (decodeMode != null) {
            if (Intents.Scan.QR_CODE_MODE.equals(decodeMode)) {
                return QR_CODE_FORMATS;
            }
            if (Intents.Scan.DATA_MATRIX_MODE.equals(decodeMode)) {
                return DATA_MATRIX_FORMATS;
            }
        }
        return null;
    }
}
