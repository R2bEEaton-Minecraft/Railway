package com.railwayteam.railways.fabric_mixin;

import com.railwayteam.railways.content.buffer.headstock.CopycatHeadstockBlock;
import com.railwayteam.railways.mixin_interfaces.CopycatDuck;
import com.railwayteam.railways.registry.CRBlockEntities;
import com.zurrtum.create.content.decoration.copycat.CopycatBlockEntity;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(value = CopycatBlockEntity.class, remap = false)
public abstract class MixinCopycatBlockEntity extends SmartBlockEntity implements CopycatDuck {
    @Shadow private BlockState material;

    @Shadow protected abstract void redraw();

    private MixinCopycatBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @ModifyArgs(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Lcom/zurrtum/create/foundation/blockEntity/SmartBlockEntity;<init>(Lnet/minecraft/world/level/block/entity/BlockEntityType;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V"
        ),
        require = 0
    )
    private static void railways$useCopycatHeadstockType(Args args) {
        BlockState state = args.get(2);
        if (state.getBlock() instanceof CopycatHeadstockBlock)
            args.set(0, CRBlockEntities.COPYCAT_HEADSTOCK.get());
    }

    // Used by RepaintingTarget: swaps the material without CopycatBlockEntity#setMaterial's block state side effects
    @Override
    public void railways$setMaterialSimple(BlockState material) {
        this.material = material;

        if (level == null || !level.isClientSide()) {
            notifyUpdate();
            return;
        }
        redraw();
    }
}
