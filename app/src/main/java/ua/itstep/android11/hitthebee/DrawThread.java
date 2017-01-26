package ua.itstep.android11.hitthebee;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.Toast;

import java.util.Random;

/**
 * Created by Maksim Baydala on 16/01/17.
 */

public class DrawThread extends Thread {

    private boolean runFlag = false;
    private SurfaceHolder surfaceHolder;

    private Handler handler;
    private Context context;
    private final Object runLock = new Object();

    // The state of the game. One of READY, RUNNING, PAUSE, LOSE, or WIN
    private int gameMode = 0;

    //game level
    private int round = 0;

    private static final int STATE_LOSE = 1;
    private static final int STATE_PAUSE = 2;
    private static final int STATE_READY = 3;
    private static final int STATE_RUNNING = 4;
    private static final int STATE_WIN = 5;

    /** X of the bee sprite center. */
    private int beeSpriteCenterX = 0;

    /** Y of the bee sprite center. */
    private int beeSpriteCenterY = 0;

    private int canvasHeight = 1;
    private int canvasWidth = 1;

    private int boardLeft = 1;
    private int boardTop = 1;
    private int boardHeight = 1;
    private int boardWidth = 1;

    private int beeSpriteWidth = 1;
    private int beeSpriteHeight = 1;

    private int beeIconeWidth = 1;
    private int beeIconeHeight = 1;

    private static final int PADDING = 7;

    //direction off next move
    private boolean horizontal= true;
    private boolean vertical = true;

    /** Used to figure out elapsed time between frames */
    private long nextRefreshTime = 0;
    private static final int REFRESH_STEP_MILLIS = 300;

    private int arrayIndex = 0;

    private Bee[] beeArray;
    private static final int ARRAY_SIZE = 14;



    DrawThread(SurfaceHolder surfaceHolder,  Context context, Handler handler) {
        if(Prefs.DEBUG) Log.d(Prefs.LOG_TAG, getClass().getSimpleName() +" Constructor" );

        this.surfaceHolder = surfaceHolder;
        this.context = context;
        this.handler = handler;

        Resources res = context.getResources();
        arrayIndex = 0;
        round = 0;


        //cashe drawables
        Drawable workerImage = res.getDrawable(R.drawable.bee_worker_selector);
        workerImage.setState( new int[] { -android.R.attr.state_pressed } );

        Drawable droneImage = res.getDrawable(R.drawable.bee_drone_selector);
        droneImage.setState( new int[] { -android.R.attr.state_pressed } );

        Drawable queenImage = res.getDrawable(R.drawable.bee_queen_selector);
        queenImage.setState( new int[] { -android.R.attr.state_pressed } );

        Drawable deadImage = res.getDrawable(R.drawable.bee_dead);

        //cashe icons
        Bitmap droneIcon = BitmapFactory.decodeResource(res, R.drawable.bee_drone_icon);
        Bitmap droneIconHit = BitmapFactory.decodeResource(res, R.drawable.bee_drone_hit_icon);

        Bitmap workerIcon = BitmapFactory.decodeResource(res, R.drawable.bee_worker_icon);
        Bitmap workerIconHit = BitmapFactory.decodeResource(res, R.drawable.bee_worker_hit_icon);

        Bitmap queenIcon = BitmapFactory.decodeResource(res, R.drawable.bee_queen_icon);
        Bitmap queenIconHit = BitmapFactory.decodeResource(res, R.drawable.bee_queen_hit_icon);

        beeIconeHeight = queenIcon.getHeight();
        beeIconeWidth = queenIcon.getWidth();

        //create array of the units
        beeArray = new Bee[ARRAY_SIZE];

        //create all objects: 1 queen, 5 workers, 8 drones
        Queen beeQueen = new Queen(queenImage, queenIcon, queenIconHit, deadImage);
        beeArray[arrayIndex] = beeQueen;
        arrayIndex++;

        for(int i = 0; i < 5; i++) {
            Worker beeWorker = new Worker(workerImage, workerIcon, workerIconHit, deadImage);
            beeArray[arrayIndex] = beeWorker;
            beeQueen.addObserver(beeWorker);
            arrayIndex++;
        }

        for(int i = 0; i < 8; i++) {
            Drone beeDrone = new Drone(droneImage, droneIcon, droneIconHit, deadImage);
            beeArray[arrayIndex] = beeDrone;
            beeQueen.addObserver(beeDrone);
            arrayIndex++;
        }

        if(Prefs.DEBUG) Log.d(Prefs.LOG_TAG, getClass().getSimpleName() +" beeSpriteWidth:" + beeSpriteWidth);
        if(Prefs.DEBUG) Log.d(Prefs.LOG_TAG, getClass().getSimpleName() +" beeSpriteHeight:" + beeSpriteHeight);

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
     * Starts the game, next round, set parameters
     */
    public void doStart() {
        if(Prefs.DEBUG) Log.d(Prefs.LOG_TAG, getClass().getSimpleName() +" doStart" );
        boolean hasNext = randomBeeChoose();

        synchronized (surfaceHolder) {


            if ( ! hasNext ) {
                Toast.makeText(context, R.string.mode_win_prefix, Toast.LENGTH_SHORT).show();
                //setGameState(STATE_WIN);
                resetGame();

            } else {

                //increment level
                round += 1;

                Toast.makeText(context, "Round " + round, Toast.LENGTH_SHORT).show();

                //random choose direction
                horizontal = Math.round(Math.random()) == 0;
                vertical = Math.round(Math.random()) == 0;

                //init  toolbar/icons
                setBoardSize();

                beeSpriteWidth = beeArray[arrayIndex].getWidth();
                beeSpriteHeight = beeArray[arrayIndex].getHeight();

                // pick a convenient initial location for the sprite
                beeSpriteCenterX = canvasWidth / 2;
                beeSpriteCenterY = (canvasHeight / 2) - beeSpriteHeight;

                beeArray[arrayIndex].setPosition(beeSpriteCenterX, beeSpriteCenterY);

                nextRefreshTime = System.currentTimeMillis() + REFRESH_STEP_MILLIS;


                setGameState(STATE_RUNNING);
            }
        }
    }

    private void resetGame() {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(context.getResources().getString(R.string.message_start))
               .setPositiveButton(R.string.alert_message_positiv, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(Prefs.DEBUG) Log.d(Prefs.LOG_TAG, getClass().getSimpleName() +"Positiv" );

                    }
                }) ;

