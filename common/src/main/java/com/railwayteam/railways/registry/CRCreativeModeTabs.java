package com.railwayteam.railways.registry;

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.content.conductor.ConductorCapItem;
import com.zurrtum.create.content.processing.sequenced.SequencedAssemblyItem;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class CRCreativeModeTabs {
    @ExpectPlatform
    public static ResourceKey<CreativeModeTab> getBaseTabKey() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static ResourceKey<CreativeModeTab> getTracksTabKey() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static ResourceKey<CreativeModeTab> getPalettesTabKey() {
        throw new AssertionError();
    }

    public static void register() {
    }

    public enum Tabs {
        MAIN(CRCreativeModeTabs::getBaseTabKey),
        TRACK(CRCreativeModeTabs::getTracksTabKey),
        PALETTES(CRCreativeModeTabs::getPalettesTabKey);

        private final Supplier<ResourceKey<CreativeModeTab>> keySupplier;

        Tabs(Supplier<ResourceKey<CreativeModeTab>> keySupplier) {
            this.keySupplier = keySupplier;
        }

        public ResourceKey<CreativeModeTab> getKey() {
            return keySupplier.get();
        }
    }

    public static final class RegistrateDisplayItemsGenerator implements CreativeModeTab.DisplayItemsGenerator {
        private final Tabs tab;

        public RegistrateDisplayItemsGenerator(Tabs tab) {
            this.tab = tab;
        }

        @Override
        public void accept(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
            Predicate<Item> exclusionPredicate = item -> item instanceof SequencedAssemblyItem;
            ResourceKey<CreativeModeTab> tabKey = tab.getKey();
            List<Item> items = new LinkedList<>();

            items.addAll(collectItems(tabKey, item -> item instanceof ConductorCapItem, true, exclusionPredicate));
            items.addAll(collectItems(tabKey, item -> item instanceof BlockItem, true, exclusionPredicate));
            items.addAll(collectItems(tabKey,
                item -> item instanceof BlockItem || item instanceof ConductorCapItem, false, exclusionPredicate));

            addPlatformParityItems(tabKey, items);
            applyOrderings(items, makeOrderings());
            for (Item item : items) {
                output.accept(new ItemStack(item));
            }
        }

        private List<Item> collectItems(ResourceKey<CreativeModeTab> tabKey, Predicate<Item> classifier,
                                        boolean expected, Predicate<Item> exclusionPredicate) {
            List<Item> items = new ArrayList<>();
            for (var entry : Railways.registrate().getAll(Registries.ITEM)) {
                if (!isInCreativeTab(entry, tabKey))
                    continue;
                Item item = entry.get();
                if (item != Items.AIR && classifier.test(item) == expected && !exclusionPredicate.test(item))
                    items.add(item);
            }
            return items;
        }

        private void addPlatformParityItems(ResourceKey<CreativeModeTab> tabKey, List<Item> items) {
            if (!tabKey.equals(Tabs.MAIN.getKey()))
                return;
            for (var entry : Railways.registrate().getAll(Registries.ITEM)) {
                if (BuiltInRegistries.ITEM.getKey(entry.get()).equals(Railways.asResource("fuel_tank")))
                    return;
            }
            addIfMissing(items, CRItems.PAINT_BRUSH.asItem());
            addIfMissing(items, CRItems.EMPTY_PAINT_PITCHER.asItem());
        }

        private static void addIfMissing(List<Item> items, Item item) {
            if (!items.contains(item))
                items.add(item);
        }

        private static List<ItemOrdering> makeOrderings() {
            List<ItemOrdering> orderings = new ArrayList<>();
            orderings.add(ItemOrdering.after(CRBlocks.CONDUCTOR_WHISTLE_FLAG.asItem(), CRItems.ITEM_CONDUCTOR_CAP.get(DyeColor.RED).asItem()));
            orderings.add(ItemOrdering.after(CRItems.REMOTE_LENS.asItem(), CRBlocks.CONDUCTOR_WHISTLE_FLAG.asItem()));
            orderings.add(ItemOrdering.after(CRBlocks.SEMAPHORE.asItem(), CRItems.REMOTE_LENS.asItem()));
            orderings.add(ItemOrdering.after(CRItems.ITEM_BENCHCART.asItem(), CRItems.EMPTY_PAINT_PITCHER.asItem()));
            orderings.add(ItemOrdering.after(CRItems.ITEM_JUKEBOXCART.asItem(), CRItems.ITEM_BENCHCART.asItem()));
            orderings.add(ItemOrdering.after(CRBlocks.CONDUCTOR_VENT.asItem(), CRItems.ITEM_JUKEBOXCART.asItem()));
            return orderings;
        }

        private static void applyOrderings(List<Item> items, List<ItemOrdering> orderings) {
            for (ItemOrdering ordering : orderings) {
                int anchorIndex = items.indexOf(ordering.anchor());
                if (anchorIndex == -1)
                    continue;
                int itemIndex = items.indexOf(ordering.item());
                if (itemIndex != -1) {
                    items.remove(itemIndex);
                    if (itemIndex < anchorIndex)
                        anchorIndex--;
                }
                items.add(anchorIndex + 1, ordering.item());
            }
        }

        private record ItemOrdering(Item item, Item anchor) {
            static ItemOrdering after(Item item, Item anchor) {
                return new ItemOrdering(item, anchor);
            }
        }
    }

    @ExpectPlatform
    private static boolean isInCreativeTab(com.tterrag.registrate.util.entry.RegistryEntry<?> entry, ResourceKey<CreativeModeTab> tab) {
        throw new AssertionError();
    }

    public record TabInfo(ResourceKey<CreativeModeTab> key, CreativeModeTab tab) {
    }

    public static ResourceKey<CreativeModeTab> key(String name) {
        return ResourceKey.create(Registries.CREATIVE_MODE_TAB, Railways.asResource(name));
    }
}
