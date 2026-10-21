package com.zurrtum.create.content.processing.recipe;

import com.zurrtum.create.foundation.fluid.FluidIngredient;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.List;

public class ProcessingRecipeBuilder<T> {
    protected final ProcessingRecipeFactory<T> factory;
    protected final Identifier id;
    protected final List<SizedIngredient> ingredients = new ArrayList<>();
    protected final List<ProcessingOutput> results = new ArrayList<>();
    protected final List<FluidIngredient> fluidIngredients = new ArrayList<>();
    protected final List<FluidStack> fluidResults = new ArrayList<>();
    protected HeatCondition heat = HeatCondition.NONE;
    protected int duration = 100;
    protected boolean keepHeldItem = false;

    public ProcessingRecipeBuilder(ProcessingRecipeFactory<T> factory, Identifier id) {
        this.factory = factory;
        this.id = id;
    }

    public ProcessingRecipeBuilder<T> withItemIngredients(Ingredient ingredient) {
        return require(ingredient);
    }

    public ProcessingRecipeBuilder<T> require(Ingredient ingredient) {
        this.ingredients.add(new SizedIngredient(ingredient, 1));
        return this;
    }

    public ProcessingRecipeBuilder<T> require(ItemLike item) {
        return require(Ingredient.of(item));
    }

    public ProcessingRecipeBuilder<T> require(TagKey<Item> tag) {
        return require(Ingredient.of(HolderSet.emptyNamed(BuiltInRegistries.ITEM, tag)));
    }

    public ProcessingRecipeBuilder<T> require(FluidIngredient fluid) {
        this.fluidIngredients.add(fluid);
        return this;
    }

    public ProcessingRecipeBuilder<T> output(ItemLike item) {
        this.results.add(new ProcessingOutput(item.asItem(), 1));
        return this;
    }

    public ProcessingRecipeBuilder<T> output(ItemLike item, int count) {
        this.results.add(new ProcessingOutput(item.asItem(), count));
        return this;
    }

    public ProcessingRecipeBuilder<T> output(ProcessingOutput output) {
        this.results.add(output);
        return this;
    }

    public ProcessingRecipeBuilder<T> withFluidOutputs(FluidStack stack) {
        this.fluidResults.add(stack);
        return this;
    }

    public ProcessingRecipeBuilder<T> withFluidOutputs(Object stack) {
        if (stack instanceof FluidStack fluidStack) {
            this.fluidResults.add(fluidStack);
        }
        return this;
    }

    public ProcessingRecipeBuilder<T> requiresHeat(HeatCondition heat) {
        this.heat = heat;
        return this;
    }

    public ProcessingRecipeBuilder<T> duration(int duration) {
        this.duration = duration;
        return this;
    }

    public ProcessingRecipeBuilder<T> keepHeldItem() {
        this.keepHeldItem = true;
        return this;
    }

    public void build(RecipeOutput consumer) {
        T recipe = this.factory.create(new ProcessingRecipeParams(
            this.id,
            this.ingredients,
            this.results,
            this.fluidIngredients,
            this.fluidResults,
            this.heat,
            this.duration,
            this.keepHeldItem
        ));
        if (recipe instanceof Recipe<?> r) {
            consumer.accept(ResourceKey.create(Registries.RECIPE, this.id), r, null);
        }
    }

    @FunctionalInterface
    public interface ProcessingRecipeFactory<T> {
        T create(ProcessingRecipeParams params);
    }

    public record ProcessingRecipeParams(
        Identifier id,
        List<SizedIngredient> ingredients,
        List<ProcessingOutput> results,
        List<FluidIngredient> fluidIngredients,
        List<FluidStack> fluidResults,
        HeatCondition heat,
        int duration,
        boolean keepHeldItem
    ) {}
}
