package com.railwayteam.railways.fabric_mixin;

import com.zurrtum.create.foundation.fluid.FluidTagIngredient;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * Fluid tags contain both source and flowing variants. Create Fly normalizes
 * both to a source fluid for display, so without this pass recipe viewers show
 * each fluid twice.
 */
@Mixin(value = FluidTagIngredient.class, remap = false)
public class MixinFluidTagIngredient {
    @Inject(method = "getMatchingFluids", at = @At("RETURN"), cancellable = true)
    private void railways$deduplicateDisplayFluids(CallbackInfoReturnable<List<Fluid>> cir) {
        cir.setReturnValue(List.copyOf(new LinkedHashSet<>(cir.getReturnValue())));
    }

    @Inject(method = "getMatchingFluidStacks", at = @At("RETURN"), cancellable = true)
    private void railways$deduplicateDisplayStacks(CallbackInfoReturnable<List<FluidStack>> cir) {
        Map<Fluid, FluidStack> unique = new LinkedHashMap<>();
        for (FluidStack stack : cir.getReturnValue()) {
            unique.putIfAbsent(stack.getFluid(), stack);
        }
        cir.setReturnValue(List.copyOf(unique.values()));
    }
}
