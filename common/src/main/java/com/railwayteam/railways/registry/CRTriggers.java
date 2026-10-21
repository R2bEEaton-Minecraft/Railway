package com.railwayteam.railways.registry;

import com.railwayteam.railways.registry.advancement.CriterionTriggerBase;
import com.railwayteam.railways.registry.advancement.SimpleRailwaysTrigger;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.ArrayList;
import java.util.List;

public class CRTriggers {
    private static final List<CriterionTriggerBase<?>> TRIGGERS = new ArrayList<>();

    public static SimpleRailwaysTrigger addSimple(String id) {
        SimpleRailwaysTrigger trigger = new SimpleRailwaysTrigger(id);
        TRIGGERS.add(trigger);
        return trigger;
    }

    public static void register() {
        for (CriterionTriggerBase<?> trigger : TRIGGERS)
            Registry.register(BuiltInRegistries.TRIGGER_TYPES, trigger.getId(), trigger);
    }
}
