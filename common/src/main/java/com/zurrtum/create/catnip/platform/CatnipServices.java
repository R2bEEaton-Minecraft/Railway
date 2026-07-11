package com.zurrtum.create.catnip.platform;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class CatnipServices {
    public static final Registries REGISTRIES = new Registries();

    public static class Registries {
        public ResourceLocation getKeyOrThrow(Block block) {
            return BuiltInRegistries.BLOCK.getKey(block);
        }

        public ResourceLocation getKeyOrThrow(Item item) {
            return BuiltInRegistries.ITEM.getKey(item);
        }
    }
}
