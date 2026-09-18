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

import com.railwayteam.railways.content.semaphore.SemaphoreBlock;
import com.railwayteam.railways.content.semaphore.SemaphoreBlockEntity;
import com.railwayteam.railways.registry.CRBlocks;
import com.zurrtum.create.AllBlocks;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;

public class SemaphoreGameTests {

    @GameTest(structure = RailwaysGameTestHelper.PLATFORM_8X8)
    public void testSemaphorePlacement(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        helper.setBlock(pos, CRBlocks.SEMAPHORE.get().defaultBlockState()
                .setValue(SemaphoreBlock.FACING, Direction.NORTH)
                .setValue(SemaphoreBlock.FLIPPED, false)
                .setValue(SemaphoreBlock.FULL, false)
                .setValue(SemaphoreBlock.UPSIDE_DOWN, false));
        helper.assertBlockPresent(CRBlocks.SEMAPHORE.get(), pos);
        RailwaysGameTestHelper.assertBlockEntity(helper, pos, SemaphoreBlockEntity.class);
        helper.succeed();
    }

    @GameTest(structure = RailwaysGameTestHelper.PLATFORM_8X8)
    public void testSemaphoreFlippedState(GameTestHelper helper) {
        BlockPos pos = new BlockPos(3, 1, 3);
        helper.setBlock(pos, CRBlocks.SEMAPHORE.get().defaultBlockState()
                .setValue(SemaphoreBlock.FACING, Direction.SOUTH)
                .setValue(SemaphoreBlock.FLIPPED, true));
        helper.assertBlockPresent(CRBlocks.SEMAPHORE.get(), pos);
        helper.assertBlockProperty(pos, SemaphoreBlock.FLIPPED, true);
        helper.succeed();
    }

    @GameTest(structure = RailwaysGameTestHelper.PLATFORM_8X8)
    public void testSemaphoreFullAndUpsideDown(GameTestHelper helper) {
        BlockPos pos = new BlockPos(4, 1, 4);
        helper.setBlock(pos, CRBlocks.SEMAPHORE.get().defaultBlockState()
                .setValue(SemaphoreBlock.FACING, Direction.EAST)
                .setValue(SemaphoreBlock.FULL, true)
                .setValue(SemaphoreBlock.UPSIDE_DOWN, true));
        helper.assertBlockPresent(CRBlocks.SEMAPHORE.get(), pos);
        helper.assertBlockProperty(pos, SemaphoreBlock.FULL, true);
        helper.assertBlockProperty(pos, SemaphoreBlock.UPSIDE_DOWN, true);
        helper.succeed();
    }

    @GameTest(structure = RailwaysGameTestHelper.PLATFORM_8X8)
    public void testSemaphoreSignalConnection(GameTestHelper helper) {
        BlockPos signalPos = new BlockPos(2, 1, 2);
        BlockPos girderPos = new BlockPos(2, 2, 2);
        BlockPos semaphorePos = new BlockPos(2, 3, 2);

        helper.setBlock(signalPos, AllBlocks.TRACK_SIGNAL.defaultBlockState());
        helper.setBlock(girderPos, AllBlocks.METAL_GIRDER.defaultBlockState());
        helper.setBlock(semaphorePos, CRBlocks.SEMAPHORE.get().defaultBlockState()
                .setValue(SemaphoreBlock.FACING, Direction.NORTH));

        helper.assertBlockPresent(CRBlocks.SEMAPHORE.get(), semaphorePos);
        SemaphoreBlockEntity semaphoreBe = RailwaysGameTestHelper.assertBlockEntity(helper, semaphorePos, SemaphoreBlockEntity.class);
        semaphoreBe.lazyTick();

        helper.assertTrue(semaphoreBe.isValid, "Semaphore should successfully link to SignalBlockEntity down the mast");
        helper.succeed();
    }
}
