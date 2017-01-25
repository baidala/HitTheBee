package ua.itstep.android11.hitthebee;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by Maksim Baydala on 16/01/17.
 */

public abstract class Bee {

    protected int hitPoints;
    protected int damage;
    protected int unitType;

    //protected Matrix beeMatrix = new Matrix();

    //protected RectF beeRectF;
    protected Rect beeRect;

    //protected float width;
    //protected float height;

    protected Drawable beeImage;
    protected Drawable deadImage;
    protected Bitmap beeIcon;
    protected Bitmap beeIconHit;

    protected boolean pressed;

    protected static final int QUEEN = 101;
    protected static final int WORKER = 102;
    protected static final int DRONE = 103;

    private int beeImageXLeft;
    private int beeImageXRight;
    private int beeImageYTop;
    private int beeImageYBottom;
    private int beeImageWidth;
    private int beeImageHeight;


    public Bee(int beeType,int hp, int dmg, Bitmap beeIcon, Bitmap beeIconHit , Drawable beeImage, Drawable deadImage) {
        this.unitType = beeType;
        this.hitPoints = hp;
        this.damage = dmg;
        this.pressed = false;
        this.beeIcon = beeIcon;
        this.beeIconHit = beeIconHit;
        this.beeImage = beeImage;
        this.deadImage = deadImage;

        this.beeImageWidth = beeImage.getIntrinsicWidth();
        this.beeImageHeight = beeImage.getIntrinsicHeight();


        //beeRect = new Rect(0, 0, beeImageWidth, beeImageHeight);
        //this.beeImage.setBounds(beeRect);

    }

    public boolean isDead() {
        return hitPoints < 1 ;

    }

    public boolean containsPoint(int x, int y) {
        beeRect = beeImage.getBounds();

        return beeRect.contains(x, y);
        //return startX <= x && endX > x && startY <= y && endY > y;

         //beeImageYTop < beeImageYBottom && beeImageXLeft < beeImageXRight  &&// check for empty first
        //return x >= beeImageYTop && x < beeImageYBottom && y >= beeImageXLeft && y < beeImageXRight;
    }


    public void setPosition(int x, int y)
    {
        //beeMatrix.setTranslate(x, y);
        //beeMatrix.mapRect(beeRect);

        beeImageXLeft =  x - (beeImageWidth / 2) ;
        beeImageXRight = beeImageXLeft + beeImageWidth;
        beeImageYTop =  y - (beeImageHeight / 2) ;
        beeImageYBottom = beeImageYTop + beeImageHeight;


        beeImage.setBounds(beeImageXLeft, beeImageYTop, beeImageXRight, beeImageYBottom);
        if(Prefs.DEBUG) Log.d(Prefs.LOG_TAG, getClass().getSimpleName() +" setPosition beeImageXLeft=" +beeImageXLeft +" beeImageYTop="+ beeImageYTop +" beeImageXRight="+ beeImageXRight +" beeImageYBottom="+ beeImageYBottom);
    }

    public void setIconPosition(int x, int y)
    {
        //beeMatrix.setTranslate(x, y);
        //beeMatrix.mapRect(beeRect);


    }

    public void setPressed(boolean pressed)
    {
        this.pressed = pressed;
        if( pressed ) {
            beeImage.setState( new int[] { android.R.attr.state_pressed } );
        } else {
            beeImage.setState( new int[] { -android.R.attr.state_pressed } );
        }
    }

    public int getHeight() {
        return this.beeImageHeight;
    }

    public int getWidth() {
        return this.beeImageWidth;
    }

    public void takeDamage() {

        if ( hitPoints > damage ) {
            hitPoints -= damage;
        } else {
            hitPoints = 0;
        }

    }

    public final int getUnitType() {
        return unitType;
    }

    public void draw(Canvas canvas)
    {

        if( isDead() ) {
            deadImage.draw(canvas);
        } else {
            beeImage.draw(canvas);
        }
    }

    public void drawIcon(Canvas canvas, float left, float top, @Nullable Paint paint)
    {
        if( isDead() ) {
            canvas.drawBitmap(beeIconHit, left, top, paint);
        } else {
            canvas.drawBitmap(beeIcon, left, top, paint);
        }
    }




}
