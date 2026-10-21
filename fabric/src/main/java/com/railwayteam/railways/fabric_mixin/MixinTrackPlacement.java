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

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.railwayteam.railways.content.custom_tracks.generic_crossing.GenericCrossingBlock;
import com.railwayteam.railways.content.custom_tracks.generic_crossing.GenericCrossingBlockEntity;
import com.railwayteam.railways.content.custom_tracks.generic_crossing.TrackShapeLookup;
import com.railwayteam.railways.content.custom_tracks.generic_crossing.TrackShapeLookup.GenericCrossingData;
import com.railwayteam.railways.registry.CRBlocks;
import com.railwayteam.railways.registry.CRTrackMaterials;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.content.trains.track.ITrackBlock;
import com.zurrtum.create.content.trains.track.TrackMaterial;
import com.zurrtum.create.content.trains.track.TrackPlacement;
import com.zurrtum.create.content.trains.track.TrackShape;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.zurrtum.create.content.trains.track.TrackBlock.SHAPE;

/**
 * Gauge-dependent minimum curve lengths, and merging overlapping tracks of different materials into generic crossings.
 */
@Mixin(value = TrackPlacement.class, remap = false)
public abstract class MixinTrackPlacement {
    @ModifyExpressionValue(method = "tryConnect", at = {
        @At(value = "CONSTANT", args = "doubleValue=7.0"),
        @At(value = "CONSTANT", args = "doubleValue=3.25")
    })
    private static double railways$modifiedCurvesForGauges(double value, Level level, Player player, BlockPos pos2,
                                                          BlockState state2, ItemStack stack) {
        Identifier type = CRTrackMaterials.getType(TrackMaterial.fromItem(stack.getItem()));
        if (type == CRTrackMaterials.CRTrackType.WIDE_GAUGE)
            return value * 2;
        if (type == CRTrackMaterials.CRTrackType.NARROW_GAUGE || type == CRTrackMaterials.CRTrackType.UNIVERSAL)
            return value * 0.5;
        return value;
    }

    @Inject(method = "placeTracks", at = @At("HEAD"), cancellable = true)
    private static void railways$preventCurveIntoGenericCrossing(Level level, TrackPlacement.PlacementInfo info,
                                                                BlockState state1, BlockState state2,
                                                                BlockPos targetPos1, BlockPos targetPos2, boolean simulate,
                                                                CallbackInfoReturnable<TrackPlacement.PlacementInfo> cir) {
        if (info.curve == null)
            return;

        for (boolean first : Iterate.trueAndFalse) {
            // not really valid for any junction, but this avoids interfering with Create while preventing #511
            if (level.getBlockState(first ? targetPos1 : targetPos2).getBlock() instanceof GenericCrossingBlock) {
                info.withMessage("junction_start");
                info.valid = false;
                cir.setReturnValue(info);
                return;
            }
        }
    }

    @WrapOperation(
        method = "placeTracks",
        at = @At(value = "INVOKE", target = "Lcom/zurrtum/create/content/trains/track/ITrackBlock;overlay(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/level/block/state/BlockState;")
    )
    private static BlockState railways$placeGenericCrossing(ITrackBlock instance, BlockGetter world, BlockPos pos,
                                                            BlockState stateAtPos, BlockState toPlace,
                                                            Operation<BlockState> original,
                                                            @Share("crossingData") LocalRef<@Nullable GenericCrossingData> crossingData) {
        BlockState result = original.call(instance, world, pos, stateAtPos, toPlace);
        crossingData.set(null);
        // Create couldn't merge the two tracks (usually different materials), so try a generic crossing
        if (result == stateAtPos
            && stateAtPos.hasProperty(SHAPE) && toPlace.hasProperty(SHAPE)
            && stateAtPos.getBlock() instanceof ITrackBlock existingTrack
            && toPlace.getBlock() instanceof ITrackBlock overlayTrack) {
            Pair<TrackShape, Boolean> merged = TrackShapeLookup.getMerged(stateAtPos.getValue(SHAPE), toPlace.getValue(SHAPE));
            if (merged != null) {
                crossingData.set(new GenericCrossingData(merged, existingTrack.getMaterial(), overlayTrack.getMaterial()));
                return CRBlocks.GENERIC_CROSSING.getDefaultState().setValue(GenericCrossingBlock.SHAPE, merged.getFirst());
            }
        }
        return result;
    }

    @WrapOperation(
        method = "placeTracks",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z", ordinal = 0)
    )
    private static boolean railways$initGenericCrossing(Level level, BlockPos pos, BlockState state, int flags,
                                                       Operation<Boolean> original,
                                                       @Share("crossingData") LocalRef<@Nullable GenericCrossingData> crossingData) {
        boolean placed = original.call(level, pos, state, flags);
        GenericCrossingData data = crossingData.get();
        crossingData.set(null);
        if (data != null && level.getBlockEntity(pos) instanceof GenericCrossingBlockEntity genericCrossingBE)
            genericCrossingBE.initFrom(data);
        return placed;
    }
}
