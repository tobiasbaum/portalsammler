package de.tntinteractive.portalsammler;

public class ValueWrapper<T> {

    private T value;

    private ValueWrapper(T value) {
        this.value = value;
    }

    public static <V> ValueWrapper<V> create(V value) {
        return new ValueWrapper<V>(value);
    }

    public void set(T value) {
        this.value = value;
    }

    public T get() {
        return this.value;
    }

}
