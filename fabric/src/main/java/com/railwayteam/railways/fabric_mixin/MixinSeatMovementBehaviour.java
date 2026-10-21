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

import com.llamalad7.mixinextras.sugar.Local;
import com.railwayteam.railways.content.conductor.ConductorEntity;
import com.zurrtum.create.content.contraptions.actors.seat.SeatMovementBehaviour;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Conductors stay seated when their seat is pushed into a block.
 */
@Mixin(value = SeatMovementBehaviour.class, remap = false)
public abstract class MixinSeatMovementBehaviour {
    @Inject(method = "visitNewPosition", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;stopRiding()V"), cancellable = true)
    private void railways$keepConductors(MovementContext context, BlockPos pos, CallbackInfo ci,
                                         @Local(name = "toDismount") Entity toDismount) {
        if (toDismount instanceof ConductorEntity)
            ci.cancel();
    }
}
