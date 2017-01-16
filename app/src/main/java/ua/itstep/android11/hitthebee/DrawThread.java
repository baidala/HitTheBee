package ua.itstep.android11.hitthebee;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;
import android.view.SurfaceHolder;

/**
 * Created by Maksim Baydala on 16/01/17.
 */

public class DrawThread extends Thread {

    private boolean runFlag = false;
    private SurfaceHolder surfaceHolder;
    private Bitmap picture;


    public DrawThread(SurfaceHolder surfaceHolder, Resources resources) {
        if(Prefs.DEBUG) Log.d(Prefs.LOG_TAG, getClass().getSimpleName() +" Constructor" );

        this.surfaceHolder = surfaceHolder;
        picture = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher);

    }

    void setRunning(boolean run) {
        if(Prefs.DEBUG) Log.d(Prefs.LOG_TAG, getClass().getSimpleName() +" setRunning" );
        runFlag = run;
    }


    @Override
    public void run() {
        if(Prefs.DEBUG) Log.d(Prefs.LOG_TAG, getClass().getSimpleName() +" run" );

        Canvas canvas;
        float left = 3.0f;
        float top = 3.0f;

        while (runFlag) {
                // получаем объект Canvas и выполняем отрисовку
                canvas = surfaceHolder.lockCanvas();

                if (canvas != null) {
                    synchronized (surfaceHolder) {
                        canvas.drawColor(Color.RED);
                        canvas.drawBitmap(picture, left, top, null);
                    }

                    // отрисовка выполнена. выводим результат на экран
                    surfaceHolder.unlockCanvasAndPost(canvas);

                } else {
                    if(Prefs.DEBUG) Log.d(Prefs.LOG_TAG, getClass().getSimpleName() +" canvas = NULL" );
                }



        }

    }
}
