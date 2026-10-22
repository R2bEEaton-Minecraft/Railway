/*
 * Steam 'n' Rails
 * Copyright (c) 2026 The Railways Team
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

package com.railwayteam.railways.fabric_mixin.client;

import com.zurrtum.create.client.AllExtensions;
import com.zurrtum.create.client.infrastructure.model.WrapperBlockStateModel;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.Predicate;

@Mixin(WrapperBlockStateModel.class)
public abstract class MixinWrapperBlockStateModel implements BlockStateModel {
    @Override
    public void emitQuads(QuadEmitter emitter, BlockAndTintGetter world, BlockPos pos, BlockState state,
                          RandomSource random, Predicate<@Nullable Direction> cullTest) {
        var layerProvider = AllExtensions.LAYER.get(state.getBlock());
        if (layerProvider == null) {
            BlockStateModel.super.emitQuads(emitter, world, pos, state, random, cullTest);
            return;
        }

        // Create's SectionCompiler layer hook is bypassed by Fabric's renderer.
        // Give default-layer quads the same dynamic layer, preserving explicit overrides.
        var layer = layerProvider.apply(world, pos);
        emitter.pushTransform(quad -> {
            if (quad.renderLayer() == null)
                quad.renderLayer(layer);
            return true;
        });
        try {
            BlockStateModel.super.emitQuads(emitter, world, pos, state, random, cullTest);
        } finally {
            emitter.popTransform();
        }
    }
}
