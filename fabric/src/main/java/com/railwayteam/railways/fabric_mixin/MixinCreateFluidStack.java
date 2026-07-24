package com.railwayteam.railways.fabric_mixin;

import com.railwayteam.railways.content.palettes.PalettesColor;
import com.railwayteam.railways.content.palettes.painting.PaintFluid;
import com.railwayteam.railways.registry.CRFluids;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Supplies Create Fly's goggle tooltip with the name held in paint metadata. */
@Mixin(value = FluidStack.class, remap = false)
public abstract class MixinCreateFluidStack {
    @Shadow public abstract Fluid getFluid();
    @Shadow public abstract net.minecraft.core.component.DataComponentMap getComponents();

    @Inject(method = "getName", at = @At("RETURN"), cancellable = true)
    private void railways$nameColoredPaint(CallbackInfoReturnable<Component> cir) {
        if (getFluid() != CRFluids.PAINT.get()) return;

        CustomData customData = getComponents().get(DataComponents.CUSTOM_DATA);
        Component name = customData == null
            ? Component.translatable("fluid.railways.paint")
            : PaintFluid.getColor(customData.copyTag())
                .map(PalettesColor::getPaintName)
                .orElseGet(() -> Component.translatable("fluid.railways.paint"));
        cir.setReturnValue(name);
    }
}
