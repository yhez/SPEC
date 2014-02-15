package specular.systems.activities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Debug;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;

import java.util.ArrayList;
import java.util.List;

import specular.systems.R;

public class LockPatternView extends View {

    private static final int ASPECT_SQUARE = 0;

    private static final int ASPECT_LOCK_WIDTH = 1;

    private static final int ASPECT_LOCK_HEIGHT = 2;

    public static final int MATRIX_WIDTH = 3;

    public static final int MATRIX_SIZE = MATRIX_WIDTH * MATRIX_WIDTH;

    private static final boolean PROFILE_DRAWING = false;
    private boolean mDrawingProfilingStarted = false;

    private Paint mPaint = new Paint();
    private Paint mPathPaint = new Paint();

    private static final int MILLIS_PER_CIRCLE_ANIMATING = 700;

    private static final float DRAG_THRESHHOLD = 0.0f;

    private OnPatternListener mOnPatternListener;
    private ArrayList<Cell> mPattern = new ArrayList<Cell>(MATRIX_SIZE);


    private boolean[][] mPatternDrawLookup = new boolean[MATRIX_WIDTH][MATRIX_WIDTH];

    private float mInProgressX = -1;
    private float mInProgressY = -1;

    private long mAnimatingPeriodStart;

    private DisplayMode mPatternDisplayMode = DisplayMode.Correct;
    private boolean mInputEnabled = true;
    private boolean mInStealthMode = false;
    private boolean mEnableHapticFeedback = true;
    private boolean mPatternInProgress = false;
    private float mDiameterFactor = 0.10f;
    private float mHitFactor = 0.6f;

    private float mSquareWidth;
    private float mSquareHeight;

    private Bitmap mBitmapBtnDefault;
    private Bitmap mBitmapBtnTouched;
    private Bitmap mBitmapCircleDefault;
    private Bitmap mBitmapCircleGreen;
    private Bitmap mBitmapCircleRed;

    private Bitmap mBitmapArrowGreenUp;
    private Bitmap mBitmapArrowRedUp;

    private final Path mCurrentPath = new Path();
    private final Rect mInvalidate = new Rect();
    private final Rect mTmpInvalidateRect = new Rect();

    private int mBitmapWidth;
    private int mBitmapHeight;

    private int mAspect;
    private final Matrix mArrowMatrix = new Matrix();
    private final Matrix mCircleMatrix = new Matrix();

    private final int mPadding = 0;
    private final int mPaddingLeft = mPadding;
    private final int mPaddingTop = mPadding;

    public static class Cell implements Parcelable {

        int mRow;
        int mColumn;

        static Cell[][] sCells = new Cell[MATRIX_WIDTH][MATRIX_WIDTH];
        static {
            for (int i = 0; i < MATRIX_WIDTH; i++) {
                for (int j = 0; j < MATRIX_WIDTH; j++) {
                    sCells[i][j] = new Cell(i, j);
                }
            }
        }

        private Cell(int row, int column) {
            checkRange(row, column);
            this.mRow = row;
            this.mColumn = column;
        }

        public int getRow() {
            return mRow;
        }
        public int getColumn() {
            return mColumn;
        }
        public int getId() {
            return mRow * MATRIX_WIDTH + mColumn;
        }
        public static synchronized Cell of(int row, int column) {
            checkRange(row, column);
            return sCells[row][column];
        }
        public static synchronized Cell of(int id) {
            return of(id / MATRIX_WIDTH, id % MATRIX_WIDTH);
        }

        private static void checkRange(int row, int column) {
            if (row < 0 || row > MATRIX_WIDTH - 1) {
                throw new IllegalArgumentException("row must be in range 0-"
                        + (MATRIX_WIDTH - 1));
            }
            if (column < 0 || column > MATRIX_WIDTH - 1) {
                throw new IllegalArgumentException("column must be in range 0-"
                        + (MATRIX_WIDTH - 1));
            }
        }

