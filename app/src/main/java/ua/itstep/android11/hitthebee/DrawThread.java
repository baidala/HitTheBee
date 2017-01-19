package ua.itstep.android11.hitthebee;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;

/**
 * Created by Maksim Baydala on 16/01/17.
 */

public class DrawThread extends Thread {

    private boolean runFlag = false;
    private SurfaceHolder surfaceHolder;

    private Bitmap workerIcon;
    private Bitmap workerIconHit;
    private Bitmap queenIcon;
    private Bitmap queenIconHit;
    private Bitmap droneIcon;
    private Bitmap droneIconHit;


    private Drawable beeImage;
    private Drawable workerImage;
    private Drawable droneImage;
    private Drawable queenImage;

    //private ImageButton beeImageButton;
    private Paint paint;
    private Paint rectPaint;
    private Handler handler;
    private Context context;
    private final Object runLock = new Object();
    private RectF scratchRectF;
    private CharSequence message;

    /** The state of the game. One of READY, RUNNING, PAUSE, LOSE, or WIN */
    private int gameMode;

    private int winsInARow;

    public static final int STATE_LOSE = 1;
    public static final int STATE_PAUSE = 2;
    public static final int STATE_READY = 3;
    public static final int STATE_RUNNING = 4;
    public static final int STATE_WIN = 5;

    /** X of the bee sprite center. */
    private float beeSpriteCenterX;

    /** Y of the bee sprite center. */
    private float beeSpriteCenterY;

    private int canvasHeight = 1;
    private int canvasWidth = 1;

    private int beeSpriteXLeft;
    private int beeSpriteYTop;

    private int beeSpriteWidth;
    private int beeSpriteHeight;


    private boolean horizontal;
    private boolean vertical;

    /** Used to figure out elapsed time between frames */
    private long nextRefreshTime;
    private int REFRESH_STEP_MILLIS = 300;









    DrawThread(SurfaceHolder surfaceHolder,  Context context, Handler handler) {
        if(Prefs.DEBUG) Log.d(Prefs.LOG_TAG, getClass().getSimpleName() +" Constructor" );

        this.surfaceHolder = surfaceHolder;
        this.context = context;
        this.handler = handler;

        Resources res = context.getResources();
        message = "";
        winsInARow = 0;


        //create all objects: 1 queen, 5 workers, 8 drones

        //create array of the objects



        //cashe drawables
        workerImage = res.getDrawable(R.drawable.bee_worker_selector);
        workerImage.setState( new int[] { -android.R.attr.state_pressed } );

        droneImage = res.getDrawable(R.drawable.bee_drone_selector);
        droneImage.setState( new int[] { -android.R.attr.state_pressed } );

        queenImage = res.getDrawable(R.drawable.bee_queen_selector);
        queenImage.setState( new int[] { -android.R.attr.state_pressed } );

        //cashe icons
        droneIcon = BitmapFactory.decodeResource(res, R.drawable.bee_drone_icon);
        droneIconHit = BitmapFactory.decodeResource(res, R.drawable.bee_drone_hit_icon);

        workerIcon = BitmapFactory.decodeResource(res, R.drawable.bee_worker_icon);
        workerIconHit = BitmapFactory.decodeResource(res, R.drawable.bee_worker_hit_icon);

        queenIcon = BitmapFactory.decodeResource(res, R.drawable.bee_queen_icon);
        queenIconHit = BitmapFactory.decodeResource(res, R.drawable.bee_queen_hit_icon);


        //set default image
        beeImage = workerImage;

        beeSpriteWidth = beeImage.getIntrinsicWidth();
        beeSpriteHeight = beeImage.getIntrinsicHeight();


        paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        rectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rectPaint.setColor(Color.WHITE);




        /*
        beeImageButton = new ImageButton(this.context);


        DisplayMetrics metrics = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(metrics);

        layoutParams = beeImageButton.getLayoutParams();

        layoutParams.width = metrics.widthPixels / 3;
        layoutParams.height = metrics.heightPixels / 2;
        */

        /*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            beeImageButton.setBackground(beeImage);
        } else {
            beeImageButton.setImageDrawable(beeImage);
        }


        //beeImageButton.setClipBounds(100.0f, 100.0f, 200.0f,200.0f);

        beeSpriteWidth = beeImageButton.getWidth();
        beeSpriteHeight = beeImageButton.getHeight();
        */

        scratchRectF = new RectF(0, 0, 50, 90); //(100.0f, 100.0f, 200.0f,200.0f); //left, topI, right, bottom




        if(Prefs.DEBUG) Log.d(Prefs.LOG_TAG, getClass().getSimpleName() +" beeSpriteWidth:" + beeSpriteWidth);
        if(Prefs.DEBUG) Log.d(Prefs.LOG_TAG, getClass().getSimpleName() +" beeSpriteHeight:" + beeSpriteHeight);

        //initial sprite location
        beeSpriteCenterX = beeSpriteWidth;
        beeSpriteCenterY = beeSpriteHeight * 2;

        nextRefreshTime = System.currentTimeMillis();





    }




