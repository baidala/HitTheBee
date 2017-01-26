package ua.itstep.android11.hitthebee;


import android.content.Context;
import android.graphics.PointF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;



public class BeeSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private DrawThread drawThread;
    private Context context;
    private SurfaceHolder holder;
    private GestureDetector gestureDetector;

    private static final int GAME_OVER = 5;

    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message m) {
            if ( m.getData().getInt("STATE") == GAME_OVER){
                // завершаем работу потока
                drawThread.setFlagRunning(false);

            }

        }
    };

    private GestureDetector.OnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {

        public boolean onDown(MotionEvent event) {
            boolean flag = false;
            if (drawThread.touchItem((int)event.getX(), (int)event.getY())) {
                flag = true;


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

        if(Prefs.DEBUG) Log.d(Prefs.LOG_TAG, getClass().getSimpleName() +" onTouchEvent "+ action + " at x=" + (int)event.getX() +
               ", y=" + (int)event.getY());

        return super.onTouchEvent(event) || this.gestureDetector.onTouchEvent(event);

    }

    private void resetTouchFeedback() {
        drawThread.releaseTouch();

    }


    public void onPause() {
        drawThread.pause();
    }

    public void onResume() {

    }


} //BeeSurfaceView
