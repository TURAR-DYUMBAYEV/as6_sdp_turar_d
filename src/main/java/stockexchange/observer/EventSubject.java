package stockexchange.observer;

import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import stockexchange.observer.events.MarketEvent;

/** Synchronous exact-class dispatch. Each publish uses a registration snapshot. */
public class EventSubject<T extends MarketEvent> implements Subject<T> {
    private record Registration<E>(Class<E> type, Observer<? super E> observer) {
        void deliver(Object event) { observer.onEvent(type.cast(event)); }
    }

    private final CopyOnWriteArrayList<Registration<?>> registrations = new CopyOnWriteArrayList<>();
    private final BiConsumer<MarketEvent, RuntimeException> failureHandler;

    public EventSubject(BiConsumer<MarketEvent, RuntimeException> failureHandler) {
        this.failureHandler = Objects.requireNonNull(failureHandler);
    }

    public <E extends T> void subscribe(Class<E> type, Observer<? super E> observer) {
        registrations.addIfAbsent(new Registration<>(Objects.requireNonNull(type), Objects.requireNonNull(observer)));
    }

    public <E extends T> void unsubscribe(Class<E> type, Observer<? super E> observer) {
        registrations.remove(new Registration<>(type, observer));
    }

    public void publish(T event) {
        Objects.requireNonNull(event);
        for (var registration : registrations) {
            if (registration.type() == event.getClass()) {
                try {
                    registration.deliver(event);
                } catch (RuntimeException exception) {
                    failureHandler.accept(event, exception);
                }
            }
        }
    }

    public int subscriptionCount() { return registrations.size(); }
}