        @Override
        public String toString() {
            return "(ROW=" + getRow() + ",COL=" + getColumn() + ")";
        }// toString()

        @Override
        public boolean equals(Object object) {
            if (object instanceof Cell)
                return getColumn() == ((Cell) object).getColumn()
                        && getRow() == ((Cell) object).getRow();
            return super.equals(object);
        }// equals()

        /*
         * PARCELABLE
         */

        @Override
        public int describeContents() {
            return 0;
        }// describeContents()

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(getColumn());
            dest.writeInt(getRow());
        }// writeToParcel()

        /**
         * Reads data from parcel.
         *
         * @param in
         *            the parcel.
         */
        public void readFromParcel(Parcel in) {
            mColumn = in.readInt();
            mRow = in.readInt();
        }// readFromParcel()

        public static final Parcelable.Creator<Cell> CREATOR = new Parcelable.Creator<Cell>() {

            public Cell createFromParcel(Parcel in) {
                return new Cell(in);
            }// createFromParcel()

            public Cell[] newArray(int size) {
                return new Cell[size];
            }// newArray()
        };// CREATOR

        private Cell(Parcel in) {
            readFromParcel(in);
        }// Cell()
    }// Cell


    public enum DisplayMode {

        /**
         * The pattern drawn is correct (i.e draw it in a friendly color)
         */
        Correct,

        /**
         * Animate the pattern (for demo, and help).
         */
        Animate,

        /**
         * The pattern is wrong (i.e draw a foreboding color)
         */
        Wrong
    }
    public static interface OnPatternListener {
        void onPatternStart();
        void onPatternCleared();
        void onPatternCellAdded(List<Cell> pattern);
        void onPatternDetected(List<Cell> pattern);
    }
    public LockPatternView(Context context) {
        this(context, null);
    }
    public LockPatternView(Context context, AttributeSet attrs) {
        super(context, attrs);
        final String aspect = "";
        if ("square".equals(aspect)) {
            mAspect = ASPECT_SQUARE;
        } else if ("lock_width".equals(aspect)) {
            mAspect = ASPECT_LOCK_WIDTH;
        } else if ("lock_height".equals(aspect)) {
            mAspect = ASPECT_LOCK_HEIGHT;
        } else {
            mAspect = ASPECT_SQUARE;
        }

        setClickable(true);
        mPathPaint.setAntiAlias(true);
        mPathPaint.setDither(true);
        mPathPaint.setColor(Color.WHITE);
        int mStrokeAlpha = 128;
        mPathPaint.setAlpha(mStrokeAlpha);
        mPathPaint.setStyle(Paint.Style.STROKE);
        mPathPaint.setStrokeJoin(Paint.Join.ROUND);
        mPathPaint.setStrokeCap(Paint.Cap.ROUND);
        mBitmapBtnDefault = getBitmapFor(R.drawable.alp_btn_code_lock_default_holo_light);
        mBitmapBtnTouched = getBitmapFor(R.drawable.alp_btn_code_lock_touched_holo_light);
        mBitmapCircleDefault = getBitmapFor(R.drawable.alp_indicator_code_lock_point_area_default_holo_light);
        mBitmapCircleGreen = getBitmapFor(R.drawable.aosp_indicator_code_lock_point_area_green_holo);
        mBitmapCircleRed = getBitmapFor(R.drawable.aosp_indicator_code_lock_point_area_red_holo);
        mBitmapArrowGreenUp = getBitmapFor(R.drawable.aosp_indicator_code_lock_drag_direction_green_up);
        mBitmapArrowRedUp = getBitmapFor(R.drawable.aosp_indicator_code_lock_drag_direction_red_up);
        final Bitmap bitmaps[] = { mBitmapBtnDefault, mBitmapBtnTouched,
                mBitmapCircleDefault, mBitmapCircleGreen, mBitmapCircleRed };

        for (Bitmap bitmap : bitmaps) {
            mBitmapWidth = Math.max(mBitmapWidth, bitmap.getWidth());
            mBitmapHeight = Math.max(mBitmapHeight, bitmap.getHeight());
        }
    }
    private Bitmap getBitmapFor(int resId) {
        return BitmapFactory.decodeResource(getContext().getResources(), resId);
    }
 /*   public boolean isInStealthMode() {
        return mInStealthMode;
    }
    public boolean isTactileFeedbackEnabled() {
        return mEnableHapticFeedback;
    }
    public void setInStealthMode(boolean inStealthMode) {
        mInStealthMode = inStealthMode;
    }
    public void setTactileFeedbackEnabled(boolean tactileFeedbackEnabled) {
        mEnableHapticFeedback = tactileFeedbackEnabled;
    }*/
    public void setOnPatternListener(OnPatternListener onPatternListener) {
        mOnPatternListener = onPatternListener;
    }
