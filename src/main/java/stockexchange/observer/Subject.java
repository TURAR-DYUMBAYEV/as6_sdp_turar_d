package stockexchange.observer;

import stockexchange.observer.events.MarketEvent;

public interface Subject<T extends MarketEvent> {
    <E extends T> void subscribe(Class<E> type, Observer<? super E> observer);
    <E extends T> void unsubscribe(Class<E> type, Observer<? super E> observer);
    void publish(T event);
}
