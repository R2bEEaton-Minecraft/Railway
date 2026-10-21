package com.railwayteam.railways.registry.fabric;

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.content.fuel.fabric.LiquidFuelTrainHandlerImpl;
import com.railwayteam.railways.content.fuel.psi.PortableFuelInterfaceBlock;
import com.railwayteam.railways.content.fuel.tank.FuelTankBlock;
import com.railwayteam.railways.content.fuel.tank.FuelTankItem;
import com.railwayteam.railways.content.fuel.tank.FuelTankMovementBehavior;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.zurrtum.create.AllMountedStorageTypes;
import com.zurrtum.create.AllMovementBehaviours;
import com.zurrtum.create.foundation.data.CreateRegistrate;
import com.zurrtum.create.foundation.data.SharedProperties;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import static com.railwayteam.railways.util.CreateBehaviourCompat.movementBehaviour;
import static com.zurrtum.create.foundation.data.ModelGen.customItemModel;
import static com.zurrtum.create.foundation.data.TagGen.axeOrPickaxe;
import static com.zurrtum.create.foundation.data.TagGen.pickaxeOnly;

public class CRBlocksImpl {
    private static final CreateRegistrate REGISTRATE = Railways.registrate();

    public static final BlockEntry<FuelTankBlock> FUEL_TANK = REGISTRATE.block("fuel_tank", FuelTankBlock::new)
        .initialProperties(SharedProperties::copperMetal)
        .properties(BlockBehaviour.Properties::noOcclusion)
        .properties(p -> p.isRedstoneConductor((state, level, pos) -> true))
        .transform(pickaxeOnly())
        .onRegister(block -> AllMountedStorageTypes.register(CRMountedStorageTypesImpl.FUEL_TANK.get(), block))
        .onRegister(movementBehaviour(new FuelTankMovementBehavior()))
        .item(FuelTankItem::new)
        .build()
        .register();

    public static final BlockEntry<PortableFuelInterfaceBlock> PORTABLE_FUEL_INTERFACE = REGISTRATE.block("portable_fuel_interface", PortableFuelInterfaceBlock::new)
        .initialProperties(SharedProperties::copperMetal)
        .properties(p -> p.mapColor(MapColor.TERRACOTTA_LIGHT_GRAY))
        .transform(axeOrPickaxe())
        // shares Create's PSI movement behaviour instance so its contraption render (attached client-side) applies too
        .onRegister(movementBehaviour(AllMovementBehaviours.PORTABLE_FLUID_INTERFACE))
        .item()
        .transform(customItemModel())
        .build()
        .register();

    public static void init() {
        LiquidFuelTrainHandlerImpl.init();
    }
}
