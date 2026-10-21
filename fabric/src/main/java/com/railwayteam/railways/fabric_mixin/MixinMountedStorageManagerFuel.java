package com.railwayteam.railways.fabric_mixin;

import com.google.common.collect.ImmutableMap;
import com.railwayteam.railways.content.fuel.tank.FuelTankMountedStorageType;
import com.railwayteam.railways.mixin_interfaces.IFuelInventory;
import com.zurrtum.create.api.contraption.storage.fluid.MountedFluidStorage;
import com.zurrtum.create.api.contraption.storage.fluid.MountedFluidStorageWrapper;
import com.zurrtum.create.content.contraptions.MountedStorageManager;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Exposes the fuel tanks of a contraption as a separate fluid wrapper, used for locomotive fuel draining and
 * the Portable Fuel Interface. Derived lazily from the full fluid wrapper, so it follows every re-initialization.
 */
@Mixin(value = MountedStorageManager.class, remap = false)
public abstract class MixinMountedStorageManagerFuel implements IFuelInventory {
    @Shadow
    protected MountedFluidStorageWrapper fluids;

    @Unique
    private MountedFluidStorageWrapper railways$fuelSource;
    @Unique
    private MountedFluidStorageWrapper railways$fluidFuels;

    @Override
    public MountedFluidStorageWrapper railways$getFluidFuels() {
        MountedFluidStorageWrapper all = fluids;
        if (all == null)
            return null;
        if (all != railways$fuelSource) {
            ImmutableMap.Builder<BlockPos, MountedFluidStorage> fuels = ImmutableMap.builder();
            all.storages.forEach((pos, storage) -> {
                if (storage.type instanceof FuelTankMountedStorageType)
                    fuels.put(pos, storage);
            });
            ImmutableMap<BlockPos, MountedFluidStorage> fuelMap = fuels.build();
            railways$fluidFuels = fuelMap.isEmpty() ? null : new MountedFluidStorageWrapper(fuelMap);
            railways$fuelSource = all;
        }
        return railways$fluidFuels;
    }
}
