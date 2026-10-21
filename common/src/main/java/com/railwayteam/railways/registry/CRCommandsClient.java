package com.railwayteam.railways.registry;

import com.mojang.brigadier.CommandDispatcher;
import com.railwayteam.railways.registry.commands.*;
import com.railwayteam.railways.util.Utils;
import net.minecraft.commands.SharedSuggestionProvider;

import static com.railwayteam.railways.multiloader.ClientCommands.literal;

public class CRCommandsClient {
    public static void register(CommandDispatcher<SharedSuggestionProvider> dispatcher) {
        var command = literal("railways_client")
            .requires(source -> net.minecraft.commands.Commands.hasPermission(net.minecraft.commands.Commands.LEVEL_ALL).test(source))
            .then(ClearCasingCacheCommand.register())
            .then(ClearCapCacheCommand.register())
            .then(ReloadDevCapesCommand.register())
            .then(IdentifyTrainCommand.register());
        if (Utils.isDevEnv() || Boolean.getBoolean("railways.force_mixin_audit_command"))
            command.then(MixinAuditCommand.register());

        var root = dispatcher.register(command);
        if (dispatcher.findNode(java.util.Collections.singleton("snrc")) == null)
            dispatcher.register(literal("snrc").redirect(root));
    }
}
