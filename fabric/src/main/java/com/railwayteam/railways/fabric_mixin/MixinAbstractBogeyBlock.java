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
import com.railwayteam.railways.registry.CRBogeyStyles;
import com.railwayteam.railways.registry.CRTrackMaterials;
import com.zurrtum.create.content.trains.bogey.AbstractBogeyBlock;
import com.zurrtum.create.content.trains.bogey.BogeyStyle;
import com.zurrtum.create.content.trains.track.ITrackBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * When cycling bogey styles, only offer styles that fit the gauge of the track the bogey sits on.
 */
@Mixin(value = AbstractBogeyBlock.class, remap = false)
public abstract class MixinAbstractBogeyBlock {
    @Unique
    private static final ThreadLocal<Identifier> railways$trackType = new ThreadLocal<>();

    @Unique
    private void railways$storeTrackType(Level level, BlockPos pos, BlockState state) {
        AbstractBogeyBlock<?> self = (AbstractBogeyBlock<?>) (Object) this;
        BlockPos trackPos = self.isUpsideDown(state) ? pos.above() : pos.below();
        if (level.getBlockState(trackPos).getBlock() instanceof ITrackBlock trackBlock)
            railways$trackType.set(CRTrackMaterials.getType(trackBlock.getMaterial()));
        else
            railways$trackType.remove();
    }

    @Inject(method = "getNextStyle(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Lcom/zurrtum/create/content/trains/bogey/BogeyStyle;", at = @At("HEAD"))
    private void railways$storeSupportType(Level level, BlockPos pos, CallbackInfoReturnable<BogeyStyle> cir) {
        railways$storeTrackType(level, pos, level.getBlockState(pos));
    }

    @Inject(method = "getNextStyle(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Lcom/zurrtum/create/content/trains/bogey/BogeyStyle;", at = @At("RETURN"))
    private void railways$clearSupportType(Level level, BlockPos pos, CallbackInfoReturnable<BogeyStyle> cir) {
        railways$trackType.remove();
    }

    @Inject(method = "useItemOn", at = @At(value = "INVOKE", target = "Lcom/zurrtum/create/content/trains/bogey/AbstractBogeyBlock;getNextStyle(Lcom/zurrtum/create/content/trains/bogey/BogeyStyle;)Lcom/zurrtum/create/content/trains/bogey/BogeyStyle;"))
    private void railways$storeSupportTypeUse(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                              InteractionHand hand, BlockHitResult hit,
                                              CallbackInfoReturnable<InteractionResult> cir) {
        railways$storeTrackType(level, pos, state);
    }

    @Inject(method = "useItemOn", at = @At("RETURN"))
    private void railways$clearSupportTypeUse(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                              InteractionHand hand, BlockHitResult hit,
                                              CallbackInfoReturnable<InteractionResult> cir) {
        railways$trackType.remove();
    }

    @WrapOperation(
        method = "getNextStyle(Lcom/zurrtum/create/content/trains/bogey/BogeyStyle;)Lcom/zurrtum/create/content/trains/bogey/BogeyStyle;",
        at = @At(value = "INVOKE", target = "Ljava/util/Map;values()Ljava/util/Collection;")
    )
    private Collection<BogeyStyle> railways$filterStyles(Map<Identifier, BogeyStyle> instance, Operation<Collection<BogeyStyle>> original) {
        Identifier trackType = railways$trackType.get();
        Collection<BogeyStyle> styles = original.call(instance);
        if (trackType == null)
            return styles;
        return styles.stream().filter(style -> CRBogeyStyles.styleFitsTrack(style, trackType)).toList();
    }

    // the current style may have been filtered out, in which case start the cycle from the first fitting style
    @WrapOperation(
        method = "getNextStyle(Lcom/zurrtum/create/content/trains/bogey/BogeyStyle;)Lcom/zurrtum/create/content/trains/bogey/BogeyStyle;",
        at = @At(value = "INVOKE", target = "Lcom/zurrtum/create/catnip/data/Iterate;cycleValue(Ljava/util/List;Ljava/lang/Object;)Ljava/lang/Object;")
    )
    private Object railways$wrapCycleWithFallback(List<Object> list, Object style, Operation<Object> original) {
        try {
            return original.call(list, style);
        } catch (IllegalArgumentException e) {
            return list.getFirst();
        }
    }
}
