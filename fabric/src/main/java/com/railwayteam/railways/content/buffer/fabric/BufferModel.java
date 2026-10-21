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

package com.railwayteam.railways.content.buffer.fabric;

import com.railwayteam.railways.content.buffer.BufferModelUtils;
import com.railwayteam.railways.content.buffer.IDyedBuffer;
import com.railwayteam.railways.content.buffer.IMaterialAdaptingBuffer;
import com.railwayteam.railways.content.buffer.TrackBufferBlock;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.foundation.model.BakedModelHelper;
import com.zurrtum.create.client.infrastructure.model.WrapperBlockStateModel;
import com.zurrtum.create.client.model.NormalsBakedQuad;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import static com.railwayteam.railways.content.buffer.BufferModelUtils.combineSwappers;
import static com.railwayteam.railways.content.buffer.BufferModelUtils.getSwapper;

@Environment(EnvType.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BufferModel extends WrapperBlockStateModel {
	private static final Matrix3f DIAGONAL_TRANSFORM = new Matrix3f().rotationY((float) Math.toRadians(-45));

	public BufferModel(BlockState state, BlockStateModel.UnbakedRoot unbaked) {
		super(state, unbaked);
	}

	@Override
	public void addPartsWithInfo(BlockAndTintGetter world, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
		boolean isDiagonal = state.getBlock() instanceof TrackBufferBlock && state.getValue(TrackBufferBlock.DIAGONAL);

		UnaryOperator<TextureAtlasSprite> materialSwapper = null;
		UnaryOperator<TextureAtlasSprite> colorSwapper = null;

		if (world.getBlockEntity(pos) instanceof IDyedBuffer be) {
			colorSwapper = getSwapper(be.getColor());
		}

		if (world.getBlockEntity(pos) instanceof IMaterialAdaptingBuffer be) {
			materialSwapper = getSwapper(be.getMaterial());
		}

		UnaryOperator<TextureAtlasSprite> swapper = combineSwappers(materialSwapper, colorSwapper);

		if (!isDiagonal && swapper == null) {
			super.addPartsWithInfo(world, pos, state, random, parts);
			return;
		}

		List<BlockStateModelPart> baseParts = new ArrayList<>();
		super.addPartsWithInfo(world, pos, state, random, baseParts);

		for (BlockStateModelPart part : baseParts) {
			QuadCollection.Builder builder = new QuadCollection.Builder();
			processQuads(part.getQuads(null), isDiagonal, swapper, builder::addUnculledFace);
			for (Direction direction : Iterate.directions) {
				Consumer<BakedQuad> consumer = isDiagonal ? builder::addUnculledFace : (q -> builder.addCulledFace(direction, q));
				processQuads(part.getQuads(direction), isDiagonal, swapper, consumer);
			}
			QuadCollection quads = builder.build();
			if (!quads.getAll().isEmpty()) {
				parts.add(new SimpleModelWrapper(quads, part.useAmbientOcclusion(), part.particleMaterial()));
			}
		}
	}

	private static void processQuads(List<BakedQuad> quads, boolean isDiagonal, @Nullable UnaryOperator<TextureAtlasSprite> swapper, Consumer<BakedQuad> consumer) {
		if (quads.isEmpty())
			return;

		List<BakedQuad> processed = quads;
		if (swapper != null) {
			processed = BakedModelHelper.swapSprites(processed, swapper);
		}

		for (BakedQuad quad : processed) {
			if (isDiagonal) {
				consumer.accept(transformDiagonal(quad));
			} else {
				consumer.accept(quad);
			}
		}
	}

	private static BakedQuad transformDiagonal(BakedQuad quad) {
		Vector3f pos0 = transformVertex(quad.position0(), DIAGONAL_TRANSFORM);
		Vector3f pos1 = transformVertex(quad.position1(), DIAGONAL_TRANSFORM);
		Vector3f pos2 = transformVertex(quad.position2(), DIAGONAL_TRANSFORM);
		Vector3f pos3 = transformVertex(quad.position3(), DIAGONAL_TRANSFORM);

		BakedQuad transformed = new BakedQuad(
			pos0, pos1, pos2, pos3,
			quad.packedUV0(), quad.packedUV1(), quad.packedUV2(), quad.packedUV3(),
			quad.direction(), quad.materialInfo()
		);

		if (((Object) quad) instanceof NormalsBakedQuad nSource) {
			Vector3f n0 = new Vector3f(nSource.create$getNormal0()).mul(DIAGONAL_TRANSFORM);
			Vector3f n1 = new Vector3f(nSource.create$getNormal1()).mul(DIAGONAL_TRANSFORM);
			Vector3f n2 = new Vector3f(nSource.create$getNormal2()).mul(DIAGONAL_TRANSFORM);
			Vector3f n3 = new Vector3f(nSource.create$getNormal3()).mul(DIAGONAL_TRANSFORM);
			BakedModelHelper.setNormals(transformed, new Vector3f[]{n0, n1, n2, n3});
		}

		return transformed;
	}

	private static Vector3f transformVertex(Vector3fc vertex, Matrix3f matrix) {
		Vector3f pos = new Vector3f(vertex);
		pos.sub(0.5f, 0.5f, 0.5f);
		matrix.transform(pos);
		pos.add(0.5f, 0.5f, 0.5f);
		return pos;
	}

	public static void register() {
		ModelLoadingPlugin.register(context -> context.modifyBlockModelBeforeBake().register((model, modifierContext) -> {
			BlockState state = modifierContext.state();
			if (state.getBlock() instanceof TrackBufferBlock)
				return new BufferModel(state, model);
			return model;
		}));
	}
}
