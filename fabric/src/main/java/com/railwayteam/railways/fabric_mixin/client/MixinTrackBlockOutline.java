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

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.railwayteam.railways.content.custom_tracks.generic_crossing.GenericCrossingBlock;
import com.railwayteam.railways.content.custom_tracks.monorail.CustomTrackBlockOutline;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.client.content.trains.track.TrackBlockOutline;
import com.zurrtum.create.content.trains.track.BezierConnection;
import com.zurrtum.create.content.trains.track.TrackBlock;
import com.zurrtum.create.content.trains.track.TrackMaterial;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Uses monorail / narrow gauge shapes for track hit-testing and outlines, and outlines generic crossings like tracks.
 */
@Mixin(value = TrackBlockOutline.class, remap = false)
public abstract class MixinTrackBlockOutline {
    // material of the track currently being outlined; every outline shape funnels through the two submitShape leaves
    @Unique
    private static @Nullable TrackMaterial railways$outlineMaterial;

    @ModifyExpressionValue(
        method = "pickCurves",
        at = @At(value = "INVOKE", target = "Lcom/zurrtum/create/catnip/math/VoxelShaper;get(Lnet/minecraft/core/Direction;)Lnet/minecraft/world/phys/shapes/VoxelShape;")
    )
    private static VoxelShape railways$pickWithCorrectShape(VoxelShape original, @Local BezierConnection bc) {
        return CustomTrackBlockOutline.convert(original, bc.getMaterial());
    }

    @Inject(method = "drawCurveSelection", at = @At("HEAD"))
    private static void railways$grabCurveMaterial(CallbackInfo ci) {
        TrackBlockOutline.BezierPointSelection selection = TrackBlockOutline.result;
        railways$outlineMaterial = null;
        if (selection == null)
            return;
        BezierConnection bc = selection.blockEntity().getConnections().get(selection.loc().curveTarget());
        if (bc != null)
            railways$outlineMaterial = bc.getMaterial();
    }

    @Inject(method = "drawCurveSelection", at = @At("RETURN"))
    private static void railways$clearCurveMaterial(CallbackInfo ci) {
        railways$outlineMaterial = null;
    }

    @ModifyExpressionValue(
        method = "drawCustomBlockSelection",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getBlock()Lnet/minecraft/world/level/block/Block;", ordinal = 0)
    )
    private static Block railways$grabBlockMaterial(Block original) {
        if (original instanceof TrackBlock trackBlock) {
            railways$outlineMaterial = trackBlock.getMaterial();
        } else if (original instanceof GenericCrossingBlock crossing) {
            railways$outlineMaterial = crossing.getMaterial();
            return AllBlocks.TRACK;
        } else {
            railways$outlineMaterial = null;
        }
        return original;
    }

    @Inject(method = "drawCustomBlockSelection", at = @At("RETURN"))
    private static void railways$clearBlockMaterial(CallbackInfoReturnable<Boolean> cir) {
        railways$outlineMaterial = null;
    }

    @ModifyVariable(
        method = {
            "submitShape(Lnet/minecraft/world/phys/shapes/VoxelShape;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;IF)V",
            "submitShape(Lnet/minecraft/world/phys/shapes/VoxelShape;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;IF)V"
        },
        at = @At("HEAD"),
        argsOnly = true
    )
    private static VoxelShape railways$convertOutlineShape(VoxelShape shape) {
        TrackMaterial material = railways$outlineMaterial;
        return material == null ? shape : CustomTrackBlockOutline.convert(shape, material);
    }
}
