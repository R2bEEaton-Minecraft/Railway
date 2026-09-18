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
 * Same gap as {@link MixinSlidingDoorVisual}, but for the folding-door Flywheel
 * visual, which looks up its left/right leaf meshes via
 * {@code AllPartialModels.FOLDING_DOORS} - also never populated for Railways' doors.
 */
@Mixin(SlidingDoorVisual.FoldingVisual.class)
public class MixinFoldingDoorVisual {
    @WrapOperation(
        method = "<init>(Lcom/zurrtum/create/client/flywheel/api/visualization/VisualizationContext;Lcom/zurrtum/create/content/decoration/slidingDoor/SlidingDoorBlockEntity;F)V",
        at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;")
    )
    private Object getPalettesFoldingPartial(@SuppressWarnings("rawtypes") Map instance, Object key, Operation<Object> original,
                                             @Local(argsOnly = true) SlidingDoorBlockEntity blockEntity) {
        BlockState state = blockEntity.getBlockState();
        if (state.getBlock() instanceof PalettesSlidingDoorBlock block && block.isFoldingDoor()) {
            return CRBlockPartials.FOLDING_DOORS.get(block.color).get(state.getValue(PalettesSlidingDoorBlock.WINDOWED));
        }
        return original.call(instance, key);
    }
}
