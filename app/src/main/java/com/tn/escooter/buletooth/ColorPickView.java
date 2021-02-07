package com.tn.escooter.buletooth;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.tn.escooter.R;

public class ColorPickView extends View {
    private int bigCircle;
    private Bitmap bitmapBack;
    private Point centerPoint;
    private OnColorChangedListener listener;
    private Paint mCenterPaint;
    private Context mContext;
    private Paint mPaint;
    private Point mRockPosition;
    private int rudeRadius;
    private Bitmap rudebitmapBack;

    public interface OnColorChangedListener {
        void onColorChange(int i);
    }

    public ColorPickView(Context context) {
        super(context);
    }

    public ColorPickView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mContext = context;
        init(attributeSet);
    }

    public ColorPickView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mContext = context;
        init(attributeSet);
    }

    public void setOnColorChangedListener(OnColorChangedListener onColorChangedListener) {
        this.listener = onColorChangedListener;
    }
    public static final int[] color_picker = {R.attr.center_radius, R.attr.circle_radius};

    private void init(AttributeSet attributeSet) {
        TypedArray obtainStyledAttributes = this.mContext.obtainStyledAttributes(attributeSet, color_picker);
        this.bigCircle = obtainStyledAttributes.getDimensionPixelOffset((int)1, 320);
        this.rudeRadius = obtainStyledAttributes.getDimensionPixelOffset((int)0, 26);
        obtainStyledAttributes.recycle();
        this.bitmapBack = BitmapFactory.decodeResource(getResources(), R.drawable.chromatic_circle);
        Bitmap bitmap = this.bitmapBack;
        int i = this.bigCircle;
        this.bitmapBack = Bitmap.createScaledBitmap(bitmap, i * 2, i * 2, false);
        int i2 = this.bigCircle;
        this.centerPoint = new Point(i2, i2);
        this.mRockPosition = new Point(this.centerPoint);
        this.mPaint = new Paint();
        this.mPaint.setAntiAlias(true);
        this.mCenterPaint = new Paint();
        this.mCenterPaint.setColor(Color.parseColor("#ffffff"));
        this.mCenterPaint.setStyle(Paint.Style.STROKE);
        this.mCenterPaint.setStrokeWidth(4.0f);
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        int action = motionEvent.getAction();
        if (action != 0) {
            if (action != 1) {
                if (action != 2) {
                    if (action != 5) {
                    }
                }
            }
            invalidate();
            return true;
        }
        motionEvent.getX();
        motionEvent.getY();
        if (getLength(motionEvent.getX(), motionEvent.getY(), (float) this.centerPoint.x, (float) this.centerPoint.y) <= this.bigCircle - this.rudeRadius) {
            this.mRockPosition.set((int) motionEvent.getX(), (int) motionEvent.getY());
        } else {
            this.mRockPosition = getBorderPoint(this.centerPoint, new Point((int) motionEvent.getX(), (int) motionEvent.getY()), this.bigCircle - this.rudeRadius);
        }
        this.listener.onColorChange(this.bitmapBack.getPixel(this.mRockPosition.x, this.mRockPosition.y));
        invalidate();
        return true;
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        new Rect(getLeft(), getTop(), getRight(), getBottom());
        canvas.drawBitmap(this.bitmapBack, 0.0f, 0.0f, this.mPaint);
        canvas.drawCircle((float) this.mRockPosition.x, (float) this.mRockPosition.y, (float) this.rudeRadius, this.mCenterPaint);
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int i, int i2) {
        int i3 = this.bigCircle;
        setMeasuredDimension(i3 * 2, i3 * 2);
    }

    private static int getLength(float f, float f2, float f3, float f4) {
        return (int) Math.sqrt(Math.pow((double) (f - f3), 2.0d) + Math.pow((double) (f2 - f4), 2.0d));
    }

    private static Point getBorderPoint(Point point, Point point2, int i) {
        float radian = getRadian(point, point2);
        int i2 = point.x;
        double d = (double) i;
        double d2 = (double) radian;
        double cos = Math.cos(d2);
        Double.isNaN(d);
        int i3 = i2 + ((int) (cos * d));
        int i4 = point.x;
        double sin = Math.sin(d2);
        Double.isNaN(d);
        return new Point(i3, i4 + ((int) (d * sin)));
    }

    private static float getRadian(Point point, Point point2) {
        float f = (float) (point2.x - point.x);
        float f2 = (float) (point2.y - point.y);
        return ((float) Math.acos((double) (f / ((float) Math.sqrt((double) ((f * f) + (f2 * f2))))))) * ((float) (point2.y < point.y ? -1 : 1));
    }
}
