package com.railwayteam.railways.registry.advancement;

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.registry.CRAdvancements;
import com.railwayteam.railways.registry.CRTriggers;
import com.tterrag.registrate.util.entry.ItemProviderEntry;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.InventoryChangeTrigger;
import net.minecraft.advancements.triggers.ItemUsedOnLocationTrigger;
import net.minecraft.advancements.triggers.PlayerTrigger;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class CRAdvancement {
    private static final Identifier BACKGROUND = Railways.asResource("gui/advancements");
    private static final String LANG = "advancement." + Railways.MOD_ID + ".";
    private static final String SECRET_SUFFIX = "\n\u00a77(Hidden Advancement)";

    private final String id;
    private final Advancement.Builder builder = Advancement.Builder.advancement();
    private final Builder options = new Builder();
    private SimpleRailwaysTrigger builtinTrigger;
    private CRAdvancement parent;
    private AdvancementHolder datagenResult;
    private String title;
    private String description;

    public CRAdvancement(String id, UnaryOperator<Builder> config) {
        this.id = id;
        config.apply(options);
        if (!options.externalTrigger) {
            builtinTrigger = CRTriggers.addSimple(id + "_builtin");
            builder.addCriterion("0", builtinTrigger.instance());
        }
        if (options.type == TaskType.SECRET)
            description += SECRET_SUFFIX;
        CRAdvancements.ENTRIES.add(this);
    }

    public boolean isAlreadyAwardedTo(Player player) {
        if (!(player instanceof ServerPlayer sp))
            return true;
        AdvancementHolder advancement = sp.level().getServer().getAdvancements().get(Railways.asResource(id));
        return advancement == null || sp.getAdvancements().getOrStartProgress(advancement).isDone();
    }

    public void awardTo(Player player) {
        if (!(player instanceof ServerPlayer sp))
            return;
        if (builtinTrigger == null)
            throw new UnsupportedOperationException("Advancement " + id + " uses external triggers");
        builtinTrigger.trigger(sp);
    }

    public void save(Consumer<AdvancementHolder> output) {
        builder.display(new ItemStackTemplate(options.icon.get()), Component.translatable(titleKey()),
            Component.translatable(descriptionKey()).withStyle(style -> style.withColor(0xDBA213)),
            id.equals("root") ? BACKGROUND : null,
            options.type.frame, options.type.toast, options.type.announce, options.type.hide);
        if (parent != null)
            builder.parent(parent.datagenResult);
        datagenResult = builder.save(output, Railways.asResource(id).toString());
    }

    public void provideLang(BiConsumer<String, String> consumer) {
        consumer.accept(titleKey(), title);
        consumer.accept(descriptionKey(), description);
    }

    private String titleKey() { return LANG + id; }
    private String descriptionKey() { return titleKey() + ".desc"; }

    public enum TaskType {
        SILENT(AdvancementType.TASK, false, false, false),
        NORMAL(AdvancementType.TASK, true, false, false),
        NOISY(AdvancementType.TASK, true, true, false),
        EXPERT(AdvancementType.GOAL, true, true, false),
        SECRET(AdvancementType.GOAL, true, true, true);

        private final AdvancementType frame;
        private final boolean toast, announce, hide;

        TaskType(AdvancementType frame, boolean toast, boolean announce, boolean hide) {
            this.frame = frame;
            this.toast = toast;
            this.announce = announce;
            this.hide = hide;
        }
    }

    public class Builder {
        private TaskType type = TaskType.NORMAL;
        private Supplier<Item> icon;
        private boolean externalTrigger;
        private int criterionIndex;

        public Builder special(TaskType type) { this.type = type; return this; }
        public Builder after(CRAdvancement advancement) { parent = advancement; return this; }
        public Builder icon(ItemProviderEntry<?> item) { return iconSupplier(() -> item.asItem()); }
        public Builder icon(ItemLike item) { return iconSupplier(() -> item.asItem()); }
        public Builder icon(ItemStack stack) { return iconSupplier(stack::getItem); }
        private Builder iconSupplier(Supplier<Item> item) { icon = item; return this; }
        public Builder title(String value) { title = value; return this; }
        public Builder description(String value) { description = value; return this; }
        public Builder whenBlockPlaced(Block block) { return externalTrigger(ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(block)); }
        public Builder whenIconCollected() { return externalTrigger(InventoryChangeTrigger.TriggerInstance.hasItems(icon.get())); }
        public Builder whenItemCollected(ItemLike item) { return externalTrigger(InventoryChangeTrigger.TriggerInstance.hasItems(item)); }
        public Builder awardedForFree() { return externalTrigger(PlayerTrigger.TriggerInstance.tick()); }

        public Builder externalTrigger(Criterion<?> criterion) {
            builder.addCriterion(String.valueOf(criterionIndex++), criterion);
            externalTrigger = true;
            return this;
        }
    }
}
