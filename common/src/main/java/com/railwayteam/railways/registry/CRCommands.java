package com.railwayteam.railways.registry;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.railwayteam.railways.Railways;
import com.railwayteam.railways.RailwaysBuildInfo;
import com.railwayteam.railways.registry.commands.*;
import com.railwayteam.railways.util.Utils;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;

import static net.minecraft.commands.Commands.literal;

public class CRCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean dedicated, CommandBuildContext context) {
        var command = literal(Railways.MOD_ID)
            .requires(source -> net.minecraft.commands.Commands.hasPermission(net.minecraft.commands.Commands.LEVEL_ALL).test(source))
            .then(SplitTrainCommand.register())
            .then(TrainInfoCommand.register())
            .then(ShadowRealmCommand.register())
            .then(FillPaintCommand.register(context))
            .then(CountPaintCommand.register(context));

        if (Utils.isDevEnv() || RailwaysBuildInfo.INCLUDE_DEV_COMMANDS) {
            command.then(TrackDemoCommand.register())
                .then(ConductorDemoCommand.register())
                .then(ReloadCasingCollisionCommand.register())
                .then(ReloadCreativeTabsCommand.register())
                .then(PalettesDemoCommand.register());
        }

        LiteralCommandNode<CommandSourceStack> root = dispatcher.register(command);
        if (dispatcher.findNode(java.util.Collections.singleton("snr")) == null)
            dispatcher.register(literal("snr").redirect(root));
    }
}
