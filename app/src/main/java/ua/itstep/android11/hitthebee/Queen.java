package ua.itstep.android11.hitthebee;


import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import java.util.Observable;

/**
 * Created by Maksim Baydala on 16/01/17.
 */

public class Queen extends Bee {
    private static final int HIT_POINTS = 100 ;
    private static final int DAMAGE = 8 ;

    public Queen(Drawable beeImage, Bitmap beeIcon, Bitmap beeIconHit, Drawable deadImage) {
        super(QUEEN, HIT_POINTS, DAMAGE, beeIcon, beeIconHit, beeImage, deadImage);

    }


}
