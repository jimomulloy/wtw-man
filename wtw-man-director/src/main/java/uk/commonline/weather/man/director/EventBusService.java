package uk.commonline.weather.man.director;

import java.util.concurrent.Executors;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;

/**
 * @author Jim O'Mulloy
 * 
 * WTW Manager Service that provides an instance of a Google Guava EventBus to process Weather report requests in a thread safe, reactive manner.
 */
public class EventBusService {

    private static EventBusService instance = new EventBusService();

    public static EventBusService $() {
        return instance;
    }

    private EventBus eventBus = null;

    private EventBusService() {
        eventBus = new AsyncEventBus(Executors.newCachedThreadPool());
    }

    public void postEvent(Object e) {
        eventBus.post(e);
    }

    public void registerSubscriber(Object subscriber) {
        eventBus.register(subscriber);
    }

    public void unRegisterSubscriber(Object subscriber) {
        eventBus.unregister(subscriber);
    }
}