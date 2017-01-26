package ua.itstep.android11.hitthebee;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;



/**
 * Created by Maksim Baydala on 16/01/17.
 */

public class Worker extends Bee implements BeeObserver {

    private static final int HIT_POINTS = 75 ;
    private static final int DAMAGE = 10 ;

    public Worker(Drawable beeImage, Bitmap beeIcon, Bitmap beeIconHit, Drawable deadImage) {
        super( WORKER, HIT_POINTS, DAMAGE, beeIcon, beeIconHit, beeImage, deadImage );
    }


    @Override
    public void kill() {
        //TODO
        this.hitPoints = 0 ;
    }
}
