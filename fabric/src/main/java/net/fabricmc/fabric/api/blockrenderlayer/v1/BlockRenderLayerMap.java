package net.fabricmc.fabric.api.blockrenderlayer.v1;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.world.level.block.Block;
import com.railwayteam.railways.mixincompat.AccessorItemBlockRenderTypes;

/** Compatibility bridge for the render-layer hook removed after Fabric API 1.20. */
public interface BlockRenderLayerMap {
    BlockRenderLayerMap INSTANCE = (block, renderType) -> {
        ChunkSectionLayer layer = renderType == RenderType.cutout()
                ? ChunkSectionLayer.CUTOUT
                : renderType == RenderType.cutoutMipped()
                    ? ChunkSectionLayer.CUTOUT_MIPPED
                    : ChunkSectionLayer.SOLID;
        AccessorItemBlockRenderTypes.railways$getTypeByBlock().put(block, layer);
    };

    void putBlock(Block block, RenderType renderType);
}
