package com.railwayteam.railways.registry;

import com.mojang.serialization.MapCodec;
import com.railwayteam.railways.Railways;
import com.railwayteam.railways.content.palettes.painting.PaintPitcherItem;
import com.zurrtum.create.api.equipment.potatoCannon.PotatoCannonProjectileType;
import com.zurrtum.create.api.equipment.potatoCannon.PotatoProjectileBlockHitAction;
import com.zurrtum.create.api.registry.CreateRegistries;
import com.zurrtum.create.api.registry.CreateRegistryKeys;
import com.zurrtum.create.content.equipment.potatoCannon.AllPotatoProjectileEntityHitActions.PotionEffect;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.BlockHitResult;

public class CRPotatoProjectileTypes {
    /** Also available to a registry bootstrap; the shipped data pack defines this same type. */
    public static void bootstrap(BootstrapContext<PotatoCannonProjectileType> context) {
        context.register(ResourceKey.create(CreateRegistryKeys.POTATO_PROJECTILE_TYPE, Railways.asResource("paint_pitcher")),
            new PotatoCannonProjectileType.Builder()
                .damage(3)
                .reloadTicks(15)
                .velocity(1.25f)
                .knockback(1.5f)
                .renderTumbling()
                .onEntityHit(new PotionEffect(MobEffects.POISON, 1, 100, false))
                .onBlockHit(PaintAction.INSTANCE)
                .addItems(CRItems.FILLED_PITCHERS.toArray(ItemEntry[]::new))
                .build());
    }

    public enum PaintAction implements PotatoProjectileBlockHitAction {
        INSTANCE;

        public static final MapCodec<PaintAction> CODEC = MapCodec.unit(INSTANCE);

        @Override
        public boolean execute(LevelAccessor level, ItemStack projectile, BlockHitResult hit) {
            if (projectile.getItem() instanceof PaintPitcherItem pitcher) {
                pitcher.projectilePaint(projectile, level, hit);
                return true;
            }
            return false;
        }

        @Override
        public MapCodec<? extends PotatoProjectileBlockHitAction> codec() {
            return CODEC;
        }
    }

    public static void register() {
        Registry.register(CreateRegistries.POTATO_PROJECTILE_BLOCK_HIT_ACTION,
            Railways.asResource("paint"), PaintAction.CODEC);
    }
}
