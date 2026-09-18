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

import com.railwayteam.railways.content.custom_tracks.generic_crossing.GenericCrossingBlockEntity;
import com.railwayteam.railways.registry.CRBlocks;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import com.zurrtum.create.AllTags;
import com.zurrtum.create.content.trains.track.TrackBlock;
import com.zurrtum.create.content.trains.track.TrackMaterial;
import com.zurrtum.create.content.trains.track.TrackShape;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

public class TrackGameTests {

    @GameTest(structure = RailwaysGameTestHelper.PLATFORM_8X8)
    public void testOakTrackPlacement(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        helper.setBlock(pos, CRBlocks.OAK_TRACK.get().defaultBlockState());
        helper.assertBlockPresent(CRBlocks.OAK_TRACK.get(), pos);
        helper.assertBlockProperty(pos, TrackBlock.SHAPE, TrackShape.ZO);
        helper.assertTrue(AllTags.AllBlockTags.TRACKS.matches(helper.getBlockState(pos)), "Oak track must match TRACKS tag");
        helper.succeed();
    }

    @GameTest(structure = RailwaysGameTestHelper.PLATFORM_8X8)
    public void testMonorailTrackPlacement(GameTestHelper helper) {
        BlockPos pos = new BlockPos(3, 1, 3);
        helper.setBlock(pos, CRBlocks.MONORAIL_TRACK.get().defaultBlockState());
        helper.assertBlockPresent(CRBlocks.MONORAIL_TRACK.get(), pos);
        helper.assertBlockProperty(pos, TrackBlock.SHAPE, TrackShape.ZO);
        helper.assertTrue(AllTags.AllBlockTags.TRACKS.matches(helper.getBlockState(pos)), "Monorail track must match TRACKS tag");
        helper.succeed();
    }

    @GameTest(structure = RailwaysGameTestHelper.PLATFORM_8X8)
    public void testPhantomTrackPlacement(GameTestHelper helper) {
        BlockPos pos = new BlockPos(4, 1, 4);
        helper.setBlock(pos, CRBlocks.PHANTOM_TRACK.get().defaultBlockState());
        helper.assertBlockPresent(CRBlocks.PHANTOM_TRACK.get(), pos);
        helper.assertTrue(AllTags.AllBlockTags.TRACKS.matches(helper.getBlockState(pos)), "Phantom track must match TRACKS tag");
        helper.succeed();
    }

    @GameTest(structure = RailwaysGameTestHelper.PLATFORM_8X8)
    public void testGenericCrossingPlacement(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        helper.setBlock(pos, CRBlocks.GENERIC_CROSSING.get().defaultBlockState());
        helper.assertBlockPresent(CRBlocks.GENERIC_CROSSING.get(), pos);
        RailwaysGameTestHelper.assertBlockEntity(helper, pos, GenericCrossingBlockEntity.class);
        helper.succeed();
    }

    @GameTest(structure = RailwaysGameTestHelper.PLATFORM_8X8)
    public void testNarrowGaugeTrackPlacement(GameTestHelper helper) {
        int index = 1;
        for (Map.Entry<TrackMaterial, NonNullSupplier<TrackBlock>> entry : CRBlocks.NARROW_GAUGE_TRACKS.entrySet()) {
            TrackBlock block = entry.getValue().get();
            BlockPos pos = new BlockPos(index % 6 + 1, 1, (index / 6) + 1);
            helper.setBlock(pos, block.defaultBlockState());
            helper.assertBlockPresent(block, pos);
            helper.assertTrue(AllTags.AllBlockTags.TRACKS.matches(helper.getBlockState(pos)), "Narrow track " + block + " must match TRACKS tag");
            index++;
            if (index > 10) break; // test representative sample
        }
        helper.succeed();
    }

    @GameTest(structure = RailwaysGameTestHelper.PLATFORM_8X8)
    public void testWideGaugeTrackPlacement(GameTestHelper helper) {
        int index = 1;
        for (Map.Entry<TrackMaterial, NonNullSupplier<TrackBlock>> entry : CRBlocks.WIDE_GAUGE_TRACKS.entrySet()) {
            TrackBlock block = entry.getValue().get();
            BlockPos pos = new BlockPos(index % 6 + 1, 1, (index / 6) + 1);
            helper.setBlock(pos, block.defaultBlockState());
            helper.assertBlockPresent(block, pos);
            helper.assertTrue(AllTags.AllBlockTags.TRACKS.matches(helper.getBlockState(pos)), "Wide track " + block + " must match TRACKS tag");
            index++;
            if (index > 10) break; // test representative sample
        }
        helper.succeed();
    }
}
