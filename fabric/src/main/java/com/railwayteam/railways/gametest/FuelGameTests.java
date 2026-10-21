package com.railwayteam.railways.gametest;

import com.google.common.collect.ImmutableMap;
import com.railwayteam.railways.config.CRConfigs;
import com.railwayteam.railways.content.fuel.LiquidFuelTrainHandler;
import com.railwayteam.railways.content.fuel.fabric.LiquidFuelTrainHandlerImpl;
import com.railwayteam.railways.content.fuel.psi.PortableFuelInterfaceBlock;
import com.railwayteam.railways.content.fuel.psi.PortableFuelInterfaceBlockEntity;
import com.railwayteam.railways.content.fuel.tank.FuelTankBlock;
import com.railwayteam.railways.content.fuel.tank.FuelTankBlockEntity;
import com.railwayteam.railways.content.fuel.tank.FuelTankMountedStorage;
import com.railwayteam.railways.content.fuel.tank.FuelTankMountedStorageType;
import com.railwayteam.railways.mixin_interfaces.IFuelInventory;
import com.railwayteam.railways.registry.CRBlocks;
import com.railwayteam.railways.registry.fabric.CRBlockEntitiesImpl;
import com.railwayteam.railways.registry.fabric.CRBlocksImpl;
import com.railwayteam.railways.registry.fabric.CRMountedStorageTypesImpl;
import com.railwayteam.railways.util.AbstractionUtils;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.Create;
import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;
import com.zurrtum.create.api.contraption.storage.fluid.MountedFluidStorage;
import com.zurrtum.create.api.contraption.storage.fluid.MountedFluidStorageType;
import com.zurrtum.create.api.contraption.storage.fluid.MountedFluidStorageWrapper;
import com.zurrtum.create.api.registry.CreateRegistries;
import com.zurrtum.create.content.contraptions.MountedStorageManager;
import com.zurrtum.create.content.contraptions.minecart.TrainCargoManager;
import com.zurrtum.create.content.trains.entity.Carriage;
import com.zurrtum.create.content.trains.entity.Train;
import com.zurrtum.create.content.trains.track.TrackBlock;
import com.zurrtum.create.content.trains.track.TrackPropagator;
import com.zurrtum.create.content.trains.track.TrackShape;
import com.zurrtum.create.foundation.fluid.FluidHelper;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class FuelGameTests {
    private static final int BUCKET = 81000;

    private static FuelTankBlockEntity placeTank(GameTestHelper helper, BlockPos pos) {
        helper.setBlock(pos, CRBlocksImpl.FUEL_TANK.get().defaultBlockState());
        return RailwaysGameTestHelper.assertBlockEntity(helper, pos, FuelTankBlockEntity.class);
    }

    private static FluidInventory tankInventory(GameTestHelper helper, BlockPos pos) {
        BlockPos absPos = helper.absolutePos(pos);
        FluidInventory inventory = FluidHelper.getFluidInventory(helper.getLevel(), absPos, null, null, Direction.UP);
        if (inventory == null)
            helper.fail("Fuel tank should expose a fluid inventory at " + pos);
        return inventory;
    }

    @GameTest(structure = RailwaysGameTestHelper.PLATFORM_8X8)
    public void testFuelRegistryIntegrity(GameTestHelper helper) {
        helper.assertTrue(CRBlocksImpl.FUEL_TANK.get() != null, "Fuel tank block must be registered");
        helper.assertTrue(CRBlocksImpl.PORTABLE_FUEL_INTERFACE.get() != null, "Portable fuel interface block must be registered");
        helper.assertTrue(BuiltInRegistries.BLOCK.getKey(CRBlocksImpl.FUEL_TANK.get()).toString().equals("railways:fuel_tank"),
            "Fuel tank must be registered as railways:fuel_tank");
        helper.assertTrue(BuiltInRegistries.BLOCK.getKey(CRBlocksImpl.PORTABLE_FUEL_INTERFACE.get()).toString().equals("railways:portable_fuel_interface"),
            "Portable fuel interface must be registered as railways:portable_fuel_interface");
        helper.assertTrue(CRBlocksImpl.FUEL_TANK.asItem() != Items.AIR, "Fuel tank must have an item");
        helper.assertTrue(CRBlocksImpl.PORTABLE_FUEL_INTERFACE.asItem() != Items.AIR, "Portable fuel interface must have an item");

        helper.assertTrue(CRBlockEntitiesImpl.FUEL_TANK.get() != null, "Fuel tank block entity type must be registered");
        helper.assertTrue(CRBlockEntitiesImpl.PORTABLE_FUEL_INTERFACE.get() != null, "Portable fuel interface block entity type must be registered");
        helper.assertTrue(CRBlockEntitiesImpl.FUEL_TANK.get().isValid(CRBlocksImpl.FUEL_TANK.getDefaultState()),
            "Fuel tank block entity type must accept the fuel tank block");
        helper.assertTrue(CRBlockEntitiesImpl.PORTABLE_FUEL_INTERFACE.get().isValid(CRBlocksImpl.PORTABLE_FUEL_INTERFACE.getDefaultState()),
            "Portable fuel interface block entity type must accept the portable fuel interface block");

        FuelTankMountedStorageType storageType = CRMountedStorageTypesImpl.FUEL_TANK.get();
        helper.assertTrue(storageType != null, "Fuel tank mounted storage type must be registered");
        helper.assertTrue(CreateRegistries.MOUNTED_FLUID_STORAGE_TYPE.getKey(storageType) != null,
            "Fuel tank mounted storage type must be in Create's mounted fluid storage registry");
        helper.assertTrue(MountedFluidStorageType.REGISTRY.get(CRBlocksImpl.FUEL_TANK.get()) == storageType,
            "Fuel tank block must map to the fuel tank mounted storage type");
        helper.assertTrue(MovementBehaviour.REGISTRY.get(CRBlocksImpl.FUEL_TANK.get()) != null,
            "Fuel tank must have a movement behaviour");
        helper.assertTrue(MovementBehaviour.REGISTRY.get(CRBlocksImpl.PORTABLE_FUEL_INTERFACE.get()) != null,
            "Portable fuel interface must have a movement behaviour");

        helper.assertTrue(AbstractionUtils.getFluidTankBlockEntry() == CRBlocksImpl.FUEL_TANK,
            "AbstractionUtils must return the fuel tank entry (used by the recipe generator)");
        helper.assertTrue(AbstractionUtils.getPortableFuelInterfaceBlockEntry() == CRBlocksImpl.PORTABLE_FUEL_INTERFACE,
            "AbstractionUtils must return the portable fuel interface entry (used by the recipe generator)");
        helper.assertTrue(AbstractionUtils.portableFuelInterfaceBlockHasState(CRBlocksImpl.PORTABLE_FUEL_INTERFACE.getDefaultState()),
            "Portable fuel interface state check must match its own block");
        helper.assertFalse(AbstractionUtils.portableFuelInterfaceBlockHasState(AllBlocks.PORTABLE_FLUID_INTERFACE.defaultBlockState()),
            "Portable fuel interface state check must not match Create's portable fluid interface");
        helper.assertTrue(AbstractionUtils.isInstanceOfFuelTankMountedStorageType(storageType),
            "Fuel tank mounted storage type check must match");
        helper.succeed();
    }

    @GameTest(structure = RailwaysGameTestHelper.PLATFORM_8X8)
    public void testFuelTankPlacement(GameTestHelper helper) {
        BlockPos pos = new BlockPos(3, 1, 3);
        FuelTankBlockEntity tank = placeTank(helper, pos);

        helper.assertTrue(AbstractionUtils.isInstanceOfFuelTankBlockEntity(tank), "Placed block entity must be a fuel tank");
        helper.assertTrue(tank.isController(), "A single fuel tank must be its own controller");
        helper.assertTrue(tank.getTotalTankSize() == 1, "A single fuel tank must have a size of 1");
        helper.assertTrue(tank.getTankInventory().getMaxAmountPerStack() == FuelTankBlockEntity.getCapacityMultiplier(),
            "A single fuel tank must hold one tank worth of fluid");
        RailwaysGameTestHelper.assertProperty(helper, pos, FuelTankBlock.TOP, true);
        RailwaysGameTestHelper.assertProperty(helper, pos, FuelTankBlock.BOTTOM, true);
        RailwaysGameTestHelper.assertProperty(helper, pos, FuelTankBlock.SHAPE, FuelTankBlock.Shape.WINDOW);
        helper.succeed();
    }

    @GameTest(structure = RailwaysGameTestHelper.PLATFORM_8X8)
    public void testFuelTankMultiblock(GameTestHelper helper) {
        BlockPos bottom = new BlockPos(3, 1, 3);
        BlockPos top = bottom.above();
        placeTank(helper, bottom);
        placeTank(helper, top);

        helper.succeedWhen(() -> {
            FuelTankBlockEntity bottomTank = RailwaysGameTestHelper.assertBlockEntity(helper, bottom, FuelTankBlockEntity.class);
            FuelTankBlockEntity topTank = RailwaysGameTestHelper.assertBlockEntity(helper, top, FuelTankBlockEntity.class);
            FuelTankBlockEntity controller = bottomTank.getControllerBE();
            helper.assertTrue(controller != null && controller.getHeight() == 2, "Stacked fuel tanks should form a 2-high multiblock");
            helper.assertTrue(topTank.getControllerBE() == controller, "Both fuel tanks should share the same controller");
            helper.assertTrue(controller.getTankInventory().getMaxAmountPerStack() == 2 * FuelTankBlockEntity.getCapacityMultiplier(),
                "A 2-high fuel tank should hold two tanks worth of fluid");
            RailwaysGameTestHelper.assertProperty(helper, bottom, FuelTankBlock.TOP, false);
            RailwaysGameTestHelper.assertProperty(helper, bottom, FuelTankBlock.BOTTOM, true);
            RailwaysGameTestHelper.assertProperty(helper, top, FuelTankBlock.TOP, true);
            RailwaysGameTestHelper.assertProperty(helper, top, FuelTankBlock.BOTTOM, false);
        });
    }

    @GameTest(structure = RailwaysGameTestHelper.PLATFORM_8X8)
    public void testFuelTankAcceptsLiquidFuel(GameTestHelper helper) {
        BlockPos pos = new BlockPos(3, 1, 3);
        FuelTankBlockEntity tank = placeTank(helper, pos);

        helper.assertTrue(LiquidFuelTrainHandler.isFuel(Fluids.LAVA), "Lava should count as a liquid fuel (lava bucket burn time)");
        helper.assertTrue(LiquidFuelTrainHandler.isFuelForTanks(Fluids.LAVA), "Fuel tanks should accept lava");

        int inserted = tankInventory(helper, pos).insert(new FluidStack(Fluids.LAVA, BUCKET), BUCKET);
        helper.assertTrue(inserted == BUCKET, "Fuel tank should accept a bucket of lava, inserted " + inserted);
        FluidStack contents = tank.getTankInventory().getFluid();
        helper.assertTrue(contents.getFluid() == Fluids.LAVA && contents.getAmount() == BUCKET,
            "Fuel tank should contain one bucket of lava");
        helper.assertTrue(tank.getFillState() > 0, "Fuel tank fill state should be positive once filled");
        helper.succeed();
    }

    @GameTest(structure = RailwaysGameTestHelper.PLATFORM_8X8)
    public void testFuelTankRejectsNonFuel(GameTestHelper helper) {
        BlockPos pos = new BlockPos(3, 1, 3);
        placeTank(helper, pos);

        int inserted = tankInventory(helper, pos).insert(new FluidStack(Fluids.WATER, BUCKET), BUCKET);
        if (CRConfigs.server().realism.realisticFuelTanks.get()) {
            helper.assertFalse(LiquidFuelTrainHandler.isFuelForTanks(Fluids.WATER), "Water must not be valid fuel tank contents");
            helper.assertTrue(inserted == 0, "Fuel tank should reject water with realistic fuel tanks enabled, inserted " + inserted);
        } else {
            helper.assertTrue(inserted == BUCKET, "Fuel tank should accept any fluid with realistic fuel tanks disabled");
        }
        helper.succeed();
    }

    @GameTest(structure = RailwaysGameTestHelper.PLATFORM_8X8)
    public void testFuelTankFabricTransferApi(GameTestHelper helper) {
        BlockPos pos = new BlockPos(3, 1, 3);
        placeTank(helper, pos);
        tankInventory(helper, pos).insert(new FluidStack(Fluids.LAVA, BUCKET), BUCKET);

        Storage<FluidVariant> storage = FluidStorage.SIDED.find(helper.getLevel(), helper.absolutePos(pos), Direction.UP);
        helper.assertTrue(storage != null, "Fuel tank should be exposed through the Fabric fluid transfer API");
        boolean foundLava = false;
        for (var view : storage) {
            if (view.getResource().isOf(Fluids.LAVA) && view.getAmount() == BUCKET)
                foundLava = true;
        }
        helper.assertTrue(foundLava, "Fabric fluid storage view should report the bucket of lava");
        helper.succeed();
    }

    @GameTest(structure = RailwaysGameTestHelper.PLATFORM_8X8)
    public void testLiquidFuelDraining(GameTestHelper helper) {
        BlockPos pos = new BlockPos(3, 1, 3);
        FuelTankBlockEntity tank = placeTank(helper, pos);
        tankInventory(helper, pos).insert(new FluidStack(Fluids.LAVA, BUCKET), BUCKET);

        BlockPos absPos = helper.absolutePos(pos);
        BlockState state = helper.getBlockState(pos);
        FuelTankMountedStorage storage = CRMountedStorageTypesImpl.FUEL_TANK.get().mount(helper.getLevel(), state, absPos, tank);
        helper.assertTrue(storage != null, "Fuel tank controller should mount as contraption storage");
        helper.assertTrue(storage.getFluid().getAmount() == BUCKET, "Mounted fuel tank should carry the tank contents");

        MountedFluidStorageWrapper fuels = new MountedFluidStorageWrapper(ImmutableMap.<BlockPos, MountedFluidStorage>of(BlockPos.ZERO, storage));
        int expectedBurnTime = LiquidFuelTrainHandler.handleFuelChecking(new FluidStack(Fluids.LAVA, LiquidFuelTrainHandlerImpl.FUEL_DRAIN_AMOUNT));
        helper.assertTrue(expectedBurnTime > 0, "Lava should provide burn time");

        int burnTime = LiquidFuelTrainHandler.handleFuelDraining(fuels);
        helper.assertTrue(burnTime == expectedBurnTime, "Draining fuel should yield " + expectedBurnTime + " ticks, got " + burnTime);
        helper.assertTrue(storage.getFluid().getAmount() == BUCKET - LiquidFuelTrainHandlerImpl.FUEL_DRAIN_AMOUNT,
            "Draining fuel should consume 100mB from the tank");
        helper.assertTrue(storage.isDirty(), "Draining fuel should mark the mounted tank for client sync");

        // unmounting writes the remaining fuel back into the tank
        storage.unmount(helper.getLevel(), state, absPos, tank);
        helper.assertTrue(tank.getTankInventory().getFluid().getAmount() == BUCKET - LiquidFuelTrainHandlerImpl.FUEL_DRAIN_AMOUNT,
            "Unmounting should restore the remaining fuel into the tank");
        helper.succeed();
    }

    @GameTest(structure = RailwaysGameTestHelper.PLATFORM_8X8)
    public void testMountedStorageExposesFuelTanks(GameTestHelper helper) {
        BlockPos tankPos = new BlockPos(2, 1, 3);
        FuelTankBlockEntity tank = placeTank(helper, tankPos);
        tankInventory(helper, tankPos).insert(new FluidStack(Fluids.LAVA, BUCKET), BUCKET);
        BlockPos createTankPos = new BlockPos(5, 1, 3);
        helper.setBlock(createTankPos, AllBlocks.FLUID_TANK.defaultBlockState());

        MountedStorageManager manager = new MountedStorageManager();
        manager.addBlock(helper.getLevel(), helper.getBlockState(tankPos), helper.absolutePos(tankPos), BlockPos.ZERO, tank);
        manager.addBlock(helper.getLevel(), helper.getBlockState(createTankPos), helper.absolutePos(createTankPos),
            new BlockPos(3, 0, 0), helper.getLevel().getBlockEntity(helper.absolutePos(createTankPos)));
        manager.initialize();

        helper.assertTrue(manager.getFluids().storages.size() == 2, "Both tanks should be mounted as fluid storage");
        MountedFluidStorageWrapper fuels = ((IFuelInventory) manager).railways$getFluidFuels();
        helper.assertTrue(fuels != null, "Contraption storage should expose its fuel tanks");
        helper.assertTrue(fuels.storages.size() == 1 && fuels.storages.containsKey(BlockPos.ZERO),
            "Only the fuel tank (not Create's fluid tank) should count as fuel storage");
        helper.assertTrue(LiquidFuelTrainHandler.handleFuelDraining(fuels) > 0, "Fuel should be drainable from contraption storage");
        helper.succeed();
    }

    @GameTest(structure = RailwaysGameTestHelper.PLATFORM_8X8)
    public void testMountedFuelTankFiltersFluids(GameTestHelper helper) {
        BlockPos pos = new BlockPos(3, 1, 3);
        FuelTankBlockEntity tank = placeTank(helper, pos);
        FuelTankMountedStorage storage = CRMountedStorageTypesImpl.FUEL_TANK.get()
            .mount(helper.getLevel(), helper.getBlockState(pos), helper.absolutePos(pos), tank);
        helper.assertTrue(storage != null, "Empty fuel tank controller should still mount as contraption storage");
        MountedFluidStorageWrapper fuels = new MountedFluidStorageWrapper(ImmutableMap.<BlockPos, MountedFluidStorage>of(BlockPos.ZERO, storage));

        int water = fuels.insert(new FluidStack(Fluids.WATER, BUCKET), BUCKET);
        if (CRConfigs.server().realism.realisticFuelTanks.get())
            helper.assertTrue(water == 0, "Mounted fuel tanks should reject water with realistic fuel tanks enabled, inserted " + water);
        else
            helper.assertTrue(water == BUCKET, "Mounted fuel tanks should accept any fluid with realistic fuel tanks disabled");

        if (water == 0) {
            int lava = fuels.insert(new FluidStack(Fluids.LAVA, BUCKET), BUCKET);
            helper.assertTrue(lava == BUCKET, "Mounted fuel tanks should accept lava, inserted " + lava);
            helper.assertTrue(storage.isDirty(), "Filling a mounted fuel tank should mark it for client sync");
        }
        helper.succeed();
    }

    @GameTest(structure = RailwaysGameTestHelper.PLATFORM_8X8)
    public void testTrainBurnsLiquidFuel(GameTestHelper helper) {
        BlockState trackState = AllBlocks.TRACK.defaultBlockState().setValue(TrackBlock.SHAPE, TrackShape.ZO);
        for (int z = 1; z <= 6; z++) {
            BlockPos trackPos = new BlockPos(3, 1, z);
            helper.setBlock(trackPos, trackState);
            TrackPropagator.onRailAdded(helper.getLevel(), helper.absolutePos(trackPos), helper.getBlockState(trackPos));
        }

        BlockPos tankPos = new BlockPos(6, 1, 6);
        FuelTankBlockEntity tank = placeTank(helper, tankPos);
        tankInventory(helper, tankPos).insert(new FluidStack(Fluids.LAVA, BUCKET), BUCKET);

        BlockPos placePos = new BlockPos(3, 1, 3);
        BlockPos absPlacePos = helper.absolutePos(placePos);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack handcarStack = new ItemStack(CRBlocks.HANDCAR.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, handcarStack);
        BlockHitResult hitResult = new BlockHitResult(Vec3.atCenterOf(absPlacePos), Direction.UP, absPlacePos, false);
        InteractionResult placeResult = handcarStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hitResult));
        helper.assertTrue(placeResult.consumesAction(), "Handcar should be successfully placed on the track");

        Train train = null;
        for (Train t : Create.RAILWAYS.trains.values()) {
            if (t.owner.equals(player.getUUID())) {
                train = t;
                break;
            }
        }
        helper.assertFalse(train == null, "A train should be registered for the player");

        try {
            // give the carriage a cargo hold containing a filled fuel tank
            TrainCargoManager cargo = new TrainCargoManager();
            cargo.addBlock(helper.getLevel(), helper.getBlockState(tankPos), helper.absolutePos(tankPos), BlockPos.ZERO, tank);
            cargo.initialize();
            Carriage carriage = train.carriages.getFirst();
            carriage.storage = cargo;

            int expectedBurnTime = LiquidFuelTrainHandler.handleFuelChecking(new FluidStack(Fluids.LAVA, LiquidFuelTrainHandlerImpl.FUEL_DRAIN_AMOUNT));
            train.fuelTicks = 0;
            train.burnFuel(helper.getLevel());
            helper.assertTrue(train.fuelTicks == expectedBurnTime,
                "Train should gain " + expectedBurnTime + " fuel ticks from liquid fuel, got " + train.fuelTicks);
            FluidStack remaining = cargo.getFluids().storages.get(BlockPos.ZERO) instanceof FuelTankMountedStorage mounted
                ? mounted.getFluid() : FluidStack.EMPTY;
            helper.assertTrue(remaining.getAmount() == BUCKET - LiquidFuelTrainHandlerImpl.FUEL_DRAIN_AMOUNT,
                "Train should drain 100mB of fuel from its fuel tank");
        } finally {
            Create.RAILWAYS.removeTrain(train.id);
        }
        helper.succeed();
    }

    @GameTest(structure = RailwaysGameTestHelper.PLATFORM_8X8)
    public void testPortableFuelInterfacePlacement(GameTestHelper helper) {
        BlockPos pos = new BlockPos(3, 1, 3);
        helper.setBlock(pos, CRBlocksImpl.PORTABLE_FUEL_INTERFACE.get().defaultBlockState()
            .setValue(PortableFuelInterfaceBlock.FACING, Direction.UP));
        helper.assertBlockPresent(CRBlocksImpl.PORTABLE_FUEL_INTERFACE.get(), pos);
        PortableFuelInterfaceBlockEntity be = RailwaysGameTestHelper.assertBlockEntity(helper, pos, PortableFuelInterfaceBlockEntity.class);

        helper.assertFalse(be.isConnected(), "An idle portable fuel interface should not be connected");
        helper.assertFalse(be.capability.hasInventory(), "An idle portable fuel interface should not expose any tanks");
        FluidInventory inventory = FluidHelper.getFluidInventory(helper.getLevel(), helper.absolutePos(pos), null, null, Direction.DOWN);
        helper.assertTrue(inventory == be.capability, "Portable fuel interface should expose its interface handler");
        helper.assertTrue(inventory.insert(new FluidStack(Fluids.LAVA, BUCKET), BUCKET) == 0,
            "An idle portable fuel interface should not accept fluid");
        helper.assertTrue(FluidStorage.SIDED.find(helper.getLevel(), helper.absolutePos(pos), Direction.DOWN) != null,
            "Portable fuel interface should be exposed through the Fabric fluid transfer API");
        helper.assertTrue(helper.getBlockState(pos).getAnalogOutputSignal(helper.getLevel(), helper.absolutePos(pos), Direction.UP) == 0,
            "An idle portable fuel interface should output no comparator signal");
        helper.succeed();
    }
}
