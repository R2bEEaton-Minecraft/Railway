package com.railwayteam.railways.registry;

import com.railwayteam.railways.registry.advancement.CRAdvancement;
import com.railwayteam.railways.registry.advancement.CRAdvancement.Builder;
import com.zurrtum.create.AllItems;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import static com.railwayteam.railways.registry.advancement.CRAdvancement.TaskType.SECRET;
import static com.railwayteam.railways.registry.advancement.CRAdvancement.TaskType.SILENT;

public class CRAdvancements implements DataProvider {
    public static final List<CRAdvancement> ENTRIES = new ArrayList<>();

    public static final CRAdvancement ROOT = create("root", b -> b.icon(CRBlocks.HANDCAR)
        .title("Welcome to Steam 'n' Rails")
        .description("Here Be Trains")
        .awardedForFree()
        .special(SILENT));

    public static final CRAdvancement STRANGE_TEA = create("strange_tea", b -> b.icon(AllItems.BUILDERS_TEA)
        .title("That's not Earl Grey")
        .description("Accidentally drink paint")
        .after(ROOT)
        .special(SECRET));

    private static CRAdvancement create(String id, UnaryOperator<Builder> config) {
        return new CRAdvancement(id, config);
    }

    private final PackOutput output;

    public CRAdvancements(PackOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        var pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "advancement");
        List<CompletableFuture<?>> futures = new ArrayList<>();
        Set<Identifier> ids = new HashSet<>();
        Consumer<AdvancementHolder> save = holder -> {
            if (!ids.add(holder.id()))
                throw new IllegalStateException("Duplicate advancement " + holder.id());
            futures.add(DataProvider.saveStable(cache, Advancement.CODEC, holder.value(), pathProvider.json(holder.id())));
        };
        ENTRIES.forEach(entry -> entry.save(save));
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() { return "Steam 'n' Rails' Advancements"; }

    public static void provideLang(BiConsumer<String, String> consumer) {
        ENTRIES.forEach(entry -> entry.provideLang(consumer));
    }

    public static void register() { /* Forces class initialization before CRTriggers.register(). */ }
}
