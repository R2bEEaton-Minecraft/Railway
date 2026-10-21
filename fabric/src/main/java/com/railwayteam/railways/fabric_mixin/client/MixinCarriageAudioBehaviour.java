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
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.railwayteam.railways.registry.CRBogeyStyles;
import com.railwayteam.railways.registry.CRSounds;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.client.foundation.entity.behaviour.CarriageAudioBehaviour;
import com.zurrtum.create.content.trains.entity.Carriage;
import com.zurrtum.create.content.trains.entity.CarriageContraptionEntity;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Silences carriages on invisible bogeys and gives handcars their cog rumble instead of steam train sounds.
 */
@Mixin(value = CarriageAudioBehaviour.class, remap = false)
public abstract class MixinCarriageAudioBehaviour {
    @Shadow LerpedFloat seatCrossfade;

    @Unique private boolean railways$isInvisible;
    @Unique private boolean railways$isHandcar;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void railways$skipInvisible(CallbackInfo ci) {
        // the carriage isn't bound yet when the behaviour is created, so classify it every tick
        CarriageContraptionEntity entity = ((CarriageAudioBehaviour) (Object) this).entity;
        Carriage carriage = entity == null ? null : entity.getCarriage();
        if (carriage == null)
            return;
        railways$isInvisible = carriage.bogeys.both(b -> b == null
            || b.getStyle() == CRBogeyStyles.INVISIBLE || b.getStyle() == CRBogeyStyles.INVISIBLE_MONOBOGEY);
        railways$isHandcar = carriage.bogeys.both(b -> b == null || b.getStyle() == CRBogeyStyles.HANDCAR);
        if (railways$isInvisible)
            ci.cancel();
    }

    @WrapOperation(
        method = { "tick", "submitSharedSoundVolume" },
        at = @At(value = "INVOKE", target = "Lcom/zurrtum/create/AllSoundEvents$SoundEntry;getMainEvent()Lnet/minecraft/sounds/SoundEvent;")
    )
    private SoundEvent railways$useCogRumble(AllSoundEvents.SoundEntry instance, Operation<SoundEvent> original) {
        if (railways$isHandcar && instance == AllSoundEvents.TRAIN)
            return CRSounds.HANDCAR_COGS.get();
        return original.call(instance);
    }

    @WrapWithCondition(
        method = "tick",
        at = @At(value = "INVOKE", target = "Lcom/zurrtum/create/AllSoundEvents$SoundEntry;playAt(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/phys/Vec3;FFZ)V")
    )
    private boolean railways$noSteamForHandcars(AllSoundEvents.SoundEntry instance, Level world, Vec3 pos, float volume,
                                                float pitch, boolean fade) {
        return instance != AllSoundEvents.STEAM || !railways$isHandcar;
    }

    @WrapWithCondition(
        method = "tick",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;playLocalSound(DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FFZ)V")
    )
    private boolean railways$noSteamReleaseForHandcars(Level instance, double x, double y, double z, SoundEvent sound,
                                                       SoundSource category, float volume, float pitch, boolean distanceDelay) {
        return !railways$isHandcar;
    }

    @WrapOperation(
        method = "finalizeSharedVolume",
        at = @At(value = "INVOKE", target = "Lcom/zurrtum/create/client/foundation/entity/behaviour/CarriageAudioBehaviour$LoopingSound;setVolume(F)V", ordinal = 0)
    )
    private void railways$handcarNoCrossfade(@Coerce AbstractTickableSoundInstance instance, float volume,
                                             Operation<Void> original) {
        if (railways$isHandcar) {
            float crossfade = seatCrossfade.getValue();
            volume = (1 - (crossfade * .125f)) * volume * 1024;
        }
        original.call(instance, volume);
    }
}
