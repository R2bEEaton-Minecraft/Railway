package com.railwayteam.railways.fabric_mixin.conductor_possession;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.railwayteam.railways.config.CRConfigs;
import com.railwayteam.railways.content.conductor.ClientHandler;
import com.railwayteam.railways.content.conductor.ConductorEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    public abstract void setPostEffect(Identifier postEffectId);

    @Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
    private void railways$bobView(CameraRenderState cameraRenderState, PoseStack poseStack, CallbackInfo ci) {
        if (!(minecraft.getCameraEntity() instanceof ConductorEntity conductor))
            return;

        float partialTicks = minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        float walk = -conductor.walkAnimation.position(partialTicks);
        float bob = Mth.lerp(partialTicks, conductor.oBob, conductor.bob);
        poseStack.translate(Mth.sin(walk * (float) Math.PI) * bob * 0.5f,
            -Math.abs(Mth.cos(walk * (float) Math.PI) * bob), 0.0);
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.sin(walk * (float) Math.PI) * bob * 3.0f));
        poseStack.mulPose(Axis.XP.rotationDegrees(Math.abs(Mth.cos(walk * (float) Math.PI - 0.2f) * bob) * 5.0f));
        ci.cancel();
    }

    @Inject(method = "checkEntityPostEffect", at = @At("RETURN"))
    private void railways$checkEntityPostEffect(Entity entity, CallbackInfo ci) {
        if (entity instanceof ConductorEntity && CRConfigs.client().useConductorSpyShader.get())
            setPostEffect(Identifier.fromNamespaceAndPath("railways", "scan_pincushion"));
    }

    @Inject(method = "renderItemInHand", at = @At("HEAD"), cancellable = true)
    private void railways$cancelItemInHand(CameraRenderState cameraRenderState, float partialTicks, Matrix4fc matrix4fc, CallbackInfo ci) {
        if (ClientHandler.isPlayerMountedOnCamera())
            ci.cancel();
    }
}
