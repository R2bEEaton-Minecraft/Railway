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
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.Create;
import com.zurrtum.create.content.trains.entity.CarriageBogey;
import com.zurrtum.create.content.trains.entity.Train;
import com.zurrtum.create.content.trains.track.TrackBlock;
import com.zurrtum.create.content.trains.track.TrackPropagator;
import com.zurrtum.create.content.trains.track.TrackShape;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

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
    public void testTrainMovement(GameTestHelper helper) {
        BlockState trackState = AllBlocks.TRACK.defaultBlockState()
                .setValue(TrackBlock.SHAPE, TrackShape.ZO);

        for (int z = 1; z <= 6; z++) {
            BlockPos trackPos = new BlockPos(3, 1, z);
            helper.setBlock(trackPos, trackState);
            BlockPos absPos = helper.absolutePos(trackPos);
            TrackPropagator.onRailAdded(helper.getLevel(), absPos, helper.getBlockState(trackPos));
        }

        BlockPos placePos = new BlockPos(3, 1, 3);
        BlockPos absPlacePos = helper.absolutePos(placePos);

        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack handcarStack = new ItemStack(CRBlocks.HANDCAR.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, handcarStack);

        BlockHitResult hitResult = new BlockHitResult(Vec3.atCenterOf(absPlacePos), Direction.UP, absPlacePos, false);
        UseOnContext context = new UseOnContext(player, InteractionHand.MAIN_HAND, hitResult);

        InteractionResult placeResult = handcarStack.useOn(context);
        helper.assertTrue(placeResult.consumesAction(), "Handcar should be successfully placed on the track");

        Train train = null;
        for (Train t : Create.RAILWAYS.trains.values()) {
            if (t.owner.equals(player.getUUID())) {
                train = t;
                break;
            }
        }
        helper.assertFalse(train == null, "A train should be registered for the player");

        CarriageBogey bogey = train.carriages.get(0).bogeys.getFirst();
        double initialPos = bogey.leading().position;
        train.speed = 0.5;

        Train finalTrain = train;
        helper.succeedWhen(() -> {
            finalTrain.tick(helper.getLevel());
            double currentPos = bogey.leading().position;
            helper.assertTrue(currentPos != initialPos, "Train travelling point position should advance as train moves");
            Create.RAILWAYS.removeTrain(finalTrain.id);
        });
    }
}
