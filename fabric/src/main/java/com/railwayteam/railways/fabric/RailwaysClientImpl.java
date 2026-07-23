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

package com.railwayteam.railways.fabric;

import com.mojang.brigadier.CommandDispatcher;
import com.railwayteam.railways.Railways;
import com.railwayteam.railways.RailwaysClient;
import com.railwayteam.railways.content.conductor.ConductorRenderer;
import com.railwayteam.railways.registry.CREntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.MinecartRenderer;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.BlockHitResult;
import com.railwayteam.railways.content.coupling.coupler.TrackCouplerBlockEntity;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue.ScrollValueRenderer;
import com.zurrtum.create.client.Create;
import com.zurrtum.create.client.catnip.outliner.Outliner;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionResult;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class RailwaysClientImpl implements ClientModInitializer {
	private static ScrollValueBehaviour<?, ?> lastCouplerValueBox;

	public void onInitializeClient() {
		EntityRendererRegistry.register(CREntities.CART_BLOCK.get(),
			context -> new MinecartRenderer(context, ModelLayers.MINECART));
		EntityRendererRegistry.register(CREntities.CART_JUKEBOX.get(),
			context -> new MinecartRenderer(context, ModelLayers.MINECART));
		EntityRendererRegistry.register(CREntities.CONDUCTOR.get(), ConductorRenderer::new);
		RailwaysBlockEntityRenderers.register();
		ClientTickEvents.END_CLIENT_TICK.register(mc -> {
			if (mc.level != null && mc.hitResult instanceof BlockHitResult hit
				&& mc.level.getBlockEntity(hit.getBlockPos()) instanceof TrackCouplerBlockEntity coupler) {
				ScrollValueRenderer.tick(mc);
				lastCouplerValueBox = coupler.getBehaviour(ScrollValueBehaviour.TYPE);
			} else if (lastCouplerValueBox != null) {
				Outliner.getInstance().remove(lastCouplerValueBox);
				lastCouplerValueBox = null;
			}
		});
		UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
			if (!(player instanceof LocalPlayer localPlayer)
				|| !(level.getBlockEntity(hit.getBlockPos()) instanceof TrackCouplerBlockEntity coupler))
				return InteractionResult.PASS;
			ScrollValueBehaviour<?, ?> behaviour = coupler.getBehaviour(ScrollValueBehaviour.TYPE);
			if (behaviour == null || !behaviour.mayInteract(localPlayer))
				return InteractionResult.PASS;
			if (behaviour.getSlotPositioning() instanceof ValueBoxTransform.Sided sided)
				sided.fromSide(hit.getDirection());
			if (!behaviour.testHit(hit.getLocation()))
				return InteractionResult.PASS;
			Create.VALUE_SETTINGS_HANDLER.startInteractionWith(
				hit.getBlockPos(), ScrollValueBehaviour.TYPE, hand, hit.getDirection());
			return InteractionResult.SUCCESS;
		});
		RailwaysClient.init();
	}

	@SuppressWarnings({"unchecked", "rawtypes"}) // jank!
	public static void registerClientCommands(Consumer<CommandDispatcher<SharedSuggestionProvider>> consumer) {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			CommandDispatcher<SharedSuggestionProvider> casted = (CommandDispatcher) dispatcher;
			consumer.accept(casted);
		});
	}

	public static void registerModelLayer(ModelLayerLocation layer, Supplier<LayerDefinition> definition) {
		EntityModelLayerRegistry.registerModelLayer(layer, definition::get);
	}

	public static void registerBuiltinPack(String id, String name) {
		ModContainer mod = FabricLoader.getInstance().getModContainer(Railways.MOD_ID).orElseThrow();
		ResourceManagerHelper.registerBuiltinResourcePack(Railways.asResource(id), mod, Component.literal(name), ResourcePackActivationType.NORMAL);
	}
}
