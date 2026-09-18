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

import com.railwayteam.railways.registry.CRBlocks;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;

public class LocomotiveGameTests {

    @GameTest(structure = RailwaysGameTestHelper.PLATFORM_8X8)
    public void testHandcarPlacement(GameTestHelper helper) {
        BlockPos pos = new BlockPos(3, 1, 3);
        helper.setBlock(pos, CRBlocks.HANDCAR.get().defaultBlockState());
        helper.assertBlockPresent(CRBlocks.HANDCAR.get(), pos);
        helper.succeed();
    }

    @GameTest(structure = RailwaysGameTestHelper.PLATFORM_8X8)
    public void testSmokestackStyles(GameTestHelper helper) {
        BlockPos p1 = new BlockPos(1, 1, 1);
        BlockPos p2 = new BlockPos(2, 1, 1);
        BlockPos p3 = new BlockPos(3, 1, 1);
        BlockPos p4 = new BlockPos(4, 1, 1);
        BlockPos p5 = new BlockPos(5, 1, 1);
        BlockPos p6 = new BlockPos(6, 1, 1);

        helper.setBlock(p1, CRBlocks.CABOOSESTYLE_STACK.get().defaultBlockState());
        helper.setBlock(p2, CRBlocks.COALBURNER_STACKS.getFirst().get().defaultBlockState());
        helper.setBlock(p3, CRBlocks.OILBURNER_STACKS.getFirst().get().defaultBlockState());
        helper.setBlock(p4, CRBlocks.STREAMLINED_STACKS.getFirst().get().defaultBlockState());
        helper.setBlock(p5, CRBlocks.WOODBURNER_STACKS.getFirst().get().defaultBlockState());
        helper.setBlock(p6, CRBlocks.DIESEL_STACK.get().defaultBlockState());

        helper.assertBlockPresent(CRBlocks.CABOOSESTYLE_STACK.get(), p1);
        helper.assertBlockPresent(CRBlocks.COALBURNER_STACKS.getFirst().get(), p2);
        helper.assertBlockPresent(CRBlocks.OILBURNER_STACKS.getFirst().get(), p3);
        helper.assertBlockPresent(CRBlocks.STREAMLINED_STACKS.getFirst().get(), p4);
        helper.assertBlockPresent(CRBlocks.WOODBURNER_STACKS.getFirst().get(), p5);
        helper.assertBlockPresent(CRBlocks.DIESEL_STACK.get(), p6);
        helper.succeed();
    }

    @GameTest(structure = RailwaysGameTestHelper.PLATFORM_8X8)
    public void testSmokestackExtender(GameTestHelper helper) {
        BlockPos basePos = new BlockPos(3, 1, 3);
        BlockPos extPos = new BlockPos(3, 2, 3);

        helper.setBlock(basePos, CRBlocks.COALBURNER_STACKS.getFirst().get().defaultBlockState());
        helper.setBlock(extPos, CRBlocks.COALBURNER_STACKS.getSecond().get().defaultBlockState());

        helper.assertBlockPresent(CRBlocks.COALBURNER_STACKS.getFirst().get(), basePos);
        helper.assertBlockPresent(CRBlocks.COALBURNER_STACKS.getSecond().get(), extPos);
        helper.succeed();
    }

    @GameTest(structure = RailwaysGameTestHelper.PLATFORM_8X8)
    public void testCustomBogeyBlocks(GameTestHelper helper) {
        BlockPos p1 = new BlockPos(1, 1, 1);
        BlockPos p2 = new BlockPos(2, 1, 1);
        BlockPos p3 = new BlockPos(3, 1, 1);
        BlockPos p4 = new BlockPos(4, 1, 1);
        BlockPos p5 = new BlockPos(5, 1, 1);
        BlockPos p6 = new BlockPos(6, 1, 1);

        helper.setBlock(p1, CRBlocks.SINGLEAXLE_BOGEY.get().defaultBlockState());
        helper.setBlock(p2, CRBlocks.DOUBLEAXLE_BOGEY.get().defaultBlockState());
        helper.setBlock(p3, CRBlocks.MONO_BOGEY.get().defaultBlockState());
        helper.setBlock(p4, CRBlocks.INVISIBLE_BOGEY.get().defaultBlockState());
        helper.setBlock(p5, CRBlocks.WIDE_DOUBLEAXLE_BOGEY.get().defaultBlockState());
        helper.setBlock(p6, CRBlocks.NARROW_SMALL_BOGEY.get().defaultBlockState());

        helper.assertBlockPresent(CRBlocks.SINGLEAXLE_BOGEY.get(), p1);
        helper.assertBlockPresent(CRBlocks.DOUBLEAXLE_BOGEY.get(), p2);
        helper.assertBlockPresent(CRBlocks.MONO_BOGEY.get(), p3);
        helper.assertBlockPresent(CRBlocks.INVISIBLE_BOGEY.get(), p4);
        helper.assertBlockPresent(CRBlocks.WIDE_DOUBLEAXLE_BOGEY.get(), p5);
        helper.assertBlockPresent(CRBlocks.NARROW_SMALL_BOGEY.get(), p6);
        helper.succeed();
    }
}
