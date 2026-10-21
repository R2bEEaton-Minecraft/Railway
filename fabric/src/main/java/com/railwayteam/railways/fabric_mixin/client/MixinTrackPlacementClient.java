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

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.railwayteam.railways.content.custom_tracks.generic_crossing.GenericCrossingBlock;
import com.zurrtum.create.client.content.trains.track.TrackPlacementClient;
import com.zurrtum.create.content.trains.track.TrackBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;

/**
 * Lets the track placement preview treat generic crossings like regular track blocks.
 */
@Mixin(value = TrackPlacementClient.class, remap = false)
public abstract class MixinTrackPlacementClient {
    @WrapOperation(method = "clientTick", constant = @Constant(classValue = TrackBlock.class))
    private static boolean railways$genericCrossingsAreTracks(Object object, Operation<Boolean> original) {
        return object instanceof GenericCrossingBlock || original.call(object);
    }
}
