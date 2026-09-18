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

import com.railwayteam.railways.content.palettes.PalettesColor;
import com.railwayteam.railways.registry.CRPalettes;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Block;

public class PaletteGameTests {

    @GameTest(structure = RailwaysGameTestHelper.PLATFORM_8X8)
    public void testBoilerBlocks(GameTestHelper helper) {
        BlockPos p1 = new BlockPos(2, 1, 2);
        BlockPos p2 = new BlockPos(3, 1, 2);

        Block boiler = (Block) CRPalettes.Styles.BOILER.get(PalettesColor.BLACK).get();
        Block wrappedBoiler = (Block) CRPalettes.Styles.BRASS_WRAPPED_BOILER.get(PalettesColor.BLACK).get();

        helper.setBlock(p1, boiler.defaultBlockState());
        helper.setBlock(p2, wrappedBoiler.defaultBlockState());

        helper.assertBlockPresent(boiler, p1);
        helper.assertBlockPresent(wrappedBoiler, p2);
        helper.succeed();
    }

    @GameTest(structure = RailwaysGameTestHelper.PLATFORM_8X8)
    public void testDoors(GameTestHelper helper) {
        BlockPos p1 = new BlockPos(2, 1, 2);
        BlockPos p2 = new BlockPos(4, 1, 2);

        Block slidingDoor = (Block) CRPalettes.Styles.SLIDING_DOOR.get(PalettesColor.BLACK).get();
        Block foldingDoor = (Block) CRPalettes.Styles.FOLDING_DOOR.get(PalettesColor.BLACK).get();

        helper.setBlock(p1, slidingDoor.defaultBlockState());
        helper.setBlock(p2, foldingDoor.defaultBlockState());

        helper.assertBlockPresent(slidingDoor, p1);
        helper.assertBlockPresent(foldingDoor, p2);
        helper.succeed();
    }

    @GameTest(structure = RailwaysGameTestHelper.PLATFORM_8X8)
    public void testHazardStripes(GameTestHelper helper) {
        BlockPos p1 = new BlockPos(2, 1, 2);
        BlockPos p2 = new BlockPos(3, 1, 2);

        Block diagonal = (Block) CRPalettes.Styles.HAZARD_STRIPES_DIAGONAL_BLACK.get(PalettesColor.BLACK).get();
        Block chevron = (Block) CRPalettes.Styles.HAZARD_STRIPES_CHEVRON_BLACK.get(PalettesColor.BLACK).get();

        helper.setBlock(p1, diagonal.defaultBlockState());
        helper.setBlock(p2, chevron.defaultBlockState());

        helper.assertBlockPresent(diagonal, p1);
        helper.assertBlockPresent(chevron, p2);
        helper.succeed();
    }

    @GameTest(structure = RailwaysGameTestHelper.PLATFORM_8X8)
    public void testLadders(GameTestHelper helper) {
        BlockPos p1 = new BlockPos(2, 1, 2);
        BlockPos p2 = new BlockPos(3, 1, 2);

        Block endLadder = (Block) CRPalettes.Styles.END_LADDER.get(PalettesColor.BLACK).get();
        Block rungLadder = (Block) CRPalettes.Styles.RUNG_LADDER.get(PalettesColor.BLACK).get();

        helper.setBlock(p1, endLadder.defaultBlockState());
        helper.setBlock(p2, rungLadder.defaultBlockState());

        helper.assertBlockPresent(endLadder, p1);
        helper.assertBlockPresent(rungLadder, p2);
        helper.succeed();
    }
}