/*    public void setPattern(DisplayMode displayMode, List<Cell> pattern) {
        mPattern.clear();
        mPattern.addAll(pattern);
        clearPatternDrawLookup();
        for (Cell cell : pattern) {
            mPatternDrawLookup[cell.getRow()][cell.getColumn()] = true;
        }

        setDisplayMode(displayMode);
    }*/
    public void setDisplayMode(DisplayMode displayMode) {
        mPatternDisplayMode = displayMode;
        if (displayMode == DisplayMode.Animate) {
            if (mPattern.size() == 0) {
                throw new IllegalStateException(
                        "you must have a pattern to "
                                + "animate if you want to set the display mode to animate");
            }
            mAnimatingPeriodStart = SystemClock.elapsedRealtime();
            final Cell first = mPattern.get(0);
            mInProgressX = getCenterXForColumn(first.getColumn());
            mInProgressY = getCenterYForRow(first.getRow());
            clearPatternDrawLookup();
        }
        invalidate();
    }
 /*   public DisplayMode getDisplayMode() {
        return mPatternDisplayMode;
    }*/
    @SuppressWarnings("unchecked")
    public List<Cell> getPattern() {
        return (List<Cell>) mPattern.clone();
    }

    private void notifyCellAdded() {
        sendAccessEvent("alp_lockscreen_access_pattern_cell_added");
        if (mOnPatternListener != null) {
            mOnPatternListener.onPatternCellAdded(mPattern);
        }
    }

    private void notifyPatternStarted() {
        sendAccessEvent("alp_lockscreen_access_pattern_start");
        if (mOnPatternListener != null) {
            mOnPatternListener.onPatternStart();
        }
    }

    private void notifyPatternDetected() {
        sendAccessEvent("alp_lockscreen_access_pattern_detected");
        if (mOnPatternListener != null) {
            mOnPatternListener.onPatternDetected(mPattern);
        }
    }

    private void notifyPatternCleared() {
        sendAccessEvent("alp_lockscreen_access_pattern_cleared");
        if (mOnPatternListener != null) {
            mOnPatternListener.onPatternCleared();
        }
    }

    /**
     * Clear the pattern.
     */
    /*public void clearPattern() {
        resetPattern();
    }*/
    private void resetPattern() {
        mPattern.clear();
        clearPatternDrawLookup();
        mPatternDisplayMode = DisplayMode.Correct;
        invalidate();
    }
    private void clearPatternDrawLookup() {
        for (int i = 0; i < MATRIX_WIDTH; i++) {
            for (int j = 0; j < MATRIX_WIDTH; j++) {
                mPatternDrawLookup[i][j] = false;
            }
        }
    }
