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

import java.util.function.Consumer;
import java.util.function.Supplier;

public class RailwaysClientImpl implements ClientModInitializer {
	public void onInitializeClient() {
		EntityRendererRegistry.register(CREntities.CART_BLOCK.get(),
			context -> new MinecartRenderer(context, ModelLayers.MINECART));
		EntityRendererRegistry.register(CREntities.CART_JUKEBOX.get(),
			context -> new MinecartRenderer(context, ModelLayers.MINECART));
		EntityRendererRegistry.register(CREntities.CONDUCTOR.get(), ConductorRenderer::new);
		RailwaysClient.init();
		RailwaysBlockEntityRenderers.register();
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
