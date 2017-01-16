package ua.itstep.android11.hitthebee;


import android.content.Context;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;



public class BeeSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private DrawThread drawThread;

    public BeeSurfaceView(Context context) {
        super(context);
        getHolder().addCallback(this);

        if(Prefs.DEBUG) Log.d(Prefs.LOG_TAG, getClass().getSimpleName() +" Constructor" );
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if(Prefs.DEBUG) Log.d(Prefs.LOG_TAG, getClass().getSimpleName() +" surfaceCreated" );

        drawThread = new DrawThread(holder, getResources());
        drawThread.setDaemon(true);
        drawThread.setRunning(true);
        drawThread.start();

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if(Prefs.DEBUG) Log.d(Prefs.LOG_TAG, getClass().getSimpleName() +" surfaceChanged" );
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if(Prefs.DEBUG) Log.d(Prefs.LOG_TAG, getClass().getSimpleName() +" surfaceDestroyed" );

        boolean retry = true;

        // завершаем работу потока
        drawThread.setRunning(false);


        if (drawThread != null) {
            if(Prefs.DEBUG) Log.d(Prefs.LOG_TAG, getClass().getSimpleName() +" surfaceDestroyed  drawThread != null" );
            Thread dummy = drawThread;
            drawThread = null;
            dummy.interrupt();
        }

        /*
        while (retry) {
            try {
                drawThread.join();
                retry = false;
            } catch (InterruptedException e) {
                // если не получилось, то будем пытаться еще и еще
            }
        }
        */

    }



}
