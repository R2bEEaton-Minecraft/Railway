package com.railwayteam.railways.mixincompat;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.world.level.block.Block;

/** Access to the vanilla block-to-chunk-layer table used by Fabric's old API. */
@Mixin(ItemBlockRenderTypes.class)
public interface AccessorItemBlockRenderTypes {
	@Accessor("TYPE_BY_BLOCK")
	static Map<Block, ChunkSectionLayer> railways$getTypeByBlock() {
		throw new AssertionError();
	}
}
