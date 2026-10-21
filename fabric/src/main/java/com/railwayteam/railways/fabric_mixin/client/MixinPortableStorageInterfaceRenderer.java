package com.railwayteam.railways.fabric_mixin.client;

import com.railwayteam.railways.registry.CRBlockPartials;
import com.railwayteam.railways.util.AbstractionUtils;
import com.zurrtum.create.client.content.contraptions.actors.psi.PortableStorageInterfaceRenderer;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Swaps in the Portable Fuel Interface partials; used by the block entity renderer, the flywheel visual and the
 * contraption movement render alike.
 */
@Mixin(value = PortableStorageInterfaceRenderer.class, remap = false)
public class MixinPortableStorageInterfaceRenderer {
    @Inject(method = "getMiddleForState", at = @At("HEAD"), cancellable = true)
    private static void railways$getMiddleForState(BlockState state, boolean lit, CallbackInfoReturnable<PartialModel> cir) {
        if (AbstractionUtils.portableFuelInterfaceBlockHasState(state))
            cir.setReturnValue(lit ? CRBlockPartials.PORTABLE_FUEL_INTERFACE_MIDDLE_POWERED
                : CRBlockPartials.PORTABLE_FUEL_INTERFACE_MIDDLE);
    }

    @Inject(method = "getTopForState", at = @At("HEAD"), cancellable = true)
    private static void railways$getTopForState(BlockState state, CallbackInfoReturnable<PartialModel> cir) {
        if (AbstractionUtils.portableFuelInterfaceBlockHasState(state))
            cir.setReturnValue(CRBlockPartials.PORTABLE_FUEL_INTERFACE_TOP);
    }
}
