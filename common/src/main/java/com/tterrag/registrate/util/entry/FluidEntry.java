package com.tterrag.registrate.util.entry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;

public class FluidEntry<T extends Fluid> extends RegistryEntry<T> {
    public FluidEntry(ResourceLocation id, T value) {
        super(id, value);
    }
}
