/*
 * Steam 'n' Rails
 * Copyright (c) 2025 The Railways Team
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
import com.railwayteam.railways.registry.CRTrackMaterials;
import com.zurrtum.create.content.kinetics.deployer.DeployerApplicationRecipe;
import com.zurrtum.create.content.kinetics.press.PressingRecipe;
import com.zurrtum.create.content.kinetics.saw.CuttingRecipe;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import com.zurrtum.create.content.processing.sequenced.SequencedAssemblyRecipe;
import com.zurrtum.create.content.trains.track.TrackMaterial;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.fabricmc.fabric.impl.datagen.FabricDataGenHelper;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public class RailwaysSequencedAssemblyRecipeBuilder {
    private final Identifier id;
    private Ingredient ingredient = Ingredient.of();
    private ItemStack transitionalItem = ItemStack.EMPTY;
    private ProcessingOutput result = new ProcessingOutput(ItemStack.EMPTY);
    private int loops = 1;
    private final List<Function<ItemStack, Recipe<?>>> steps = new ArrayList<>();
    private final List<ResourceCondition> recipeConditions = new ArrayList<>();

    public RailwaysSequencedAssemblyRecipeBuilder(Identifier id) {
        this.id = id;
    }

    public RailwaysSequencedAssemblyRecipeBuilder require(ItemLike item) {
        return require(Ingredient.of(item));
    }

    public RailwaysSequencedAssemblyRecipeBuilder require(TagKey<Item> tag) {
        return require(Ingredient.of(HolderSet.emptyNamed(BuiltInRegistries.ITEM, tag)));
    }

    public RailwaysSequencedAssemblyRecipeBuilder require(Ingredient ingredient) {
        this.ingredient = ingredient;
        return this;
    }

    public RailwaysSequencedAssemblyRecipeBuilder transitionTo(ItemLike item) {
        this.transitionalItem = new ItemStack(item);
        return this;
    }

    public RailwaysSequencedAssemblyRecipeBuilder transitionTo(ItemStack stack) {
        this.transitionalItem = stack;
        return this;
    }

    public RailwaysSequencedAssemblyRecipeBuilder transitionTo(ProcessingOutput output) {
        this.transitionalItem = output.create();
        return this;
    }

    public RailwaysSequencedAssemblyRecipeBuilder loops(int loops) {
        this.loops = loops;
        return this;
    }

    public RailwaysSequencedAssemblyRecipeBuilder addOutput(ItemLike item, float chance) {
        return addOutput(new ItemStack(item), chance);
    }

    public RailwaysSequencedAssemblyRecipeBuilder addOutput(ItemStack stack, float chance) {
        this.result = new ProcessingOutput(
            stack.typeHolder(),
            stack.getCount(),
            stack.getComponentsPatch(),
            chance
        );
        return this;
    }

    public RailwaysSequencedAssemblyRecipeBuilder conditionalMaterial(TrackMaterial trackMaterial) {
        String namespace = CRTrackMaterials.id(trackMaterial).getNamespace();
        if (!Railways.MOD_ID.equals(namespace)) {
            this.recipeConditions.add(ResourceConditions.allModsLoaded(namespace));
        }
        return this;
    }

    public RailwaysSequencedAssemblyRecipeBuilder addCondition(ResourceCondition condition) {
        this.recipeConditions.add(condition);
        return this;
    }

    @FunctionalInterface
    public interface CuttingFactory<T extends Recipe<?>> {
        T create(int time, List<ProcessingOutput> results, Ingredient ingredient);
    }

    @FunctionalInterface
    public interface PressingFactory<T extends Recipe<?>> {
        T create(List<ProcessingOutput> results, Ingredient ingredient);
    }

    @FunctionalInterface
    public interface DeployerFactory<T extends Recipe<?>> {
        T create(List<ProcessingOutput> results, boolean keepHeldItem, Ingredient target, Ingredient ingredient);
    }

    public <T extends Recipe<?>> RailwaysSequencedAssemblyRecipeBuilder addStep(
        CuttingFactory<T> factory,
        UnaryOperator<StepBuilder> config
    ) {
        StepBuilder builder = config.apply(new StepBuilder());
        this.steps.add(transitional -> factory.create(
            builder.time,
            List.of(new ProcessingOutput(transitional)),
            Ingredient.of(transitional.getItem())
        ));
        return this;
    }

    public <T extends Recipe<?>> RailwaysSequencedAssemblyRecipeBuilder addStep(
        PressingFactory<T> factory,
        UnaryOperator<StepBuilder> config
    ) {
        StepBuilder builder = config.apply(new StepBuilder());
        this.steps.add(transitional -> factory.create(
            List.of(new ProcessingOutput(transitional)),
            Ingredient.of(transitional.getItem())
        ));
        return this;
    }

    public <T extends Recipe<?>> RailwaysSequencedAssemblyRecipeBuilder addStep(
        DeployerFactory<T> factory,
        UnaryOperator<StepBuilder> config
    ) {
        StepBuilder builder = config.apply(new StepBuilder());
        this.steps.add(transitional -> factory.create(
            List.of(new ProcessingOutput(transitional)),
            builder.keepHeldItem,
            Ingredient.of(transitional.getItem()),
            builder.heldItem
        ));
        return this;
    }

    public void build(RecipeOutput consumer) {
        build(consumer, this.id);
    }

    public void build(RecipeOutput consumer, Identifier id) {
        List<Recipe<?>> sequence = new ArrayList<>();
        for (Function<ItemStack, Recipe<?>> step : this.steps) {
            sequence.add(step.apply(this.transitionalItem));
        }

        SequencedAssemblyRecipe recipe = new SequencedAssemblyRecipe(
            this.ingredient,
            ItemStackTemplate.fromStack(this.transitionalItem),
            this.result,
            Collections.emptyList(),
            this.loops,
            sequence
        );

        if (!this.recipeConditions.isEmpty()) {
            FabricDataGenHelper.addConditions(recipe, this.recipeConditions.toArray(ResourceCondition[]::new));
        }

        consumer.accept(ResourceKey.create(Registries.RECIPE, id), recipe, null);
    }

    public static class StepBuilder {
        private Ingredient heldItem = Ingredient.of();
        private int time = 50;
        private boolean keepHeldItem = false;

        public StepBuilder require(ItemLike item) {
            this.heldItem = Ingredient.of(item);
            return this;
        }

        public StepBuilder require(TagKey<Item> tag) {
            this.heldItem = Ingredient.of(HolderSet.emptyNamed(BuiltInRegistries.ITEM, tag));
            return this;
        }

        public StepBuilder require(Ingredient ingredient) {
            this.heldItem = ingredient;
            return this;
        }

        public StepBuilder processingTime(int time) {
            this.time = time;
            return this;
        }

        public StepBuilder keepHeldItem() {
            this.keepHeldItem = true;
            return this;
        }
    }
}
