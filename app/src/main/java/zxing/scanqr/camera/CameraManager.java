package zxing.scanqr.camera;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Handler;
import android.view.SurfaceHolder;

import java.io.IOException;

import specular.systems.R;
import zxing.scanqr.PlanarYUVLuminanceSource;

public final class CameraManager {

    private static final int MIN_FRAME_WIDTH = 240;
    private static final int MIN_FRAME_HEIGHT = 240;
    private static final int MAX_FRAME_WIDTH = 600;
    private static final int MAX_FRAME_HEIGHT = 400;

    private final CameraConfigurationManager configManager;
    private Camera camera;
    private Rect framingRect;
    private Rect framingRectInPreview;
    private boolean initialized;
    private boolean previewing;
    private int requestedFramingRectWidth;
    private int requestedFramingRectHeight;
    /**
     * Preview frames are delivered here, which we pass on to the registered handler. Make sure to
     * clear the handler so it will only receive one message.
     */
    private final PreviewCallback previewCallback;
    /**
     * Autofocus callbacks arrive here, and are dispatched to the Handler which requested them.
     */
    private final AutoFocusCallback autoFocusCallback;

    public CameraManager(Context context) {
        this.configManager = new CameraConfigurationManager(context);
        previewCallback = new PreviewCallback(configManager);
        autoFocusCallback = new AutoFocusCallback();
    }

    public void onF(boolean b) {
        configManager.setTorch(camera, b);
    }

    /**
     * Opens the camera driver and initializes the hardware parameters.
     *
     * @param holder The surface object which the camera will draw preview_direct_msg frames into.
     * @throws IOException Indicates the camera driver failed to open.
     */
    public void openDriver(SurfaceHolder holder) throws IOException {
        Camera theCamera = camera;
        if (theCamera == null) {
            theCamera = Camera.open();
            if (theCamera == null) {
                throw new IOException();
            }
            camera = theCamera;
        }
        theCamera.setPreviewDisplay(holder);

        if (!initialized) {
            initialized = true;
            configManager.initFromCameraParameters(theCamera);
            if (requestedFramingRectWidth > 0 && requestedFramingRectHeight > 0) {
                setManualFramingRect(requestedFramingRectWidth, requestedFramingRectHeight);
                requestedFramingRectWidth = 0;
                requestedFramingRectHeight = 0;
            }
        }
        configManager.setDesiredCameraParameters(theCamera);
    }

    /**
     * Closes the camera driver if still in use.
     */
    public void closeDriver() {
        if (camera != null) {
            camera.release();
            camera = null;
            // Make sure to clear these each time we close the camera, so that any scanning rect
            // requested by intent is forgotten.
            framingRect = null;
            framingRectInPreview = null;
        }
    }

    /**
     * Asks the camera hardware to begin drawing preview_direct_msg frames to the screen.
     */
    public void startPreview() {
        Camera theCamera = camera;
        if (theCamera != null && !previewing) {
            theCamera.startPreview();
            previewing = true;
        }
    }

    /**
     * Tells the camera to stop drawing preview_direct_msg frames.
     */
    public void stopPreview() {
        if (camera != null && previewing) {
            camera.stopPreview();
            previewCallback.setHandler(null, 0);
            autoFocusCallback.setHandler(null, 0);
            previewing = false;
        }
    }

    /**
     * A single preview_direct_msg frame will be returned to the handler supplied. The data will arrive as byte[]
     * in the message.obj field, with width and height encoded as message.arg1 and message.arg2,
     * respectively.
     *
     * @param handler The handler to send the message to.
     */
    public void requestPreviewFrame(Handler handler) {
        Camera theCamera = camera;
        if (theCamera != null && previewing) {
            previewCallback.setHandler(handler, R.id.decode);
            theCamera.setOneShotPreviewCallback(previewCallback);
        }
    }

