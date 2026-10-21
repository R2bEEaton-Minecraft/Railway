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
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.railwayteam.railways.content.custom_tracks.generic_crossing.fabric;

import com.railwayteam.railways.content.custom_tracks.generic_crossing.GenericCrossingBlock;
import com.railwayteam.railways.mixin_interfaces.IGenericCrossingTrackBE;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.client.infrastructure.model.WrapperBlockStateModel;
import com.zurrtum.create.content.trains.track.TrackBlock;
import com.zurrtum.create.content.trains.track.TrackMaterial;
import com.zurrtum.create.content.trains.track.TrackShape;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@Environment(EnvType.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GenericCrossingModel extends WrapperBlockStateModel {

	public GenericCrossingModel(BlockState state, BlockStateModel.UnbakedRoot unbaked) {
		super(state, unbaked);
	}

	@Override
	public void addPartsWithInfo(BlockAndTintGetter world, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
		if (world.getBlockEntity(pos) instanceof IGenericCrossingTrackBE genericCrossing) {
			Pair<TrackMaterial, TrackShape> piece1 = genericCrossing.railways$getFirstCrossingPiece();
			Pair<TrackMaterial, TrackShape> piece2 = genericCrossing.railways$getSecondCrossingPiece();

			if (piece1 != null) {
				BlockStateModel model1 = IGenericCrossingTrackBE.getModel(piece1);
				BlockState state1 = piece1.getFirst().getBlock().defaultBlockState().setValue(TrackBlock.SHAPE, piece1.getSecond());
				WrapperBlockStateModel.addPartsWithInfo(model1, world, pos, state1, random, parts);
			}
			if (piece2 != null) {
				BlockStateModel model2 = IGenericCrossingTrackBE.getModel(piece2);
				BlockState state2 = piece2.getFirst().getBlock().defaultBlockState().setValue(TrackBlock.SHAPE, piece2.getSecond());
				WrapperBlockStateModel.addPartsWithInfo(model2, world, pos, state2, random, parts);
			}
		} else {
			super.addPartsWithInfo(world, pos, state, random, parts);
		}
	}

	@Override
	public Material.Baked particleMaterialWithInfo(BlockAndTintGetter world, BlockPos pos, BlockState state) {
		if (world.getBlockEntity(pos) instanceof IGenericCrossingTrackBE genericCrossing) {
			Pair<TrackMaterial, TrackShape> piece1 = genericCrossing.railways$getFirstCrossingPiece();
			if (piece1 != null) {
				BlockStateModel model1 = IGenericCrossingTrackBE.getModel(piece1);
				if (model1 instanceof WrapperBlockStateModel wrapper) {
					return wrapper.particleMaterialWithInfo(world, pos, state);
				}
				return model1.particleMaterial();
			}
		}
		return super.particleMaterialWithInfo(world, pos, state);
	}

	public static void register() {
		ModelLoadingPlugin.register(context -> context.modifyBlockModelBeforeBake().register((model, modifierContext) -> {
			BlockState state = modifierContext.state();
			if (state.getBlock() instanceof GenericCrossingBlock)
				return new GenericCrossingModel(state, model);
			return model;
		}));
	}
}
