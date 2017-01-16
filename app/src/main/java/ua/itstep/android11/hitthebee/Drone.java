package ua.itstep.android11.hitthebee;

/**
 * Created by Maksim Baydala on 16/01/17.
 */

public class Drone extends Bee {

    public Drone(int _id) {
        this.id = _id;
        this.hitPoints = 50;
        this.damage = 12;
        this.unitType = Prefs.droneType;
    }


}
