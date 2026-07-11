package com.railwayteam.railways.mixincompat;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.resources.language.ClientLanguage;

/** Uses a block's existing translation for its 1.21 item-form description ID. */
@Mixin(ClientLanguage.class)
public abstract class ClientLanguageMixin {
	@Shadow private Map<String, String> storage;

	@Inject(method = "getOrDefault", at = @At("HEAD"), cancellable = true)
	private void railways$translateBlockItems(String key, String fallback,
		CallbackInfoReturnable<String> cir) {
		if (!key.startsWith("item.railways."))
			return;
		String translated = storage.get("block." + key.substring("item.".length()));
		if (translated != null)
			cir.setReturnValue(translated);
	}
}
