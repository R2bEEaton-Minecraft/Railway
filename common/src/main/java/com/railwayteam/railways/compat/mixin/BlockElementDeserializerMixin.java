package com.railwayteam.railways.mixincompat;

import com.google.gson.JsonObject;
import com.llamalad7.mixinextras.sugar.Local;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.client.renderer.block.model.BlockElement$Deserializer")
public class BlockElementDeserializerMixin {
    @Inject(method = "getFrom", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/model/BlockElement$Deserializer;getVector3f(Lcom/google/gson/JsonObject;Ljava/lang/String;)Lorg/joml/Vector3f;", shift = At.Shift.BY, by = 2), cancellable = true)
    private void railways$allowLargeFrom(JsonObject json, CallbackInfoReturnable<Vector3f> cir, @Local Vector3f value) {
        cir.setReturnValue(value);
    }

    @Inject(method = "getTo", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/model/BlockElement$Deserializer;getVector3f(Lcom/google/gson/JsonObject;Ljava/lang/String;)Lorg/joml/Vector3f;", shift = At.Shift.BY, by = 2), cancellable = true)
    private void railways$allowLargeTo(JsonObject json, CallbackInfoReturnable<Vector3f> cir, @Local Vector3f value) {
        cir.setReturnValue(value);
    }
}