/*    public void disableInput() {
        mInputEnabled = false;
    }
    public void enableInput() {
        mInputEnabled = true;
    }
*/
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        int mPaddingRight = mPadding;
        final int width = w - mPaddingLeft - mPaddingRight;
        mSquareWidth = width / (float) MATRIX_WIDTH;

        int mPaddingBottom = mPadding;
        final int height = h - mPaddingTop - mPaddingBottom;
        mSquareHeight = height / (float) MATRIX_WIDTH;
    }

    private int resolveMeasured(int measureSpec, int desired) {
        int result = 0;
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (MeasureSpec.getMode(measureSpec)) {
            case MeasureSpec.UNSPECIFIED:
                result = desired;
                break;
            case MeasureSpec.AT_MOST:
                result = Math.max(specSize, desired);
                break;
            case MeasureSpec.EXACTLY:
            default:
                result = specSize;
        }
        return result;
    }

    @Override
    protected int getSuggestedMinimumWidth() {
        /*
         * View should be large enough to contain MATRIX_WIDTH side-by-side
         * target bitmaps
         */
        return MATRIX_WIDTH * mBitmapWidth;
    }

    @Override
    protected int getSuggestedMinimumHeight() {
        /*
         * View should be large enough to contain MATRIX_WIDTH side-by-side
         * target bitmaps
         */
        return MATRIX_WIDTH * mBitmapWidth;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int minimumWidth = getSuggestedMinimumWidth();
        final int minimumHeight = getSuggestedMinimumHeight();
        int viewWidth = resolveMeasured(widthMeasureSpec, minimumWidth);
        int viewHeight = resolveMeasured(heightMeasureSpec, minimumHeight);

        switch (mAspect) {
            case ASPECT_SQUARE:
                viewWidth = viewHeight = Math.min(viewWidth, viewHeight);
                break;
            case ASPECT_LOCK_WIDTH:
                viewHeight = Math.min(viewWidth, viewHeight);
                break;
            case ASPECT_LOCK_HEIGHT:
                viewWidth = Math.min(viewWidth, viewHeight);
                break;
        }
        /*
         * Log.v(TAG, "LockPatternView dimensions: " + viewWidth + "x" +
         * viewHeight);
         */
        setMeasuredDimension(viewWidth, viewHeight);
    }
    private Cell detectAndAddHit(float x, float y) {
        final Cell cell = checkForNewHit(x, y);
        if (cell != null) {

            /*
             * check for gaps in existing pattern
             */
            Cell fillInGapCell = null;
            final ArrayList<Cell> pattern = mPattern;
            if (!pattern.isEmpty()) {
                final Cell lastCell = pattern.get(pattern.size() - 1);
                int dRow = cell.mRow - lastCell.mRow;
                int dColumn = cell.mColumn - lastCell.mColumn;

                int fillInRow = lastCell.mRow;
                int fillInColumn = lastCell.mColumn;

                if (Math.abs(dRow) == 2 && Math.abs(dColumn) != 1) {
                    fillInRow = lastCell.mRow + ((dRow > 0) ? 1 : -1);
                }

                if (Math.abs(dColumn) == 2 && Math.abs(dRow) != 1) {
                    fillInColumn = lastCell.mColumn + ((dColumn > 0) ? 1 : -1);
                }

                fillInGapCell = Cell.of(fillInRow, fillInColumn);
            }

            if (fillInGapCell != null
                    && !mPatternDrawLookup[fillInGapCell.mRow][fillInGapCell.mColumn]) {
                addCellToPattern(fillInGapCell);
            }
            addCellToPattern(cell);
            if (mEnableHapticFeedback) {
                performHapticFeedback(
                        HapticFeedbackConstants.VIRTUAL_KEY,
                        HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
                                | HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
            }
            return cell;
        }
        return null;
    }
    private void addCellToPattern(Cell newCell) {
        mPatternDrawLookup[newCell.getRow()][newCell.getColumn()] = true;
        mPattern.add(newCell);
        notifyCellAdded();
    }
    private Cell checkForNewHit(float x, float y) {

        final int rowHit = getRowHit(y);
        if (rowHit < 0) {
            return null;
        }
        final int columnHit = getColumnHit(x);
        if (columnHit < 0) {
            return null;
        }

        if (mPatternDrawLookup[rowHit][columnHit]) {
            return null;
        }
        return Cell.of(rowHit, columnHit);
    }
    private int getRowHit(float y) {

        final float squareHeight = mSquareHeight;
        float hitSize = squareHeight * mHitFactor;

        float offset = mPaddingTop + (squareHeight - hitSize) / 2f;
        for (int i = 0; i < MATRIX_WIDTH; i++) {

            final float hitTop = offset + squareHeight * i;
            if (y >= hitTop && y <= hitTop + hitSize) {
                return i;
            }
        }
        return -1;
    }
    private int getColumnHit(float x) {
        final float squareWidth = mSquareWidth;
        float hitSize = squareWidth * mHitFactor;

        float offset = mPaddingLeft + (squareWidth - hitSize) / 2f;
        for (int i = 0; i < MATRIX_WIDTH; i++) {

            final float hitLeft = offset + squareWidth * i;
            if (x >= hitLeft && x <= hitLeft + hitSize) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mInputEnabled || !isEnabled()) {
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                handleActionDown(event);
                return true;
            case MotionEvent.ACTION_UP:
                handleActionUp(event);
                return true;
            case MotionEvent.ACTION_MOVE:
                handleActionMove(event);
                return true;
            case MotionEvent.ACTION_CANCEL:
                mPatternInProgress = false;
                resetPattern();
                notifyPatternCleared();

                if (PROFILE_DRAWING) {
                    if (mDrawingProfilingStarted) {
                        Debug.stopMethodTracing();
                        mDrawingProfilingStarted = false;
                    }
                }
                return true;
        }
        return false;
    }

    private void handleActionMove(MotionEvent event) {
        /*
         * Handle all recent motion events so we don't skip any cells even when
         * the device is busy...
         */
        final float radius = (mSquareWidth * mDiameterFactor * 0.5f);
        final int historySize = event.getHistorySize();
        mTmpInvalidateRect.setEmpty();
        boolean invalidateNow = false;
        for (int i = 0; i < historySize + 1; i++) {
            final float x = i < historySize ? event.getHistoricalX(i) : event
                    .getX();
            final float y = i < historySize ? event.getHistoricalY(i) : event
                    .getY();
            Cell hitCell = detectAndAddHit(x, y);
            final int patternSize = mPattern.size();
            if (hitCell != null && patternSize == 1) {
                mPatternInProgress = true;
                notifyPatternStarted();
            }
            /*
             * note current x and y for rubber banding of in progress patterns
             */
            final float dx = Math.abs(x - mInProgressX);
            final float dy = Math.abs(y - mInProgressY);
            if (dx > DRAG_THRESHHOLD || dy > DRAG_THRESHHOLD) {
                invalidateNow = true;
            }

            if (mPatternInProgress && patternSize > 0) {
                final ArrayList<Cell> pattern = mPattern;
                final Cell lastCell = pattern.get(patternSize - 1);
                float lastCellCenterX = getCenterXForColumn(lastCell.mColumn);
                float lastCellCenterY = getCenterYForRow(lastCell.mRow);

                /*
                 * Adjust for drawn segment from last cell to (x,y). Radius
                 * accounts for line width.
                 */
                float left = Math.min(lastCellCenterX, x) - radius;
                float right = Math.max(lastCellCenterX, x) + radius;
                float top = Math.min(lastCellCenterY, y) - radius;
                float bottom = Math.max(lastCellCenterY, y) + radius;

                /*
                 * Invalidate between the pattern's new cell and the pattern's
                 * previous cell
                 */
                if (hitCell != null) {
                    final float width = mSquareWidth * 0.5f;
                    final float height = mSquareHeight * 0.5f;
                    final float hitCellCenterX = getCenterXForColumn(hitCell.mColumn);
                    final float hitCellCenterY = getCenterYForRow(hitCell.mRow);

                    left = Math.min(hitCellCenterX - width, left);
                    right = Math.max(hitCellCenterX + width, right);
                    top = Math.min(hitCellCenterY - height, top);
                    bottom = Math.max(hitCellCenterY + height, bottom);
                }

                /*
                 * Invalidate between the pattern's last cell and the previous
                 * location
                 */
                mTmpInvalidateRect.union(Math.round(left), Math.round(top),
                        Math.round(right), Math.round(bottom));
            }
        }
        mInProgressX = event.getX();
        mInProgressY = event.getY();

        /*
         * To save updates, we only invalidate if the user moved beyond a
         * certain amount.
         */
        if (invalidateNow) {
            mInvalidate.union(mTmpInvalidateRect);
            invalidate(mInvalidate);
            mInvalidate.set(mTmpInvalidateRect);
        }
    }

    private void sendAccessEvent(String resId) {
            setContentDescription(resId);
            sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED);
            setContentDescription(null);
    }

    private void handleActionUp(MotionEvent event) {

        if (!mPattern.isEmpty()) {
            mPatternInProgress = false;
            notifyPatternDetected();
            invalidate();
        }
        if (PROFILE_DRAWING) {
            if (mDrawingProfilingStarted) {
                Debug.stopMethodTracing();
                mDrawingProfilingStarted = false;
            }
        }
    }

    private void handleActionDown(MotionEvent event) {
        resetPattern();
        final float x = event.getX();
        final float y = event.getY();
        final Cell hitCell = detectAndAddHit(x, y);
        if (hitCell != null) {
            mPatternInProgress = true;
            mPatternDisplayMode = DisplayMode.Correct;
            notifyPatternStarted();
        } else {
            mPatternInProgress = false;
            notifyPatternCleared();
        }
        if (hitCell != null) {
            final float startX = getCenterXForColumn(hitCell.mColumn);
            final float startY = getCenterYForRow(hitCell.mRow);

            final float widthOffset = mSquareWidth / 2f;
            final float heightOffset = mSquareHeight / 2f;

            invalidate((int) (startX - widthOffset),
                    (int) (startY - heightOffset),
                    (int) (startX + widthOffset), (int) (startY + heightOffset));
        }
        mInProgressX = x;
        mInProgressY = y;
        if (PROFILE_DRAWING) {
            if (!mDrawingProfilingStarted) {
                Debug.startMethodTracing("LockPatternDrawing");
                mDrawingProfilingStarted = true;
            }
        }
    }

    private float getCenterXForColumn(int column) {
        return mPaddingLeft + column * mSquareWidth + mSquareWidth / 2f;
    }

    private float getCenterYForRow(int row) {
        return mPaddingTop + row * mSquareHeight + mSquareHeight / 2f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final ArrayList<Cell> pattern = mPattern;
        final int count = pattern.size();
        final boolean[][] drawLookup = mPatternDrawLookup;

        if (mPatternDisplayMode == DisplayMode.Animate) {
            final int oneCycle = (count + 1) * MILLIS_PER_CIRCLE_ANIMATING;
            final int spotInCycle = (int) (SystemClock.elapsedRealtime() - mAnimatingPeriodStart)
                    % oneCycle;
            final int numCircles = spotInCycle / MILLIS_PER_CIRCLE_ANIMATING;

            clearPatternDrawLookup();
            for (int i = 0; i < numCircles; i++) {
                final Cell cell = pattern.get(i);
                drawLookup[cell.getRow()][cell.getColumn()] = true;
            }

            final boolean needToUpdateInProgressPoint = numCircles > 0
                    && numCircles < count;

            if (needToUpdateInProgressPoint) {
                final float percentageOfNextCircle = ((float) (spotInCycle % MILLIS_PER_CIRCLE_ANIMATING))
                        / MILLIS_PER_CIRCLE_ANIMATING;

                final Cell currentCell = pattern.get(numCircles - 1);
                final float centerX = getCenterXForColumn(currentCell.mColumn);
                final float centerY = getCenterYForRow(currentCell.mRow);

                final Cell nextCell = pattern.get(numCircles);
                final float dx = percentageOfNextCircle
                        * (getCenterXForColumn(nextCell.mColumn) - centerX);
                final float dy = percentageOfNextCircle
                        * (getCenterYForRow(nextCell.mRow) - centerY);
                mInProgressX = centerX + dx;
                mInProgressY = centerY + dy;
            }
            invalidate();
        }

        final float squareWidth = mSquareWidth;
        final float squareHeight = mSquareHeight;

        float radius = (squareWidth * mDiameterFactor * 0.5f);
        mPathPaint.setStrokeWidth(radius);

        final Path currentPath = mCurrentPath;
        currentPath.rewind();

        /*
         * draw the circles
         */
        final int paddingTop = mPaddingTop;
        final int paddingLeft = mPaddingLeft;

        for (int i = 0; i < MATRIX_WIDTH; i++) {
            float topY = paddingTop + i * squareHeight;
            /*
             * float centerY = mPaddingTop + i * mSquareHeight + (mSquareHeight
             * / 2);
             */
            for (int j = 0; j < MATRIX_WIDTH; j++) {
                float leftX = paddingLeft + j * squareWidth;
                drawCircle(canvas, (int) leftX, (int) topY, drawLookup[i][j]);
            }
        }
        final boolean drawPath = (!mInStealthMode || mPatternDisplayMode == DisplayMode.Wrong);
        boolean oldFlag = (mPaint.getFlags() & Paint.FILTER_BITMAP_FLAG) != 0;
        mPaint.setFilterBitmap(true);
        if (drawPath) {
            for (int i = 0; i < count - 1; i++) {
                Cell cell = pattern.get(i);
                Cell next = pattern.get(i + 1);
                if (!drawLookup[next.mRow][next.mColumn]) {
                    break;
                }

                float leftX = paddingLeft + cell.mColumn * squareWidth;
                float topY = paddingTop + cell.mRow * squareHeight;

                drawArrow(canvas, leftX, topY, cell, next);
            }
        }

        if (drawPath) {
            boolean anyCircles = false;
            for (int i = 0; i < count; i++) {
                Cell cell = pattern.get(i);

                /*
                 * only draw the part of the pattern stored in the lookup table
                 * (this is only different in the case of animation).
                 */
                if (!drawLookup[cell.mRow][cell.mColumn]) {
                    break;
                }
                anyCircles = true;

                float centerX = getCenterXForColumn(cell.mColumn);
                float centerY = getCenterYForRow(cell.mRow);
                if (i == 0) {
                    currentPath.moveTo(centerX, centerY);
                } else {
                    currentPath.lineTo(centerX, centerY);
                }
            }

            /*
             * add last in progress section
             */
            if ((mPatternInProgress || mPatternDisplayMode == DisplayMode.Animate)
                    && anyCircles && count > 1) {
                currentPath.lineTo(mInProgressX, mInProgressY);
            }
            canvas.drawPath(currentPath, mPathPaint);
        }

        /*
         * restore default flag
         */
        mPaint.setFilterBitmap(oldFlag);
    }

    private void drawArrow(Canvas canvas, float leftX, float topY, Cell start,
                           Cell end) {
        boolean green = mPatternDisplayMode != DisplayMode.Wrong;

        final int endRow = end.mRow;
        final int startRow = start.mRow;
        final int endColumn = end.mColumn;
        final int startColumn = start.mColumn;

        /*
         * offsets for centering the bitmap in the cell
         */
        final int offsetX = ((int) mSquareWidth - mBitmapWidth) / 2;
        final int offsetY = ((int) mSquareHeight - mBitmapHeight) / 2;

        /*
         * compute transform to place arrow bitmaps at correct angle inside
         * circle. This assumes that the arrow image is drawn at 12:00 with it's
         * top edge coincident with the circle bitmap's top edge.
         */
        Bitmap arrow = green ? mBitmapArrowGreenUp : mBitmapArrowRedUp;
        final int cellWidth = mBitmapWidth;
        final int cellHeight = mBitmapHeight;

        /*
         * the up arrow bitmap is at 12:00, so find the rotation from x axis and
         * add 90 degrees.
         */
        final float theta = (float) Math.atan2((double) (endRow - startRow),
                (double) (endColumn - startColumn));
        final float angle = (float) Math.toDegrees(theta) + 90.0f;

        /*
         * compose matrix
         */
        float sx = Math.min(mSquareWidth / mBitmapWidth, 1.0f);
        float sy = Math.min(mSquareHeight / mBitmapHeight, 1.0f);
        /*
         * transform to cell position
         */
        mArrowMatrix.setTranslate(leftX + offsetX, topY + offsetY);
        mArrowMatrix.preTranslate(mBitmapWidth / 2, mBitmapHeight / 2);
        mArrowMatrix.preScale(sx, sy);
        mArrowMatrix.preTranslate(-mBitmapWidth / 2, -mBitmapHeight / 2);
        /*
         * rotate about cell center
         */
        mArrowMatrix.preRotate(angle, cellWidth / 2.0f, cellHeight / 2.0f);
        /*
         * translate to 12:00 pos
         */
        mArrowMatrix.preTranslate((cellWidth - arrow.getWidth()) / 2.0f, 0.0f);
        canvas.drawBitmap(arrow, mArrowMatrix, mPaint);
    }

    private void drawCircle(Canvas canvas, int leftX, int topY,
                            boolean partOfPattern) {
        Bitmap outerCircle;
        Bitmap innerCircle;

        if (!partOfPattern
                || (mInStealthMode && mPatternDisplayMode != DisplayMode.Wrong)) {
            /*
             * unselected circle
             */
            outerCircle = mBitmapCircleDefault;
            innerCircle = mBitmapBtnDefault;
        } else if (mPatternInProgress) {
            /*
             * user is in middle of drawing a pattern
             */
            outerCircle = mBitmapCircleGreen;
            innerCircle = mBitmapBtnTouched;
        } else if (mPatternDisplayMode == DisplayMode.Wrong) {
            /*
             * the pattern is wrong
             */
            outerCircle = mBitmapCircleRed;
            innerCircle = mBitmapBtnDefault;
        } else if (mPatternDisplayMode == DisplayMode.Correct
                || mPatternDisplayMode == DisplayMode.Animate) {
            /*
             * the pattern is correct
             */
            outerCircle = mBitmapCircleGreen;
            innerCircle = mBitmapBtnDefault;
        } else {
            throw new IllegalStateException("unknown display mode "
                    + mPatternDisplayMode);
        }

        final int width = mBitmapWidth;
        final int height = mBitmapHeight;

        final float squareWidth = mSquareWidth;
        final float squareHeight = mSquareHeight;

        int offsetX = (int) ((squareWidth - width) / 2f);
        int offsetY = (int) ((squareHeight - height) / 2f);

        /*
         * Allow circles to shrink if the view is too small to hold them.
         */
        float sx = Math.min(mSquareWidth / mBitmapWidth, 1.0f);
        float sy = Math.min(mSquareHeight / mBitmapHeight, 1.0f);

        mCircleMatrix.setTranslate(leftX + offsetX, topY + offsetY);
        mCircleMatrix.preTranslate(mBitmapWidth / 2, mBitmapHeight / 2);
        mCircleMatrix.preScale(sx, sy);
        mCircleMatrix.preTranslate(-mBitmapWidth / 2, -mBitmapHeight / 2);

        canvas.drawBitmap(outerCircle, mCircleMatrix, mPaint);
        canvas.drawBitmap(innerCircle, mCircleMatrix, mPaint);
    }


}