    /**
     * Asks the camera hardware to perform an autofocus.
     *
     * @param handler The Handler to notify when the autofocus completes.
     */
    public void requestAutoFocus(Handler handler) {
        if (camera != null && previewing) {
            autoFocusCallback.setHandler(handler, R.id.auto_focus);
            try {
                camera.autoFocus(autoFocusCallback);
            } catch (RuntimeException re) {
                // Have heard RuntimeException reported in Android 4.0.x+; continue?
            }
        }
    }

    /**
     * Calculates the framing rect which the UI should draw to show the user where to place the
     * barcode. This target helps with alignment as well as forces the user to hold the device
     * far enough away to ensure the image will be in focus.
     *
     * @return The rectangle to draw on screen in window coordinates.
     */
    public Rect getFramingRect() {
        if (framingRect == null) {
            if (camera == null) {
                return null;
            }
            Point screenResolution = configManager.getScreenResolution();
            int width = screenResolution.x * 3 / 4;
            if (width < MIN_FRAME_WIDTH) {
                width = MIN_FRAME_WIDTH;
            } else if (width > MAX_FRAME_WIDTH) {
                width = MAX_FRAME_WIDTH;
            }
            int height = screenResolution.y * 3 / 4;
            if (height < MIN_FRAME_HEIGHT) {
                height = MIN_FRAME_HEIGHT;
            } else if (height > MAX_FRAME_HEIGHT) {
                height = MAX_FRAME_HEIGHT;
            }
            int leftOffset = (screenResolution.x - width) / 2;
            int topOffset = (screenResolution.y - height) / 2;
            framingRect = new Rect(leftOffset, topOffset, leftOffset + width, topOffset + height);
        }
        return framingRect;
    }

    /**
     * Like {@link #getFramingRect} but coordinates are in terms of the preview_direct_msg frame,
     * not UI / screen.
     */
    public Rect getFramingRectInPreview() {
        if (framingRectInPreview == null) {
            Rect framingRect = getFramingRect();
            if (framingRect == null) {
                return null;
            }
            Rect rect = new Rect(framingRect);
            Point cameraResolution = configManager.getCameraResolution();
            Point screenResolution = configManager.getScreenResolution();
            rect.left = rect.left * cameraResolution.x / screenResolution.x;
            rect.right = rect.right * cameraResolution.x / screenResolution.x;
            rect.top = rect.top * cameraResolution.y / screenResolution.y;
            rect.bottom = rect.bottom * cameraResolution.y / screenResolution.y;
            framingRectInPreview = rect;
        }
        return framingRectInPreview;
    }

    /**
     * Allows third party apps to specify the scanning rectangle dimensions, rather than determine
     * them automatically based on screen resolution.
     *
     * @param width  The width in pixels to scan.
     * @param height The height in pixels to scan.
     */
    public void setManualFramingRect(int width, int height) {
        if (initialized) {
            Point screenResolution = configManager.getScreenResolution();
            if (width > screenResolution.x) {
                width = screenResolution.x;
            }
            if (height > screenResolution.y) {
                height = screenResolution.y;
            }
            int leftOffset = (screenResolution.x - width) / 2;
            int topOffset = (screenResolution.y - height) / 2;
            framingRect = new Rect(leftOffset, topOffset, leftOffset + width, topOffset + height);
            framingRectInPreview = null;
        } else {
            requestedFramingRectWidth = width;
            requestedFramingRectHeight = height;
        }
    }

    /**
     * A factory method to build the appropriate LuminanceSource object based on the format
     * of the preview_direct_msg buffers, as described by Camera.Parameters.
     *
     * @param data   A preview_direct_msg frame.
     * @param width  The width of the image.
     * @param height The height of the image.
     * @return A PlanarYUVLuminanceSource instance.
     */
    public PlanarYUVLuminanceSource buildLuminanceSource(byte[] data, int width, int height) {
        Rect rect = getFramingRectInPreview();
        if (rect == null) {
            return null;
        }
        // Go ahead and assume it's YUV rather than die.
        return new PlanarYUVLuminanceSource(data, width, height, rect.left, rect.top,
                rect.width(), rect.height());
    }

}
