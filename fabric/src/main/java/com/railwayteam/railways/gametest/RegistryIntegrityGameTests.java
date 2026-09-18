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

import com.railwayteam.railways.registry.CRBlockEntities;
import com.railwayteam.railways.registry.CRBlocks;
import com.railwayteam.railways.registry.CRCreativeModeTabs;
import com.railwayteam.railways.registry.CREdgePointTypes;
import com.railwayteam.railways.registry.CREntities;
import com.railwayteam.railways.registry.CRItems;
import com.railwayteam.railways.registry.CRTrackMaterials;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

public class RegistryIntegrityGameTests {

    @GameTest(structure = RailwaysGameTestHelper.PLATFORM_8X8)
    public void testCreativeTabsRegistered(GameTestHelper helper) {
        helper.assertTrue(CRCreativeModeTabs.getBaseTabKey() != null, "Base creative tab key must not be null");
        helper.assertTrue(CRCreativeModeTabs.getTracksTabKey() != null, "Tracks creative tab key must not be null");
        helper.assertTrue(CRCreativeModeTabs.getPalettesTabKey() != null, "Palettes creative tab key must not be null");
        helper.succeed();
    }

    @GameTest(structure = RailwaysGameTestHelper.PLATFORM_8X8)
    public void testEdgePointTypesRegistered(GameTestHelper helper) {
        helper.assertTrue(CREdgePointTypes.COUPLER != null, "Coupler edge point type must be registered");
        helper.assertTrue(CREdgePointTypes.SWITCH != null, "Switch edge point type must be registered");
        helper.assertTrue(CREdgePointTypes.BUFFER != null, "Buffer edge point type must be registered");
        helper.succeed();
    }

    @GameTest(structure = RailwaysGameTestHelper.PLATFORM_8X8)
    public void testEntitiesRegistered(GameTestHelper helper) {
        helper.assertTrue(CREntities.CONDUCTOR.get() != null, "Conductor entity type must be registered");
        helper.assertTrue(CREntities.CART_BLOCK.get() != null, "Cart block entity type must be registered");
        helper.assertTrue(CREntities.CART_JUKEBOX.get() != null, "Cart jukebox entity type must be registered");
        helper.succeed();
    }

    @GameTest(structure = RailwaysGameTestHelper.PLATFORM_8X8)
    public void testBlockEntitiesRegistered(GameTestHelper helper) {
        helper.assertTrue(CRBlockEntities.TRACK_BUFFER.get() != null, "Track buffer block entity must be registered");
        helper.assertTrue(CRBlockEntities.SEMAPHORE.get() != null, "Semaphore block entity must be registered");
        helper.assertTrue(CRBlockEntities.TRACK_COUPLER.get() != null, "Track coupler block entity must be registered");
        helper.assertTrue(CRBlockEntities.ANDESITE_SWITCH.get() != null, "Andesite switch block entity must be registered");
        helper.assertTrue(CRBlockEntities.BRASS_SWITCH.get() != null, "Brass switch block entity must be registered");
        helper.assertTrue(CRBlockEntities.GENERIC_CROSSING.get() != null, "Generic crossing block entity must be registered");
        helper.assertTrue(CRBlockEntities.CONDUCTOR_WHISTLE_FLAG.get() != null, "Conductor whistle flag block entity must be registered");
        helper.succeed();
    }

    @GameTest(structure = RailwaysGameTestHelper.PLATFORM_8X8)
    public void testKeyItemsRegistered(GameTestHelper helper) {
        helper.assertTrue(CRItems.REMOTE_LENS.get() != null, "Remote lens item must be registered");
        helper.assertTrue(CRItems.ITEM_BENCHCART.get() != null, "Workbench cart item must be registered");
        helper.assertTrue(CRItems.ITEM_JUKEBOXCART.get() != null, "Jukebox cart item must be registered");
        helper.succeed();
    }
}
