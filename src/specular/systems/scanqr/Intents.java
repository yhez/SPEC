package specular.systems.scanqr;

/**
 * This class provides the constants to use when sending an Intent to Barcode Scanner.
 * These strings are effectively API and cannot be changed.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
final class Intents
{
    private Intents()
	{
    }

    public static final class Scan
	{
        /**
         * By default, sending this will decode all barcodes that we understand. However it
         * may be useful to limit scanning to certain formats. Use
         * {@link android.content.Intent#putExtra(String, String)} with one of the values below.
         * <p/>
         * Setting this is effectively shorthand for setting explicit formats with {@link #FORMATS}.
         * It is overridden by that setting.
         */
        public static final String MODE = "SCAN_MODE";

        /**
         * Decode only UPC and EAN barcodes. This is the right choice for shopping apps which getKeysFromSdcard
         * prices, reviews, etc. for products.
         */
       // public static final String PRODUCT_MODE = "PRODUCT_MODE";

        /**
         * Decode only 1D barcodes.
         */
        //public static final String ONE_D_MODE = "ONE_D_MODE";

        /**
         * Decode only QR codes.
         */
        public static final String QR_CODE_MODE = "QR_CODE_MODE";

        /**
         * Decode only Data Matrix codes.
         */
        public static final String DATA_MATRIX_MODE = "DATA_MATRIX_MODE";

        /**
         * Comma-separated list of formats to scan for. The values must match the names of
         * {@link com.google.zxing.BarcodeFormat}s, e.g. {@link com.google.zxing.BarcodeFormat#EAN_13}.
         * Example: "EAN_13,EAN_8,QR_CODE"
         * <p/>
         * This overrides {@link #MODE}.
         */
        public static final String FORMATS = "SCAN_FORMATS";

        /**
         * @see com.google.zxing.DecodeHintType#CHARACTER_SET
         */
        public static final String CHARACTER_SET = "CHARACTER_SET";
	}
}
