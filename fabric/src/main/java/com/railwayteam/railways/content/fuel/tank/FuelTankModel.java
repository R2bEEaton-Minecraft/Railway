/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2025 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.railwayteam.railways.content.fuel.tank;

import com.railwayteam.railways.registry.CRSpriteShifts;
import com.zurrtum.create.client.content.fluids.tank.FluidTankCTBehaviour;
import com.zurrtum.create.client.infrastructure.model.FluidTankModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel.UnbakedRoot;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Connected-texture model for fuel tanks. Reuses Create's fluid tank CT model (which also culls the
 * faces between connected tanks) with the Railways fuel tank sprite shifts.
 */
public final class FuelTankModel {
    private static FluidTankCTBehaviour behaviour;

    private FuelTankModel() {}

    public static FluidTankModel standard(BlockState state, UnbakedRoot unbaked) {
        if (behaviour == null)
            behaviour = new FluidTankCTBehaviour(CRSpriteShifts.FUEL_TANK, CRSpriteShifts.FUEL_TANK_TOP, CRSpriteShifts.FUEL_TANK_INNER);
        return new FluidTankModel(state, unbaked, behaviour);
    }
}
