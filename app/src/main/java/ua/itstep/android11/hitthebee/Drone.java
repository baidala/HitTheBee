package ua.itstep.android11.hitthebee;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

/**
 * Created by Maksim Baydala on 16/01/17.
 */

public class Drone extends Bee {

    private static final int HIT_POINTS = 50 ;
    private static final int DAMAGE = 12 ;

    public Drone(Drawable beeImage, Bitmap beeIcon, Bitmap beeIconHit, Drawable deadImage) {
        super(DRONE, HIT_POINTS, DAMAGE, beeIcon, beeIconHit, beeImage, deadImage);
    }


}
