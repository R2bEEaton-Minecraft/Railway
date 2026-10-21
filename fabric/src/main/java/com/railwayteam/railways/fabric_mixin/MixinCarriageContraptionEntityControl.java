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

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.railwayteam.railways.config.CRConfigs;
import com.railwayteam.railways.content.conductor.ConductorEntity;
import com.railwayteam.railways.content.conductor.IConductorHoldingFakePlayer;
import com.railwayteam.railways.mixin_interfaces.IHandcarTrain;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.content.trains.entity.Carriage;
import com.zurrtum.create.content.trains.entity.CarriageContraptionEntity;
import com.zurrtum.create.content.trains.entity.Train;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

/**
 * Train control tweaks: conductors drive trains through their fake player (no reach check, their own throttle),
 * handcars cost hunger, and the optional client-side derail skip.
 * Kept separate from {@link MixinCarriageContraptionEntity} so the two can evolve independently.
 */
@Mixin(value = CarriageContraptionEntity.class, remap = false)
public abstract class MixinCarriageContraptionEntityControl {
    @Shadow private Carriage carriage;

    @WrapOperation(method = "control", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;closerThan(Lnet/minecraft/core/Position;D)Z"))
    private boolean railways$conductorsAlwaysInReach(Vec3 instance, Position pos, double distance, Operation<Boolean> original,
                                                     BlockPos controlsLocalPos, Collection<Integer> heldControls, Player player) {
        if (player.getGameProfile() == ConductorEntity.FAKE_PLAYER_PROFILE)
            return true;
        return original.call(instance, pos, distance);
    }

    @WrapOperation(method = "control", at = @At(value = "FIELD", target = "Lcom/zurrtum/create/content/trains/entity/Train;throttle:D", opcode = Opcodes.GETFIELD))
    private double railways$conductorSpeedControl(Train instance, Operation<Double> original,
                                                  BlockPos controlsLocalPos, Collection<Integer> heldControls, Player player) {
        if (player instanceof IConductorHoldingFakePlayer conductorHolder && conductorHolder.getConductor() != null)
            return Math.abs(conductorHolder.getConductor().getThrottle()) / 15.0d;
        return original.call(instance);
    }

    @Inject(method = "control", at = @At("TAIL"))
    private void railways$handcarHungerDepletion(BlockPos controlsLocalPos, Collection<Integer> heldControls, Player player,
                                                 CallbackInfoReturnable<Boolean> cir) {
        if (carriage == null || !((IHandcarTrain) carriage.train).railways$isHandcar())
            return;
        if (player.getItemInHand(InteractionHand.MAIN_HAND).is(AllItems.EXTENDO_GRIP))
            return;
        player.causeFoodExhaustion((float) Math.abs(carriage.train.speed) * CRConfigs.server().handcarHungerMultiplier.getF());
    }

    @Inject(
        method = "updateTrackGraph",
        at = @At(value = "FIELD", target = "Lcom/zurrtum/create/content/trains/entity/Train;graph:Lcom/zurrtum/create/content/trains/graph/TrackGraph;", opcode = Opcodes.PUTFIELD, ordinal = 0),
        cancellable = true
    )
    private void railways$skipClientDerailing(CallbackInfo ci) {
        CarriageContraptionEntity self = (CarriageContraptionEntity) (Object) this;
        if (self.level().isClientSide() && CRConfigs.client().skipClientDerailing.get())
            ci.cancel();
    }
}
