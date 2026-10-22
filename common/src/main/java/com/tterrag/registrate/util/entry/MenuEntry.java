package com.tterrag.registrate.util.entry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public class MenuEntry<T extends AbstractContainerMenu> extends RegistryEntry<MenuType<T>> {
    public MenuEntry(ResourceLocation id, MenuType<T> value) {
        super(id, value);
    }
}
