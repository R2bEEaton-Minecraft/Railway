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

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public class RailwaysGameTestHelper {
    public static final String PLATFORM_8X8 = "railways:gametest_platform_8x8";
    public static final String PLATFORM_12X12 = "railways:gametest_platform_12x12";
    public static final String PLATFORM_16X16 = "railways:gametest_platform_16x16";
    public static final String EMPTY = "fabric-gametest-api-v1:empty";

    public static void setupFloor(GameTestHelper helper, int width, int depth) {
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                Block block = (x == 0 || x == width - 1 || z == 0 || z == depth - 1)
                        ? Blocks.SMOOTH_STONE
                        : Blocks.POLISHED_ANDESITE;
                helper.setBlock(new BlockPos(x, 0, z), block.defaultBlockState());
            }
        }
    }

    public static <T extends BlockEntity> T assertBlockEntity(GameTestHelper helper, BlockPos pos, Class<T> clazz) {
        T blockEntity = helper.getBlockEntity(pos, clazz);
        if (blockEntity == null) {
            helper.fail("Expected block entity of type " + clazz.getSimpleName() + " at " + pos);
        }
        return blockEntity;
    }

    public static <T extends Comparable<T>> void assertProperty(GameTestHelper helper, BlockPos pos, Property<T> prop, T expected) {
        helper.assertBlockProperty(pos, prop, expected);
    }
}
