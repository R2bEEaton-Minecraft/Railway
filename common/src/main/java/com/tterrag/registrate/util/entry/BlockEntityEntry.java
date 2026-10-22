package com.tterrag.registrate.util.entry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEntityEntry<T extends BlockEntity> extends RegistryEntry<BlockEntityType<T>> {
    public BlockEntityEntry(ResourceLocation id, BlockEntityType<T> value) {
        super(id, value);
    }

    public T create(BlockPos pos, BlockState state) {
        return get().create(pos, state);
    }
}
