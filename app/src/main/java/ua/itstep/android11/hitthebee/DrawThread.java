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
import android.os.Message;
import android.support.annotation.StringRes;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.Toast;

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


    private Drawable deadImage;
    private Drawable workerImage;
    private Drawable droneImage;
    private Drawable queenImage;

    //private ImageButton beeImageButton;
    //private Paint paint;
    //private Paint rectPaint;
    private Handler handler;
    private Context context;
    private final Object runLock = new Object();
    //private RectF scratchRectF;
    private CharSequence message;

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

    //boardHeight = canvasHeight - toolbar/icons
    //boardWidth = canvasWidth - PADDING*2
    private int boardLeft = 1;
    private int boardTop = 1;
    private int boardHeight = 1;
    private int boardWidth = 1;

    private int beeSpriteXLeft = 1;
    private int beeSpriteYTop = 1;

    private int beeSpriteWidth = 1;
    private int beeSpriteHeight = 1;

    private int beeIconeWidth = 1;
    private int beeIconeHeight = 1;

    private static final int PADDING = 7;

    //direction off a next move
    private boolean horizontal= true;
    private boolean vertical = true;

    /** Used to figure out elapsed time between frames */
    private long nextRefreshTime = 0;
    private static final int REFRESH_STEP_MILLIS = 300;

    private Queen beeQueen;
    private Drone beeDrone;
    private Worker beeWorker;

    private int arrayIndex = 0;

    private Bee[] beeArray;
    private static final int ARRAY_SIZE = 14;



    DrawThread(SurfaceHolder surfaceHolder,  Context context, Handler handler) {
        if(Prefs.DEBUG) Log.d(Prefs.LOG_TAG, getClass().getSimpleName() +" Constructor" );

        this.surfaceHolder = surfaceHolder;
        this.context = context;
        this.handler = handler;

        Resources res = context.getResources();
        message = "";
        arrayIndex = 0;
        round = 0;


        //cashe drawables
        workerImage = res.getDrawable(R.drawable.bee_worker_selector);
        workerImage.setState( new int[] { -android.R.attr.state_pressed } );

        droneImage = res.getDrawable(R.drawable.bee_drone_selector);
        droneImage.setState( new int[] { -android.R.attr.state_pressed } );

        queenImage = res.getDrawable(R.drawable.bee_queen_selector);
        queenImage.setState( new int[] { -android.R.attr.state_pressed } );

        deadImage = res.getDrawable(R.drawable.bee_dead);

        //cashe icons
        droneIcon = BitmapFactory.decodeResource(res, R.drawable.bee_drone_icon);
        droneIconHit = BitmapFactory.decodeResource(res, R.drawable.bee_drone_hit_icon);

        workerIcon = BitmapFactory.decodeResource(res, R.drawable.bee_worker_icon);
        workerIconHit = BitmapFactory.decodeResource(res, R.drawable.bee_worker_hit_icon);

        queenIcon = BitmapFactory.decodeResource(res, R.drawable.bee_queen_icon);
        queenIconHit = BitmapFactory.decodeResource(res, R.drawable.bee_queen_hit_icon);

        beeIconeHeight = queenIcon.getHeight();
        beeIconeWidth = queenIcon.getWidth();

        //create array of the units
        beeArray = new Bee[ARRAY_SIZE];

        //create all objects: 1 queen, 5 workers, 8 drones
        beeQueen = new Queen(queenImage, queenIcon, queenIconHit, deadImage);
        beeArray[arrayIndex] = beeQueen;
        arrayIndex++;

        for(int i = 0; i < 5; i++) {
            beeWorker = new Worker(workerImage, workerIcon, workerIconHit, deadImage);
            beeArray[arrayIndex] = beeWorker;
            arrayIndex++;
        }

        for(int i = 0; i < 8; i++) {
            beeDrone = new Drone(droneImage, droneIcon, droneIconHit, deadImage);
            beeArray[arrayIndex] = beeDrone;
            arrayIndex++;
        }





        //set default image
        //Bitmap beeImage = BitmapFactory.decodeResource(res, R.drawable.bee_queen);
        //int w = beeImage.getWidth();

        //Rect rect = workerImage.getBounds();
        //rect.contains()


        //beeSpriteWidth = beeImage.getIntrinsicWidth();
        //beeSpriteHeight = beeImage.getIntrinsicHeight();


        //paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        //rectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        //rectPaint.setColor(Color.WHITE);







        //scratchRectF = new RectF(0, 0, 0, 0); //(100.0f, 100.0f, 200.0f,200.0f); //left, topI, right, bottom




        if(Prefs.DEBUG) Log.d(Prefs.LOG_TAG, getClass().getSimpleName() +" beeSpriteWidth:" + beeSpriteWidth);
        if(Prefs.DEBUG) Log.d(Prefs.LOG_TAG, getClass().getSimpleName() +" beeSpriteHeight:" + beeSpriteHeight);

        //initial sprite location
        //beeSpriteCenterX = beeSpriteWidth;
        //beeSpriteCenterY = beeSpriteHeight * 2;

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
        //if(Prefs.DEBUG) Log.d(Prefs.LOG_TAG, getClass().getSimpleName() +" doStart" );


        synchronized (surfaceHolder) {


            if ( ! randomBeeChoose() ) {
                Toast.makeText(context, R.string.message_game_over, Toast.LENGTH_SHORT).show();
                //setGameState(STATE_WIN); //exit
                return;
            }

            //increment level
            round += 1;

            Toast.makeText(context, "Round "+ round, Toast.LENGTH_SHORT).show();

            //random choose direction
            horizontal = Math.round(Math.random()) == 0;
            vertical = Math.round(Math.random()) == 0;

            //init  toolbar/icons
            setBoardSize();


            //scratchRectF.set(330.0f, 100.0f, 400.0f, 200.0f);
            //beeImage.setState( new int[] { -android.R.attr.state_pressed } );

            //imageButton.setImageResource(R.drawable.bee_worker_selector);
            //imageButton.setBackground(beeImage);

            //scratchRectF.set();




            beeSpriteWidth = beeArray[arrayIndex].getWidth();
            beeSpriteHeight = beeArray[arrayIndex].getHeight();

            // pick a convenient initial location for the sprite
            beeSpriteCenterX = canvasWidth / 2;
            beeSpriteCenterY = (canvasHeight / 2) - beeSpriteHeight;

            beeArray[arrayIndex].setPosition(beeSpriteCenterX, beeSpriteCenterY);

            //beeSpriteXLeft = (int) beeSpriteCenterX - beeSpriteWidth / 2;
            //beeSpriteYTop = (int) beeSpriteCenterY + beeSpriteHeight / 2;


            //beeImage.setBounds(beeSpriteXLeft, beeSpriteYTop, beeSpriteXLeft + beeSpriteWidth, beeSpriteYTop + beeSpriteHeight);


            nextRefreshTime = System.currentTimeMillis() + REFRESH_STEP_MILLIS;


            setGameState(STATE_RUNNING);
        }
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
        //if(Prefs.DEBUG) Log.d(Prefs.LOG_TAG, getClass().getSimpleName() +" setState" );

        synchronized (surfaceHolder) {
            gameMode = mode;


            if (gameMode == STATE_WIN) {
                Message message = handler.obtainMessage();
                Bundle b = new Bundle();
                b.putInt("STATE", STATE_WIN);
                message.setData(b);
                handler.sendMessage(message);
            }


            /*
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
            */
        }

    }

    public boolean touchItem(int x, int y) { //isHit
        if(Prefs.DEBUG) Log.d(Prefs.LOG_TAG, getClass().getSimpleName() +" touchItem X=" + x +", Y=" + y);
        if(Prefs.DEBUG) Log.d(Prefs.LOG_TAG, getClass().getSimpleName() +" touchItem beeX=" + beeSpriteCenterX +"  beeY=" + beeSpriteCenterY);

        boolean flag = false;

        //Rect rect = beeImage.getBounds();

        synchronized (runLock) {
            if ( beeArray[arrayIndex].containsPoint(x,y) ) {
                //beeImage.setState(new int[]{android.R.attr.state_pressed});
                beeArray[arrayIndex].setPressed(true);
                beeArray[arrayIndex].takeDamage();


                flag = true;

                if(Prefs.DEBUG) Log.d(Prefs.LOG_TAG, getClass().getSimpleName() +" touchItem HIT" );

            }
        }

        if( beeArray[arrayIndex].isDead() ){
            if(beeArray[arrayIndex].getUnitType() == Bee.QUEEN) {
                //TODO beeArray[arrayIndex].notifyAll();  kill them all -> bee.kill();
            }
            //TODO changeImage to deadBee
            //TODO nextRound
            doStart();
        }



        return flag;
    }


    public boolean releaseTouch() {
        if(Prefs.DEBUG) Log.d(Prefs.LOG_TAG, getClass().getSimpleName() +" releaseTouch" );

        //beeImage.setState(new int[]{-android.R.attr.state_pressed});
        beeArray[arrayIndex].setPressed(false);

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

    //random choose the bee/object from the array
    private boolean randomBeeChoose() {

        //TODO
        boolean choose;
        arrayIndex -= 1;

        if( arrayIndex >= 0 ) {
            choose = true;
        } else {
            arrayIndex = 0;
            choose = false;

        }



        return choose;
    }


    /**
     * Called at the start of draw().
     * Detects the end-of-game and sets the UI to the next state.
     */
    private void updatePhysics() {
        //if(Prefs.DEBUG) Log.d(Prefs.LOG_TAG, getClass().getSimpleName() +" updatePhysics" );
        long now = System.currentTimeMillis();

        if ( nextRefreshTime > now ) return;

        setBoardSize();



        //move direction

        //if(horizontal) horizontal = Math.round(Math.random()) == 0;
        //if(vertical) vertical = Math.round(Math.random()) == 0;

        horizontal = vertical || horizontal && (Math.round(Math.random()) == 0);
        vertical = horizontal || vertical && (Math.round(Math.random()) == 0);

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


        //beeSpriteYTop = (int) beeSpriteCenterY + beeSpriteHeight / 2;
        //beeSpriteXLeft = (int) beeSpriteCenterX - beeSpriteWidth / 2;

        //beeImage.setBounds(beeSpriteXLeft, beeSpriteYTop, beeSpriteXLeft + beeSpriteWidth, beeSpriteYTop + beeSpriteHeight);

        beeArray[arrayIndex].setPosition(beeSpriteCenterX, beeSpriteCenterY);

        if(Prefs.DEBUG) Log.d(Prefs.LOG_TAG, getClass().getSimpleName() +" updatePhysics  beeSpriteCenterX:" + beeSpriteCenterX +"  beeSpriteCenterY:" + beeSpriteCenterY);
        if(Prefs.DEBUG) Log.d(Prefs.LOG_TAG, getClass().getSimpleName() +" updatePhysics  boardLeft:" + boardLeft +"  boardTop:" + boardTop);
        if(Prefs.DEBUG) Log.d(Prefs.LOG_TAG, getClass().getSimpleName() +" updatePhysics  boardRight:" + (boardLeft+boardWidth) +"  boardBotome:" + (boardTop+boardHeight) );


        this.releaseTouch();

        nextRefreshTime = now + REFRESH_STEP_MILLIS;;

        int result = STATE_WIN;


        //doStart();
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


        //top = beeIconeHeight + beeIconeHeight + PADDING + PADDING + PADDING + (beeSpriteHeight/2);
        //if(Prefs.DEBUG) Log.d(Prefs.LOG_TAG, getClass().getSimpleName() +" doDraw  top=" + top );


        canvas.drawLine(boardLeft, boardTop, (boardLeft+boardWidth), boardTop, paint);
        canvas.drawLine(boardLeft, boardTop, boardLeft, (boardTop+boardHeight), paint);
        canvas.drawLine(boardLeft, (boardTop+boardHeight), (boardLeft+boardWidth), (boardTop+boardHeight), paint);
        canvas.drawLine((boardLeft+boardWidth), boardTop, (boardLeft+boardWidth), (boardTop+boardHeight), paint);

        //Rect rectImg = new Rect(xLeft, yTop, xLeft + beeSpriteWidth, yTop + beeSpriteHeight);
        //beeImageButton.bringToFront();
        //beeImageButton.buildLayer();

        //imageButton.setClipBounds(rectImg);
        //beeImageButton.draw(canvas);

        //beeImage.setBounds(xLeft, yTop, xLeft + beeSpriteWidth, yTop + beeSpriteHeight);

        //beeImage.draw(canvas);



        //scratchRectF.centerX();

        /*
        Resources res = context.getResources();
        Bitmap img = BitmapFactory.decodeResource(res, R.drawable.bee_worker);
        float top = canvasHeight - img.getHeight();
        float left = canvasWidth/2 ;

        canvas.drawBitmap(img, left, top, paint);
        */

        //beeSpriteWidth = beeImage.getIntrinsicWidth();
        //beeSpriteHeight = beeImage.getIntrinsicHeight();

        /*
        int top = canvasHeight - droneImage.getIntrinsicHeight();
        int left = canvasWidth/2 ;



        droneImage.setBounds(left, top, left + droneImage.getIntrinsicWidth(), canvasHeight);
        droneImage.draw(canvas);
        */

        //canvas.drawRect(scratchRectF, rectPaint);
        //canvas.drawText(message, 0, message.length(), 300.0f, 300.0f, rectPaint);






    }











}//DrawThread
