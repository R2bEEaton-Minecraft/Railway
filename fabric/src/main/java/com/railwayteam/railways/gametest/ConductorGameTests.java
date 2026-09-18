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

import com.railwayteam.railways.content.conductor.ConductorEntity;
import com.railwayteam.railways.content.conductor.whistle.ConductorWhistleFlagBlockEntity;
import com.railwayteam.railways.registry.CRBlocks;
import com.railwayteam.railways.registry.CREntities;
import com.railwayteam.railways.registry.CRItems;
import com.railwayteam.railways.registry.CRTags;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllItems;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class ConductorGameTests {

    @GameTest(structure = RailwaysGameTestHelper.PLATFORM_8X8)
    public void testConductorEntitySpawning(GameTestHelper helper) {
        BlockPos pos = new BlockPos(3, 1, 3);
        ConductorEntity conductor = helper.spawn(CREntities.CONDUCTOR.get(), pos);
        helper.assertEntityPresent(CREntities.CONDUCTOR.get(), pos);

        ItemStack blueCap = CRItems.ITEM_CONDUCTOR_CAP.get(DyeColor.BLUE).asStack();
        conductor.setItemSlot(EquipmentSlot.HEAD, blueCap);
        ItemStack equipped = conductor.getItemBySlot(EquipmentSlot.HEAD);
        helper.assertFalse(equipped.isEmpty(), "Conductor head slot should not be empty");
        helper.assertTrue(CRTags.AllItemTags.CONDUCTOR_CAPS.matches(equipped), "Equipped item must match CONDUCTOR_CAPS tag");
        helper.succeed();
    }

    @GameTest(structure = RailwaysGameTestHelper.PLATFORM_8X8)
    public void testConductorScheduleHolding(GameTestHelper helper) {
        BlockPos pos = new BlockPos(3, 1, 3);
        ConductorEntity conductor = helper.spawn(CREntities.CONDUCTOR.get(), pos);
        helper.assertFalse(conductor.isHoldingSchedules(), "Initial conductor should not hold schedules");

        conductor.addSchedule(new ItemStack(AllItems.SCHEDULE));
        helper.assertTrue(conductor.isHoldingSchedules(), "Conductor should be holding schedule after adding one");
        helper.succeed();
    }

    @GameTest(structure = RailwaysGameTestHelper.PLATFORM_8X8)
    public void testConductorWhistleFlagPlacement(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        helper.setBlock(pos, CRBlocks.CONDUCTOR_WHISTLE_FLAG.get().defaultBlockState());
        helper.assertBlockPresent(CRBlocks.CONDUCTOR_WHISTLE_FLAG.get(), pos);
        RailwaysGameTestHelper.assertBlockEntity(helper, pos, ConductorWhistleFlagBlockEntity.class);
        helper.succeed();
    }

    @GameTest(structure = RailwaysGameTestHelper.PLATFORM_8X8)
    public void testConductorVentBlock(GameTestHelper helper) {
        BlockPos pos = new BlockPos(4, 1, 4);
        helper.setBlock(pos, CRBlocks.CONDUCTOR_VENT.get().defaultBlockState());
        helper.assertBlockPresent(CRBlocks.CONDUCTOR_VENT.get(), pos);
        helper.assertTrue(ConductorEntity.canSpyInteract(helper.getBlockState(pos)), "Conductor should be able to spy-interact with vent block");
        helper.succeed();
    }

    @GameTest(structure = RailwaysGameTestHelper.PLATFORM_8X8)
    public void testAllConductorCapColorsExist(GameTestHelper helper) {
        for (DyeColor color : DyeColor.values()) {
            helper.assertTrue(CRItems.ITEM_CONDUCTOR_CAP.containsKey(color), "Cap missing for color " + color);
            ItemStack cap = CRItems.ITEM_CONDUCTOR_CAP.get(color).asStack();
            helper.assertTrue(CRTags.AllItemTags.CONDUCTOR_CAPS.matches(cap), "Cap item for " + color + " must match tag");
        }
        helper.succeed();
    }

    @GameTest(structure = RailwaysGameTestHelper.PLATFORM_8X8)
    public void testConductorCapConvertsCasing(GameTestHelper helper) {
        BlockPos localPos = new BlockPos(2, 1, 2);
        BlockPos absPos = helper.absolutePos(localPos);
        helper.setBlock(localPos, AllBlocks.ANDESITE_CASING.defaultBlockState());
        helper.assertBlockPresent(AllBlocks.ANDESITE_CASING, localPos);

        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack redCap = CRItems.ITEM_CONDUCTOR_CAP.get(DyeColor.RED).asStack();
        player.setItemInHand(InteractionHand.MAIN_HAND, redCap);

        BlockHitResult hitResult = new BlockHitResult(Vec3.atCenterOf(absPos), Direction.UP, absPos, false);
        UseOnContext context = new UseOnContext(player, InteractionHand.MAIN_HAND, hitResult);
        InteractionResult result = redCap.useOn(context);

        helper.assertTrue(result.consumesAction(), "Cap use on casing should consume action");
        helper.assertBlockNotPresent(AllBlocks.ANDESITE_CASING, localPos);
        helper.assertEntityPresent(CREntities.CONDUCTOR.get(), localPos);
        helper.succeed();
    }

    @GameTest(structure = RailwaysGameTestHelper.PLATFORM_8X8)
    public void testConductorGogglesSpyJob(GameTestHelper helper) {
        BlockPos pos = new BlockPos(3, 1, 3);
        ConductorEntity conductor = helper.spawn(CREntities.CONDUCTOR.get(), pos);
        helper.assertTrue(conductor.getJob() == ConductorEntity.Job.DEFAULT, "Initial conductor job should be DEFAULT");

        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(AllItems.GOGGLES));

        InteractionResult result = player.interactOn(conductor, InteractionHand.MAIN_HAND);
        helper.assertTrue(result.consumesAction(), "Giving goggles to conductor should succeed");
        helper.assertTrue(conductor.getJob() == ConductorEntity.Job.SPY, "Conductor job should become SPY");
        helper.assertTrue(conductor.getSecondaryHeadStack().is(AllItems.GOGGLES), "Conductor secondary head stack should match AllItems.GOGGLES");

        // Shift-interact with empty hand retrieves goggles
        player.setShiftKeyDown(true);
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        player.interactOn(conductor, InteractionHand.MAIN_HAND);
        helper.assertTrue(conductor.getJob() == ConductorEntity.Job.DEFAULT, "Conductor job should revert to DEFAULT");
        helper.assertTrue(player.getItemInHand(InteractionHand.MAIN_HAND).is(AllItems.GOGGLES), "Player should have retrieved the goggles");
        helper.succeed();
    }

    @GameTest(structure = RailwaysGameTestHelper.PLATFORM_8X8)
    public void testConductorRedstoneLinkJob(GameTestHelper helper) {
        BlockPos pos = new BlockPos(3, 1, 3);
        ConductorEntity conductor = helper.spawn(CREntities.CONDUCTOR.get(), pos);
        helper.assertTrue(conductor.getJob() == ConductorEntity.Job.DEFAULT, "Initial conductor job should be DEFAULT");

        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(AllBlocks.REDSTONE_LINK));

        InteractionResult result = player.interactOn(conductor, InteractionHand.MAIN_HAND);
        helper.assertTrue(result.consumesAction(), "Giving redstone link to conductor should succeed");
        helper.assertTrue(conductor.getJob() == ConductorEntity.Job.REMOTE_CONTROL, "Conductor job should become REMOTE_CONTROL");

        // Shift-interact with empty hand retrieves redstone link
        player.setShiftKeyDown(true);
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        player.interactOn(conductor, InteractionHand.MAIN_HAND);
        helper.assertTrue(conductor.getJob() == ConductorEntity.Job.DEFAULT, "Conductor job should revert to DEFAULT");
        helper.assertTrue(player.getItemInHand(InteractionHand.MAIN_HAND).is(AllBlocks.REDSTONE_LINK.asItem()), "Player should have retrieved redstone link");
        helper.succeed();
    }

    @GameTest(structure = RailwaysGameTestHelper.PLATFORM_8X8)
    public void testRemoteLensBinding(GameTestHelper helper) {
        BlockPos pos = new BlockPos(3, 1, 3);
        ConductorEntity conductor = helper.spawn(CREntities.CONDUCTOR.get(), pos);
        conductor.setJob(ConductorEntity.Job.SPY);

        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack lensStack = new ItemStack(CRItems.REMOTE_LENS.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, lensStack);

        InteractionResult result = CRItems.REMOTE_LENS.get().interactLivingEntity(lensStack, player, conductor, InteractionHand.MAIN_HAND);
        helper.assertTrue(result == InteractionResult.SUCCESS, "Remote lens should successfully bind to SPY conductor");

        CompoundTag tag = player.getItemInHand(InteractionHand.MAIN_HAND).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        helper.assertTrue(tag.contains("SelectedConductor"), "Custom data should contain SelectedConductor");
        helper.assertTrue(tag.getStringOr("SelectedConductor", "").equals(conductor.getUUID().toString()), "SelectedConductor must match conductor UUID");
        helper.succeed();
    }
}
