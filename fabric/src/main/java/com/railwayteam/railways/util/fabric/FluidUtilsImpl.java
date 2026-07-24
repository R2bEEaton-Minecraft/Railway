package com.railwayteam.railways.util.fabric;

import com.zurrtum.create.content.processing.recipe.ProcessingRecipe;
import com.zurrtum.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

public class FluidUtilsImpl {
    public static boolean canUseAsFuelStorage(BlockEntity be) {
        return false;
    }

    public static Fluid getFluid(Object o) {
        if (o instanceof FluidVariant fluidVariant)
            return fluidVariant.getFluid();
        return Fluids.EMPTY;
    }

    public static void addFluidOutput(ProcessingRecipeBuilder<ProcessingRecipe<?>> b, Fluid fluid, long amount, @Nullable CompoundTag nbt) {
        DataComponentPatch components = nbt == null || nbt.isEmpty()
            ? DataComponentPatch.EMPTY
            : DataComponentPatch.builder()
                .set(DataComponents.CUSTOM_DATA, CustomData.of(nbt))
                .build();
        b.withFluidOutputs(new FluidStack(fluid, Math.toIntExact(amount), components));
    }
}
