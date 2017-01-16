package ua.itstep.android11.hitthebee;

/**
 * Created by Maksim Baydala on 16/01/17.
 */

public class Worker extends Bee {

    public Worker(int _id) {
        this.id = _id;
        this.hitPoints = 75;
        this.damage = 10;
        this.unitType = Prefs.workerType;
    }
}
