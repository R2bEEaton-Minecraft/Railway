package com.railwayteam.railways.content.palettes.painting.fabric;

import com.railwayteam.railways.content.palettes.PalettesColor;
import com.railwayteam.railways.content.palettes.painting.PaintFluid;
import com.railwayteam.railways.content.palettes.painting.PaintPitcherItem;
import com.railwayteam.railways.content.palettes.painting.PitcherColor;
import com.railwayteam.railways.registry.CRFluids;
import com.zurrtum.create.infrastructure.fluids.FluidItemInventoryWrapper;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

import static com.railwayteam.railways.content.palettes.painting.PaintPitcherItem.FLUID_PER_LEVEL;
import static com.railwayteam.railways.content.palettes.painting.PaintPitcherItem.MAX_LEVELS;

/** Create Fly's native item-fluid adapter for paint pitchers. */
class PaintPitcherFluidItemInventory extends FluidItemInventoryWrapper {
    private @Nullable PitcherColor pitcherColor() {
        return stack.getItem() instanceof PaintPitcherItem item ? new PitcherColor(item.getColor()) : null;
    }

    private int levels() {
        return stack.getItem() instanceof PaintPitcherItem item ? item.getLevels(stack) : 0;
    }

    private static @Nullable PitcherColor colorOf(FluidStack fluid) {
        if (fluid.isOf(Fluids.WATER)) return PitcherColor.SANDY_WATER;
        if (!fluid.isOf(CRFluids.PAINT.get())) return null;

        var customData = fluid.getComponentChanges().get(DataComponents.CUSTOM_DATA);
        PalettesColor color = customData == null ? null : customData
            .map(CustomData::copyTag)
            .flatMap(PaintFluid::getColor)
            .orElse(null);
        return color == null ? null : new PitcherColor(color);
    }

    private @Nullable PitcherColor validColor(FluidStack fluid) {
        PitcherColor incoming = colorOf(fluid);
        PitcherColor current = pitcherColor();
        return incoming != null && (current == null || current.equals(incoming)) ? incoming : null;
    }

    private void setFilled(PitcherColor color, int levels) {
        stack = color.getItemEntry().get().copyAsFilledStack(stack, levels);
    }

    @Override
    public int getMaxAmountPerStack() {
        return (int) (MAX_LEVELS * FLUID_PER_LEVEL);
    }

    @Override
    public int insert(FluidStack fluid, int maxAmount) {
        PitcherColor color = validColor(fluid);
        if (color == null) return 0;

        int insertedLevels = (int) Math.min(maxAmount / FLUID_PER_LEVEL, MAX_LEVELS - levels());
        if (insertedLevels <= 0) return 0;
        setFilled(color, levels() + insertedLevels);
        return (int) (insertedLevels * FLUID_PER_LEVEL);
    }

    @Override
    public int extract(FluidStack fluid, int maxAmount) {
        // Item Drains pass back the stack obtained from getStack().  Their
        // component patch may have been copied or normalised on the way, so
        // do not use it to identify the pitcher colour here: the item is the
        // authoritative source of that information.
        PitcherColor color = pitcherColor();
        if (color == null || !matchesContainedFluid(color, fluid)) {
            return 0;
        }

        int extractedLevels = (int) Math.min(maxAmount / FLUID_PER_LEVEL, levels());
        if (extractedLevels <= 0) {
            return 0;
        }
        setFilled(color, levels() - extractedLevels);
        return (int) (extractedLevels * FLUID_PER_LEVEL);
    }

    private static boolean matchesContainedFluid(PitcherColor color, FluidStack fluid) {
        return color.isSandyWater() ? fluid.isOf(Fluids.WATER) : fluid.isOf(CRFluids.PAINT.get());
    }

    @Override
    public FluidStack getStack() {
        if (!(stack.getItem() instanceof PaintPitcherItem item) || levels() == 0) return FluidStack.EMPTY;
        // Create's Item Drain can only hold 1.5 buckets.  Present one bucket
        // at a time so a four-bucket pitcher empties over four drain cycles
        // instead of being rejected because the whole contents do not fit.
        int drainableLevels = Math.min(levels(), MAX_LEVELS / 4);
        if (item.getColor() == null) return new FluidStack(Fluids.WATER, (int) (drainableLevels * FLUID_PER_LEVEL));

        DataComponentPatch components = DataComponentPatch.builder()
            .set(DataComponents.CUSTOM_DATA, CustomData.of(PaintFluid.setColor(new CompoundTag(), item.getColor())))
            .build();
        return new FluidStack(CRFluids.PAINT.get(), drainableLevels * FLUID_PER_LEVEL, components);
    }

    @Override
    public void setStack(FluidStack fluid) {
        PitcherColor color = colorOf(fluid);
        if (color == null || fluid.isEmpty()) {
            stack = new ItemStack(com.railwayteam.railways.registry.CRItems.EMPTY_PAINT_PITCHER.get());
            return;
        }
        setFilled(color, Math.min(MAX_LEVELS, fluid.getAmount() / (int) FLUID_PER_LEVEL));
    }
}
