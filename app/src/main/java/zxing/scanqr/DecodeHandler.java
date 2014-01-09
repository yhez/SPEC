package zxing.scanqr;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.Map;

import specular.systems.R;
import zxing.BinaryBitmap;
import zxing.DecodeHintType;
import zxing.MultiFormatReader;
import zxing.ReaderException;
import zxing.Result;
import zxing.common.HybridBinarizer;

final class DecodeHandler extends Handler {


    private final CaptureActivity activity;
    private final MultiFormatReader multiFormatReader;
    private boolean running = true;

    DecodeHandler(CaptureActivity activity, Map<DecodeHintType, Object> hints) {
        multiFormatReader = new MultiFormatReader();
        multiFormatReader.setHints(hints);
        this.activity = activity;
    }

    @Override
    public void handleMessage(Message message) {
        if (!running) {
            return;
        }
        if (message.what == R.id.decode) {
            decode((byte[]) message.obj, message.arg1, message.arg2);
        } else if (message.what == R.id.quit) {
            running = false;
            Looper.myLooper().quit();
        }
    }

    private void decode(byte[] data, int width, int height) {
        Result rawResult = null;
        PlanarYUVLuminanceSource source = activity.getCameraManager().buildLuminanceSource(data, width, height);
        if (source != null) {
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            try {
                rawResult = multiFormatReader.decodeWithState(bitmap);
            } catch (ReaderException re) {
                // continue
            } finally {
                multiFormatReader.reset();
            }
        }

        Handler handler = activity.getHandler();
        if (rawResult != null) {
            // Don't log the barcode contents for security.
            if (handler != null) {
                Message message = Message.obtain(handler, R.id.decode_succeeded, rawResult);
                Bundle bundle = new Bundle();
                bundle.putParcelable(DecodeThread.BARCODE_BITMAP, source.renderCroppedGreyscaleBitmap());
                message.setData(bundle);
                message.sendToTarget();
            }
        } else {
            if (handler != null) {
                Message message = Message.obtain(handler, R.id.decode_failed);
                message.sendToTarget();
            }
        }
    }

}
