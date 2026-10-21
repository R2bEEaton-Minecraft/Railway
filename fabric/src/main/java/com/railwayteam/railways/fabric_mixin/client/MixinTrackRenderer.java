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

import com.mojang.blaze3d.vertex.PoseStack;
import com.railwayteam.railways.content.custom_tracks.casing.CasingRenderUtils;
import com.railwayteam.railways.mixin_interfaces.IHasTrackCasing;
import com.railwayteam.railways.registry.CRBlockPartials;
import com.railwayteam.railways.registry.CRTrackMaterials;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.trains.track.RailwaysTrackVisualBridge;
import com.zurrtum.create.client.content.trains.track.TrackRenderer;
import com.zurrtum.create.client.content.trains.track.TrackRenderer.SegmentAngles;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.content.trains.track.BezierConnection;
import com.zurrtum.create.content.trains.track.TrackBlock;
import com.zurrtum.create.content.trains.track.TrackBlockEntity;
import com.zurrtum.create.content.trains.track.TrackShape;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

import static com.railwayteam.railways.content.custom_tracks.casing.CasingRenderUtils.casingPositions;
import static com.railwayteam.railways.registry.CRTrackMaterials.CRTrackType.NARROW_GAUGE;
import static com.railwayteam.railways.registry.CRTrackMaterials.CRTrackType.WIDE_GAUGE;

/**
 * Non-Flywheel fallback for track casings (Flywheel backend off / unsupported). Mirrors {@link MixinTrackVisual}.
 */
@Mixin(TrackRenderer.class)
public class MixinTrackRenderer {
    @Unique
    private static final RenderStateDataKey<List<SuperByteBufferRenderState>> railways$CASINGS =
        RenderStateDataKey.create(() -> "railways:track_casings");

    @Inject(
        method = "extractRenderState(Lcom/zurrtum/create/content/trains/track/TrackBlockEntity;Lcom/zurrtum/create/client/content/trains/track/TrackRenderer$TrackRenderState;FLnet/minecraft/world/phys/Vec3;Lnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V",
        at = @At("RETURN")
    )
    private void railways$extractCasings(TrackBlockEntity be, TrackRenderer.TrackRenderState state, float partialTick,
                                         Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay,
                                         CallbackInfo ci) {
        Level level = be.getLevel();
        if (level == null)
            return;

        List<SuperByteBufferRenderState> casings = new ArrayList<>();
        railways$extractStraightCasing(be, level, casings);
        for (BezierConnection bc : be.getConnections().values())
            railways$extractCurveCasing(be, level, bc, casings);

        if (casings.isEmpty())
            return;

        // TrackRenderer bails out early (leaving the state unbound) for tracks without curves/girders
        state.blockPos = be.getBlockPos();
        state.blockEntityType = be.getType();
        state.setData(railways$CASINGS, casings);
    }

    @Inject(
        method = "submit(Lcom/zurrtum/create/client/content/trains/track/TrackRenderer$TrackRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
        at = @At("TAIL")
    )
    private void railways$submitCasings(TrackRenderer.TrackRenderState state, PoseStack ms, SubmitNodeCollector queue,
                                        CameraRenderState cameraState, CallbackInfo ci) {
        List<SuperByteBufferRenderState> casings = state.getData(railways$CASINGS);
        if (casings == null)
            return;

        for (SuperByteBufferRenderState casing : casings)
            casing.submit(ms, queue);
    }

    @Unique
    private static void railways$extractStraightCasing(TrackBlockEntity be, Level level, List<SuperByteBufferRenderState> casings) {
        Block casingBlock = ((IHasTrackCasing) be).railways$getTrackCasing();
        if (casingBlock == null)
            return;

        BlockState state = be.getBlockState();
        TrackShape shape = state.getValue(TrackBlock.SHAPE);
        if (!CRBlockPartials.TRACK_CASINGS.containsKey(shape))
            return;

        Identifier trackType = null;
        if (state.getBlock() instanceof TrackBlock trackBlock)
            trackType = CRTrackMaterials.getType(trackBlock.getMaterial());

        CRBlockPartials.TrackCasingSpec spec = CRBlockPartials.TRACK_CASINGS.get(shape);
        if (((IHasTrackCasing) be).railways$isAlternate())
            spec = spec.getNonNullAltSpec(trackType);
        else
            spec = spec.getFor(trackType);

        BlockPos pos = be.getBlockPos();
        railways$addStraightCasing(be, level, shape, spec.model, casingBlock, spec.transform, pos, casings);
        for (CRBlockPartials.ModelTransform transform : spec.additionalTransforms) {
            BlockPos lightPos = pos.offset(Mth.floor(transform.x()), Mth.floor(transform.y()), Mth.floor(transform.z()));
            railways$addStraightCasing(be, level, shape, spec.model, casingBlock, transform, lightPos, casings);
        }
    }

