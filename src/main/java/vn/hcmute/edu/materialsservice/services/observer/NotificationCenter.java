package vn.hcmute.edu.materialsservice.services.observer;

import vn.hcmute.edu.materialsservice.models.NotificationEvent;
import java.util.ArrayList;
import java.util.List;

public class NotificationCenter {

    private static final List<NotificationObserver> observers = new ArrayList<>();

    public static void registerObserver(NotificationObserver observer) {
        observers.add(observer);
    }

    public static void notifyObservers(NotificationEvent event) {
        for (NotificationObserver observer : observers) {
            observer.onNotify(event);
        }
    }
}