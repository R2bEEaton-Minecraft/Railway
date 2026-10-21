/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.fabric_mixin.client;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.railwayteam.railways.registry.CRBogeyStyles;
import com.zurrtum.create.client.foundation.entity.behaviour.CarriageParticleBehaviour;
import com.zurrtum.create.content.trains.entity.Carriage;
import com.zurrtum.create.content.trains.entity.CarriageContraptionEntity;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Handcars only emit brake sparks, not steam.
 */
@Mixin(value = CarriageParticleBehaviour.class, remap = false)
public abstract class MixinCarriageParticleBehaviour {
    @WrapWithCondition(
        method = "tick",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V")
    )
    private boolean railways$skipParticlesForHandcar(Level instance, ParticleOptions particleData, double x, double y,
                                                     double z, double xSpeed, double ySpeed, double zSpeed,
                                                     @Local(name = "spark") boolean spark) {
        if (spark)
            return true;
        CarriageContraptionEntity entity = ((CarriageParticleBehaviour) (Object) this).entity;
        Carriage carriage = entity == null ? null : entity.getCarriage();
        return carriage == null || !carriage.bogeys.both(b -> b == null || b.getStyle() == CRBogeyStyles.HANDCAR);
    }
}
