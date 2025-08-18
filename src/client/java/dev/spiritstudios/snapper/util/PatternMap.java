package dev.spiritstudios.snapper.util;

import java.util.*;
import java.util.stream.Collectors;

import com.mojang.datafixers.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A map that allows for pattern matching (instanceof) when retrieving values.
 * This is extremely inefficient due to us having to iterate over all entries to find a match.
 * Unfortunately, this is the best we can do if we want to check for inheritance.
 *
 * @param <V> The value type
 */
public class PatternMap<V> implements Map<Class<?>, V> {
    private final List<Pair<Class<?>, V>> entries = new ArrayList<>();

    public V get(Class<?> clazz) {
        return entries.stream().
                filter(entry -> entry.getFirst().isAssignableFrom(clazz))
                .findFirst()
                .map(Pair::getSecond)
                .orElse(null);
    }

    @Override
    public int size() {
        return entries.size();
    }

    @Override
    public boolean isEmpty() {
        return entries.isEmpty();
    }

    @Override
    public boolean containsKey(Object o) {
        return entries.stream().anyMatch(entry -> entry.getFirst().isAssignableFrom((Class<?>) o));
    }

    @Override
    public boolean containsValue(Object o) {
        return entries.stream().anyMatch(entry -> entry.getSecond().equals(o));
    }

    public V get(Object object) {
        return get(object.getClass());
    }

    @Override
    public @Nullable V put(Class<?> aClass, V v) {
        V old = get(aClass);
        entries.add(Pair.of(aClass, v));
        return old;
    }

    @Override
    public V remove(Object o) {
        V value = get(o);
        entries.removeIf(pair -> pair.getFirst().equals(o));
        return value;
    }

    @Override
    public void putAll(@NotNull Map<? extends Class<?>, ? extends V> map) {
        map.forEach(this::put);
    }

    @Override
    public void clear() {
        entries.clear();
    }

    @Override
    public @NotNull Set<Class<?>> keySet() {
        return entries.stream().map(Pair::getFirst).collect(Collectors.toSet());
    }

    @Override
    public @NotNull Collection<V> values() {
        return entries.stream().map(Pair::getSecond).collect(Collectors.toList());
    }

    @Override
    public @NotNull Set<Entry<Class<?>, V>> entrySet() {
        return entries.stream()
                .map(pair -> new AbstractMap.SimpleEntry<Class<?>, V>(pair.getFirst(), pair.getSecond()))
                .collect(Collectors.toSet());
    }
}