package com.tools.recoderlib;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.tools.recoderlib.model.MediaObject;


public class CircleProgressBar extends View {

    private int mProgressStartColor;
    private int mBgCirColor;
    private float mProgressWidth;
    private int mStartAngle;
    private MediaObject mMediaObject;
    /** 最长时长 */
    private float mMaxDuration, mVLineWidth;
    private int mRecordTimeMin=1500;
    private static final int CIRCLE = 360;
    private int mMeasureHeight;
    private int mMeasureWidth;

    private Paint mBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
    private Paint mProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);

    private RectF pRectF;
    private RectF pCenterRectF;

    public CircleProgressBar(Context context) {
        this(context, null);
    }

    public CircleProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttr(context, attrs);
        //初始化画笔
        initPaint();
    }

    private void initAttr(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.UpdateProgressBar);

        mProgressStartColor = ta.getColor(R.styleable.UpdateProgressBar_cover_start_color, Color.YELLOW);
        mBgCirColor = ta.getColor(R.styleable.UpdateProgressBar_cover_bg_cir_color, Color.LTGRAY);

        // 公共属性
        mProgressWidth = ta.getDimension(R.styleable.UpdateProgressBar_progress_width, 8f);
        mStartAngle = ta.getColor(R.styleable.UpdateProgressBar_start_angle, 90);

        ta.recycle();
    }

    /**
     * 初始化画笔
     */
    private void initPaint() {

        //浅色灰色背景画笔
        mBgPaint.setStyle(Paint.Style.STROKE);
        mBgPaint.setStrokeCap(Paint.Cap.ROUND);
        mBgPaint.setStrokeWidth(mProgressWidth);


        //彩色进度条画笔
        mProgressPaint.setStyle(Paint.Style.STROKE);
        mProgressPaint.setStrokeCap(Paint.Cap.SQUARE);
        mProgressPaint.setAntiAlias(true);
        mProgressPaint.setStrokeWidth(mProgressWidth);

    }




    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mMeasureWidth = getMeasuredWidth();
        mMeasureHeight = getMeasuredHeight();
        if (pRectF == null) {
            float halfProgressWidth = mProgressWidth / 2;
            pRectF = new RectF(halfProgressWidth + getPaddingLeft(),
                    halfProgressWidth + getPaddingTop(),
                    mMeasureWidth - halfProgressWidth - getPaddingRight(),
                    mMeasureHeight - halfProgressWidth - getPaddingBottom());
        }

        if (pCenterRectF == null) {
            float halfProgressWidth = mProgressWidth / 2;
            pCenterRectF = new RectF(halfProgressWidth + getPaddingLeft() + 15,
                    halfProgressWidth + getPaddingTop() + 15,
                    mMeasureWidth - halfProgressWidth - getPaddingRight() - 15,
                    mMeasureHeight - halfProgressWidth - getPaddingBottom() - 15);
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        drawProgress(canvas);
        drawBgCirProgress(canvas);
    }

    // 画背景圆
    private void drawBgCirProgress(Canvas canvas) {
        canvas.save();
        mBgPaint.setColor(mBgCirColor);
        canvas.drawArc(pRectF, 0, CIRCLE, false, mBgPaint);
        canvas.restore();
    }
    private void drawProgress(Canvas canvas) {
        if (mMediaObject == null) {
            return;
        }
        canvas.save();
        canvas.rotate(-90, mMeasureWidth / 2, mMeasureHeight / 2);
        int circle = (int) (mMediaObject.getDuration() /mMaxDuration * CIRCLE);
        //绘制彩色进度条
        for (int i = 0; i <=circle; i++) {
            mProgressPaint.setColor(mProgressStartColor);
            canvas.drawArc(pRectF,
                     i ,
                    1,
                    false,
                    mProgressPaint);
        }
        canvas.restore();

    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    public void setData(MediaObject mMediaObject) {
        this.mMediaObject = mMediaObject;
    }

    public void setMinTime(int recordTimeMin) {
        this.mRecordTimeMin=recordTimeMin;
    }

    public void setMaxDuration(int duration) {
        this.mMaxDuration = duration;
    }
}
