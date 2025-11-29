package com.koteinik.chunksfadein.config;

import com.moandjiezana.toml.Toml;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class ConfigEntry<T> {
	protected final Set<Consumer<T>> updateListeners = new HashSet<>();
	protected final Type type;

	public final T defaultValue;
	public final String configKey;
	public final String description;

	protected T value;

	public ConfigEntry(T defaultValue, String configKey, String description, Type type) {
		this.defaultValue = defaultValue;
		this.configKey = configKey;
		this.description = description;
		this.type = type;
	}

	public void reset() {
		this.value = defaultValue;
		pollListeners();
	}

	public T get() {
		return value;
	}

	public T set(T value) {
		this.value = value;
		pollListeners();

		return value;
	}

	@SuppressWarnings("unchecked")
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
		return "# %s\n#\n".formatted(description.replace("\n", "\n# ")) +
			"# Default: %s\n".formatted(defaultValue) +
			"%s = %s\n".formatted(configKey, type.makeString(value));
	}

	public enum Type {
		INTEGER((t, k) -> t.getLong(k) == null ? null : t.getLong(k).intValue()),
		DOUBLE(Toml::getDouble),
		BOOLEAN(Toml::getBoolean),
		STRING(Toml::getString, o -> "\"" + o + "\"");

		private final BiFunction<Toml, String, Object> get;
		private final Function<Object, String> toString;

		Type(BiFunction<Toml, String, Object> get) {
			this.get = get;
			this.toString = Object::toString;
		}

		Type(BiFunction<Toml, String, Object> get, Function<Object, String> toString) {
			this.get = get;
			this.toString = toString;
		}

		public Object get(Toml toml, String key) {
			return get.apply(toml, key);
		}

		public String makeString(Object object) {
			return toString.apply(object);
		}
	}
}
