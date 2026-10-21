/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2024 The Railways Team
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

package com.railwayteam.railways.content.fuel.psi;

import com.railwayteam.railways.mixin_interfaces.IFuelInventory;
import com.zurrtum.create.api.contraption.storage.fluid.MountedFluidStorageWrapper;
import com.zurrtum.create.content.contraptions.Contraption;
import com.zurrtum.create.content.contraptions.actors.psi.PortableStorageInterfaceBlockEntity;
import com.zurrtum.create.foundation.fluid.FluidTank;
import com.zurrtum.create.infrastructure.fluids.CombinedTankWrapper;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import com.zurrtum.create.infrastructure.fluids.SidedFluidInventory;
import com.zurrtum.create.infrastructure.transfer.SlotRangeCache;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;


public class PortableFuelInterfaceBlockEntity extends PortableStorageInterfaceBlockEntity {

    public final InterfaceFluidHandler capability = new InterfaceFluidHandler();

    public PortableFuelInterfaceBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void startTransferringTo(Contraption contraption, float distance) {
        MountedFluidStorageWrapper fuels = contraption.getStorage() instanceof IFuelInventory fuelInventory
            ? fuelInventory.railways$getFluidFuels()
            : null;
        if (fuels != null)
            capability.setInventory(fuels);
        else
            capability.setEmpty();
        super.startTransferringTo(contraption, distance);
    }

    @Override
    protected void stopTransferring() {
        capability.setEmpty();
        super.stopTransferring();
    }

    /**
     * Exposes only the fuel tanks of the connected contraption, and only while connected.
     */
    public class InterfaceFluidHandler implements SidedFluidInventory {
        private static final FluidTank EMPTY = new FluidTank(0);
        private int[] slots = SlotRangeCache.EMPTY;
        private FluidInventory wrapped = EMPTY;

        @Override
        public int[] getAvailableSlots(@Nullable Direction side) {
            return slots;
        }

        @Override
        public boolean canExtract(int slot, FluidStack stack, @Nullable Direction dir) {
            return wrapped != EMPTY && canTransfer() && ((SidedFluidInventory) wrapped).canExtract(slot, stack, dir);
        }

        @Override
        public boolean canInsert(int slot, FluidStack stack, @Nullable Direction dir) {
            return wrapped != EMPTY && isConnected() && ((SidedFluidInventory) wrapped).canInsert(slot, stack, dir);
        }

        public void setInventory(CombinedTankWrapper wrapped) {
            this.wrapped = wrapped;
            this.slots = wrapped.getAvailableSlots(null);
        }

        public void setEmpty() {
            this.wrapped = EMPTY;
            this.slots = SlotRangeCache.EMPTY;
        }

        public boolean hasInventory() {
            return wrapped != EMPTY;
        }

        @Override
        public int size() {
            return slots.length;
        }

        @Override
        public int getMaxAmountPerStack() {
            return wrapped.getMaxAmountPerStack();
        }

        @Override
        public int getMaxAmount(FluidStack stack) {
            return wrapped.getMaxAmount(stack);
        }

        @Override
        public FluidStack getStack(int slot) {
            return wrapped.getStack(slot);
        }

        @Override
        public void setStack(int slot, FluidStack stack) {
            wrapped.setStack(slot, stack);
        }

        @Override
        public int insert(FluidStack stack, int maxAmount, @Nullable Direction side) {
            if (!isConnected())
                return 0;
            int insert = wrapped.insert(stack, maxAmount, side);
            if (insert != 0)
                markDirty();
            return insert;
        }

        @Override
        public int extract(FluidStack stack, int maxAmount, @Nullable Direction side) {
            if (!canTransfer())
                return 0;
            int extract = wrapped.extract(stack, maxAmount, side);
            if (extract != 0)
                markDirty();
            return extract;
        }

        @Override
        public void markDirty() {
            keepAlive();
        }

        public void keepAlive() {
            onContentTransferred();
        }

        @Override
        public java.util.Iterator<FluidStack> iterator(@Nullable Direction side) {
            return wrapped.iterator(side);
        }
    }
}
