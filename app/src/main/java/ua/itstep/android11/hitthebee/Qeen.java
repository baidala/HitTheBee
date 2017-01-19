package ua.itstep.android11.hitthebee;


import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

/**
 * Created by Maksim Baydala on 16/01/17.
 */

public class Qeen extends Bee {
    private static final int HIT_POINTS = 100 ;
    private static final int DAMAGE = 8 ;

    public Qeen(Drawable beeImage, Bitmap beeIcon, Bitmap beeIconHit) {
        super(UnitType.QUEEN, HIT_POINTS, DAMAGE, beeIcon, beeIconHit, beeImage);

    }

}
