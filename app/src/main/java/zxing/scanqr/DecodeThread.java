package zxing.scanqr;

import android.os.Handler;
import android.os.Looper;

import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import zxing.BarcodeFormat;
import zxing.DecodeHintType;
import zxing.ResultPointCallback;

final class DecodeThread extends Thread {

    public static final String BARCODE_BITMAP = "barcode_bitmap";

    private final CaptureActivity activity;
    private final Map<DecodeHintType, Object> hints;
    private Handler handler;
    private final CountDownLatch handlerInitLatch;

    DecodeThread(CaptureActivity activity,
                 Collection<BarcodeFormat> decodeFormats, String characterSet,
                 ResultPointCallback resultPointCallback) {

        this.activity = activity;
        handlerInitLatch = new CountDownLatch(1);

        hints = new EnumMap<DecodeHintType, Object>(DecodeHintType.class);

        // The prefs can't change while the thread is running, so pick them up
        // once here.
        if (decodeFormats == null || decodeFormats.isEmpty()) {
            decodeFormats = EnumSet.noneOf(BarcodeFormat.class);
            //decodeFormats.addAll(DecodeFormatManager.ONE_D_FORMATS);
            decodeFormats.addAll(DecodeFormatManager.QR_CODE_FORMATS);
            decodeFormats.addAll(DecodeFormatManager.DATA_MATRIX_FORMATS);
        }
        hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);

        if (characterSet != null) {
            hints.put(DecodeHintType.CHARACTER_SET, characterSet);
        }
        hints.put(DecodeHintType.NEED_RESULT_POINT_CALLBACK,
                resultPointCallback);
    }

    Handler getHandler() {
        try {
            handlerInitLatch.await();
        } catch (InterruptedException ie) {
            // continue?
        }
        return handler;
    }

    @Override
    public void run() {
        Looper.prepare();
        handler = new DecodeHandler(activity, hints);
        handlerInitLatch.countDown();
        Looper.loop();
    }

}
