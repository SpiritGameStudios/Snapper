package dev.spiritstudios.snapper.util;

public record Range<T extends Number & Comparable<T>>(T min, T max) {
    public boolean contains(T value) {
        return value.compareTo(min) >= 0 && value.compareTo(max) <= 0;
    }

    public T clamp(T value) {
        return value.compareTo(min) < 0 ? min : value.compareTo(max) > 0 ? max : value;
    }

    public T range() {
        return GenericMath.subtract(max, min);
    }

    public T lerp(T delta) {
        return GenericMath.add(min, GenericMath.multiply(delta, range()));
    }

    public T lerpProgress(T value) {
        return GenericMath.divide(GenericMath.subtract(value, min), range());
    }

    public T map(T value, Range<T> from) {
        return lerp(from.lerpProgress(value));
    }

    public T map01(T value) {
        return GenericMath.add(GenericMath.multiply(value, range()), min);
    }
}