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

import com.railwayteam.railways.content.coupling.coupler.TrackCouplerBlock;
import com.railwayteam.railways.content.coupling.coupler.TrackCouplerBlockEntity;
import com.railwayteam.railways.registry.CRBlocks;
import com.railwayteam.railways.mixin.AccessorTrackTargetingBehavior;
import com.zurrtum.create.infrastructure.component.BezierTrackPointLocation;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;

public class CouplerGameTests {

    @GameTest(structure = RailwaysGameTestHelper.PLATFORM_8X8)
    public void testCouplerDefaultPlacement(GameTestHelper helper) {
        BlockPos pos = new BlockPos(3, 1, 3);
        helper.setBlock(pos, CRBlocks.TRACK_COUPLER.get().defaultBlockState()
                .setValue(TrackCouplerBlock.POWERED, false)
                .setValue(TrackCouplerBlock.MODE, TrackCouplerBlockEntity.AllowedOperationMode.BOTH));
        helper.assertBlockPresent(CRBlocks.TRACK_COUPLER.get(), pos);
        helper.assertBlockProperty(pos, TrackCouplerBlock.POWERED, false);
        helper.assertBlockProperty(pos, TrackCouplerBlock.MODE, TrackCouplerBlockEntity.AllowedOperationMode.BOTH);
        RailwaysGameTestHelper.assertBlockEntity(helper, pos, TrackCouplerBlockEntity.class);
        helper.succeed();
    }

    @GameTest(structure = RailwaysGameTestHelper.PLATFORM_8X8)
    public void testCouplerModes(GameTestHelper helper) {
        BlockPos posCouple = new BlockPos(2, 1, 2);
        helper.setBlock(posCouple, CRBlocks.TRACK_COUPLER.get().defaultBlockState()
                .setValue(TrackCouplerBlock.MODE, TrackCouplerBlockEntity.AllowedOperationMode.COUPLING));
        helper.assertBlockProperty(posCouple, TrackCouplerBlock.MODE, TrackCouplerBlockEntity.AllowedOperationMode.COUPLING);

        BlockPos posDecouple = new BlockPos(4, 1, 2);
        helper.setBlock(posDecouple, CRBlocks.TRACK_COUPLER.get().defaultBlockState()
                .setValue(TrackCouplerBlock.MODE, TrackCouplerBlockEntity.AllowedOperationMode.DECOUPLING));
        helper.assertBlockProperty(posDecouple, TrackCouplerBlock.MODE, TrackCouplerBlockEntity.AllowedOperationMode.DECOUPLING);
        helper.succeed();
    }

    @GameTest(structure = RailwaysGameTestHelper.PLATFORM_8X8)
    public void testCouplerPoweredState(GameTestHelper helper) {
        BlockPos pos = new BlockPos(3, 1, 3);
        helper.setBlock(pos, CRBlocks.TRACK_COUPLER.get().defaultBlockState()
                .setValue(TrackCouplerBlock.POWERED, true));
        helper.assertBlockProperty(pos, TrackCouplerBlock.POWERED, true);
        helper.succeed();
    }
    @GameTest(structure = RailwaysGameTestHelper.PLATFORM_8X8)
    public void testCouplerTargetingSurvivesSaveReload(GameTestHelper helper) {
        BlockPos pos = new BlockPos(3, 1, 3);
        helper.setBlock(pos, CRBlocks.TRACK_COUPLER.get());
        TrackCouplerBlockEntity coupler = RailwaysGameTestHelper.assertBlockEntity(helper, pos, TrackCouplerBlockEntity.class);
        AccessorTrackTargetingBehavior primary = (AccessorTrackTargetingBehavior) coupler.edgePoint;
        AccessorTrackTargetingBehavior secondary = (AccessorTrackTargetingBehavior) coupler.secondEdgePoint;
        UUID primaryId = UUID.randomUUID();
        UUID secondaryId = UUID.randomUUID();
        primary.setId(primaryId);
        primary.setTargetTrack(new BlockPos(1, -1, 2));
        secondary.setId(secondaryId);
        secondary.setTargetTrack(new BlockPos(-2, -1, 3));
        secondary.setTargetDirection(Direction.AxisDirection.POSITIVE);
        secondary.setOrthogonal(true);
        BezierTrackPointLocation curve = new BezierTrackPointLocation(coupler.getBlockPos().offset(4, 0, 5), 7);
        secondary.setTargetBezier(curve);

        var saved = coupler.saveWithFullMetadata(helper.getLevel().registryAccess());
        saved.putInt("EdgeSpacing", 9);
        TrackCouplerBlockEntity loaded = (TrackCouplerBlockEntity) BlockEntity.loadStatic(
            coupler.getBlockPos(), coupler.getBlockState(), saved, helper.getLevel().registryAccess());
        helper.assertTrue(loaded != null, Component.literal("Coupler must load from its saved data"));
        AccessorTrackTargetingBehavior loadedPrimary = (AccessorTrackTargetingBehavior) loaded.edgePoint;
        AccessorTrackTargetingBehavior loadedSecondary = (AccessorTrackTargetingBehavior) loaded.secondEdgePoint;
        helper.assertTrue(primaryId.equals(loadedPrimary.getId()), Component.literal("Primary target ID must survive reload"));
        helper.assertTrue(primary.getTargetTrack().equals(loadedPrimary.getTargetTrack()), Component.literal("Primary target position must survive reload"));
        helper.assertTrue(secondaryId.equals(loadedSecondary.getId()), Component.literal("Secondary target ID must survive reload independently"));
        helper.assertTrue(secondary.getTargetTrack().equals(loadedSecondary.getTargetTrack()), Component.literal("Secondary target position must survive reload"));
        helper.assertTrue(loadedSecondary.getTargetDirection() == Direction.AxisDirection.POSITIVE && loadedSecondary.isOrthogonal(),
            Component.literal("Secondary target orientation must survive reload"));
        helper.assertTrue(curve.equals(loadedSecondary.getTargetBezier()), Component.literal("Secondary curve position and segment must survive reload"));
        helper.assertTrue(loaded.saveWithoutMetadata(helper.getLevel().registryAccess()).getIntOr("EdgeSpacing", 0) == 9,
            Component.literal("Coupler spacing must survive reload"));
        helper.succeed();
    }
}