    @Override
    public void run() {
        if(Prefs.DEBUG) Log.d(Prefs.LOG_TAG, getClass().getSimpleName() +" run" );

        Canvas canvas = null;

        while (runFlag) {
                // получаем объект Canvas и выполняем отрисовку
                canvas = surfaceHolder.lockCanvas();

                if (canvas != null) {
                    synchronized (surfaceHolder) {
                        if (gameMode == STATE_RUNNING) updatePhysics();

                        synchronized (runLock) {
                            if (runFlag) doDraw(canvas);
                        }

                    }
                    // отрисовка выполнена. выводим результат на экран
                    surfaceHolder.unlockCanvasAndPost(canvas);

                } else {
                    if(Prefs.DEBUG) Log.d(Prefs.LOG_TAG, getClass().getSimpleName() +" canvas = NULL" );
                }
        }

    }


    /**
     * Starts the game, setting parameters
     */
    public void doStart() {
        //if(Prefs.DEBUG) Log.d(Prefs.LOG_TAG, getClass().getSimpleName() +" doStart" );
        synchronized (surfaceHolder) {

            //random choose the bee/object from the array
            horizontal = Math.round(Math.random()) == 0;
            vertical = Math.round(Math.random()) == 0;

            //init  toolbar/icons



            //scratchRectF.set(330.0f, 100.0f, 400.0f, 200.0f);
            //beeImage.setState( new int[] { -android.R.attr.state_pressed } );

            //imageButton.setImageResource(R.drawable.bee_worker_selector);
            //imageButton.setBackground(beeImage);


            // pick a convenient initial location for the sprite
            beeSpriteCenterX = canvasWidth / 2;
            beeSpriteCenterY = canvasHeight / 2;

            beeSpriteXLeft = (int) beeSpriteCenterX - beeSpriteWidth / 2;
            beeSpriteYTop = (int) beeSpriteCenterY + beeSpriteHeight / 2;


            beeImage.setBounds(beeSpriteXLeft, beeSpriteYTop, beeSpriteXLeft + beeSpriteWidth, beeSpriteYTop + beeSpriteHeight);


            nextRefreshTime = System.currentTimeMillis() + REFRESH_STEP_MILLIS;


            setGameState(STATE_RUNNING);
        }
    }

    public void setFlagRunning(boolean run) {
        if(Prefs.DEBUG) Log.d(Prefs.LOG_TAG, getClass().getSimpleName() +" setRunning" );
        synchronized (runLock) {
            runFlag = run;
        }
    }


    public void setGameState(int mode) {
        synchronized (surfaceHolder) {
            setGameState(mode, null);
        }
    }

    public void setGameState(int mode, CharSequence msg) {
        //if(Prefs.DEBUG) Log.d(Prefs.LOG_TAG, getClass().getSimpleName() +" setState" );
        synchronized (surfaceHolder) {
            gameMode = mode;

            if (gameMode == STATE_RUNNING) {

            } else {
                Resources res = context.getResources();
                StringBuilder str = new StringBuilder();

                if (msg != null) {
                    str.append( msg );
                    str.append( "\n" );
                }

                if (gameMode == STATE_READY)
                    str.append( res.getText(R.string.mode_ready) ) ;
                else if (gameMode == STATE_PAUSE)
                    str.append( res.getText(R.string.mode_pause) );
                else if (gameMode == STATE_LOSE)
                    str.append( res.getText(R.string.mode_lose) );
                else if (gameMode == STATE_WIN) {
                    str.append(res.getString(R.string.mode_win_prefix));
                    str.append(winsInARow);
                    str.append(" ");
                    str.append(res.getString(R.string.mode_win_suffix));
                }

                str.trimToSize();

                if (gameMode == STATE_LOSE) winsInARow = 0;

                message = str.toString();

            }
        }
    }

