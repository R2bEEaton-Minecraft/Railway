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

import com.railwayteam.railways.content.coupling.coupler.SecondaryTrackTargetingBehaviour;
import com.railwayteam.railways.registry.CREdgePointTypes;
import com.zurrtum.create.Create;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.content.trains.graph.DimensionPalette;
import com.zurrtum.create.content.trains.graph.EdgePointType;
import com.zurrtum.create.content.trains.signal.TrackEdgePoint;
import com.zurrtum.create.content.trains.track.TrackTargetingBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Couplers target two edge points; migrate the secondary one too, and let couplers / switches share track with
 * other edge points.
 */
@Mixin(value = TrackEdgePoint.class, remap = false)
public abstract class MixinTrackEdgePoint {
    @Shadow public abstract void write(ValueOutput view, DimensionPalette dimensions);

    @Shadow public abstract EdgePointType<?> getType();

    @Inject(method = "invalidateAt", at = @At("RETURN"))
    private void railways$invalidateSecondaryEdgePoint(LevelAccessor level, BlockPos blockEntityPos, CallbackInfo ci) {
        TrackTargetingBehaviour<?> behaviour = BlockEntityBehaviour.get(level, blockEntityPos, SecondaryTrackTargetingBehaviour.TYPE);
        if (behaviour == null)
            return;
        // mirrors TrackEdgePoint#invalidateAt for the primary behaviour
        try (ProblemReporter.ScopedCollector logging = new ProblemReporter.ScopedCollector(() -> "railways:secondary_edge_point", Create.LOGGER)) {
            TagValueOutput view = TagValueOutput.createWithContext(logging, level.registryAccess());
            DimensionPalette dimensions = new DimensionPalette();
            write(view, dimensions);
            view.store("DimensionPalette", DimensionPalette.CODEC, dimensions);
            behaviour.invalidateEdgePoint(view.buildResult());
        }
    }

    @Inject(method = "canCoexistWith", at = @At("RETURN"), cancellable = true)
    private void railways$couplersAndSwitchesCoexist(EdgePointType<?> otherType, boolean front, CallbackInfoReturnable<Boolean> cir) {
        if (getType() == CREdgePointTypes.COUPLER || otherType == CREdgePointTypes.COUPLER
            || getType() == CREdgePointTypes.SWITCH || otherType == CREdgePointTypes.SWITCH)
            cir.setReturnValue(true);
    }
}
