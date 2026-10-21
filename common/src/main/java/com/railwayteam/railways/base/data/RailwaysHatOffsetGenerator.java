/*
 * Steam 'n' Rails
 * Copyright (c) 2024 The Railways Team
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
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.railwayteam.railways.base.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class RailwaysHatOffsetGenerator implements DataProvider {
    private final PackOutput packOutput;

    public RailwaysHatOffsetGenerator(PackOutput packOutput) {
        this.packOutput = packOutput;
    }

    @Override
    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput output) {
        JsonObject json = new JsonObject();
        JsonArray offset = new JsonArray();
        offset.add(0.0);
        offset.add(-1.0);
        offset.add(0.0);
        json.add("offset", offset);

        Path path = packOutput.getOutputFolder().resolve("assets/railways/train_hat_info/conductor.json");
        return DataProvider.saveStable(output, json, path);
    }

    @Override
    public @NotNull String getName() {
        return "Steam 'n' Rails Train Hat Information";
    }
}
