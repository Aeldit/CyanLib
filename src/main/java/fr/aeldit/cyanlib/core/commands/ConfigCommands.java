/*
 * Copyright (c) 2023  -  Made by Aeldit
 *
 *              GNU LESSER GENERAL PUBLIC LICENSE
 *                  Version 3, 29 June 2007
 *
 *  Copyright (C) 2007 Free Software Foundation, Inc. <https://fsf.org/>
 *  Everyone is permitted to copy and distribute verbatim copies
 *  of this license document, but changing it is not allowed.
 *
 *
 * This version of the GNU Lesser General Public License incorporates
 * the terms and conditions of version 3 of the GNU General Public
 * License, supplemented by the additional permissions listed in the LICENSE.txt file
 * in the repo of this mod (https://github.com/Aeldit/CyanSetHome)
 */

package fr.aeldit.cyanlib.core.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import fr.aeldit.cyanlib.core.commands.arguments.ArgumentSuggestion;
import fr.aeldit.cyanlib.lib.CyanLibCommands;
import fr.aeldit.cyanlib.lib.CyanLibConfig;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;

import static fr.aeldit.cyanlib.core.utils.Utils.LibUtils;
import static fr.aeldit.cyanlib.core.utils.Utils.getDefaultTranslations;

public class ConfigCommands
{
    public static void register(@NotNull CommandDispatcher<ServerCommandSource> dispatcher)
    {
        dispatcher.register(CommandManager.literal("cyanlib")
                .then(CommandManager.literal("config")
                        .then(CommandManager.argument("optionName", StringArgumentType.string())
                                .suggests((context, builder) -> ArgumentSuggestion.getOptions(builder))
                                .then(CommandManager.literal("set")
                                        .then(CommandManager.argument("booleanValue", BoolArgumentType.bool())
                                                .then(CommandManager.argument("mode", BoolArgumentType.bool())
                                                        .executes(ConfigCommands::setBoolOption)
                                                )
                                                .executes(ConfigCommands::setBoolOptionFromCommand)
                                        )
                                        .then(CommandManager.argument("integerValue", IntegerArgumentType.integer())
                                                .suggests((context, builder) -> ArgumentSuggestion.getOPLevels(builder))
                                                .then(CommandManager.argument("mode", BoolArgumentType.bool())
                                                        .executes(ConfigCommands::setIntOption)
                                                )
                                                .executes(ConfigCommands::setIntOptionFromCommand)
                                        )
                                )
                                .executes(ConfigCommands::getOptionChatConfig)
                        )
                )
                .then(CommandManager.literal("getConfig")
                        .executes(ConfigCommands::getConfigOptions)
                )

                .then(CommandManager.literal("reloadTranslations")
                        .executes(ConfigCommands::reloadTranslations)
                )
        );
    }

    public static int reloadTranslations(@NotNull CommandContext<ServerCommandSource> context)
    {
        CyanLibCommands.reloadTranslations(context, getDefaultTranslations(), LibUtils);

        return Command.SINGLE_SUCCESS;
    }

    /**
     * Called by the command {@code /cyanlib <optionName> set [booleanValue] [mode]}
     * <p>
     * Changes the option in the {@link CyanLibConfig} class to the value [booleanValue] and executes the
     * {@code /cyanlib getConfig} command if {@code [mode]} is true, and the command {@code /cyanlib config <optionName>} otherwise.
     * This allows to see the changed option in the chat
     */
    public static int setBoolOption(@NotNull CommandContext<ServerCommandSource> context)
    {
        CyanLibCommands.setBoolOption(context, getDefaultTranslations(), LibUtils,
                StringArgumentType.getString(context, "optionName"), BoolArgumentType.getBool(context, "booleanValue"),
                false, BoolArgumentType.getBool(context, "mode")
        );
        return Command.SINGLE_SUCCESS;
    }

    /**
     * Called by the command {@code /cyanlib <optionName> set [boolValue]}
     * <p>
     * Changes the option in the {@link CyanLibConfig} class to the value [boolValue]
     */
    public static int setBoolOptionFromCommand(@NotNull CommandContext<ServerCommandSource> context)
    {
        CyanLibCommands.setBoolOption(context, getDefaultTranslations(), LibUtils,
                StringArgumentType.getString(context, "optionName"), BoolArgumentType.getBool(context, "booleanValue"),
                true, false
        );
        return Command.SINGLE_SUCCESS;
    }

    /**
     * Called by the command {@code /cyanlib <optionName> set [intValue] [mode]}
     * <p>
     * Changes the option in the {@link CyanLibConfig} class to the value [intValue] and executes the
     * {@code /cyanlib getConfig} command if {@code [mode]} is true, and the command {@code /cyanlib config <optionName>} otherwise.
     * This allows to see the changed option in the chat
     */
    public static int setIntOption(@NotNull CommandContext<ServerCommandSource> context)
    {
        CyanLibCommands.setIntOption(context, LibUtils,
                StringArgumentType.getString(context, "optionName"), IntegerArgumentType.getInteger(context, "integerValue"),
                false, BoolArgumentType.getBool(context, "mode")
        );
        return Command.SINGLE_SUCCESS;
    }

    /**
     * Called by the command {@code /cyanlib <optionName> set [intValue]}
     * <p>
     * Changes the option in the {@link CyanLibConfig} class to the value [intValue]
     */
    public static int setIntOptionFromCommand(@NotNull CommandContext<ServerCommandSource> context)
    {
        CyanLibCommands.setIntOption(context, LibUtils,
                StringArgumentType.getString(context, "optionName"), IntegerArgumentType.getInteger(context, "integerValue"),
                true, false
        );
        return Command.SINGLE_SUCCESS;
    }

    /**
     * Called by the command {@code /cyanlib config <optionName>}
     * <p>
     * Send a message in the player's chat with a description of the option {@code optionName} and its value
     */
    public static int getOptionChatConfig(@NotNull CommandContext<ServerCommandSource> context)
    {
        CyanLibCommands.getOptionChatConfig(context, LibUtils, StringArgumentType.getString(context, "optionName"));

        return Command.SINGLE_SUCCESS;
    }

    /**
     * Called by the command {@code /cyanlib getConfig}
     * <p>
     * Send a messsage in the player's chat with all the mod's options and their values
     */
    public static int getConfigOptions(@NotNull CommandContext<ServerCommandSource> context)
    {
        CyanLibCommands.getConfigOptions(context, LibUtils);

        return Command.SINGLE_SUCCESS;
    }
}