    public boolean touchItem(float x, float y) { //isHit
        if(Prefs.DEBUG) Log.d(Prefs.LOG_TAG, getClass().getSimpleName() +" touchItem" );

        boolean flag = false;

        Rect rect = beeImage.getBounds();
        if ( rect.contains((int)x,(int)y) ) {
            beeImage.setState(new int[]{android.R.attr.state_pressed});
            flag = true;

            if(Prefs.DEBUG) Log.d(Prefs.LOG_TAG, getClass().getSimpleName() +" touchItem HIT" );

        }

        return flag;
    }


    public boolean releaseTouch() {
        if(Prefs.DEBUG) Log.d(Prefs.LOG_TAG, getClass().getSimpleName() +" releaseTouch" );

        beeImage.setState(new int[]{-android.R.attr.state_pressed});
        /*
        if ( (beeImage.getState())[0] == android.R.attr.state_pressed ){
            beeImage.setState(new int[]{-android.R.attr.state_pressed});

        }
        */

        return true;
    }

    /**
     * Restores game state from the indicated Bundle.
     */
    public synchronized void restoreState(Bundle savedState) {
        synchronized (surfaceHolder) {
            setGameState(STATE_PAUSE);



        }
    }

    /* Callback invoked when the surface dimensions change. */
    public void setSurfaceSize(int width, int height) {

        synchronized (surfaceHolder) {
            canvasWidth = width;
            canvasHeight = height;
        }
    }




    private void doDraw(Canvas canvas) {
        //if(Prefs.DEBUG) Log.d(Prefs.LOG_TAG, getClass().getSimpleName() +" doDraw" );



        canvas.drawColor(Color.RED);

        //Rect rectImg = new Rect(xLeft, yTop, xLeft + beeSpriteWidth, yTop + beeSpriteHeight);
        //beeImageButton.bringToFront();
        //beeImageButton.buildLayer();

        //imageButton.setClipBounds(rectImg);
        //beeImageButton.draw(canvas);

        //beeImage.setBounds(xLeft, yTop, xLeft + beeSpriteWidth, yTop + beeSpriteHeight);

        beeImage.draw(canvas);


        //canvas.drawBitmap(beeImage, left, top, paint);
        canvas.drawRect(scratchRectF, rectPaint);
        canvas.drawText(message, 0, message.length(), 300.0f, 300.0f, rectPaint);

        //draw toolbar/icons



    }


    /**
     * Does not invalidate(). Called at the start of draw().
     * Detects the end-of-game and sets the UI to the next state.
     */
    private void updatePhysics() {
        //if(Prefs.DEBUG) Log.d(Prefs.LOG_TAG, getClass().getSimpleName() +" updatePhysics" );
        long now = System.currentTimeMillis();

        if ( nextRefreshTime > now ) return;

        //move direction
        horizontal = Math.round(Math.random()) == 0;
        vertical = Math.round(Math.random()) == 0;

        //choose random x y
        beeSpriteCenterX += ( horizontal ? 20 : -20 );
        if( beeSpriteCenterX >= canvasWidth ) {
            beeSpriteCenterX = canvasWidth - beeSpriteWidth / 2 ;
            horizontal = false;
        } else if ( beeSpriteCenterX <= 0 ) {
            beeSpriteCenterX = beeSpriteWidth / 2 ;
            horizontal = true;
        }

        beeSpriteCenterY += ( vertical ? 30 : -30 );
        if( beeSpriteCenterY >= canvasHeight ) {
            beeSpriteCenterY = canvasHeight - beeSpriteHeight / 2 ;
            vertical = false;
        } else if ( beeSpriteCenterY <= 0 ) {
            beeSpriteCenterY = beeSpriteHeight / 2 ;
            vertical = true;
        }


        beeSpriteYTop = (int) beeSpriteCenterY + beeSpriteHeight / 2;
        beeSpriteXLeft = (int) beeSpriteCenterX - beeSpriteWidth / 2;

        beeImage.setBounds(beeSpriteXLeft, beeSpriteYTop, beeSpriteXLeft + beeSpriteWidth, beeSpriteYTop + beeSpriteHeight);

        this.releaseTouch();

        nextRefreshTime = now + REFRESH_STEP_MILLIS;;

        int result = STATE_WIN;


        //doStart();
    }









}//DrawThread
