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

import com.railwayteam.railways.content.buffer.DyeableBlockEntity;
import com.railwayteam.railways.content.buffer.StandardTrackBufferBlock;
import com.railwayteam.railways.content.buffer.TrackBufferBlockEntity;
import com.railwayteam.railways.content.buffer.headstock.CopycatHeadstockBlockEntity;
import com.railwayteam.railways.registry.CRBlocks;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;

public class TrackBufferGameTests {

    @GameTest(structure = RailwaysGameTestHelper.PLATFORM_8X8)
    public void testStandardTrackBuffer(GameTestHelper helper) {
        BlockPos pos = new BlockPos(3, 1, 3);
        helper.setBlock(pos, CRBlocks.TRACK_BUFFER.get().defaultBlockState()
                .setValue(StandardTrackBufferBlock.FACING, Direction.NORTH)
                .setValue(StandardTrackBufferBlock.STYLE, StandardTrackBufferBlock.Style.STANDARD));
        helper.assertBlockPresent(CRBlocks.TRACK_BUFFER.get(), pos);
        helper.assertBlockProperty(pos, StandardTrackBufferBlock.STYLE, StandardTrackBufferBlock.Style.STANDARD);
        RailwaysGameTestHelper.assertBlockEntity(helper, pos, TrackBufferBlockEntity.class);
        helper.succeed();
    }

    @GameTest(structure = RailwaysGameTestHelper.PLATFORM_8X8)
    public void testNarrowTrackBuffer(GameTestHelper helper) {
        BlockPos pos = new BlockPos(3, 1, 3);
        helper.setBlock(pos, CRBlocks.TRACK_BUFFER_NARROW.get().defaultBlockState()
                .setValue(StandardTrackBufferBlock.FACING, Direction.NORTH));
        helper.assertBlockPresent(CRBlocks.TRACK_BUFFER_NARROW.get(), pos);
        RailwaysGameTestHelper.assertBlockEntity(helper, pos, TrackBufferBlockEntity.class);
        helper.succeed();
    }

    @GameTest(structure = RailwaysGameTestHelper.PLATFORM_8X8)
    public void testMonoTrackBuffer(GameTestHelper helper) {
        BlockPos pos = new BlockPos(3, 1, 3);
        helper.setBlock(pos, CRBlocks.TRACK_BUFFER_MONO.get().defaultBlockState()
                .setValue(StandardTrackBufferBlock.FACING, Direction.NORTH));
        helper.assertBlockPresent(CRBlocks.TRACK_BUFFER_MONO.get(), pos);
        RailwaysGameTestHelper.assertBlockEntity(helper, pos, TrackBufferBlockEntity.class);
        helper.succeed();
    }

    @GameTest(structure = RailwaysGameTestHelper.PLATFORM_8X8)
    public void testWideTrackBuffer(GameTestHelper helper) {
        BlockPos pos = new BlockPos(3, 1, 3);
        helper.setBlock(pos, CRBlocks.TRACK_BUFFER_WIDE.get().defaultBlockState()
                .setValue(StandardTrackBufferBlock.FACING, Direction.NORTH));
        helper.assertBlockPresent(CRBlocks.TRACK_BUFFER_WIDE.get(), pos);
        RailwaysGameTestHelper.assertBlockEntity(helper, pos, TrackBufferBlockEntity.class);
        helper.succeed();
    }

    @GameTest(structure = RailwaysGameTestHelper.PLATFORM_8X8)
    public void testBigBufferDyeing(GameTestHelper helper) {
        BlockPos pos = new BlockPos(3, 1, 3);
        helper.setBlock(pos, CRBlocks.BIG_BUFFER.get().defaultBlockState()
                .setValue(StandardTrackBufferBlock.FACING, Direction.NORTH));
        helper.assertBlockPresent(CRBlocks.BIG_BUFFER.get(), pos);
        DyeableBlockEntity be = RailwaysGameTestHelper.assertBlockEntity(helper, pos, DyeableBlockEntity.class);
        helper.assertTrue(be.getColor() == null, "Initial buffer dye color should be null");
        helper.succeed();
    }

