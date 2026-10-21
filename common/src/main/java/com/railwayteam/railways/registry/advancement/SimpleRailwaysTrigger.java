package com.railwayteam.railways.registry.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.advancements.predicates.ContextAwarePredicate;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;

import java.util.Optional;

public class SimpleRailwaysTrigger extends CriterionTriggerBase<SimpleRailwaysTrigger.Instance> {
    public SimpleRailwaysTrigger(String id) {
        super(id);
    }

    @Override
    public Codec<Instance> codec() {
        return Instance.CODEC;
    }

    public Criterion<Instance> instance() {
        return createCriterion(new Instance(Optional.empty()));
    }

    public record Instance(Optional<ContextAwarePredicate> player) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<Instance> CODEC = MapCodec.unit(new Instance(Optional.empty())).codec();
    }
}
