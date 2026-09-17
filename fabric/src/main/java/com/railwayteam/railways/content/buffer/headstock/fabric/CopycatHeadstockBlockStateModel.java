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

package com.railwayteam.railways.content.buffer.headstock.fabric;

import com.railwayteam.railways.content.buffer.headstock.CopycatHeadstockBlock;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.foundation.model.BakedModelHelper;
import com.zurrtum.create.client.infrastructure.model.CopycatModel;
import com.zurrtum.create.content.decoration.copycat.CopycatBlock;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CopycatHeadstockBlockStateModel extends CopycatModel {
	private static final AABB CUBE_AABB = new AABB(BlockPos.ZERO);
	private static final Identifier COPYCAT_BASE_TEXTURE = Identifier.fromNamespaceAndPath("create", "block/copycat_base");

	public CopycatHeadstockBlockStateModel(BlockState state, BlockStateModel.UnbakedRoot unbaked) {
		super(state, unbaked);
	}

	@Override
	protected void addPartsWithInfo(BlockAndTintGetter world, BlockPos pos, BlockState state, CopycatBlock block,
									BlockState material, RandomSource random, List<BlockStateModelPart> parts) {
		addWrappedParts(random, parts);
		DirectionData directionData = gatherDirectionData(block, state);
		addHeadstockParts(directionData, state, block, getMaterialParts(world, pos, material, random, getModelOf(material)), parts);
	}

	private void addWrappedParts(RandomSource random, List<BlockStateModelPart> parts) {
		List<BlockStateModelPart> modelParts = new ArrayList<>();
		model.collectParts(random, modelParts);
		for (BlockStateModelPart part : modelParts) {
			QuadCollection.Builder builder = new QuadCollection.Builder();
			addFilteredWrappedQuads(part.getQuads(null), builder::addUnculledFace);
			for (Direction direction : Iterate.directions)
				addFilteredWrappedQuads(part.getQuads(direction), quad -> builder.addCulledFace(direction, quad));
			QuadCollection quads = builder.build();
			if (!quads.getAll().isEmpty())
				parts.add(new SimpleModelWrapper(quads, part.useAmbientOcclusion(), part.particleMaterial()));
		}
	}

	private void addFilteredWrappedQuads(List<BakedQuad> quads, Consumer<BakedQuad> consumer) {
		for (BakedQuad quad : quads) {
			if (!COPYCAT_BASE_TEXTURE.equals(quad.materialInfo().sprite().contents().name()))
				consumer.accept(quad);
		}
	}

	private void addHeadstockParts(DirectionData directionData, BlockState state, CopycatBlock block,
								   List<BlockStateModelPart> original, List<BlockStateModelPart> parts) {
		if (original.isEmpty())
			return;

		Direction facing = state.getOptionalValue(CopycatHeadstockBlock.FACING).orElse(Direction.NORTH);
		boolean upsideDown = state.getOptionalValue(CopycatHeadstockBlock.UPSIDE_DOWN).orElse(false);
		Vec3 normal = Vec3.atLowerCornerOf(facing.getUnitVec3i());
		Vec3 normalScaled14 = normal.scale(14 / 16f);

		for (BlockStateModelPart part : original) {
			QuadCollection.Builder builder = new QuadCollection.Builder();
			addCroppedHeadstockQuads(facing, upsideDown, normal, normalScaled14, part.getQuads(null), builder::addUnculledFace);
			for (Direction direction : Iterate.directions) {
				if (directionData.isCull(direction))
					continue;
				addCroppedHeadstockQuads(
					facing,
					upsideDown,
					normal,
					normalScaled14,
					part.getQuads(direction),
					block.shouldFaceAlwaysRender(state, direction)
						? builder::addUnculledFace
						: quad -> builder.addCulledFace(direction, quad)
				);
			}
			parts.add(new SimpleModelWrapper(builder.build(), part.useAmbientOcclusion(), part.particleMaterial()));
		}
	}

	private void addCroppedHeadstockQuads(Direction facing, boolean upsideDown, Vec3 normal, Vec3 normalScaled14,
										  List<BakedQuad> quads, Consumer<BakedQuad> consumer) {
		int size = quads.size();
		if (size == 0)
			return;

		for (boolean top : Iterate.trueAndFalse) {
			for (boolean front : Iterate.trueAndFalse) {
				Vec3 offset = normal.scale(front ? 0 : -13 / 16f);
				float contract = 16 - (front ? 1 : 2);
				AABB bb = CUBE_AABB.contract(normal.x * contract / 16, 10 / 16d, normal.z * contract / 16);
				if (!front)
					bb = bb.move(normalScaled14);
				if (top)
					bb = bb.move(0, 10 / 16d, 0);
				else
					offset = offset.add(0, 4 / 16d, 0);
				if (upsideDown)
					offset = offset.add(0, -4 / 16d, 0);

				for (int i = 0; i < size; i++) {
					BakedQuad quad = quads.get(i);
					Direction direction = quad.direction();

					if (front && direction == facing)
						continue;
					if (!front && direction == facing.getOpposite())
						continue;
					if (top && direction == Direction.DOWN)
						continue;
					if (!top && direction == Direction.UP)
						continue;

					consumer.accept(BakedModelHelper.cropAndMove(quad, bb, offset));
				}
			}
		}
	}
}
