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

package com.railwayteam.railways.fabric_mixin.client;

import com.railwayteam.railways.content.custom_tracks.CurvedTrackPickPacket;
import com.railwayteam.railways.registry.CRPackets;
import com.zurrtum.create.client.content.trains.track.TrackBlockOutline;
import com.zurrtum.create.client.content.trains.track.TrackBlockOutline.BezierPointSelection;
import com.zurrtum.create.content.trains.track.BezierConnection;
import com.zurrtum.create.content.trains.track.TrackBlockEntity;
import com.zurrtum.create.infrastructure.component.BezierTrackPointLocation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(Minecraft.class)
public class MixinMinecraft {
    @Shadow public LocalPlayer player;

    @Inject(method = "pickBlockOrEntity", at = @At("HEAD"), cancellable = true)
    private void railways$pickCurvedTrack(CallbackInfo ci) {
        BezierPointSelection result = TrackBlockOutline.result;
        if (result == null || this.player == null || this.player.isSpectator())
            return;

        TrackBlockEntity track = result.blockEntity();
        if (track == null)
            return;
        BezierTrackPointLocation loc = result.loc();
        if (loc == null)
            return;
        BlockPos curveTarget = loc.curveTarget();
        Map<BlockPos, BezierConnection> connections = track.getConnections();
        if (connections == null)
            return;
        BezierConnection connection = connections.get(curveTarget);
        if (connection == null)
            return;

        boolean preferCasing = this.player.isShiftKeyDown();
        ItemStack toPick = CurvedTrackPickPacket.getItemForConnection(connection, track, preferCasing);
        if (toPick.isEmpty())
            return;

        Inventory inv = this.player.getInventory();
        int slot = inv.findSlotMatchingItem(toPick);
        if (slot != -1) {
            if (Inventory.isHotbarSlot(slot)) {
                inv.setSelectedSlot(slot);
            } else {
                inv.pickSlot(slot);
            }
        } else if (this.player.hasInfiniteMaterials()) {
            inv.addAndPickItem(toPick);
            Minecraft.getInstance().gameMode.handleCreativeModeItemAdd(toPick, 36 + inv.getSelectedSlot());
        }

        BlockPos lookPos = BlockPos.containing(result.vec());
        CRPackets.PACKETS.send(new CurvedTrackPickPacket(track.getBlockPos(), curveTarget, lookPos, preferCasing));
        ci.cancel();
    }
}
