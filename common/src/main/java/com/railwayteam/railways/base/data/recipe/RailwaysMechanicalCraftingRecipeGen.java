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
import com.railwayteam.railways.base.data.compat.emi.EmiRecipeDefaultsGen;
import com.railwayteam.railways.base.data.recipe.EnumRecipeList.PalettesRecipeList;
import com.railwayteam.railways.registry.CRPalettes.Styles;
import com.zurrtum.create.catnip.platform.CatnipServices;
import com.zurrtum.create.content.kinetics.crafter.MechanicalCraftingRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

@SuppressWarnings("unused")
public class RailwaysMechanicalCraftingRecipeGen extends RailwaysRecipeProvider {
    PalettesRecipeList BOILERS = new PalettesRecipeList(color -> create(() -> Styles.BOILER.get(color).get())
        .returns(4)
        .setEmiDefault(color.isNetherite())
        .recipe(b -> b.key('#', Styles.SLASHED.get(color).get())
            .key('u', Items.BUCKET)
            .key('/', Items.BLAZE_ROD)
            .patternLine("  #  ")
            .patternLine(" #u# ")
            .patternLine("#///#")
            .patternLine(" #/# ")
            .patternLine("  #  ")));

    PalettesRecipeList BRASS_WRAPPED_BOILERS = new PalettesRecipeList(color -> create(() -> Styles.BRASS_WRAPPED_BOILER.get(color).get())
        .returns(4)
        .setEmiDefault(color.isNetherite())
        .recipe(b -> b.key('#', Styles.BRASS_WRAPPED_SLASHED.get(color).get())
            .key('u', Items.BUCKET)
            .key('/', Items.BLAZE_ROD)
            .patternLine("  #  ")
            .patternLine(" #u# ")
            .patternLine("#///#")
            .patternLine(" #/# ")
            .patternLine("  #  ")));

    PalettesRecipeList COPPER_WRAPPED_BOILERS = new PalettesRecipeList(color -> create(() -> Styles.COPPER_WRAPPED_BOILER.get(color).get())
        .returns(4)
        .setEmiDefault(color.isNetherite())
        .recipe(b -> b.key('#', Styles.COPPER_WRAPPED_SLASHED.get(color).get())
            .key('u', Items.BUCKET)
            .key('/', Items.BLAZE_ROD)
            .patternLine("  #  ")
            .patternLine(" #u# ")
            .patternLine("#///#")
            .patternLine(" #/# ")
            .patternLine("  #  ")));

    PalettesRecipeList IRON_WRAPPED_BOILERS = new PalettesRecipeList(color -> create(() -> Styles.IRON_WRAPPED_BOILER.get(color).get())
        .returns(4)
        .setEmiDefault(color.isNetherite())
        .recipe(b -> b.key('#', Styles.IRON_WRAPPED_SLASHED.get(color).get())
            .key('u', Items.BUCKET)
            .key('/', Items.BLAZE_ROD)
            .patternLine("  #  ")
            .patternLine(" #u# ")
            .patternLine("#///#")
            .patternLine(" #/# ")
            .patternLine("  #  ")));

    public RailwaysMechanicalCraftingRecipeGen(PackOutput pPackoutput) {
        super(pPackoutput);
    }

    GeneratedRecipeBuilder create(Supplier<ItemLike> result) {
        return new GeneratedRecipeBuilder(result);
    }

    class GeneratedRecipeBuilder {
        private String suffix;
        private Supplier<ItemLike> result;
        private int amount;
        private boolean addToEmiDefaults;

        public GeneratedRecipeBuilder(Supplier<ItemLike> result) {
            this.suffix = "";
            this.result = result;
            this.amount = 1;
        }

        GeneratedRecipeBuilder returns(int amount) {
            this.amount = amount;
            return this;
        }

        GeneratedRecipeBuilder withSuffix(String suffix) {
            this.suffix = suffix;
            return this;
        }

        GeneratedRecipeBuilder setEmiDefault() {
            return setEmiDefault(true);
        }

        GeneratedRecipeBuilder setEmiDefault(boolean addToEmiDefaults) {
            this.addToEmiDefaults = addToEmiDefaults;
            return this;
        }

        private static Identifier clean(Identifier loc) {
            String path = loc.getPath();
            while (path.contains("//"))
                path = path.replaceAll("//", "/");
            return Identifier.fromNamespaceAndPath(loc.getNamespace(), path);
        }

        GeneratedRecipe recipe(UnaryOperator<MechanicalCraftingRecipeBuilder> builder) {
            return register(consumer -> {
                MechanicalCraftingRecipeBuilder b =
                    builder.apply(MechanicalCraftingRecipeBuilder.shapedRecipe(result.get(), amount));
                Identifier location = clean(Railways.asResource("mechanical_crafting/" + CatnipServices.REGISTRIES.getKeyOrThrow(result.get()
                        .asItem())
                    .getPath() + suffix));
                if (addToEmiDefaults) {
                    EmiRecipeDefaultsGen.DEFAULT_RECIPES.add(location);
                }
                b.build(consumer, location);
            });
        }
    }

    public static class MechanicalCraftingRecipeBuilder {
        private final ItemLike result;
        private final int amount;
        private final Map<Character, Ingredient> key = new HashMap<>();
        private final List<String> pattern = new ArrayList<>();

        public MechanicalCraftingRecipeBuilder(ItemLike result, int amount) {
            this.result = result;
            this.amount = amount;
        }

        public static MechanicalCraftingRecipeBuilder shapedRecipe(ItemLike result, int amount) {
            return new MechanicalCraftingRecipeBuilder(result, amount);
        }

        public MechanicalCraftingRecipeBuilder key(char symbol, ItemLike item) {
            this.key.put(symbol, Ingredient.of(item));
            return this;
        }

        public MechanicalCraftingRecipeBuilder key(char symbol, Ingredient ingredient) {
            this.key.put(symbol, ingredient);
            return this;
        }

        public MechanicalCraftingRecipeBuilder patternLine(String line) {
            this.pattern.add(line);
            return this;
        }

        public void build(RecipeOutput consumer, Identifier location) {
            ShapedRecipePattern shapedPattern = ShapedRecipePattern.of(this.key, this.pattern);
            ItemStackTemplate template = new ItemStackTemplate(this.result.asItem(), this.amount);
            MechanicalCraftingRecipe recipe = new MechanicalCraftingRecipe(shapedPattern, template, false);
            consumer.accept(ResourceKey.create(Registries.RECIPE, location), recipe, null);
        }
    }

    @Override
    public @NotNull String getName() {
        return "Steam 'n' Rails Mechanical Crafting Recipes";
    }
}
