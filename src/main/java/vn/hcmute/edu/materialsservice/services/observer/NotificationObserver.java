package vn.hcmute.edu.materialsservice.services.observer;

import vn.hcmute.edu.materialsservice.models.NotificationEvent;

public interface NotificationObserver {
    void onNotify(NotificationEvent event);
}