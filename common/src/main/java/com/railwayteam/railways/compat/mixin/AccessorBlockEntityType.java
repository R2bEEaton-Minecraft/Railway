package com.railwayteam.railways.mixincompat;

import java.util.Set;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

/** Allows compatibility registrations to extend an existing block-entity type. */
@Mixin(BlockEntityType.class)
public interface AccessorBlockEntityType {
	@Accessor Set<Block> getValidBlocks();

	@Accessor
	@Final
	@Mutable
	void setValidBlocks(Set<Block> validBlocks);
}
