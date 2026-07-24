package com.railwayteam.railways.registry.fabric;

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.content.palettes.PalettesColor;
import com.railwayteam.railways.content.palettes.painting.PaintFluid;
import com.railwayteam.railways.registry.CRFluids;
import com.zurrtum.create.client.AllFluidConfigs;
import com.zurrtum.create.client.infrastructure.fluid.FluidConfig;
import com.zurrtum.create.content.fluids.VirtualFluid;
import com.tterrag.registrate.util.entry.FluidEntry;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributeHandler;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.AtlasIds;
import net.minecraft.world.item.component.CustomData;

public class CRFluidsImpl {
    public static FluidEntry<VirtualFluid> registerPaint() {
        VirtualFluid paint = Registry.register(BuiltInRegistries.FLUID, Railways.asResource("paint"), new VirtualFluid());
        return new FluidEntry<>(Railways.asResource("paint"), paint);
    }

    public static void initRendering() {
        AllFluidConfigs.ALL.put(CRFluids.PAINT.get(), new FluidConfig(
            () -> Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS)
                .getSprite(Railways.asResource("fluid/paint_still/white")),
            () -> Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS)
                .getSprite(Railways.asResource("fluid/paint_flow/white")),
            CRFluidsImpl::getPaintColor
        ));

        FluidVariantAttributes.register(CRFluids.PAINT.get(), new FluidVariantAttributeHandler() {
            @Override
            public net.minecraft.network.chat.Component getName(FluidVariant variant) {
                return getPaintPaletteColor(variant.getComponents())
                    .map(PalettesColor::getPaintName)
                    .orElseGet(() -> net.minecraft.network.chat.Component.translatable("fluid.railways.paint"));
            }
        });
    }

    private static int getPaintColor(DataComponentPatch components) {
        return getPaintPaletteColor(components)
            .map(PalettesColor::getDiffuseColor)
            .orElse(0xFFFFFF);
    }

    private static java.util.Optional<PalettesColor> getPaintPaletteColor(DataComponentPatch components) {
        var customData = components.get(DataComponents.CUSTOM_DATA);
        return customData == null ? java.util.Optional.empty() : customData
            .map(CustomData::copyTag)
            .flatMap(PaintFluid::getColor);
    }
}
