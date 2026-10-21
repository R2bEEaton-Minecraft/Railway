package com.railwayteam.railways.util.fabric;

import com.railwayteam.railways.content.fuel.tank.FuelTankBlockEntity;
import com.railwayteam.railways.content.fuel.tank.FuelTankMountedStorageType;
import com.railwayteam.railways.registry.fabric.CRBlocksImpl;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.zurrtum.create.api.contraption.storage.fluid.MountedFluidStorageType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class AbstractionUtilsImpl {
    public static BlockEntry<?> getFluidTankBlockEntry() {
        return CRBlocksImpl.FUEL_TANK;
    }

    public static BlockEntry<?> getPortableFuelInterfaceBlockEntry() {
        return CRBlocksImpl.PORTABLE_FUEL_INTERFACE;
    }

    public static boolean portableFuelInterfaceBlockHasState(BlockState state) {
        return CRBlocksImpl.PORTABLE_FUEL_INTERFACE.has(state);
    }

    public static boolean isInstanceOfFuelTankBlockEntity(BlockEntity blockEntity) {
        return blockEntity instanceof FuelTankBlockEntity;
    }

    public static boolean isInstanceOfFuelTankMountedStorageType(MountedFluidStorageType<?> type) {
        return type instanceof FuelTankMountedStorageType;
    }
}
