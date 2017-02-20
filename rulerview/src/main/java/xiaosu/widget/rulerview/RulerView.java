package xiaosu.widget.rulerview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ScrollerCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;


public class RulerView extends View implements ValueAnimator.AnimatorUpdateListener {

    private static final String TAG = "RulerView";

    private int mWidth;
    private Paint mLinePaint;
    private GestureDetectorCompat mGestureDetectorCompat;
    private int mSpaceCount;
    private float mSpaceWidth;

    ILineCreator mLineCreator;
    private int mHeight;
    private float mDensity;
    private Paint mTextPaint;
    private float mGapBetweenLineAndText;
    private int mPointerColor;
    private float mScrollDistanceX;

    private OnValueChangedListener mOnValueChangedListener;
    private float mPointerWidth;
    private float mStartIndex;//开始显示的位置
    private float mWillConsumedDis;

    private int mLastFlingX;
    private ScrollerCompat mScroller;

    //滑动结束后是否停在最近的指针上
    private boolean mPauseOnNearestIndex;
    private float mPrePauseScrollDistanceX;

    public RulerView(Context context) {
        this(context, null);
    }

    public RulerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        parseAttrs(context, attrs);
        init(context);
    }

    private void parseAttrs(Context context, AttributeSet attrs) {
        mDensity = getResources().getDisplayMetrics().density;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RulerView);
        mSpaceCount = a.getInt(R.styleable.RulerView_spaceCount, 10);
        mSpaceWidth = a.getDimensionPixelSize(R.styleable.RulerView_spaceWidth,
                (int) (mDensity * 4));
        mGapBetweenLineAndText = a.getDimensionPixelSize(R.styleable.RulerView_gapBetweenLineAndText,
                (int) (mDensity * 6));
        mPointerColor = a.getColor(R.styleable.RulerView_pointerColor, Color.RED);
        mPointerWidth = a.getDimensionPixelSize(R.styleable.RulerView_pointerWidth,
                (int) (mDensity * 1));
        if (a.hasValue(R.styleable.RulerView_lineCreator)) {
            try {
                String lineCreatorClassPath = a.getString(R.styleable.RulerView_lineCreator);
                mLineCreator = (ILineCreator) Class.forName(lineCreatorClassPath).newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        float index = a.getFloat(R.styleable.RulerView_startIndex, 0);
        mPauseOnNearestIndex = a.getBoolean(R.styleable.RulerView_pauseOnNearestIndex, true);
        setIndex(index);

        mWillConsumedDis = mSpaceCount * mSpaceWidth;

        a.recycle();
    }

    private void correctIndex() {
        mStartIndex = mStartIndex < 0 ? 0 : mStartIndex;
        mStartIndex = mStartIndex > mSpaceCount ? mSpaceCount : mStartIndex;
    }

    private void init(Context context) {

        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setColor(0xFF333333);

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(0xFF333333);

        if (null == mLineCreator)
            mLineCreator = new DefaultLineCreator();

        mScroller = ScrollerCompat.create(getContext(), sQuinticInterpolator);
        mGestureDetectorCompat = new GestureDetectorCompat(context, new GestureListener());
    }

    public RulerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float centerX = mWidth / 2;
        float x = centerX - mScrollDistanceX;
        for (int i = 0; i <= mSpaceCount; i++) {
            Line line = mLineCreator.getLine(i, mHeight, mDensity);
            mLinePaint.setColor(line.color);
            mLinePaint.setStrokeWidth(line.width);

            canvas.save();
            canvas.translate(0, (mHeight - line.height) / 2);
            canvas.drawLine(x, 0, x, line.height, mLinePaint);

            if (!TextUtils.isEmpty(line.desc)) {

                mTextPaint.setTextSize(line.textSize);
                mTextPaint.setColor(line.textColor);
                Rect textBound = getTextBound(line.desc, mTextPaint);

                canvas.translate(x - textBound.width() / 2, textBound.height() + line.height + mGapBetweenLineAndText);
                canvas.drawText(line.desc, 0, 0, mTextPaint);
            }
            canvas.restore();

            x += mSpaceWidth;
        }
        //指针
        mLinePaint.setColor(mPointerColor);
        mLinePaint.setStrokeWidth(mPointerWidth);

        canvas.save();
        canvas.translate(centerX, 0);
        canvas.drawLine(0, 0, 0, mHeight, mLinePaint);
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean handle = mGestureDetectorCompat.onTouchEvent(event);
        final int action = MotionEventCompat.getActionMasked(event);
        if (!handle && action == MotionEvent.ACTION_UP && mPauseOnNearestIndex) {
            makeAndStartPauseOnNearestIndexValueAnimator();
        }
        return handle;
    }

    public void setOnValueChangedListener(OnValueChangedListener onValueChangedListener) {
        mOnValueChangedListener = onValueChangedListener;
    }

    private Rect getTextBound(String desc, Paint textPaint) {
        Rect rect = new Rect();
        textPaint.getTextBounds(desc, 0, desc.length(), rect);
        return rect;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
    }

    class DefaultLineCreator implements ILineCreator {

        @Override
        public Line getLine(int index, int parentHeight, float density) {
            if (index % 10 == 0) {
                return new Line(density * 2, density * 20, Color.RED, index + "", density * 12);
            } else if (index % 5 == 0) {
                return new Line(density * 1, density * 14, Color.GREEN);
            } else {
                return new Line(density * 1, density * 10, Color.BLACK);
            }
        }
    }

    class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            getParent().requestDisallowInterceptTouchEvent(true);
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            boolean canScrollHorizontal = canScrollHorizontal(distanceX);
            if (canScrollHorizontal) {
                scrollXBy(distanceX, true);
            }
            return canScrollHorizontal;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            mLastFlingX = 0;
            mScroller.fling(0, 0, (int) velocityX, 0,
                    Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
            ViewCompat.postInvalidateOnAnimation(RulerView.this);
            return true;
        }
    }

    public void scrollXBy(float distanceX, boolean scale) {
        mScrollDistanceX += scale ? distanceX * 0.6f : distanceX;
        mScrollDistanceX = mScrollDistanceX < 0 ? 0 : mScrollDistanceX;

        mScrollDistanceX = mScrollDistanceX > mWillConsumedDis ? mWillConsumedDis : mScrollDistanceX;

        if (null != mOnValueChangedListener) {
            mOnValueChangedListener.onValueChanged(mScrollDistanceX / mSpaceWidth);
        }
        ViewCompat.postInvalidateOnAnimation(RulerView.this);
    }

    private boolean canScrollHorizontal(float distanceX) {
        if (mScrollDistanceX == 0 && distanceX <= 0)
            return false;
        else if (mScrollDistanceX == mWillConsumedDis && distanceX >= 0)
            return false;
        return true;
    }

    public interface OnValueChangedListener {
        void onValueChanged(float value);
    }

    public void setIndex(float startIndex) {
        mStartIndex = startIndex;
        correctIndex();
        mScrollDistanceX = mStartIndex * mSpaceWidth;
        if (null != mOnValueChangedListener) {
            mOnValueChangedListener.onValueChanged(mScrollDistanceX / mSpaceWidth);
        }
        postInvalidate();
    }

    @Override
    public void computeScroll() {
        final ScrollerCompat scroller = mScroller;
        if (scroller.computeScrollOffset()) {
            final int x = scroller.getCurrX();
            final int dx = mLastFlingX - x;
            mLastFlingX = x;

            if (!canScrollHorizontal(dx))
                scroller.abortAnimation();

            if (!scroller.isFinished()) {
                scrollXBy(dx, false);
            }

            if (scroller.isFinished() && mPauseOnNearestIndex) {
                //滚动到最近的指针上
                makeAndStartPauseOnNearestIndexValueAnimator();
            }
        }
    }

    private void makeAndStartPauseOnNearestIndexValueAnimator() {
        float eIndex = mScrollDistanceX / mSpaceWidth;
        int rIndex = Math.round(eIndex);

        if (eIndex == rIndex) return;

        float extra = (rIndex - eIndex) * mSpaceWidth;

        mPrePauseScrollDistanceX = mScrollDistanceX;
        ValueAnimator animator = ValueAnimator.ofFloat(0, extra);
        animator.setDuration(300);
        animator.addUpdateListener(this);
        animator.start();
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        float extra = (float) animation.getAnimatedValue();
        float disX = mPrePauseScrollDistanceX + extra - mScrollDistanceX;
        scrollXBy(disX, false);
    }

    static final Interpolator sQuinticInterpolator = new Interpolator() {
        @Override
        public float getInterpolation(float t) {
            t -= 1.0f;
            return t * t * t * t * t + 1.0f;
        }
    };

}