        Dialog dialog = builder.create();
        dialog.show();



        if(Prefs.DEBUG) Log.d(Prefs.LOG_TAG, getClass().getSimpleName() +" resetGame" );

        //resetHP
        for (Bee beeUnit : beeArray) {
            beeUnit.reset();
        }

        arrayIndex = 0;
        round = 0;

        setGameState(STATE_RUNNING);

        doStart();

    }

    private void setBoardSize() {
        boardLeft = beeSpriteWidth/2;
        boardTop = (PADDING * 3) + (beeIconeHeight*2) + beeSpriteHeight/2;

        boardHeight = canvasHeight - boardTop - beeSpriteHeight/2;
        boardWidth = canvasWidth - beeSpriteWidth ;
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
        if(Prefs.DEBUG) Log.d(Prefs.LOG_TAG, getClass().getSimpleName() +" setState" );

        synchronized (surfaceHolder) {
            gameMode = mode;

        }

    }

    public boolean touchItem(int x, int y) { //isHit
        if(Prefs.DEBUG) Log.d(Prefs.LOG_TAG, getClass().getSimpleName() +" touchItem X=" + x +", Y=" + y);
        if(Prefs.DEBUG) Log.d(Prefs.LOG_TAG, getClass().getSimpleName() +" touchItem beeX=" + beeSpriteCenterX +"  beeY=" + beeSpriteCenterY);

        boolean flag = false;

        synchronized (runLock) {
            if ( beeArray[arrayIndex].containsPoint(x,y) ) {
                beeArray[arrayIndex].setPressed(true);
                beeArray[arrayIndex].takeDamage();

                flag = true;

                if(Prefs.DEBUG) Log.d(Prefs.LOG_TAG, getClass().getSimpleName() +" touchItem HIT" );

            }
        }

        if( beeArray[arrayIndex].isDead() ){
            if(beeArray[arrayIndex].getUnitType() == Bee.QUEEN) {
                beeArray[arrayIndex].notifyObservers();
            }
             //nextRound
            doStart();
        }



        return flag;
    }


    public boolean releaseTouch() {
        if(Prefs.DEBUG) Log.d(Prefs.LOG_TAG, getClass().getSimpleName() +" releaseTouch" );

           beeArray[arrayIndex].setPressed(false);

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

    //random choose the bee/object from the array
    private boolean randomBeeChoose() {

        boolean choose = true;
        int min = 0;
        int max = ARRAY_SIZE - 1;

        Random r = new Random();

        for( int i = 0; choose && (i < ARRAY_SIZE); i++ ){
            arrayIndex = r.nextInt((max - min) + 1) + min;
            choose = beeArray[arrayIndex].isDead();

            if(Prefs.DEBUG) Log.d(Prefs.LOG_TAG, getClass().getSimpleName() +" randomBeeChoose choose=" + choose);
            if(Prefs.DEBUG) Log.d(Prefs.LOG_TAG, getClass().getSimpleName() +" randomBeeChoose arrayIndex=" + arrayIndex);

        }

        if(Prefs.DEBUG) Log.d(Prefs.LOG_TAG, getClass().getSimpleName() +" randomBeeChoose ends arrayIndex=" + arrayIndex);


        return !choose;
    }


    /**
     * Called at the start of draw().
     * Detects the end-of-game and sets the UI to the next state.
     */
    private void updatePhysics() {
        if(Prefs.DEBUG) Log.d(Prefs.LOG_TAG, getClass().getSimpleName() +" updatePhysics" );
        long now = System.currentTimeMillis();

        if ( nextRefreshTime > now ) return;

        setBoardSize();

        //move direction
        horizontal = (Math.round(Math.random()) == 0);
        vertical = (Math.round(Math.random()) == 0);

        //choose random x y
        beeSpriteCenterX += ( horizontal ? 10 : -10 );
        if( beeSpriteCenterX > (boardLeft + boardWidth) ) {
            beeSpriteCenterX = boardLeft + boardWidth  ;
            //horizontal = false;
        } else if ( beeSpriteCenterX < boardLeft ) {
            beeSpriteCenterX = boardLeft ;
            //horizontal = true;
        }

        beeSpriteCenterY += ( vertical ? 20 : -20 );
        if( beeSpriteCenterY > (boardTop + boardHeight) ) {
            beeSpriteCenterY = boardTop + boardHeight ;
            //vertical = false;
        } else if ( beeSpriteCenterY < boardTop ) {
            beeSpriteCenterY = boardTop ;
            //vertical = true;
        }


        beeArray[arrayIndex].setPosition(beeSpriteCenterX, beeSpriteCenterY);

        if(Prefs.DEBUG) Log.d(Prefs.LOG_TAG, getClass().getSimpleName() +" updatePhysics  beeSpriteCenterX:" + beeSpriteCenterX +"  beeSpriteCenterY:" + beeSpriteCenterY);
        if(Prefs.DEBUG) Log.d(Prefs.LOG_TAG, getClass().getSimpleName() +" updatePhysics  boardLeft:" + boardLeft +"  boardTop:" + boardTop);
        if(Prefs.DEBUG) Log.d(Prefs.LOG_TAG, getClass().getSimpleName() +" updatePhysics  boardRight:" + (boardLeft+boardWidth) +"  boardBotome:" + (boardTop+boardHeight) );


        this.releaseTouch();

        nextRefreshTime = now + REFRESH_STEP_MILLIS;;

    }



    private void doDraw(Canvas canvas) {
        if(Prefs.DEBUG) Log.d(Prefs.LOG_TAG, getClass().getSimpleName() +" doDraw" );
        int left = PADDING;
        int top = PADDING;

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.WHITE);

        //draw a background
        canvas.drawColor(Color.RED);

        //draw the bee on the board
        beeArray[arrayIndex].draw(canvas);


        //draw toolbar/icons
        int maxIcones = canvasWidth / (beeIconeWidth + PADDING);
        for(int i = 0, j = 0; i < ARRAY_SIZE; i++, j++) {
            if(j > maxIcones) {
                left = PADDING;
                top += beeIconeHeight + PADDING;
                j = 0;
            }
            beeArray[i].drawIcon(canvas, left, top, null);
            left += beeIconeWidth + PADDING;
        }

        //draw text near icons
        top += (beeIconeHeight/2);
        left *= 2;

        StringBuilder str = new StringBuilder();
        str.append(context.getString(R.string.hp_title));
        str.append( beeArray[arrayIndex].getHitPoints() );

        String hitPoints = str.toString();

        canvas.drawText(hitPoints, 0, hitPoints.length(), left, top, paint);

    }


    public void pause() {
        synchronized (surfaceHolder) {
            if (gameMode == STATE_RUNNING) setGameState(STATE_PAUSE);
        }
    }










}//DrawThread