    @Unique
    private static void railways$addStraightCasing(TrackBlockEntity be, Level level, TrackShape shape, PartialModel model,
                                                   Block casingBlock, CRBlockPartials.ModelTransform transform,
                                                   BlockPos lightPos, List<SuperByteBufferRenderState> casings) {
        SuperByteBuffer buffer = railways$casingBuffer(be, level, model, casingBlock);
        if (be.isTilted()) {
            double angle = be.tilt.smoothingAngle.get();
            switch (shape) {
                case ZO -> buffer.rotateXDegrees((float) -angle);
                case XO -> buffer.rotateZDegrees((float) angle);
                default -> {}
            }
        }

        casings.add(buffer
            .rotateX(transform.rx())
            .rotateY(transform.ry())
            .rotateZ(transform.rz())
            .translate(transform.x(), transform.y(), transform.z())
            .light(LightCoordsUtil.getLightCoords(level, lightPos))
            .extractRenderState());
    }

    @Unique
    private static void railways$extractCurveCasing(TrackBlockEntity be, Level level, BezierConnection bc,
                                                    List<SuperByteBufferRenderState> casings) {
        if (!bc.isPrimary())
            return;

        Block casingBlock = ((IHasTrackCasing) bc).railways$getTrackCasing();
        if (casingBlock == null)
            return;

        BlockPos bePos = be.getBlockPos();
        int heightDiff = Math.abs(bc.bePositions.get(false).getY() - bc.bePositions.get(true).getY());
        double shiftDown = ((IHasTrackCasing) bc).railways$isAlternate() && heightDiff > 0 ? -0.25 : 0;
        PartialModel casingModel = heightDiff == 0 ? CRBlockPartials.TRACK_CASING_FLAT : CRBlockPartials.TRACK_CASING_FLAT_THICK;

        if (heightDiff / bc.getLength() <= 4 / 30d) {
            for (Vec3 pos : casingPositions(bc)) {
                casings.add(railways$casingBuffer(be, level, casingModel, casingBlock)
                    .translate(0, shiftDown, 0)
                    .translate(pos.x, pos.y, pos.z)
                    .scale(1.001f)
                    .light(LightCoordsUtil.getLightCoords(level, BlockPos.containing(pos).offset(bePos)))
                    .extractRenderState());
            }
            return;
        }

        SegmentAngles segments = bc.getBakedSegments(RailwaysTrackVisualBridge::segmentAngles);
        Identifier trackType = CRTrackMaterials.getType(bc.getMaterial());
        for (int i = 1; i < segments.length; i++) {
            if (i % 2 == 0)
                continue;

            int light = LightCoordsUtil.getLightCoords(level, segments.lightPosition[i].offset(bePos));
            casings.add(railways$casingBuffer(be, level, casingModel, casingBlock)
                .transform(segments.tieTransform[i])
                .translate(0, (i % 4) * 0.001f, 0)
                .translate(0, shiftDown, 0)
                .scale(1.001f)
                .light(light)
                .extractRenderState());

            if (trackType == WIDE_GAUGE) {
                for (boolean first : Iterate.trueAndFalse) {
                    for (boolean inner : Iterate.trueAndFalse) {
                        casings.add(railways$casingBuffer(be, level, casingModel, casingBlock)
                            .transform(segments.railTransforms[i].get(first))
                            .translate(0, (i % 4) * 0.001f, 0)
                            .translate((first ? -(61 / 64d) : -(1 / 32d)) + (inner ? 0 : (first ? 1 : -1)), shiftDown, 0)
                            .light(light)
                            .extractRenderState());
                    }
                }
            } else {
                for (boolean first : Iterate.trueAndFalse) {
                    casings.add(railways$casingBuffer(be, level, casingModel, casingBlock)
                        .transform(segments.railTransforms[i].get(first))
                        .translate(0, (i % 4) * 0.001f, 0)
                        .translate(-0.5 + (trackType == NARROW_GAUGE ? (first ? 0.5 : -0.5) : 0), shiftDown, 0)
                        .light(light)
                        .extractRenderState());
                }
            }
        }
    }

    @Unique
    private static SuperByteBuffer railways$casingBuffer(TrackBlockEntity be, Level level, PartialModel model, Block casingBlock) {
        return CasingRenderUtils.casingBuffer(model, casingBlock)
            .cardinalLighting(level)
            .nudge((int) be.getBlockPos().asLong());
    }
}
