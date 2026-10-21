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

import com.railwayteam.railways.mixin_interfaces.IHasTrackCasing;
import com.railwayteam.railways.registry.CRBlockPartials;
import com.railwayteam.railways.registry.CRTrackMaterials;
import com.zurrtum.create.client.content.trains.track.StandardTrackBlockRenderer;
import com.zurrtum.create.client.content.trains.track.TrackBlockRenderState;
import com.zurrtum.create.client.flywheel.lib.transform.Affine;
import com.zurrtum.create.content.trains.station.StationBlockEntity;
import com.zurrtum.create.content.trains.track.BezierConnection;
import com.zurrtum.create.content.trains.track.TrackBlock;
import com.zurrtum.create.content.trains.track.TrackBlockEntity;
import com.zurrtum.create.content.trains.track.TrackShape;
import com.zurrtum.create.content.trains.track.TrackTargetingBehaviour.RenderedTrackOverlayType;
import com.zurrtum.create.infrastructure.component.BezierTrackPointLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Lifts Create's track overlays (signals, stations, observers, train assembly) onto monorails and track casings.
 */
@Mixin(value = StandardTrackBlockRenderer.class, remap = false)
public abstract class MixinStandardTrackBlockRenderer {
    @Inject(
        method = "prepareTrackOverlay",
        at = @At(value = "INVOKE", target = "Lcom/zurrtum/create/client/flywheel/lib/transform/Affine;translate(FFF)Lcom/zurrtum/create/client/flywheel/lib/transform/Translate;", ordinal = 0, shift = At.Shift.AFTER)
    )
    private <Self extends Affine<Self>> void railways$bezierShiftTrackOverlay(Affine<Self> affine, BlockGetter world, BlockPos pos,
                                                                              BlockState state, BezierTrackPointLocation bezierPoint,
                                                                              AxisDirection direction, RenderedTrackOverlayType type,
                                                                              CallbackInfo ci) {
        if (bezierPoint == null || !(world.getBlockEntity(pos) instanceof TrackBlockEntity trackBE))
            return;
        BezierConnection bc = trackBE.getConnections().get(bezierPoint.curveTarget());
        if (bc == null)
            return;

        if (CRTrackMaterials.CRTrackType.MONORAIL.equals(CRTrackMaterials.getType(bc.getMaterial()))) {
            affine.translate(0, 14 / 16f, 0);
            return;
        }
        IHasTrackCasing casingBc = (IHasTrackCasing) bc;
        // don't shift up if the curve is a slope and the casing is under the track, rather than in it
        if (casingBc.railways$getTrackCasing() != null) {
            if (bc.bePositions.getFirst().getY() == bc.bePositions.getSecond().getY()) {
                affine.translate(0, 1 / 16f, 0);
            } else if (!casingBc.railways$isAlternate()) {
                affine.translate(0, 4 / 16f, 0);
            }
        }
    }

    @Inject(
        method = "prepareTrackOverlay",
        at = @At(value = "INVOKE", target = "Lcom/zurrtum/create/client/content/trains/track/TrackRenderer;getModelAngles(Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;")
    )
    private <Self extends Affine<Self>> void railways$blockShiftTrackOverlay(Affine<Self> affine, BlockGetter world, BlockPos pos,
                                                                             BlockState state, BezierTrackPointLocation bezierPoint,
                                                                             AxisDirection direction, RenderedTrackOverlayType type,
                                                                             CallbackInfo ci) {
        if (bezierPoint != null || !(state.getBlock() instanceof TrackBlock trackBlock))
            return;
        if (CRTrackMaterials.CRTrackType.MONORAIL.equals(CRTrackMaterials.getType(trackBlock.getMaterial()))) {
            affine.translate(0, 14 / 16f, 0);
            return;
        }
        Vec3 shift = railways$casingShift(world, pos, state);
        if (shift != null)
            affine.translate((float) shift.x, (float) shift.y, (float) shift.z);
    }

    @Inject(method = "getAssemblyRenderState", at = @At("RETURN"))
    private void railways$shiftAssemblyOverlayOnEncasedTracks(StationBlockEntity be, Vec3 offset, Level level, BlockPos pos,
                                                              BlockState trackState,
                                                              CallbackInfoReturnable<TrackBlockRenderState> cir) {
        if (!(cir.getReturnValue() instanceof StandardTrackBlockRenderer.StandardTrackAssemblyRenderState assembly))
            return;
        Vec3 shift = railways$casingShift(level, pos, trackState);
        if (shift != null)
            assembly.offset = assembly.offset.add(shift);
    }

    @Unique
    private static @Nullable Vec3 railways$casingShift(BlockGetter world, BlockPos pos, BlockState state) {
        if (!(state.getBlock() instanceof TrackBlock trackBlock) || !(world.getBlockEntity(pos) instanceof TrackBlockEntity trackBE))
            return null;
        IHasTrackCasing casing = (IHasTrackCasing) trackBE;
        if (casing.railways$getTrackCasing() == null)
            return null;
        TrackShape shape = state.getValue(TrackBlock.SHAPE);
        CRBlockPartials.TrackCasingSpec spec = CRBlockPartials.TRACK_CASINGS.get(shape);
        if (spec == null)
            return null;
        Identifier trackType = CRTrackMaterials.getType(trackBlock.getMaterial());
        return new Vec3(
            spec.getXShift(trackType),
            (spec.getTopSurfacePixelHeight(trackType, casing.railways$isAlternate()) - 2) / 16f,
            spec.getZShift(trackType)
        );
    }
}
