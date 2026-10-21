package com.railwayteam.railways.registry.fabric;

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.content.fuel.tank.FuelTankMountedStorageType;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.zurrtum.create.api.registry.CreateRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;

public class CRMountedStorageTypesImpl {
    public static final RegistryEntry<FuelTankMountedStorageType> FUEL_TANK = registerFluid("fuel_tank", new FuelTankMountedStorageType());

    private static <T extends FuelTankMountedStorageType> RegistryEntry<T> registerFluid(String name, T type) {
        Identifier id = Railways.asResource(name);
        return new RegistryEntry<>(id, Registry.register(CreateRegistries.MOUNTED_FLUID_STORAGE_TYPE, id, type));
    }

    public static void init() {
    }
}
