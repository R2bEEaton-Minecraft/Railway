package com.railwayteam.railways.fabric_mixin;

import com.railwayteam.railways.content.fuel.LiquidFuelTrainHandler;
import com.railwayteam.railways.mixin_interfaces.IFuelInventory;
import com.zurrtum.create.api.contraption.storage.fluid.MountedFluidStorageWrapper;
import com.zurrtum.create.content.trains.entity.Carriage;
import com.zurrtum.create.content.trains.entity.Train;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Lets locomotives burn liquid fuel out of fuel tanks, in addition to Create's item fuel.
 */
@Mixin(value = Train.class, remap = false)
public class MixinTrainLiquidFuel {
    @Shadow
    public List<Carriage> carriages;
    @Shadow
    public double speed;
    @Shadow
    public int fuelTicks;

    @Inject(method = "burnFuel", at = @At(value = "INVOKE", target = "Ljava/util/List;size()I", shift = At.Shift.AFTER))
    private void railways$fluidFuelsSystem(Level world, CallbackInfo ci) {
        boolean iterateFromBack = speed < 0;
        int carriageCount = carriages.size();

        for (int index = 0; index < carriageCount; index++) {
            int i = iterateFromBack ? carriageCount - 1 - index : index;
            Carriage carriage = carriages.get(i);
            if (!(carriage.storage instanceof IFuelInventory fuelInventory))
                continue;
            MountedFluidStorageWrapper fuelFluids = fuelInventory.railways$getFluidFuels();
            if (fuelFluids == null)
                continue;

            fuelTicks += LiquidFuelTrainHandler.handleFuelDraining(fuelFluids);
        }
    }
}
