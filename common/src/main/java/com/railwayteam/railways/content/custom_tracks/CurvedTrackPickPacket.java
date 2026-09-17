/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.\
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.railwayteam.railways.content.custom_tracks;

import com.railwayteam.railways.mixin_interfaces.IHasTrackCasing;
import com.railwayteam.railways.multiloader.C2SPacket;
import com.railwayteam.railways.util.EntityUtils;
import com.zurrtum.create.content.trains.track.BezierConnection;
import com.zurrtum.create.content.trains.track.TrackBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class CurvedTrackPickPacket implements C2SPacket {
    private final BlockPos pos;
    private final BlockPos targetPos;
    private final BlockPos lookPos;
    private final boolean preferCasing;

    public CurvedTrackPickPacket(BlockPos pos, BlockPos targetPos, BlockPos lookPos, boolean preferCasing) {
        this.pos = pos;
        this.targetPos = targetPos;
        this.lookPos = lookPos;
        this.preferCasing = preferCasing;
    }

    public CurvedTrackPickPacket(FriendlyByteBuf buffer) {
        this.pos = buffer.readBlockPos();
        this.targetPos = buffer.readBlockPos();
        this.lookPos = buffer.readBlockPos();
        this.preferCasing = buffer.readBoolean();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeBlockPos(targetPos);
        buffer.writeBlockPos(lookPos);
        buffer.writeBoolean(preferCasing);
    }

    @Override
    public void handle(ServerPlayer player) {
        if (player.isSpectator())
            return;
        if (!player.level().isLoaded(pos) || !player.level().isLoaded(targetPos))
            return;
        double reach = EntityUtils.getReachDistance(player) + 1.0;
        if (player.distanceToSqr(Vec3.atCenterOf(lookPos)) > reach * reach)
            return;
        if (!(player.level().getBlockEntity(pos) instanceof TrackBlockEntity trackBE))
            return;

        BezierConnection connection = trackBE.getConnections().get(targetPos);
        if (connection == null)
            return;

        ItemStack toPick = getItemForConnection(connection, trackBE, preferCasing);
        if (toPick.isEmpty())
            return;

        Inventory inv = player.getInventory();
        int slot = inv.findSlotMatchingItem(toPick);
        if (slot != -1) {
            if (Inventory.isHotbarSlot(slot)) {
                inv.setSelectedSlot(slot);
            } else {
                inv.pickSlot(slot);
            }
        } else if (player.hasInfiniteMaterials()) {
            inv.addAndPickItem(toPick);
        }
        player.connection.send(new ClientboundSetHeldSlotPacket(inv.getSelectedSlot()));
        player.inventoryMenu.broadcastChanges();
    }

    public static ItemStack getItemForConnection(BezierConnection connection, TrackBlockEntity trackBE, boolean preferCasing) {
        if (preferCasing && connection instanceof IHasTrackCasing casing && casing.railways$getTrackCasing() != null) {
            return new ItemStack(casing.railways$getTrackCasing());
        }
        ItemStack item = new ItemStack(connection.getMaterial().asItem());
        if (item.isEmpty()) {
            item = new ItemStack(trackBE.getBlockState().getBlock().asItem());
        }
        return item;
    }
}
