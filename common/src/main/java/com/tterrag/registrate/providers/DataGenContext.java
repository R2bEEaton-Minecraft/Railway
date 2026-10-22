package com.tterrag.registrate.providers;

import net.minecraft.resources.ResourceLocation;

public record DataGenContext<R, T>(ResourceLocation id, T entry) {
}