    @GameTest(structure = RailwaysGameTestHelper.PLATFORM_8X8)
    public void testHeadstockAndLinkAndPin(GameTestHelper helper) {
        BlockPos headstockPos = new BlockPos(2, 1, 2);
        helper.setBlock(headstockPos, CRBlocks.HEADSTOCK.get().defaultBlockState()
                .setValue(StandardTrackBufferBlock.FACING, Direction.NORTH));
        helper.assertBlockPresent(CRBlocks.HEADSTOCK.get(), headstockPos);

        BlockPos linkPos = new BlockPos(4, 1, 2);
        helper.setBlock(linkPos, CRBlocks.LINK_AND_PIN.get().defaultBlockState()
                .setValue(StandardTrackBufferBlock.FACING, Direction.NORTH));
        helper.assertBlockPresent(CRBlocks.LINK_AND_PIN.get(), linkPos);
        helper.succeed();
    }

    @GameTest(structure = RailwaysGameTestHelper.PLATFORM_8X8)
    public void testCopycatHeadstockDefaultHasNoCustomMaterial(GameTestHelper helper) {
        BlockPos pos = new BlockPos(3, 1, 3);
        helper.setBlock(pos, CRBlocks.COPYCAT_HEADSTOCK.get().defaultBlockState()
                .setValue(StandardTrackBufferBlock.FACING, Direction.NORTH));
        helper.assertBlockPresent(CRBlocks.COPYCAT_HEADSTOCK.get(), pos);

        CopycatHeadstockBlockEntity be = RailwaysGameTestHelper.assertBlockEntity(helper, pos, CopycatHeadstockBlockEntity.class);
        helper.assertFalse(be.hasCustomMaterial(), "Freshly placed copycat headstock should not have a custom material");
        helper.succeed();
    }

    @GameTest(structure = RailwaysGameTestHelper.PLATFORM_8X8)
    public void testCopycatHeadstockCustomTexture(GameTestHelper helper) {
        BlockPos pos = new BlockPos(3, 1, 3);
        helper.setBlock(pos, CRBlocks.COPYCAT_HEADSTOCK.get().defaultBlockState()
                .setValue(StandardTrackBufferBlock.FACING, Direction.NORTH));
        helper.assertBlockPresent(CRBlocks.COPYCAT_HEADSTOCK.get(), pos);

        CopycatHeadstockBlockEntity be = RailwaysGameTestHelper.assertBlockEntity(helper, pos, CopycatHeadstockBlockEntity.class);
        BlockState material = Blocks.BRICKS.defaultBlockState();
        be.setMaterial(material);
        helper.assertTrue(be.hasCustomMaterial(), "Copycat headstock should report a custom material after being set");
        helper.assertTrue(be.getMaterial() == material, "Copycat headstock material should match the applied texture");
        helper.succeed();
    }

    @GameTest(structure = RailwaysGameTestHelper.PLATFORM_8X8)
    public void testCopycatHeadstockCustomTextureCycle(GameTestHelper helper) {
        BlockPos pos = new BlockPos(3, 1, 3);
        helper.setBlock(pos, CRBlocks.COPYCAT_HEADSTOCK.get().defaultBlockState()
                .setValue(StandardTrackBufferBlock.FACING, Direction.NORTH));
        helper.assertBlockPresent(CRBlocks.COPYCAT_HEADSTOCK.get(), pos);

        CopycatHeadstockBlockEntity be = RailwaysGameTestHelper.assertBlockEntity(helper, pos, CopycatHeadstockBlockEntity.class);
        // CopycatBlockEntity#cycleMaterial() only cycles a trapdoor's HALF when it is also open
        be.setMaterial(Blocks.OAK_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.OPEN, true));
        helper.assertTrue(be.hasCustomMaterial(), "Copycat headstock should have a custom material set before cycling");

        BlockState beforeCycle = be.getMaterial();
        boolean cycled = be.cycleMaterial();
        helper.assertTrue(cycled, "Cycling the copycat headstock's material variant should succeed for an open trapdoor material");
        helper.assertTrue(be.getMaterial() != beforeCycle, "Cycling should change the stored material variant");
        helper.succeed();
    }
}
