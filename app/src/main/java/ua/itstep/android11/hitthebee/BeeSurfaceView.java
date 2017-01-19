package ua.itstep.android11.hitthebee;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;


public class BeeSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private DrawThread drawThread;
    private Context context;
    private SurfaceHolder holder;
    private GestureDetector gestureDetector;

    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message m) {

        }
    };

    private GestureDetector.OnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {

        public boolean onDown(MotionEvent event) {
            boolean flag = false;
            if (drawThread.touchItem(event.getX(), event.getY())) {
                //invalidate();
                flag = true;
                //resetTouchFeedback();

            }


            PointF current = new PointF(event.getX(), event.getY());
            String action = "";

            if(Prefs.DEBUG) Log.d(Prefs.LOG_TAG, getClass().getSimpleName() +" OnGestureListener onDown "+ action + " at x=" + current.x +
                    ", y=" + current.y);

            return flag;
        }


        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            resetTouchFeedback();
            return true;
        }

        public boolean onSingleTapUp(MotionEvent e) {
            resetTouchFeedback();
            return super.onSingleTapUp(e);
        }

    };


    public BeeSurfaceView(Context cntx,  AttributeSet attrs) {
        super(cntx, attrs);
        initSurface(cntx);

        if(Prefs.DEBUG) Log.d(Prefs.LOG_TAG, getClass().getSimpleName() +" Constructor2" );
    }


    public BeeSurfaceView(Context cntx, AttributeSet attrs, int defStyle) {
        super(cntx, attrs, defStyle);
        initSurface(cntx);

        if(Prefs.DEBUG) Log.d(Prefs.LOG_TAG, getClass().getSimpleName() +" Constructor3" );
    }

    private void initSurface(Context cntx) {
        if (!isInEditMode()) {
            if(Prefs.DEBUG) Log.d(Prefs.LOG_TAG, getClass().getSimpleName() +" initSurface" );
            this.context = cntx;
            this.holder = getHolder();
            holder.addCallback(this);

            drawThread = new DrawThread(holder, context, handler);
            gestureDetector = new GestureDetector(getContext(), gestureListener);

            setFocusable(true);
        }
    }

    public DrawThread getThread() {
        return drawThread;
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if(Prefs.DEBUG) Log.d(Prefs.LOG_TAG, getClass().getSimpleName() +" surfaceCreated" );

        if (drawThread != null) {
            drawThread.setSurfaceSize(this.getRight(), this.getBottom());
            //drawThread.setDaemon(true);
            //drawThread.setState(DrawThread.STATE_READY);
            drawThread.setFlagRunning(true);
            drawThread.doStart();
            drawThread.start();
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if(Prefs.DEBUG) Log.d(Prefs.LOG_TAG, getClass().getSimpleName() +" surfaceChanged" );
        drawThread.setSurfaceSize(width, height);

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if(Prefs.DEBUG) Log.d(Prefs.LOG_TAG, getClass().getSimpleName() +" surfaceDestroyed" );

        // завершаем работу потока
        drawThread.setFlagRunning(false);

        if (drawThread != null) {
            if(Prefs.DEBUG) Log.d(Prefs.LOG_TAG, getClass().getSimpleName() +" surfaceDestroyed  drawThread != null" );
            Thread dummy = drawThread;
            drawThread = null;
            dummy.interrupt();
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int action = event.getAction();

        if (action == MotionEvent.ACTION_CANCEL ) {
            resetTouchFeedback();
        }

        /*
        if (action == MotionEvent.ACTION_UP) {
            resetTouchFeedback();
        }
        */



        if(Prefs.DEBUG) Log.d(Prefs.LOG_TAG, getClass().getSimpleName() +" onTouchEvent "+ action + " at x=" + event.getX() +
               ", y=" + event.getY());

        return super.onTouchEvent(event) || this.gestureDetector.onTouchEvent(event);

        /*
        PointF current = new PointF(event.getX(), event.getY());
        String action = "";


        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                action = "ACTION_DOWN";
                break;
            case MotionEvent.ACTION_MOVE:
                action = "ACTION_MOVE";
                break;
            case MotionEvent.ACTION_UP:
                action = "ACTION_UP";
                break;
            case MotionEvent.ACTION_CANCEL:
                action = "ACTION_CANCEL";
                break;
        }
        if(Prefs.DEBUG) Log.d(Prefs.LOG_TAG, getClass().getSimpleName() +" onTouchEvent "+ action + " at x=" + current.x +
               ", y=" + current.y);

        return true;
        */
     }

    private void resetTouchFeedback() {

        if(drawThread.releaseTouch()) {
            //invalid()
        }

    }


} //BeeSurfaceView
