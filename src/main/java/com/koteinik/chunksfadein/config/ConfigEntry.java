package com.koteinik.chunksfadein.config;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import com.moandjiezana.toml.Toml;

public class ConfigEntry<T> {
    protected final Set<Consumer<T>> updateListeners = new HashSet<>();
    protected final Type type;

    public final T defaultValue;
    public final String configKey;

    protected T value;

    public ConfigEntry(T defaultValue, String configKey, Type type) {
        this.defaultValue = defaultValue;
        this.configKey = configKey;
        this.type = type;
    }

    public void reset() {
        this.value = defaultValue;
        pollListeners();
    }

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
        pollListeners();
    }

    public void load(Toml toml) {
        T tomlValue = (T) type.get(toml, configKey);

        if (tomlValue == null)
            tomlValue = defaultValue;

        value = tomlValue;
        pollListeners();
    }

    protected void pollListeners() {
        updateListeners.forEach((c) -> c.accept(value));
    }

    public Consumer<T> addListener(Consumer<T> consumer) {
        updateListeners.add(consumer);

        return consumer;
    }

    public void removeListener(Consumer<?> consumer) {
        updateListeners.remove(consumer);
    }

    @Override
    public String toString() {
        return configKey + " = " + value + "\n";
    }

    public static enum Type {
        INTEGER((t, k) -> t.getLong(k) == null ? null : t.getLong(k).intValue()),
        DOUBLE((t, k) -> t.getDouble(k)),
        BOOLEAN((t, k) -> t.getBoolean(k));

        private BiFunction<Toml, String, Object> get;

        private Type(BiFunction<Toml, String, Object> get) {
            this.get = get;
        }

        public Object get(Toml toml, String key) {
            return get.apply(toml, key);
        }
    }
}
