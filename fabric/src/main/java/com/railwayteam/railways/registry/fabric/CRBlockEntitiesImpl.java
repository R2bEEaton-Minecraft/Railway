package com.railwayteam.railways.registry.fabric;

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.content.fuel.psi.PortableFuelInterfaceBlockEntity;
import com.railwayteam.railways.content.fuel.tank.FuelTankBlockEntity;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.zurrtum.create.foundation.data.CreateRegistrate;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import com.zurrtum.create.infrastructure.transfer.FluidInventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;

public class CRBlockEntitiesImpl {
    private static final CreateRegistrate REGISTRATE = Railways.registrate();

    public static final BlockEntityEntry<FuelTankBlockEntity> FUEL_TANK = REGISTRATE
        .blockEntity("fuel_tank", FuelTankBlockEntity::new)
        .validBlocks(CRBlocksImpl.FUEL_TANK)
        .register();

    public static final BlockEntityEntry<PortableFuelInterfaceBlockEntity> PORTABLE_FUEL_INTERFACE = REGISTRATE
        .blockEntity("portable_fuel_interface", PortableFuelInterfaceBlockEntity::new)
        .validBlocks(CRBlocksImpl.PORTABLE_FUEL_INTERFACE)
        .register();

    public static void init() {
        // Create's own fluid logic (pipes, pumps, spouts, ...) goes through FluidInventoryProvider on the blocks;
        // expose the same inventories through the Fabric Transfer API for everything else.
        FluidStorage.SIDED.registerForBlockEntity((be, side) -> {
            if (be.fluidCapability == null)
                be.refreshCapability();
            FluidInventory inventory = be.fluidCapability;
            return inventory == null ? null : FluidInventoryStorage.of(inventory, side);
        }, FUEL_TANK.get());
        FluidStorage.SIDED.registerForBlockEntity((be, side) -> FluidInventoryStorage.of(be.capability, side),
            PORTABLE_FUEL_INTERFACE.get());
    }
}
