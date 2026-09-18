package com.railwayteam.railways.fabric_mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.railwayteam.railways.content.palettes.doors.PalettesSlidingDoorBlock;
import com.railwayteam.railways.registry.CRBlockPartials;
import com.zurrtum.create.client.content.decoration.slidingDoor.SlidingDoorVisual;
import com.zurrtum.create.content.decoration.slidingDoor.SlidingDoorBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;

/**
 * The Flywheel visual for plain (non-folding) sliding doors looks up its animated
 * mesh via {@code AllPartialModels.SLIDING_DOORS}, a map Create only populates for
 * its own stock doors. Railways' locometal sliding doors were never registered into
 * it, so the lookup returned null and the instance failed to construct - the door's
 * open/close animation silently never played. This substitutes Railways' own
 * per-color partial instead, mirroring how {@link MixinSlidingDoorRenderer} already
 * does this for the (separate) non-Flywheel renderer.
 */
@Mixin(SlidingDoorVisual.SlidingVisual.class)
public class MixinSlidingDoorVisual {
    @WrapOperation(
        method = "<init>(Lcom/zurrtum/create/client/flywheel/api/visualization/VisualizationContext;Lcom/zurrtum/create/content/decoration/slidingDoor/SlidingDoorBlockEntity;F)V",
        at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;")
    )
    private Object getPalettesSlidingPartial(@SuppressWarnings("rawtypes") Map instance, Object key, Operation<Object> original,
                                             @Local(argsOnly = true) SlidingDoorBlockEntity blockEntity) {
        BlockState state = blockEntity.getBlockState();
        if (state.getBlock() instanceof PalettesSlidingDoorBlock block && !block.isFoldingDoor()) {
            return CRBlockPartials.SLIDING_DOORS.get(block.color).get(state.getValue(PalettesSlidingDoorBlock.WINDOWED));
        }
        return original.call(instance, key);
    }
}
