/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.fabric_mixin;

import com.railwayteam.railways.config.CRConfigs;
import com.zurrtum.create.AllBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.EntityBasedExplosionDamageCalculator;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Creepers and ghast fireballs don't destroy tracks unless the server config allows it.
 */
@Mixin({ ExplosionDamageCalculator.class, EntityBasedExplosionDamageCalculator.class })
public abstract class MixinExplosionDamageCalculator {
    @Inject(method = "shouldBlockExplode", at = @At("HEAD"), cancellable = true)
    private void railways$creepersDontBreakTracks(Explosion explosion, BlockGetter reader, BlockPos pos, BlockState state,
                                                  float power, CallbackInfoReturnable<Boolean> cir) {
        if (!(explosion.getDirectSourceEntity() instanceof Creeper || explosion.getDirectSourceEntity() instanceof LargeFireball))
            return;
        if (state.is(AllBlockTags.TRACKS) && !CRConfigs.server().explosiveTrackDamage.get())
            cir.setReturnValue(false);
    }
}
