package com.railwayteam.railways.content.fuel.fabric;

import com.railwayteam.railways.content.fuel.LiquidFuelTrainHandler;
import com.zurrtum.create.api.contraption.storage.fluid.MountedFluidStorageWrapper;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

public class LiquidFuelTrainHandlerImpl {
    /** 100 mB (1/10th of a bucket) in Fabric droplets. */
    public static final int FUEL_DRAIN_AMOUNT = 8100;

    private static @Nullable MinecraftServer server;
    private static boolean initialized;

    /**
     * Tracks the running server so fluid fuel values can fall back to the (data-driven) item burn times
     * of the fluid's bucket. Must be called during mod initialization.
     */
    public static void init() {
        if (initialized)
            return;
        initialized = true;
        ServerLifecycleEvents.SERVER_STARTING.register(s -> server = s);
        ServerLifecycleEvents.SERVER_STOPPED.register(s -> server = null);
    }

    public static int getItemBurnTime(Item item) {
        MinecraftServer current = server;
        if (current == null)
            return 0;
        return current.fuelValues().burnDuration(item.getDefaultInstance());
    }

    /**
     * Drains 100 mB of the first valid fuel fluid out of the given fuel tanks.
     *
     * @return the burn time gained, or 0 if no fuel could be drained
     */
    public static int handleFuelDraining(MountedFluidStorageWrapper fuelFluids) {
        for (int slot = 0, size = fuelFluids.size(); slot < size; slot++) {
            FluidStack held = fuelFluids.getStack(slot);
            if (held.isEmpty() || held.getAmount() < FUEL_DRAIN_AMOUNT)
                continue;

            int burnTime = LiquidFuelTrainHandler.handleFuelChecking(held);
            if (burnTime <= 0)
                continue;

            if (fuelFluids.extract(held.copy(), FUEL_DRAIN_AMOUNT) != FUEL_DRAIN_AMOUNT)
                continue;

            return burnTime;
        }

        return 0;
    }
}
