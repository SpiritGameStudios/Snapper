package dev.spiritstudios.snapper.util;

import java.util.function.BiFunction;

/**
 * A few very cursed methods for doing arithmetic with {@link Number}s.
 */
public final class GenericMath {
    private static final Ops<Byte> BYTE_OPS = Ops.create(
            (a, b) -> (byte) (a + b),
            (a, b) -> (byte) (a - b),
            (a, b) -> (byte) (a * b),
            (a, b) -> (byte) (a / b)
    );
    private static final Ops<Short> SHORT_OPS = Ops.create(
            (a, b) -> (short) (a + b),
            (a, b) -> (short) (a - b),
            (a, b) -> (short) (a * b),
            (a, b) -> (short) (a / b)
    );
    private static final Ops<Integer> INTEGER_OPS = Ops.create(
            Integer::sum,
            (a, b) -> a - b,
            (a, b) -> a * b,
            (a, b) -> a / b
    );
    private static final Ops<Long> LONG_OPS = Ops.create(
            Long::sum,
            (a, b) -> a - b,
            (a, b) -> a * b,
            (a, b) -> a / b
    );
    private static final Ops<Float> FLOAT_OPS = Ops.create(
            Float::sum,
            (a, b) -> a - b,
            (a, b) -> a * b,
            (a, b) -> a / b
    );
    private static final Ops<Double> DOUBLE_OPS = Ops.create(
            Double::sum,
            (a, b) -> a - b,
            (a, b) -> a * b,
            (a, b) -> a / b
    );

    private GenericMath() {
        throw new UnsupportedOperationException("Cannot instantiate utility class.");
    }

    @SuppressWarnings("unchecked")
    private static <T extends Number> Ops<T> getOps(T x) {
        return switch (x) {
            case Byte ignored -> (Ops<T>) BYTE_OPS;
            case Short ignored -> (Ops<T>) SHORT_OPS;
            case Integer ignored -> (Ops<T>) INTEGER_OPS;
            case Long ignored -> (Ops<T>) LONG_OPS;
            case Float ignored -> (Ops<T>) FLOAT_OPS;
            case Double ignored -> (Ops<T>) DOUBLE_OPS;
            default -> throw new IllegalArgumentException("Unsupported number type: " + x.getClass());
        };
    }

    public static <T extends Number> T add(T a, T b) {
        return getOps(a).add(a, b);
    }

    public static <T extends Number> T subtract(T a, T b) {
        return getOps(a).subtract(a, b);
    }

    public static <T extends Number> T multiply(T a, T b) {
        return getOps(a).multiply(a, b);
    }

    public static <T extends Number> T divide(T a, T b) {
        return getOps(a).divide(a, b);
    }

    /**
     * A set of arithmetic operations that can be performed on {@link Number}s.
     *
     * @param <T> The type of number to perform operations on.
     */
    public abstract static class Ops<T extends Number> {
        public static <T extends Number> Ops<T> create(BiFunction<T, T, T> add, BiFunction<T, T, T> subtract, BiFunction<T, T, T> multiply, BiFunction<T, T, T> divide) {
            return new Ops<>() {
                @Override
                T add(T a, T b) {
                    return add.apply(a, b);
                }

                @Override
                T subtract(T a, T b) {
                    return subtract.apply(a, b);
                }

                @Override
                T multiply(T a, T b) {
                    return multiply.apply(a, b);
                }

                @Override
                T divide(T a, T b) {
                    return divide.apply(a, b);
                }
            };
        }

        abstract T add(T a, T b);

        abstract T subtract(T a, T b);

        abstract T multiply(T a, T b);

        abstract T divide(T a, T b);
    }
}