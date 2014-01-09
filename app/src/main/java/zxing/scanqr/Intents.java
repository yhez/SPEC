package zxing.scanqr;

final class Intents {
    private Intents() {
    }

    public static final class Scan {
        public static final String MODE = "SCAN_MODE";

        public static final String QR_CODE_MODE = "QR_CODE_MODE";

        public static final String DATA_MATRIX_MODE = "DATA_MATRIX_MODE";

        public static final String FORMATS = "SCAN_FORMATS";

        public static final String CHARACTER_SET = "CHARACTER_SET";
    }
}
