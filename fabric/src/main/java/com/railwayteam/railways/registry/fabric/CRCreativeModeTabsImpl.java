package com.railwayteam.railways.registry.fabric;

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.annotation.multiloader.ImplClass;
import com.railwayteam.railways.content.palettes.PalettesColor;
import com.railwayteam.railways.registry.CRBlocks;
import com.railwayteam.railways.registry.CRCreativeModeTabs.RegistrateDisplayItemsGenerator;
import com.railwayteam.railways.registry.CRCreativeModeTabs.TabInfo;
import com.railwayteam.railways.registry.CRCreativeModeTabs.Tabs;
import com.railwayteam.railways.registry.CRPalettes;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.zurrtum.create.foundation.data.CreateRegistrate;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;

import java.util.function.Supplier;

import static com.railwayteam.railways.registry.CRItems.ITEM_CONDUCTOR_CAP;

@ImplClass
public class CRCreativeModeTabsImpl {

    private static final TabInfo MAIN_TAB = register("main",
        () -> FabricCreativeModeTab.builder()
            .title(Component.translatable("itemGroup.railways"))
            .icon(() -> ITEM_CONDUCTOR_CAP.get(DyeColor.BLUE).asStack())
            .displayItems(new RegistrateDisplayItemsGenerator(Tabs.MAIN))
            .build());

    @SuppressWarnings("Convert2MethodRef")
    private static final TabInfo TRACKS_TAB = register("tracks",
        () -> FabricCreativeModeTab.builder()
            .title(Component.translatable("itemGroup.railways_tracks"))
            .icon(() -> CRBlocks.TRACK_COUPLER.asStack())
            .displayItems(new RegistrateDisplayItemsGenerator(Tabs.TRACK))
            .build());

    private static final TabInfo PALETTES_TAB = register("palettes",
        () -> FabricCreativeModeTab.builder()
            .title(Component.translatable("itemGroup.railways_palettes"))
            .icon(() -> CRPalettes.Styles.BOILER.get(PalettesColor.RED).asStack())
            .displayItems(new RegistrateDisplayItemsGenerator(Tabs.PALETTES))
            .build());

    public static ResourceKey<CreativeModeTab> getBaseTabKey() {
        return MAIN_TAB.key();
    }

    public static ResourceKey<CreativeModeTab> getTracksTabKey() {
        return TRACKS_TAB.key();
    }

    public static ResourceKey<CreativeModeTab> getPalettesTabKey() {
        return PALETTES_TAB.key();
    }

    public static CreativeModeTab getBaseTab() {
        return MAIN_TAB.tab();
    }

    public static CreativeModeTab getTracksTab() {
        return TRACKS_TAB.tab();
    }

    public static CreativeModeTab getPalettesTab() {
        return PALETTES_TAB.tab();
    }

    public static TabInfo register(String name, Supplier<CreativeModeTab> supplier) {
        Identifier id = Railways.asResource(name);
        ResourceKey<CreativeModeTab> key = ResourceKey.create(Registries.CREATIVE_MODE_TAB, id);
        CreativeModeTab tab = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, key, supplier.get());
        return new TabInfo(key, tab);
    }

    public static void useBaseTab() {
        CreateRegistrate registrate = Railways.registrate();
        registrate.setCreativeTab(MAIN_TAB.key());
    }

    public static void useTracksTab() {
        CreateRegistrate registrate = Railways.registrate();
        registrate.setCreativeTab(TRACKS_TAB.key());
    }

    public static void usePalettesTab() {
        CreateRegistrate registrate = Railways.registrate();
        registrate.setCreativeTab(PALETTES_TAB.key());
    }
}
