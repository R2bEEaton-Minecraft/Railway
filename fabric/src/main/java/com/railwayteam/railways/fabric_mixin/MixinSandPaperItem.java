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

import com.railwayteam.railways.content.palettes.PalettesColor;
import com.railwayteam.railways.content.palettes.painting.RepaintingTarget;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.content.equipment.sandPaper.SandPaperItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LevelEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Sanding painted locometal strips it back to bare (netherite) locometal.
 */
@Mixin(value = SandPaperItem.class, remap = false)
public abstract class MixinSandPaperItem {
    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    private void railways$stripLocometal(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        RepaintingTarget target = RepaintingTarget.get(level, pos, level.getBlockState(pos));
        if (target == null || target.getColor().isNetherite())
            return;

        Player player = context.getPlayer();
        target.repaint(PalettesColor.NETHERITE);
        AllSoundEvents.SANDING_LONG.play(level, player, pos, 1, 1 + (level.getRandom().nextFloat() * 0.5f - 1f) / 5f);
        level.levelEvent(player, LevelEvent.PARTICLES_SCRAPE, pos, 0);
        if (player != null)
            context.getItemInHand().hurtAndBreak(1, player, context.getHand());
        cir.setReturnValue(InteractionResult.SUCCESS);
    }
}
