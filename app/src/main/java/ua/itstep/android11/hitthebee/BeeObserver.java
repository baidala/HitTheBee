package ua.itstep.android11.hitthebee;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by Maksim Baydala on 26/01/17.
 */

interface BeeObserver extends Observer {

    @Override
    void update(Observable observable, Object data);
}
