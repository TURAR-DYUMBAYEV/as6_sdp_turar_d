package stockexchange.observer;

@FunctionalInterface
public interface Observer<T> {
    void onEvent(T event);
}
