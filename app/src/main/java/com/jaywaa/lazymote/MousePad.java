package com.jaywaa.lazymote;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;
/**
 * Created by James on 2/28/2015.
 */
public class MousePad extends ViewGroup
{
    private int mTouchSlop;
    private boolean mIsScrolling=false;

    private float posX,posY;

    private Paint paint;
    private boolean fingerDown=false;


    public MousePad(Context context) {
        super(context);
        setWillNotDraw(false);
    }

    public MousePad(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        setWillNotDraw(false);
    }

    public MousePad(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setWillNotDraw(false);
    }
    @Override
    public boolean shouldDelayChildPressedState() {
        return false;
    }
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        Log.v("MOUSEPAD", "onLayout");
        paint = new Paint();
        paint.setColor(Color.BLACK);

        paint.setStrokeWidth(2);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        /*
         * This method JUST determines whether we want to intercept the motion.
         * If we return true, onTouchEvent will be called and we do the actual
         * scrolling there.
         */


        return false;
    }
    public void setTouchLocation(float x, float y)
    {
        fingerDown = true;
        posX = x;
        posY = y;
        invalidate();
    }
    public void setTouchUp()
    {
        fingerDown = false;
    }
    @Override
    public void dispatchDraw(Canvas c)
    {
        super.dispatchDraw(c);

        if (fingerDown)
            c.drawCircle(posX,posY,50, paint);
    }
    /*@Override
    public void onDraw(Canvas c)
    {
        super.onDraw(c);
        Log.v("MOUSEPAD", "draw");
        c.drawCircle(x,y,50, paint);
    }*/
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        // Here we actually handle the touch event (e.g. if the action is ACTION_MOVE,
        // scroll this container).
        // This method will only be called if the touch event was intercepted in
        // onInterceptTouchEvent
       /* if (ev.getAction() == MotionEvent.ACTION_MOVE || ev.getAction() == MotionEvent.ACTION_DOWN)
        {
            fingerDown = true;
            Log.v("MOUSEPAD", "touchEvent");
            x = ev.getX();
            y = ev.getY();
            invalidate();
        }
        else if (ev.getAction() == MotionEvent.ACTION_UP)
        {
            Log.v("MOUSEPAD", "action up");
            fingerDown=false;
            invalidate();
        }*/
        Log.v("MOUSEPAD", "Action: "+ev.getAction());
        return false;
    }
}
