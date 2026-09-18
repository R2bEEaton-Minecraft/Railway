/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2026 The Railways Team
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

package com.railwayteam.railways.gametest;

import com.railwayteam.railways.content.minecarts.MinecartJukebox;
import com.railwayteam.railways.content.minecarts.MinecartWorkbench;
import com.railwayteam.railways.registry.CREntities;
import com.railwayteam.railways.registry.CRItems;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;

public class MinecartGameTests {

    @GameTest(structure = RailwaysGameTestHelper.PLATFORM_8X8)
    public void testWorkbenchCart(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        MinecartWorkbench cart = helper.spawn(CREntities.CART_BLOCK.get(), pos);
        helper.assertEntityPresent(CREntities.CART_BLOCK.get(), pos);
        helper.assertTrue(cart != null, "Workbench cart must not be null");
        helper.assertTrue(CRItems.ITEM_BENCHCART.get() != null, "Workbench cart item must be registered");
        helper.succeed();
    }

    @GameTest(structure = RailwaysGameTestHelper.PLATFORM_8X8)
    public void testJukeboxCart(GameTestHelper helper) {
        BlockPos pos = new BlockPos(4, 1, 4);
        MinecartJukebox cart = helper.spawn(CREntities.CART_JUKEBOX.get(), pos);
        helper.assertEntityPresent(CREntities.CART_JUKEBOX.get(), pos);
        helper.assertTrue(cart != null, "Jukebox cart must not be null");
        helper.assertTrue(CRItems.ITEM_JUKEBOXCART.get() != null, "Jukebox cart item must be registered");
        helper.succeed();
    }
}
