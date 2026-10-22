/*
 * Steam 'n' Rails
 * Copyright (c) 2025 The Railways Team
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

package com.railwayteam.railways.content.custom_tracks.gen_template;

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.registry.CRTrackMaterials;
import com.zurrtum.create.content.trains.track.TrackMaterial;
import net.minecraft.resources.ResourceLocation;

public interface TrackGenTemplate {
    ResourceLocation getTexture(TrackMaterial material, TextureKey key);
    ResourceLocation getParentModel(TrackMaterial material, String model);

    TrackGenTemplate DEFAULT = new Default();
    class Default implements TrackGenTemplate {
        protected Default() {}
        public ResourceLocation getTexture(TrackMaterial material, TextureKey key) {
            if (key == TextureKey.PARTICLE) {
                return CRTrackMaterials.particle(material);
            }

            if (material == CRTrackMaterials.NARROW_GAUGE_ANDESITE || material == CRTrackMaterials.WIDE_GAUGE_ANDESITE) {
                return ResourceLocation.fromNamespaceAndPath("create", "block/" + key.getPath());
            }

            String resName;
            if (CRTrackMaterials.getType(material) == CRTrackMaterials.CRTrackType.NARROW_GAUGE) {
                resName = material.getId().getPath().replaceFirst("_narrow", "");
            } else if (CRTrackMaterials.getType(material) == CRTrackMaterials.CRTrackType.WIDE_GAUGE) {
                resName = material.getId().getPath().replaceFirst("_wide", "");
            } else {
                resName = material.getId().getPath();
            }
            String texturePrefix = "block/track/" + resName + "/";

            return CRTrackMaterials.id(material).withPath(texturePrefix + key.getPrefix() + resName);
        }
        public ResourceLocation getParentModel(TrackMaterial material, String model) {
            ResourceLocation prefix;
            if (CRTrackMaterials.getType(material) == CRTrackMaterials.CRTrackType.NARROW_GAUGE) {
                prefix = Railways.asResource("block/narrow_gauge_base/");
            } else if (CRTrackMaterials.getType(material) == CRTrackMaterials.CRTrackType.WIDE_GAUGE) {
                prefix = Railways.asResource("block/wide_gauge_base/");
            } else {
                prefix = ResourceLocation.fromNamespaceAndPath("create", "block/track/");
            }

            return prefix.withSuffix(model);
        }
    }
}
