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

import com.railwayteam.railways.content.buffer.BufferModelUtils;
import com.railwayteam.railways.content.buffer.IDyedBuffer;
import com.railwayteam.railways.content.buffer.headstock.CopycatHeadstockBarsBlock;
import com.railwayteam.railways.content.buffer.headstock.CopycatHeadstockBlock;
import com.railwayteam.railways.registry.CRBlocks;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.foundation.model.BakedModelHelper;
import com.zurrtum.create.client.infrastructure.model.CopycatModel;
import com.zurrtum.create.content.decoration.copycat.CopycatBlock;
import com.zurrtum.create.content.decoration.copycat.CopycatSpecialCases;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
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
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public class CopycatHeadstockBlockStateModel extends CopycatModel {
	private static final AABB CUBE_AABB = new AABB(BlockPos.ZERO);
	private static final Identifier COPYCAT_BASE_TEXTURE = Identifier.fromNamespaceAndPath("create", "block/copycat_base");

	public CopycatHeadstockBlockStateModel(BlockState state, BlockStateModel.UnbakedRoot unbaked) {
		super(state, unbaked);
	}

	@Override
	protected void addPartsWithInfo(BlockAndTintGetter world, BlockPos pos, BlockState state, CopycatBlock block,
									BlockState material, RandomSource random, List<BlockStateModelPart> parts) {
		UnaryOperator<TextureAtlasSprite> colorSwapper = world.getBlockEntity(pos) instanceof IDyedBuffer be
			? BufferModelUtils.getSwapper(be.getColor())
			: null;
		addWrappedParts(random, colorSwapper, parts);

		DirectionData directionData = gatherDirectionData(block, state);
		BlockStateModel materialModel = getModelOf(material);
		List<BlockStateModelPart> materialParts = getMaterialParts(world, pos, material, random, materialModel);

		if (CopycatSpecialCases.isBarsMaterial(material)) {
			BlockState barsState = CRBlocks.COPYCAT_HEADSTOCK_BARS.get().defaultBlockState()
				.setValue(CopycatHeadstockBarsBlock.FACING, state.getOptionalValue(CopycatHeadstockBlock.FACING).orElse(Direction.NORTH))
				.setValue(CopycatHeadstockBarsBlock.UPSIDE_DOWN, state.getOptionalValue(CopycatHeadstockBlock.UPSIDE_DOWN).orElse(false));
			List<BlockStateModelPart> barsParts = new ArrayList<>();
			getModelOf(barsState).collectParts(random, barsParts);
			addBarsParts(directionData, materialModel.particleMaterial().sprite(), materialParts, barsParts, parts);
			return;
		}

		addHeadstockParts(directionData, state, materialParts, parts);
	}

	private void addWrappedParts(RandomSource random, @Nullable UnaryOperator<TextureAtlasSprite> colorSwapper,
								 List<BlockStateModelPart> parts) {
		List<BlockStateModelPart> modelParts = new ArrayList<>();
		model.collectParts(random, modelParts);
		for (BlockStateModelPart part : modelParts) {
			QuadCollection.Builder builder = new QuadCollection.Builder();
			addFilteredWrappedQuads(part.getQuads(null), colorSwapper, builder::addUnculledFace);
			for (Direction direction : Iterate.directions)
				addFilteredWrappedQuads(part.getQuads(direction), colorSwapper, quad -> builder.addCulledFace(direction, quad));
			QuadCollection quads = builder.build();
			if (!quads.getAll().isEmpty())
				parts.add(new SimpleModelWrapper(quads, part.useAmbientOcclusion(), part.particleMaterial()));
		}
	}

	private void addFilteredWrappedQuads(List<BakedQuad> quads, @Nullable UnaryOperator<TextureAtlasSprite> colorSwapper,
										 Consumer<BakedQuad> consumer) {
		List<BakedQuad> filtered = new ArrayList<>(quads.size());
		for (BakedQuad quad : quads) {
			if (!COPYCAT_BASE_TEXTURE.equals(quad.materialInfo().sprite().contents().name()))
				filtered.add(quad);
		}
		if (colorSwapper != null && !filtered.isEmpty())
			filtered = BakedModelHelper.swapSprites(filtered, colorSwapper);
		filtered.forEach(consumer);
	}

	/**
	 * Iron-bars-like materials use the dedicated bars model, re-textured with the material's sprites
	 * (top/bottom faces use the material's top sprite, everything else its particle sprite).
	 */
	private void addBarsParts(DirectionData directionData, TextureAtlasSprite particle,
							  List<BlockStateModelPart> material, List<BlockStateModelPart> bars,
							  List<BlockStateModelPart> parts) {
		TextureAtlasSprite topSprite = particle;
		findTop:
		for (BlockStateModelPart materialPart : material) {
			for (BakedQuad quad : materialPart.getQuads(null)) {
				if (quad.direction() == Direction.UP) {
					topSprite = quad.materialInfo().sprite();
					break findTop;
				}
			}
		}

		for (BlockStateModelPart part : bars) {
			QuadCollection.Builder builder = new QuadCollection.Builder();
			for (BakedQuad quad : part.getQuads(null))
				builder.addUnculledFace(replaceSprite(quad, particle));
			for (Direction direction : Iterate.directions) {
				TextureAtlasSprite sprite = direction.getAxis() == Direction.Axis.Y ? topSprite : particle;
				for (BakedQuad quad : part.getQuads(direction)) {
					if (directionData.isUncull(direction))
						builder.addUnculledFace(replaceSprite(quad, sprite));
					else
						builder.addCulledFace(direction, replaceSprite(quad, sprite));
				}
			}
			parts.add(new SimpleModelWrapper(builder.build(), part.useAmbientOcclusion(), part.particleMaterial()));
		}
	}

	private static BakedQuad replaceSprite(BakedQuad quad, TextureAtlasSprite target) {
		BakedQuad.MaterialInfo info = quad.materialInfo();
		TextureAtlasSprite original = info.sprite();
		return BakedModelHelper.replaceBakedQuadUV(quad,
			BakedModelHelper.calcSpriteUv(quad.packedUV0(), original, target),
			BakedModelHelper.calcSpriteUv(quad.packedUV1(), original, target),
			BakedModelHelper.calcSpriteUv(quad.packedUV2(), original, target),
			BakedModelHelper.calcSpriteUv(quad.packedUV3(), original, target),
			info);
	}

	private void addHeadstockParts(DirectionData directionData, BlockState state,
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
				addCroppedHeadstockQuads(
					facing,
					upsideDown,
					normal,
					normalScaled14,
					part.getQuads(direction),
					directionData.isUncull(direction)
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
