/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2024 The Railways Team
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

package com.railwayteam.railways.events;

import com.railwayteam.railways.annotation.multiloader.MultiLoaderEvent;
import com.railwayteam.railways.config.CRConfigs;
import com.railwayteam.railways.content.bogey_menu.handler.BogeyMenuEventsHandler;
import com.railwayteam.railways.content.conductor.ConductorPossessionController;
import com.railwayteam.railways.content.custom_tracks.phantom.PhantomSpriteManager;
import com.railwayteam.railways.content.cycle_menu.TagCycleHandlerClient;
import com.railwayteam.railways.content.qol.TrackEdgePointHighlighter;
import com.railwayteam.railways.registry.CRKeys;
import com.railwayteam.railways.registry.CRPackets;
import com.railwayteam.railways.util.UpdateChecker;
import com.railwayteam.railways.util.packet.ConfigureDevCapeC2SPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.ApiStatus;

import java.net.URI;

public class ClientEvents {

    @ApiStatus.Internal
    public static boolean previousDevCapeSetting = false;

    @MultiLoaderEvent
    public static void onClientTickStart(Minecraft mc) {
        CRKeys.fixBinds();
        PhantomSpriteManager.tick(mc);

        Level level = mc.level;
        long ticks = level == null ? 1 : level.getGameTime();
        if (ticks % 40 == 0 && previousDevCapeSetting != (previousDevCapeSetting = CRConfigs.client().useDevCape.get())) {
            CRPackets.PACKETS.send(new ConfigureDevCapeC2SPacket(previousDevCapeSetting));
        }

        if (isGameActive()) {
            BogeyMenuEventsHandler.clientTick();
            TagCycleHandlerClient.clientTick();
            ConductorPossessionController.onClientTick(mc, true);
            TrackEdgePointHighlighter.clientTick(mc);
        }
    }

    @MultiLoaderEvent
    public static void onClientTickEnd(Minecraft mc) {
        if (isGameActive()) {
            ConductorPossessionController.onClientTick(mc, false);
        }
    }

    @MultiLoaderEvent
    public static void onClientWorldLoad(Level level) {
        PhantomSpriteManager.firstRun = true;
        announceUpdateIfAvailable();
    }

    private static void announceUpdateIfAvailable() {
        if (!UpdateChecker.INSTANCE.claimNotification())
            return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null)
            return;

        MutableComponent modrinthLink = linkComponent("Modrinth", UpdateChecker.INSTANCE.getDownloadUrl());
        MutableComponent curseforgeLink = linkComponent("CurseForge", UpdateChecker.INSTANCE.getCurseForgeUrl());

        mc.player.displayClientMessage(Component.literal("[Steam 'n' Rails] ")
            .withStyle(ChatFormatting.GOLD)
            .append(Component.literal("A new version (" + UpdateChecker.INSTANCE.getLatestVersion() + ") is available: "))
            .append(modrinthLink)
            .append(Component.literal(" | "))
            .append(curseforgeLink), false);
    }

    private static MutableComponent linkComponent(String label, String url) {
        return Component.literal(label)
            .withStyle(style -> style
                .withColor(ChatFormatting.AQUA)
                .withUnderlined(true)
                .withClickEvent(new ClickEvent.OpenUrl(URI.create(url)))
                .withHoverEvent(new HoverEvent.ShowText(Component.literal(url))));
    }

    protected static boolean isGameActive() {
        return !(Minecraft.getInstance().level == null || Minecraft.getInstance().player == null);
    }

    @MultiLoaderEvent
    public static void onKeyInput(int key, boolean pressed) {
        if (Minecraft.getInstance().screen != null)
            return;
        BogeyMenuEventsHandler.onKeyInput(key, pressed);
        if (Minecraft.getInstance().screen != null)
            return;
        TagCycleHandlerClient.onKeyInput(key, pressed);
    }

    @MultiLoaderEvent
    public static void onTagsUpdated() {
        TagCycleHandlerClient.onTagsUpdated();
    }
}