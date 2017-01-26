package ua.itstep.android11.hitthebee;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Maksim Baydala on 26/01/17.
 */
public class BeeObservable   {

    List<BeeObserver> observers = new ArrayList<BeeObserver>();

    public BeeObservable() {
    }

    public void addObserver(BeeObserver observer) {
        if (observer == null) {
            throw new NullPointerException("observer == null");
        }
        synchronized (this) {
            if (!observers.contains(observer))
                observers.add(observer);
        }
    }

    public void notifyObservers() {
        int size = 0;
        BeeObserver[] arrays = null;
        synchronized (this) {
                size = observers.size();
                arrays = new BeeObserver[size];
                observers.toArray(arrays);
        }
        if (arrays != null) {
            for (BeeObserver observer : arrays) {
                observer.kill();
            }
        }
    }

    public int countObservers() {
        return observers.size();
    }

    public synchronized void deleteObserver(BeeObserver observer) {
        observers.remove(observer);
    }

    public synchronized void deleteObservers() {
        observers.clear();
    }


}
