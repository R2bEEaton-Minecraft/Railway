package com.tterrag.registrate.util.nullness;

import java.util.function.Function;

@FunctionalInterface
public interface NonNullFunction<T, R> extends Function<T, R> {
    default <V> NonNullFunction<T, V> andThen(NonNullFunction<? super R, ? extends V> after) {
        return value -> after.apply(apply(value));
    }
}
