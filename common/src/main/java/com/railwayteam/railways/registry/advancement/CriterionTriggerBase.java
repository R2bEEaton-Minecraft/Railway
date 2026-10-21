package com.railwayteam.railways.registry.advancement;

import com.railwayteam.railways.Railways;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

/** Base for Railway triggers whose criterion has no event-specific conditions. */
public abstract class CriterionTriggerBase<T extends SimpleCriterionTrigger.SimpleInstance> extends SimpleCriterionTrigger<T> {
    private final Identifier id;

    protected CriterionTriggerBase(String id) {
        this.id = Railways.asResource(id);
    }

    public Identifier getId() {
        return id;
    }

    public void trigger(ServerPlayer player) {
        super.trigger(player, instance -> true);
    }
}
