package com.railwayteam.railways.content.fuel.fabric;

import com.railwayteam.railways.content.fuel.tank.FuelTankModel;
import com.railwayteam.railways.content.fuel.tank.FuelTankRenderer;
import com.railwayteam.railways.content.fuel.tank.FuelTankTooltipBehaviour;
import com.railwayteam.railways.registry.fabric.CRBlockEntitiesImpl;
import com.railwayteam.railways.registry.fabric.CRBlocksImpl;
import com.zurrtum.create.client.AllBlockEntityBehaviours;
import com.zurrtum.create.client.AllBlockEntityRenders;
import com.zurrtum.create.client.AllModels;
import com.zurrtum.create.client.content.contraptions.actors.psi.PSIVisual;
import com.zurrtum.create.client.content.contraptions.actors.psi.PortableStorageInterfaceRenderer;

/**
 * Client registration for fuel tanks and the Portable Fuel Interface.
 */
public class FuelClientRegistration {
    public static void register() {
        AllBlockEntityRenders.render(CRBlockEntitiesImpl.FUEL_TANK.get(), FuelTankRenderer::new);
        AllBlockEntityRenders.visual(CRBlockEntitiesImpl.PORTABLE_FUEL_INTERFACE.get(),
            PortableStorageInterfaceRenderer::new, PSIVisual::new);
        AllBlockEntityBehaviours.add(CRBlockEntitiesImpl.FUEL_TANK.get(), FuelTankTooltipBehaviour::new);
        AllModels.register(CRBlocksImpl.FUEL_TANK.get(), FuelTankModel::standard);
    }
}
