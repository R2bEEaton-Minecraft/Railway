package com.tterrag.registrate.util.entry;

import com.tterrag.registrate.util.nullness.NonNullSupplier;
import net.minecraft.resources.ResourceLocation;

public class RegistryEntry<T> implements NonNullSupplier<T> {
    protected final ResourceLocation identifier;
    protected final ResourceLocation id;
    protected final T value;

    public RegistryEntry(ResourceLocation id, T value) {
        this.identifier = id;
        this.id = id;
        this.value = value;
    }

    public ResourceLocation getId() {
        return id;
    }

    public ResourceLocation getIdentifier() {
        return identifier;
    }

    @Override
    public T get() {
        return value;
    }

    public boolean is(T other) {
        return value == other;
    }

    public static <T> RegistryEntry<T> empty() {
        return new RegistryEntry<>(ResourceLocation.fromNamespaceAndPath("railways", "empty"), null);
    }
}
