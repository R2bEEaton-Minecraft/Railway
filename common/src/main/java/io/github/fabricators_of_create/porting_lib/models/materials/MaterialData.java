package io.github.fabricators_of_create.porting_lib.models.materials;

import java.util.function.Function;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceLocation;

/**
 * Compatibility copy of the Porting Lib model-material value object expected by
 * Create Fly's transformed vanilla model classes. Porting Lib no longer ships
 * this module on 1.21.8, but the Create Fly binary still references its ABI.
 */
public record MaterialData(int color, boolean emissive, boolean ambientOcclusion, ResourceLocation blendMode) {
	public static final MaterialData DEFAULT =
		new MaterialData(0xFFFFFFFF, false, true, ResourceLocation.withDefaultNamespace("solid"));

	public static final Codec<Integer> COLOR = Codec.either(Codec.INT, Codec.STRING).xmap(
		either -> either.map(Function.identity(), value -> (int) Long.parseLong(value, 16)),
		color -> Either.right(Integer.toHexString(color)));

	public static final Codec<MaterialData> CODEC = RecordCodecBuilder.create(builder -> builder.group(
		COLOR.optionalFieldOf("color", 0xFFFFFFFF).forGetter(MaterialData::color),
		Codec.BOOL.optionalFieldOf("emissive", false).forGetter(MaterialData::emissive),
		Codec.BOOL.optionalFieldOf("ambient_occlusion", true).forGetter(MaterialData::ambientOcclusion),
		ResourceLocation.CODEC.optionalFieldOf("blendMode", ResourceLocation.withDefaultNamespace("solid"))
			.forGetter(MaterialData::blendMode))
		.apply(builder, MaterialData::new));
}
