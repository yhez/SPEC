package specular.systems.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.webkit.MimeTypeMap;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import specular.systems.FilesManagement;
import specular.systems.KeysDeleter;
import specular.systems.R;


public class FilesOpener extends Activity {
    public static boolean locationPrivate;
    public static String type;
    String path;
    MediaPlayer mp;
    boolean done;

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        String fileName = getIntent().getStringExtra("file_name");
        if (!locationPrivate) {
            File path = new File(Environment.getExternalStorageDirectory() + "/SPEC/attachments");
            File f = new File(path, fileName);
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setDataAndType(Uri.fromFile(f), type);
            finish();
            try {
                startActivity(i);
            } catch (Exception e) {
                Toast t = Toast.makeText(this, R.string.cant_find_an_app_to_open_file, Toast.LENGTH_SHORT);
                t.setGravity(Gravity.CENTER, 0, 0);
                t.show();
            }
            return;
        }
        path = new File(getFilesDir(), fileName).getPath();
        if (type.startsWith("image")) {
            Bitmap bm = BitmapFactory.decodeFile(path);
            TouchImageView iv = new TouchImageView(this);
            iv.setImageBitmap(bm);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
            iv.setLayoutParams(params);
            FrameLayout fl = new FrameLayout(this);
            fl.addView(iv);
            TextView tv = new TextView(this);
            params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL | Gravity.TOP);
            tv.setLayoutParams(params);
            tv.setTextColor(Color.WHITE);
            tv.setBackgroundColor(Color.BLACK);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            tv.setPadding(20, 20, 20, 0);
            tv.setText(path.substring(path.lastIndexOf("/") + 1));
            fl.addView(tv);
            setContentView(fl);
        } else if (type.startsWith("audio") || type.equals("application/ogg") || type.equals("video/3gpp")) {
            setContentView(R.layout.player_progress);
            startPlay();
        } else if (type.startsWith("text")) {
            try {
                InputStream is = openFileInput(fileName);
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();
                TextView tv = new TextView(this);
                tv.setText(Html.fromHtml(new String(buffer)));
                tv.setPadding(32, 32, 32, 32);
                tv.setGravity(Gravity.CENTER);
                tv.setTextIsSelectable(true);
                tv.setMovementMethod(LinkMovementMethod.getInstance());
                setContentView(tv);
            } catch (Exception e) {
                Toast t = Toast.makeText(this, "failed to read file", Toast.LENGTH_SHORT);
                t.setGravity(Gravity.CENTER, 0, 0);
                t.show();
                finish();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mp != null) {
            mp.stop();
            done=true;
            mp.release();
        }
        new KeysDeleter();
    }

    @Override
    public void onResume() {
        super.onResume();
        KeysDeleter.stop();
    }

    public void play(View v) {
        if (mp != null) {
            if(done){
                done=false;
                mp.reset();
                try {
                    mp.setDataSource(path);
                    mp.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            mp.start();
            v.setEnabled(false);
            findViewById(R.id.pause).setEnabled(true);
            progress(mp);
        }
    }

    private void startPlay() {
        try {
            mp = new MediaPlayer();
            mp.setScreenOnWhilePlaying(true);
            mp.setDataSource(path);
            mp.prepare();
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    findViewById(R.id.play).setEnabled(true);
                    findViewById(R.id.pause).setEnabled(false);
                    done = true;
                    ((TextView) findViewById(R.id.current_time)).setText(time(0));
                    ((ProgressBar) findViewById(R.id.progress_bar)).setProgress(0);
                }
            });
            ((TextView) findViewById(R.id.total_time)).setText(time(mp.getDuration()));
            findViewById(R.id.play).setEnabled(false);
            mp.start();
            int duration = mp.getDuration();
            ((ProgressBar) findViewById(R.id.progress_bar)).setMax(duration);
            progress(mp);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void progress(final MediaPlayer mp) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!done&&mp.isPlaying()) {
                    final int currentPosition = mp.getCurrentPosition();
                    synchronized (this) {
                        try {
                            ((Object) this).wait(25);
                        } catch (Exception e) {
                        }
                    }
                    findViewById(R.id.current_time).post(new Runnable() {
                        @Override
                        public void run() {
                            if(!done)
                            ((TextView) findViewById(R.id.current_time)).setText(time(currentPosition));
                        }
                    });
                    findViewById(R.id.progress_bar).post(new Runnable() {
                        @Override
                        public void run() {
                            if(!done)
                            ((ProgressBar) findViewById(R.id.progress_bar)).setProgress(currentPosition);
                        }
                    });
                }
            }
        }).start();
    }

    public void pause(View v) {
        if (mp != null) {
            mp.pause();
            v.setEnabled(false);
            findViewById(R.id.play).setEnabled(true);
        }
    }

    private String time(int length) {
        length /= 1000;
        int sec = length % 60;
        length /= 60;
        int min = length % 60;
        length /= 60;
        int h = length;
        return h + ":" + min + ":" + sec;
    }

    enum State {NONE, DRAG, ZOOM, FLING, ANIMATE_ZOOM}

    public static boolean saveFileToOpen(Activity a, byte[] file, String name) {
        MimeTypeMap m = MimeTypeMap.getSingleton();
        String ext = name.substring(name.lastIndexOf(".") + 1);
        type = m.getMimeTypeFromExtension(ext);
        if (type != null && (type.startsWith("image")
                || type.startsWith("audio")
                || type.equals("application/ogg")
                || type.equals("video/3gpp")
                || type.startsWith("text"))) {
            locationPrivate = true;
            return FilesManagement.saveFileOnDevice(a, file, name);
        }
        locationPrivate = false;
        return FilesManagement.saveFileOnPublicFolder(a, file, name);
    }

    public class TouchImageView extends ImageView {
        private static final float SUPER_MIN_MULTIPLIER = .75f;
        private static final float SUPER_MAX_MULTIPLIER = 1.25f;
        private float normalizedScale;
        private Matrix matrix, prevMatrix;

        private State state;

        private float minScale;
        private float maxScale;
        private float superMinScale;
        private float superMaxScale;
        private float[] m;

        private Context context;
        private Fling fling;

        private int viewWidth, viewHeight, prevViewWidth, prevViewHeight;

        private float matchViewWidth, matchViewHeight, prevMatchViewWidth, prevMatchViewHeight;

        private boolean maintainZoomAfterSetImage;

        private boolean setImageCalledRecenterImage;

        private ScaleGestureDetector mScaleDetector;
        private GestureDetector mGestureDetector;

        public TouchImageView(Context context) {
            super(context);
            sharedConstructing(context);
        }

        private void sharedConstructing(Context context) {
            super.setClickable(true);
            this.context = context;
            mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
            mGestureDetector = new GestureDetector(context, new GestureListener());
            matrix = new Matrix();
            prevMatrix = new Matrix();
            m = new float[9];
            normalizedScale = 1;
            minScale = 1;
            maxScale = 3;
            superMinScale = SUPER_MIN_MULTIPLIER * minScale;
            superMaxScale = SUPER_MAX_MULTIPLIER * maxScale;
            maintainZoomAfterSetImage = true;
            setImageMatrix(matrix);
            setScaleType(ScaleType.MATRIX);
            setState(State.NONE);
            setOnTouchListener(new TouchImageViewListener());
        }

        @Override
        public void setImageResource(int resId) {
            super.setImageResource(resId);
            setImageCalled();
            savePreviousImageValues();
            fitImageToView();
        }

        @Override
        public void setImageBitmap(Bitmap bm) {
            super.setImageBitmap(bm);
            setImageCalled();
            savePreviousImageValues();
            fitImageToView();
        }

        @Override
        public void setImageDrawable(Drawable drawable) {
            super.setImageDrawable(drawable);
            setImageCalled();
            savePreviousImageValues();
            fitImageToView();
        }

        @Override
        public void setImageURI(Uri uri) {
            super.setImageURI(uri);
            setImageCalled();
            savePreviousImageValues();
            fitImageToView();
        }

        private void setImageCalled() {
            if (!maintainZoomAfterSetImage) {
                setImageCalledRecenterImage = true;
            }
        }

        /**
         * Save the current matrix and view dimensions
         * in the prevMatrix and prevView variables.
         */
        private void savePreviousImageValues() {
            if (matrix != null) {
                matrix.getValues(m);
                prevMatrix.setValues(m);
                prevMatchViewHeight = matchViewHeight;
                prevMatchViewWidth = matchViewWidth;
                prevViewHeight = viewHeight;
                prevViewWidth = viewWidth;
            }
        }

        @Override
        public Parcelable onSaveInstanceState() {
            Bundle bundle = new Bundle();
            bundle.putParcelable("instanceState", super.onSaveInstanceState());
            bundle.putFloat("saveScale", normalizedScale);
            bundle.putFloat("matchViewHeight", matchViewHeight);
            bundle.putFloat("matchViewWidth", matchViewWidth);
            bundle.putInt("viewWidth", viewWidth);
            bundle.putInt("viewHeight", viewHeight);
            matrix.getValues(m);
            bundle.putFloatArray("matrix", m);
            return bundle;
        }

        @Override
        public void onRestoreInstanceState(Parcelable state) {
            if (state instanceof Bundle) {
                Bundle bundle = (Bundle) state;
                normalizedScale = bundle.getFloat("saveScale");
                m = bundle.getFloatArray("matrix");
                prevMatrix.setValues(m);
                prevMatchViewHeight = bundle.getFloat("matchViewHeight");
                prevMatchViewWidth = bundle.getFloat("matchViewWidth");
                prevViewHeight = bundle.getInt("viewHeight");
                prevViewWidth = bundle.getInt("viewWidth");
                super.onRestoreInstanceState(bundle.getParcelable("instanceState"));
                return;
            }

            super.onRestoreInstanceState(state);
        }

        private void fixTrans() {
            matrix.getValues(m);
            float transX = m[Matrix.MTRANS_X];
            float transY = m[Matrix.MTRANS_Y];

            float fixTransX = getFixTrans(transX, viewWidth, getImageWidth());
            float fixTransY = getFixTrans(transY, viewHeight, getImageHeight());

            if (fixTransX != 0 || fixTransY != 0) {
                matrix.postTranslate(fixTransX, fixTransY);
            }
        }

        private void fixScaleTrans() {
            fixTrans();
            matrix.getValues(m);
            if (getImageWidth() < viewWidth) {
                m[Matrix.MTRANS_X] = (viewWidth - getImageWidth()) / 2;
            }

            if (getImageHeight() < viewHeight) {
                m[Matrix.MTRANS_Y] = (viewHeight - getImageHeight()) / 2;
            }
            matrix.setValues(m);
        }

        private float getFixTrans(float trans, float viewSize, float contentSize) {
            float minTrans, maxTrans;

            if (contentSize <= viewSize) {
                minTrans = 0;
                maxTrans = viewSize - contentSize;

            } else {
                minTrans = viewSize - contentSize;
                maxTrans = 0;
            }

            if (trans < minTrans)
                return -trans + minTrans;
            if (trans > maxTrans)
                return -trans + maxTrans;
            return 0;
        }

        private float getFixDragTrans(float delta, float viewSize, float contentSize) {
            if (contentSize <= viewSize) {
                return 0;
            }
            return delta;
        }

        private float getImageWidth() {
            return matchViewWidth * normalizedScale;
        }

        private float getImageHeight() {
            return matchViewHeight * normalizedScale;
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            Drawable drawable = getDrawable();
            if (drawable == null || drawable.getIntrinsicWidth() == 0 || drawable.getIntrinsicHeight() == 0) {
                setMeasuredDimension(0, 0);
                return;
            }

            int drawableWidth = drawable.getIntrinsicWidth();
            int drawableHeight = drawable.getIntrinsicHeight();
            int widthSize = MeasureSpec.getSize(widthMeasureSpec);
            int widthMode = MeasureSpec.getMode(widthMeasureSpec);
            int heightSize = MeasureSpec.getSize(heightMeasureSpec);
            int heightMode = MeasureSpec.getMode(heightMeasureSpec);
            viewWidth = setViewSize(widthMode, widthSize, drawableWidth);
            viewHeight = setViewSize(heightMode, heightSize, drawableHeight);
            setMeasuredDimension(viewWidth, viewHeight);
            fitImageToView();
        }

        private void fitImageToView() {
            Drawable drawable = getDrawable();
            if (drawable == null || drawable.getIntrinsicWidth() == 0 || drawable.getIntrinsicHeight() == 0) {
                return;
            }
            if (matrix == null || prevMatrix == null) {
                return;
            }

            int drawableWidth = drawable.getIntrinsicWidth();
            int drawableHeight = drawable.getIntrinsicHeight();

            //
            // Scale image for view
            //
            float scaleX = (float) viewWidth / drawableWidth;
            float scaleY = (float) viewHeight / drawableHeight;
            float scale = Math.min(scaleX, scaleY);

            //
            // Center the image
            //
            float redundantYSpace = viewHeight - (scale * drawableHeight);
            float redundantXSpace = viewWidth - (scale * drawableWidth);
            matchViewWidth = viewWidth - redundantXSpace;
            matchViewHeight = viewHeight - redundantYSpace;
            if (normalizedScale == 1 || setImageCalledRecenterImage) {
                //
                // Stretch and center image to fit view
                //
                matrix.setScale(scale, scale);
                matrix.postTranslate(redundantXSpace / 2, redundantYSpace / 2);
                normalizedScale = 1;
                setImageCalledRecenterImage = false;

            } else {
                prevMatrix.getValues(m);

                //
                // Rescale Matrix after rotation
                //
                m[Matrix.MSCALE_X] = matchViewWidth / drawableWidth * normalizedScale;
                m[Matrix.MSCALE_Y] = matchViewHeight / drawableHeight * normalizedScale;

                //
                // TransX and TransY from previous matrix
                //
                float transX = m[Matrix.MTRANS_X];
                float transY = m[Matrix.MTRANS_Y];

                //
                // Width
                //
                float prevActualWidth = prevMatchViewWidth * normalizedScale;
                float actualWidth = getImageWidth();
                translateMatrixAfterRotate(Matrix.MTRANS_X, transX, prevActualWidth, actualWidth, prevViewWidth, viewWidth, drawableWidth);

                //
                // Height
                //
                float prevActualHeight = prevMatchViewHeight * normalizedScale;
                float actualHeight = getImageHeight();
                translateMatrixAfterRotate(Matrix.MTRANS_Y, transY, prevActualHeight, actualHeight, prevViewHeight, viewHeight, drawableHeight);

                //
                // Set the matrix to the adjusted scale and translate values.
                //
                matrix.setValues(m);
            }
            setImageMatrix(matrix);
        }

        private int setViewSize(int mode, int size, int drawableWidth) {
            int viewSize;
            switch (mode) {
                case MeasureSpec.EXACTLY:
                    viewSize = size;
                    break;

                case MeasureSpec.AT_MOST:
                    viewSize = Math.min(drawableWidth, size);
                    break;

                case MeasureSpec.UNSPECIFIED:
                    viewSize = drawableWidth;
                    break;

                default:
                    viewSize = size;
                    break;
            }
            return viewSize;
        }

        private void translateMatrixAfterRotate(int axis, float trans, float prevImageSize, float imageSize, int prevViewSize, int viewSize, int drawableSize) {
            if (imageSize < viewSize) {
                //
                // The width/height of image is less than the view's width/height. Center it.
                //
                m[axis] = (viewSize - (drawableSize * m[Matrix.MSCALE_X])) * 0.5f;

            } else if (trans > 0) {
                //
                // The image is larger than the view, but was not before rotation. Center it.
                //
                m[axis] = -((imageSize - viewSize) * 0.5f);

            } else {
                //
                // Find the area of the image which was previously centered in the view. Determine its distance
                // from the left/top side of the view as a fraction of the entire image's width/height. Use that percentage
                // to calculate the trans in the new view width/height.
                //
                float percentage = (Math.abs(trans) + (0.5f * prevViewSize)) / prevImageSize;
                m[axis] = -((percentage * imageSize) - (viewSize * 0.5f));
            }
        }

        private void setState(State state) {
            this.state = state;
        }

        private class GestureListener extends GestureDetector.SimpleOnGestureListener {

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                return performClick();
            }

            @Override
            public void onLongPress(MotionEvent e) {
                performLongClick();
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (fling != null) {
                    //
                    // If a previous fling is still active, it should be cancelled so that two flings
                    // are not run simultaenously.
                    //
                    fling.cancelFling();
                }
                fling = new Fling((int) velocityX, (int) velocityY);
                compatPostOnAnimation(fling);
                return super.onFling(e1, e2, velocityX, velocityY);
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                boolean consumed = false;
                if (state == State.NONE) {
                    float targetZoom = (normalizedScale == minScale) ? maxScale : minScale;
                    DoubleTapZoom doubleTap = new DoubleTapZoom(targetZoom, e.getX(), e.getY(), false);
                    compatPostOnAnimation(doubleTap);
                    consumed = true;
                }
                return consumed;
            }
        }

        class TouchImageViewListener implements OnTouchListener {

            //
            // Remember last point position for dragging
            //
            private PointF last = new PointF();

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mScaleDetector.onTouchEvent(event);
                mGestureDetector.onTouchEvent(event);
                PointF curr = new PointF(event.getX(), event.getY());

                if (state == State.NONE || state == State.DRAG || state == State.FLING) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            last.set(curr);
                            if (fling != null)
                                fling.cancelFling();
                            setState(State.DRAG);
                            break;

                        case MotionEvent.ACTION_MOVE:
                            if (state == State.DRAG) {
                                float deltaX = curr.x - last.x;
                                float deltaY = curr.y - last.y;
                                float fixTransX = getFixDragTrans(deltaX, viewWidth, getImageWidth());
                                float fixTransY = getFixDragTrans(deltaY, viewHeight, getImageHeight());
                                matrix.postTranslate(fixTransX, fixTransY);
                                fixTrans();
                                last.set(curr.x, curr.y);
                            }
                            break;

                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_POINTER_UP:
                            setState(State.NONE);
                            break;
                    }
                }

                setImageMatrix(matrix);
                //
                // indicate event was handled
                //
                return true;
            }
        }

        private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                setState(State.ZOOM);
                return true;
            }

            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                scaleImage(detector.getScaleFactor(), detector.getFocusX(), detector.getFocusY(), true);
                return true;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {
                super.onScaleEnd(detector);
                setState(State.NONE);
                boolean animateToZoomBoundary = false;
                float targetZoom = normalizedScale;
                if (normalizedScale > maxScale) {
                    targetZoom = maxScale;
                    animateToZoomBoundary = true;

                } else if (normalizedScale < minScale) {
                    targetZoom = minScale;
                    animateToZoomBoundary = true;
                }

                if (animateToZoomBoundary) {
                    DoubleTapZoom doubleTap = new DoubleTapZoom(targetZoom, viewWidth / 2, viewHeight / 2, true);
                    compatPostOnAnimation(doubleTap);
                }
            }
        }

        private void scaleImage(float deltaScale, float focusX, float focusY, boolean stretchImageToSuper) {

            float lowerScale, upperScale;
            if (stretchImageToSuper) {
                lowerScale = superMinScale;
                upperScale = superMaxScale;

            } else {
                lowerScale = minScale;
                upperScale = maxScale;
            }

            float origScale = normalizedScale;
            normalizedScale *= deltaScale;
            if (normalizedScale > upperScale) {
                normalizedScale = upperScale;
                deltaScale = upperScale / origScale;
            } else if (normalizedScale < lowerScale) {
                normalizedScale = lowerScale;
                deltaScale = lowerScale / origScale;
            }

            matrix.postScale(deltaScale, deltaScale, focusX, focusY);
            fixScaleTrans();
        }

        /**
         * DoubleTapZoom calls a series of runnables which apply
         * an animated zoom in/out graphic to the image.
         *
         * @author Ortiz
         */
        private class DoubleTapZoom implements Runnable {

            private long startTime;
            private static final float ZOOM_TIME = 500;
            private float startZoom, targetZoom;
            private float bitmapX, bitmapY;
            private boolean stretchImageToSuper;
            private AccelerateDecelerateInterpolator interpolator = new AccelerateDecelerateInterpolator();
            private PointF startTouch;
            private PointF endTouch;

            DoubleTapZoom(float targetZoom, float focusX, float focusY, boolean stretchImageToSuper) {
                setState(State.ANIMATE_ZOOM);
                startTime = System.currentTimeMillis();
                this.startZoom = normalizedScale;
                this.targetZoom = targetZoom;
                this.stretchImageToSuper = stretchImageToSuper;
                PointF bitmapPoint = transformCoordTouchToBitmap(focusX, focusY, false);
                this.bitmapX = bitmapPoint.x;
                this.bitmapY = bitmapPoint.y;

                //
                // Used for translating image during scaling
                //
                startTouch = transformCoordBitmapToTouch(bitmapX, bitmapY);
                endTouch = new PointF(viewWidth / 2, viewHeight / 2);
            }

            @Override
            public void run() {
                float t = interpolate();
                float deltaScale = calculateDeltaScale(t);
                scaleImage(deltaScale, bitmapX, bitmapY, stretchImageToSuper);
                translateImageToCenterTouchPosition(t);
                fixScaleTrans();
                setImageMatrix(matrix);

                if (t < 1f) {
                    //
                    // We haven't finished zooming
                    //
                    compatPostOnAnimation(this);

                } else {
                    //
                    // Finished zooming
                    //
                    setState(State.NONE);
                }
            }

            private void translateImageToCenterTouchPosition(float t) {
                float targetX = startTouch.x + t * (endTouch.x - startTouch.x);
                float targetY = startTouch.y + t * (endTouch.y - startTouch.y);
                PointF curr = transformCoordBitmapToTouch(bitmapX, bitmapY);
                matrix.postTranslate(targetX - curr.x, targetY - curr.y);
            }

            private float interpolate() {
                long currTime = System.currentTimeMillis();
                float elapsed = (currTime - startTime) / ZOOM_TIME;
                elapsed = Math.min(1f, elapsed);
                return interpolator.getInterpolation(elapsed);
            }

            private float calculateDeltaScale(float t) {
                float zoom = startZoom + t * (targetZoom - startZoom);
                return zoom / normalizedScale;
            }
        }

        private PointF transformCoordTouchToBitmap(float x, float y, boolean clipToBitmap) {
            matrix.getValues(m);
            float origW = getDrawable().getIntrinsicWidth();
            float origH = getDrawable().getIntrinsicHeight();
            float transX = m[Matrix.MTRANS_X];
            float transY = m[Matrix.MTRANS_Y];
            float finalX = ((x - transX) * origW) / getImageWidth();
            float finalY = ((y - transY) * origH) / getImageHeight();

            if (clipToBitmap) {
                finalX = Math.min(Math.max(x, 0), origW);
                finalY = Math.min(Math.max(y, 0), origH);
            }

            return new PointF(finalX, finalY);
        }

        private PointF transformCoordBitmapToTouch(float bx, float by) {
            matrix.getValues(m);
            float origW = getDrawable().getIntrinsicWidth();
            float origH = getDrawable().getIntrinsicHeight();
            float px = bx / origW;
            float py = by / origH;
            float finalX = m[Matrix.MTRANS_X] + getImageWidth() * px;
            float finalY = m[Matrix.MTRANS_Y] + getImageHeight() * py;
            return new PointF(finalX, finalY);
        }

        private class Fling implements Runnable {

            Scroller scroller;
            int currX, currY;

            Fling(int velocityX, int velocityY) {
                setState(State.FLING);
                scroller = new Scroller(context);
                matrix.getValues(m);

                int startX = (int) m[Matrix.MTRANS_X];
                int startY = (int) m[Matrix.MTRANS_Y];
                int minX, maxX, minY, maxY;

                if (getImageWidth() > viewWidth) {
                    minX = viewWidth - (int) getImageWidth();
                    maxX = 0;

                } else {
                    minX = maxX = startX;
                }

                if (getImageHeight() > viewHeight) {
                    minY = viewHeight - (int) getImageHeight();
                    maxY = 0;

                } else {
                    minY = maxY = startY;
                }

                scroller.fling(startX, startY, velocityX, velocityY, minX,
                        maxX, minY, maxY);
                currX = startX;
                currY = startY;
            }

            public void cancelFling() {
                if (scroller != null) {
                    setState(State.NONE);
                    scroller.forceFinished(true);
                }
            }

            @Override
            public void run() {
                if (scroller.isFinished()) {
                    scroller = null;
                    return;
                }

                if (scroller.computeScrollOffset()) {
                    int newX = scroller.getCurrX();
                    int newY = scroller.getCurrY();
                    int transX = newX - currX;
                    int transY = newY - currY;
                    currX = newX;
                    currY = newY;
                    matrix.postTranslate(transX, transY);
                    fixTrans();
                    setImageMatrix(matrix);
                    compatPostOnAnimation(this);
                }
            }
        }

        private void compatPostOnAnimation(Runnable runnable) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                postOnAnimation(runnable);

            } else {
                postDelayed(runnable, 1000 / 60);
            }
        }
    }
}
