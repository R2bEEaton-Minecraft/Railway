package com.railwayteam.railways.fabric_mixin;

import com.railwayteam.railways.content.fuel.psi.PortableFuelInterfaceBlockEntity;
import com.zurrtum.create.content.fluids.FluidNetwork;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

/**
 * Keeps a Portable Fuel Interface engaged while a pipe network is still searching for its targets, mirroring
 * what Create does for its own Portable Fluid Interface.
 */
@Mixin(value = FluidNetwork.class, remap = false)
public class MixinFluidNetworkFuelInterface {
    @Shadow
    FluidInventory source;
    @Shadow
    Set<?> frontier;

    @Inject(method = "keepPortableFluidInterfaceEngaged", at = @At("HEAD"))
    private void railways$keepPortableFuelInterfaceEngaged(CallbackInfo ci) {
        if (source instanceof PortableFuelInterfaceBlockEntity.InterfaceFluidHandler handler && !frontier.isEmpty())
            handler.keepAlive();
    }
}
