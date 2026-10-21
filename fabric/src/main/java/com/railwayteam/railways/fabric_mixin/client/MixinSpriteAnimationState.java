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

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.content.custom_tracks.phantom.PhantomSpriteManager;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Phantom track textures are two-frame animations (visible, invisible). Instead of cycling, pin the frame to whether the
 * player is holding a phantom-revealing item; the atlas redraws the sprite whenever the state is marked dirty.
 */
@Mixin(SpriteContents.AnimationState.class)
public abstract class MixinSpriteAnimationState {
    @Shadow private int frame;
    @Shadow private int subFrame;
    @Shadow private boolean isDirty;
    @Shadow @Final private SpriteContents.AnimatedTexture animationInfo;

    @Unique private Boolean railways$isPhantom;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void railways$pinPhantomFrame(CallbackInfo ci) {
        if (railways$isPhantom == null) {
            AccessorSpriteAnimatedTexture info = (AccessorSpriteAnimatedTexture) (Object) animationInfo;
            SpriteContents owner = info.railways$getOwner();
            Identifier name = owner == null ? null : owner.name();
            railways$isPhantom = name != null
                && name.getNamespace().equals(Railways.MOD_ID)
                && name.getPath().startsWith("block/track/phantom/")
                && info.railways$getFrames().size() > 1;
        }
        if (!railways$isPhantom)
            return;

        int target = PhantomSpriteManager.isVisible() ? 0 : 1;
        isDirty = frame != target;
        frame = target;
        subFrame = 0;
        ci.cancel();
    }
}
