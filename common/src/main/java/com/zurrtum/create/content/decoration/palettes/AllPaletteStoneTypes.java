package com.zurrtum.create.content.decoration.palettes;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.List;
import java.util.function.Supplier;

public enum AllPaletteStoneTypes {
    ANDESITE(Blocks.ANDESITE),
    ASURINE(Blocks.DEEPSLATE),
    CALCITE(Blocks.CALCITE),
    CRIMSITE(Blocks.TUFF),
    DEEPSLATE(Blocks.DEEPSLATE),
    DIORITE(Blocks.DIORITE),
    DRIPSTONE(Blocks.DRIPSTONE_BLOCK),
    GRANITE(Blocks.GRANITE),
    LIMESTONE(Blocks.CALCITE),
    OCHRUM(Blocks.DYED_TERRACOTTA.yellow()),
    SCORIA(Blocks.BLACKSTONE),
    SCORCHIA(Blocks.BASALT),
    TUFF(Blocks.TUFF),
    VERIDIUM(Blocks.DYED_TERRACOTTA.green());

    private final Block fallback;
    private final VariantSet variants;

    AllPaletteStoneTypes(Block fallback) {
        this.fallback = fallback;
        this.variants = new VariantSet(fallback);
    }

    public Supplier<Block> getBaseBlock() {
        return () -> fallback;
    }

    public VariantSet getVariants() {
        return variants;
    }

    public static class VariantSet {
        public final List<Supplier<Block>> registeredBlocks;

        VariantSet(Block fallback) {
            Supplier<Block> supplier = () -> fallback;
            this.registeredBlocks = List.of(supplier, supplier, supplier, supplier, supplier, supplier);
        }
    }
}
