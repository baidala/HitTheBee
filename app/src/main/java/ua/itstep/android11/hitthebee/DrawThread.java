package ua.itstep.android11.hitthebee;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.SurfaceHolder;

/**
 * Created by Maksim Baydala on 16/01/17.
 */

public class DrawThread extends Thread {

    private boolean runFlag = false;
    private SurfaceHolder surfaceHolder;
    private Bitmap picture;


    public DrawThread(SurfaceHolder surfaceHolder, Resources resources) {
        this.surfaceHolder = surfaceHolder;
        picture = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher);

    }

    public void setRunning(boolean run) {
        runFlag = run;
    }


    @Override
    public void run() {
        Canvas canvas;

        while (runFlag) {
            canvas = null;
            try {
                // получаем объект Canvas и выполняем отрисовку
                canvas = surfaceHolder.lockCanvas(null);
                synchronized (surfaceHolder) {
                    canvas.drawColor(Color.RED);
                    canvas.drawBitmap(picture, null, null);
                }
            }
            finally {
                if (canvas != null) {
                    // отрисовка выполнена. выводим результат на экран
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
        }

    }
}
