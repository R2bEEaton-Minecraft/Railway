package com.railwayteam.railways.fabric;

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.content.coupling.coupler.TrackCouplerBlockEntity;
import com.railwayteam.railways.content.coupling.coupler.TrackCouplerRenderer;
import com.railwayteam.railways.util.MinRespectingScrollValueBehaviour;
import com.zurrtum.create.client.AllBlockEntityBehaviours;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.network.chat.Component;

/** Loads renderer-only registration references after common registration has finished. */
final class RailwaysBlockEntityRenderers {
    private RailwaysBlockEntityRenderers() {
    }

    @SuppressWarnings("unchecked")
    static void register() {
        BlockEntityType<?> type = BuiltInRegistries.BLOCK_ENTITY_TYPE
            .getValue(Railways.asResource("track_coupler"));
        Railways.LOGGER.info("[Coupler diagnostics] resolved block entity type: {} (registry id: {})",
            type, BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(type));
        BlockEntityRendererRegistry.register((BlockEntityType<TrackCouplerBlockEntity>) type,
            TrackCouplerRenderer::new);
        AllBlockEntityBehaviours.add((BlockEntityType<TrackCouplerBlockEntity>) type, be -> {
            MinRespectingScrollValueBehaviour behaviour = new MinRespectingScrollValueBehaviour(
                Component.translatable("railways.coupler.edge_spacing"), be,
                new TrackCouplerBlockEntity.TrackCouplerValueBoxTransform(true));
            behaviour.between(3, 15);
            behaviour.withFormatter(i -> i + "m");
            Railways.LOGGER.info("[Coupler diagnostics] creating client wrench behaviour for {} at {}",
                be.getType(), be.getBlockPos());
            return behaviour;
        });
        Railways.LOGGER.info("[Coupler diagnostics] renderer registration call completed; client behaviour factories: {}",
            com.zurrtum.create.api.behaviour.BlockEntityBehaviour.CLIENT_REGISTRY.get(type).size());
    }
}
