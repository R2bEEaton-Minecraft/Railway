/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.fabric_mixin;

import com.railwayteam.railways.registry.CREdgePointTypes;
import com.zurrtum.create.content.trains.graph.EdgePointType;
import com.zurrtum.create.content.trains.signal.SignalBoundary;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * SignalBoundary overrides canCoexistWith, so it needs its own exception for couplers and switches.
 */
@Mixin(value = SignalBoundary.class, priority = 1023, remap = false)
public abstract class MixinSignalBoundary {
    @Inject(method = "canCoexistWith", at = @At("RETURN"), cancellable = true)
    private void railways$switchOrCouplerCanCoexist(EdgePointType<?> otherType, boolean front, CallbackInfoReturnable<Boolean> cir) {
        if (otherType == CREdgePointTypes.COUPLER || otherType == CREdgePointTypes.SWITCH)
            cir.setReturnValue(true);
    }
}
