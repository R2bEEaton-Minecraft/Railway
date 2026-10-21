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

package com.railwayteam.railways.base.data.recipe;

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.content.buffer.headstock.HeadstockStyle;
import com.railwayteam.railways.content.buffer.single_deco.LinkPinBlock;
import com.railwayteam.railways.content.palettes.PalettesColor;
import com.railwayteam.railways.content.palettes.painting.PaintFluid;
import com.railwayteam.railways.multiloader.CommonTags;
import com.railwayteam.railways.multiloader.fluid.MultiloaderFluidStack;
import com.railwayteam.railways.registry.CRBlocks;
import com.railwayteam.railways.registry.CRFluids;
import com.railwayteam.railways.registry.CRItems;
import com.railwayteam.railways.registry.CRTags;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.foundation.fluid.FluidIngredient;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public abstract class RailwaysRecipeProvider implements DataProvider {

    protected final PackOutput packOutput;
    protected final List<GeneratedRecipe> all = new ArrayList<>();

    public RailwaysRecipeProvider(PackOutput pOutput) {
        this.packOutput = pOutput;
    }

    @Override
    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput output) {
        PackOutput.PathProvider recipePathProvider = packOutput.createRegistryElementsPathProvider(Registries.RECIPE);
        PackOutput.PathProvider advancementPathProvider = packOutput.createRegistryElementsPathProvider(Registries.ADVANCEMENT);
        List<CompletableFuture<?>> futures = new ArrayList<>();
        Set<ResourceKey<Recipe<?>>> seen = new HashSet<>();

        HolderLookup.Provider registries = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);

        RecipeOutput recipeOutput = new RecipeOutput() {
            @Override
            public void accept(ResourceKey<Recipe<?>> key, Recipe<?> recipe, AdvancementHolder advancement) {
                if (!seen.add(key)) {
                    throw new IllegalStateException("Duplicate recipe " + key.identifier());
                }
                futures.add(DataProvider.saveStable(output, registries, Recipe.CODEC, recipe, recipePathProvider.json(key.identifier())));
                if (advancement != null) {
                    futures.add(DataProvider.saveStable(output, registries, Advancement.CODEC, advancement.value(), advancementPathProvider.json(advancement.id())));
                }
            }

            @Override
            public Advancement.Builder advancement() {
                return Advancement.Builder.recipeAdvancement().parent(RecipeBuilder.ROOT_RECIPE_ADVANCEMENT);
            }

            @Override
            public void includeRootAdvancement() {
            }
        };

        for (GeneratedRecipe recipe : all) {
            recipe.register(recipeOutput);
        }
        Railways.LOGGER.info(getName() + " registered " + all.size() + " recipe" + (all.size() == 1 ? "" : "s"));
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    protected GeneratedRecipe register(GeneratedRecipe recipe) {
        all.add(recipe);
        return recipe;
    }

    @FunctionalInterface
    public interface GeneratedRecipe {
        void register(RecipeOutput consumer);
    }

    @SuppressWarnings("SameReturnValue")
    public static class Ingredients {
        public static TagKey<Item> string() {
            return CommonTags.STRING.tag;
        }

        public static ItemLike precisionMechanism() {
            return AllItems.PRECISION_MECHANISM;
        }

        public static TagKey<Item> ironNugget() {
            return CommonTags.IRON_NUGGETS.tag;
        }

        public static TagKey<Item> ironIngot() {
            return CommonTags.IRON_INGOTS.tag;
        }

        public static TagKey<Item> zincNugget() {
            return CommonTags.ZINC_NUGGETS.tag;
        }

        public static ItemLike girder() {
            return AllBlocks.METAL_GIRDER;
        }

        public static ItemLike metalBracket() {
            return AllBlocks.METAL_BRACKET;
        }

        public static TagKey<Item> ironSheet() {
            return CommonTags.IRON_PLATES.tag;
        }

        public static TagKey<Item> fence() {
            return TagKey.create(Registries.ITEM, Identifier.parse("minecraft:fences"));
        }

        public static ItemLike campfire() {
            return Blocks.CAMPFIRE;
        }

        public static ItemLike redstone() {
            return Items.REDSTONE;
        }

        public static ItemLike lever() { return Items.LEVER; }

        public static ItemLike cogwheel() {
            return AllBlocks.COGWHEEL;
        }

        public static ItemLike railwayCasing() {
            return AllBlocks.RAILWAY_CASING;
        }

        public static ItemLike brassCasing() {
            return AllBlocks.BRASS_CASING;
        }

        public static ItemLike andesiteCasing() {
            return AllBlocks.ANDESITE_CASING;
        }

        public static ItemLike propeller() {
            return AllItems.PROPELLER;
        }

        public static ItemLike electronTube() {
            return AllItems.ELECTRON_TUBE;
        }

        public static TagKey<Item> copperIngot() {
            return CommonTags.COPPER_INGOTS.tag;
        }

        public static TagKey<Item> brassNugget() {
            return CommonTags.BRASS_NUGGETS.tag;
        }

        public static ItemLike phantomMembrane() {
            return Items.PHANTOM_MEMBRANE;
        }

        public static ItemLike eyeOfEnder() {
            return Items.ENDER_EYE;
        }

        public static ItemLike industrialIron() {
            return AllBlocks.INDUSTRIAL_IRON_BLOCK;
        }

        public static TagKey<Item> brassSheet() {
            return CommonTags.BRASS_PLATES.tag;
        }

        public static TagKey<Item> woodenSlab() {
            return ItemTags.WOODEN_SLABS;
        }

        public static ItemLike contraptionControls() {
            return AllBlocks.CONTRAPTION_CONTROLS;
        }

        public static ItemLike stick() {
            return Items.STICK;
        }

        public static ItemLike andesiteAlloy() {
            return AllItems.ANDESITE_ALLOY;
        }

        public static ItemLike smallCog() {
            return AllBlocks.COGWHEEL;
        }

        public static ItemLike ironBlock() {
            return Blocks.IRON_BLOCK;
        }

        public static TagKey<Item> dye(@NotNull DyeColor color) {
            return CommonTags.DYES.get(color).tag;
        }

        public static TagKey<Item> bindingAgent() {
            return CRTags.AllItemTags.BINDING_AGENTS.tag;
        }

        public static FluidIngredient palettesPaint(@NotNull PalettesColor color, long amount) {
            return MultiloaderFluidStack.create(
                CRFluids.PAINT.get(),
                amount,
                PaintFluid.setColor(new CompoundTag(), color)
            ).asFluidIngredient();
        }

        public static TagKey<Item> brassIngot() {
            return CommonTags.BRASS_INGOTS.tag;
        }

        public static ItemLike shaft() {
            return AllBlocks.SHAFT;
        }

        public static ItemLike smallBuffer() {
            return CRBlocks.SMALL_BUFFER.get();
        }

        public static ItemLike linkPin() {
            return CRBlocks.LINK_AND_PIN_GROUP.get(LinkPinBlock.Style.LINK).get();
        }

        public static TagKey<Item> linkPinTag() {
            return CRTags.AllItemTags.DECO_COUPLERS.tag;
        }

        public static ItemLike headstock() {
            return CRBlocks.HEADSTOCK_GROUP.get(HeadstockStyle.LINK).get();
        }

        public static TagKey<Item> headstockTag() {
            return CRTags.AllItemTags.WOODEN_HEADSTOCKS.tag;
        }

        public static ItemLike copycatHeadstock() {
            return CRBlocks.COPYCAT_HEADSTOCK_GROUP.get(HeadstockStyle.LINK).get();
        }

        public static ItemLike copycatPanel() {
            return AllBlocks.COPYCAT_PANEL;
        }

        public static ItemLike sturdySheet() {
            return AllItems.STURDY_SHEET;
        }

        public static ItemLike chute() {
            return AllBlocks.CHUTE;
        }

        public static ItemLike flywheel() {
            return AllBlocks.FLYWHEEL;
        }

        public static TagKey<Item> woodenDoors() {
            return ItemTags.WOODEN_DOORS;
        }

        public static TagKey<Item> ironDoors() {
            return TagKey.create(Registries.ITEM, Identifier.parse("minecraft:doors"));
        }

        public static TagKey<Item> woodenTrapdoors() {
            return ItemTags.WOODEN_TRAPDOORS;
        }

        public static TagKey<Item> colorlessGlass() {
            return CommonTags.COLORLESS_GLASS_I.tag;
        }

        public static ItemLike emptyPaintPitcher() {
            return CRItems.EMPTY_PAINT_PITCHER.get();
        }

        public static ItemLike paintBrush() {
            return CRItems.PAINT_BRUSH.get();
        }

        public static ItemLike feather() {
            return Items.FEATHER;
        }
    }
}
