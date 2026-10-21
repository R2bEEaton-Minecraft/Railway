/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.\n */

package com.railwayteam.railways.base.data.lang;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.railwayteam.railways.registry.CRAdvancements;
import com.railwayteam.railways.registry.CRPalettes;
import com.railwayteam.railways.registry.CRTags;
import com.zurrtum.create.foundation.utility.FilesHelper;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class CRLangGen implements DataProvider {
    private final PackOutput packOutput;

    public CRLangGen(PackOutput packOutput) {
        this.packOutput = packOutput;
    }

    @Override
    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput output) {
        Map<String, String> entries = new TreeMap<>();

        // Load existing en_us.json if available so pre-existing translations are retained
        try {
            JsonElement existing = FilesHelper.loadJsonResource("assets/railways/lang/en_us.json");
            if (existing != null && existing.isJsonObject()) {
                for (Map.Entry<String, JsonElement> entry : existing.getAsJsonObject().entrySet()) {
                    entries.put(entry.getKey(), entry.getValue().getAsString());
                }
            }
        } catch (Exception ignored) {
        }

        BiConsumer<String, String> langConsumer = entries::put;

        provideDefaultLang("interface", langConsumer);
        provideDefaultLang("tooltips", langConsumer);
        provideDefaultLang("commands", langConsumer);
        CRAdvancements.provideLang(langConsumer);
        CRTags.provideLangEntries(langConsumer);
        CRPalettes.provideLangEntries(langConsumer);

        JsonObject jsonObject = new JsonObject();
        for (Map.Entry<String, String> entry : entries.entrySet()) {
            jsonObject.addProperty(entry.getKey(), entry.getValue());
        }

        Path path = packOutput.getOutputFolder().resolve("assets/railways/lang/en_us.json");
        return DataProvider.saveStable(output, jsonObject, path);
    }

    @Override
    public @NotNull String getName() {
        return "Steam 'n' Rails Language Provider";
    }

    private static void provideDefaultLang(String fileName, BiConsumer<String, String> consumer) {
        String path = "assets/railways/lang/default/" + fileName + ".json";
        JsonElement jsonElement = FilesHelper.loadJsonResource(path);
        if (jsonElement == null) {
            throw new IllegalStateException(String.format("Could not find default lang file: %s", path));
        }
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue().getAsString();
            consumer.accept(key, value);
        }
    }
}
