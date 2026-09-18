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

import com.railwayteam.railways.content.switches.TrackSwitchBlock;
import com.railwayteam.railways.content.switches.TrackSwitchBlockEntity;
import com.railwayteam.railways.registry.CRBlocks;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;

public class TrackSwitchGameTests {

    @GameTest(structure = RailwaysGameTestHelper.PLATFORM_8X8)
    public void testManualSwitchPlacement(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        helper.setBlock(pos, CRBlocks.ANDESITE_SWITCH.get().defaultBlockState()
                .setValue(TrackSwitchBlock.FACING, Direction.NORTH)
                .setValue(TrackSwitchBlock.LOCKED, false));
        helper.assertBlockPresent(CRBlocks.ANDESITE_SWITCH.get(), pos);
        helper.assertBlockProperty(pos, TrackSwitchBlock.LOCKED, false);

        TrackSwitchBlockEntity be = RailwaysGameTestHelper.assertBlockEntity(helper, pos, TrackSwitchBlockEntity.class);
        helper.assertFalse(be.isAutomatic(), "Andesite switch should be manual");
        helper.succeed();
    }

    @GameTest(structure = RailwaysGameTestHelper.PLATFORM_8X8)
    public void testAutomaticSwitchPlacement(GameTestHelper helper) {
        BlockPos pos = new BlockPos(3, 1, 3);
        helper.setBlock(pos, CRBlocks.BRASS_SWITCH.get().defaultBlockState()
                .setValue(TrackSwitchBlock.FACING, Direction.NORTH)
                .setValue(TrackSwitchBlock.LOCKED, false));
        helper.assertBlockPresent(CRBlocks.BRASS_SWITCH.get(), pos);

        TrackSwitchBlockEntity be = RailwaysGameTestHelper.assertBlockEntity(helper, pos, TrackSwitchBlockEntity.class);
        helper.assertTrue(be.isAutomatic(), "Brass switch should be automatic");
        helper.succeed();
    }

    @GameTest(structure = RailwaysGameTestHelper.PLATFORM_8X8)
    public void testSwitchLockingState(GameTestHelper helper) {
        BlockPos pos = new BlockPos(4, 1, 4);
        helper.setBlock(pos, CRBlocks.ANDESITE_SWITCH.get().defaultBlockState()
                .setValue(TrackSwitchBlock.FACING, Direction.NORTH)
                .setValue(TrackSwitchBlock.LOCKED, true));
        helper.assertBlockPresent(CRBlocks.ANDESITE_SWITCH.get(), pos);
        helper.assertBlockProperty(pos, TrackSwitchBlock.LOCKED, true);
        helper.succeed();
    }
}
