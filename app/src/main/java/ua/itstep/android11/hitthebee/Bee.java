package ua.itstep.android11.hitthebee;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

/**
 * Created by Maksim Baydala on 16/01/17.
 */

public abstract class Bee {

    protected int hitPoints;
    protected int damage;
    protected int id;
    protected int unitType;

    protected Matrix beeMatrix = new Matrix();

    protected RectF beeRect;

    protected float width;
    protected float height;

    protected Drawable beeImage;
    protected Bitmap beeIcon;
    protected Bitmap beeIconHit;

    protected boolean pressed;

    public Bee(int beeType,int hp, int dmg, Bitmap beeIcon, Bitmap beeIconHit , Drawable beeImage) {
        this.unitType = beeType;
        this.hitPoints = hp;
        this.damage = dmg;
        this.pressed = false;
        this.beeIcon = beeIcon;
        this.beeIconHit = beeIconHit;
        this.beeImage = beeImage;

    }

    public boolean isAlive() {
        return hitPoints < 1 ;

    }

    public boolean containsPoint(float x, float y) {
        return beeRect.contains((int)x,(int)y);
        //return startX <= x && endX > x && startY <= y && endY > y;
    }


    public void setPosition(float x, float y)
    {
        beeMatrix.setTranslate(x, y);
        beeMatrix.mapRect(beeRect);
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
        beeImage.draw(canvas);
    }

    public void drawIcon(Canvas canvas, float left, float top)
    {
        if( isAlive() ) {
            canvas.drawBitmap(beeIcon, left, top, null);
        } else {
            canvas.drawBitmap(beeIconHit, left, top, null);
        }
    }




}
