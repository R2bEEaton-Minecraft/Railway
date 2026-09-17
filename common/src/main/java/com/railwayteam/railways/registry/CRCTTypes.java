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

package com.railwayteam.railways.registry;

import com.railwayteam.railways.Railways;
import com.zurrtum.create.client.foundation.block.connected.CTType;

public class CRCTTypes {
    public static final CTType VERTICAL_PINKMACHINE = new CTType(Railways.asResource("vertical_pinkmachine"), 2, CTType.VERTICAL) {
        @Override
        public int getTextureIndex(int flag) {
            boolean up = (flag & UP_FLAG) != 0;
            boolean down = (flag & DOWN_FLAG) != 0;
            return !up && !down ? 0 : !up ? 3 : !down ? 2 : 1;
        }
    };
}
