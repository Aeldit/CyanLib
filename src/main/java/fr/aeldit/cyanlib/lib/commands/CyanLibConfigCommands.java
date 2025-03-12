package fr.aeldit.cyanlib.lib.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import fr.aeldit.cyanlib.lib.CyanLib;
import fr.aeldit.cyanlib.lib.config.CyanLibOptionsStorage;
import fr.aeldit.cyanlib.lib.utils.RULES;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import static fr.aeldit.cyanlib.core.CyanLibCore.CYANLIB_MODID;
import static fr.aeldit.cyanlib.core.config.CyanLibConfigImpl.MIN_OP_LVL_EDIT_CONFIG;
import static fr.aeldit.cyanlib.lib.config.CyanLibOptionsStorage.getOptionsSuggestions;

public record CyanLibConfigCommands(String modid, CyanLib libUtils)
{
    public void register(@NotNull CommandDispatcher<ServerCommandSource> dispatcher)
    {
        dispatcher.register(
                CommandManager.literal(modid).then(
                        CommandManager.literal("config").then(
                                CommandManager.argument("optionName", StringArgumentType.string())
                                              .suggests((context, builder) -> getOptionsSuggestions(
                                                                builder,
                                                                libUtils.getOptionsStorage()
                                                        )
                                              )
                                              .executes(this::getOptionChatConfig)
                        )
                )
        );
        dispatcher.register(
                CommandManager.literal(modid).then(
                        CommandManager.literal("config").then(
                                CommandManager.argument("optionName", StringArgumentType.string())
                                              .suggests((context, builder) -> getOptionsSuggestions(
                                                                builder,
                                                                libUtils.getOptionsStorage()
                                                        )
                                              )
                                              .then(CommandManager.literal("set").then(
                                                      CommandManager.argument("boolVal", BoolArgumentType.bool())
                                                                    .then(CommandManager.argument(
                                                                                                "mode",
                                                                                                BoolArgumentType.bool()
                                                                                        )
                                                                                        .executes(this::setBoolOption)
                                                                    )
                                                                    .executes(this::setBoolOptionFromCommand)
                                              ))
                        )
                )
        );
        dispatcher.register(
                CommandManager.literal(modid).then(
                        CommandManager.literal("config").then(
                                CommandManager.argument("optionName", StringArgumentType.string())
                                              .suggests((context, builder) -> getOptionsSuggestions(
                                                                builder,
                                                                libUtils.getOptionsStorage()
                                                        )
                                              )
                                              .then(CommandManager.literal("set").then(
                                                      CommandManager.argument("intVal", IntegerArgumentType.integer())
                                                                    .suggests(
                                                                            (context, builder) -> CommandSource.suggestMatching(
                                                                                    Arrays.asList(
                                                                                            "0", "1", "2", "3", "4"),
                                                                                    builder
                                                                            )
                                                                    )
                                                                    .then(CommandManager.argument(
                                                                                                "mode",
                                                                                                BoolArgumentType.bool()
                                                                                        )
                                                                                        .executes(this::setIntOption)
                                                                    )
                                                                    .executes(this::setIntOptionFromCommand)
                                              ))
                        )
                )
        );
        dispatcher.register(
                CommandManager.literal(modid).then(
                        CommandManager.literal("get-config")
                                      .executes(this::getConfigOptions)
                )
        );
        dispatcher.register(
                CommandManager.literal(modid).then(
                        CommandManager.literal("reloadTranslations")
                                      .executes(this::reloadTranslations)
                )
        );
    }

    public int reloadTranslations(@NotNull CommandContext<ServerCommandSource> context)
    {
        libUtils.getLanguageUtils().loadCustomLanguage(
                libUtils.getOptionsStorage().getConfigClass().getDefaultTranslations()
        );

        if (context.getSource().getPlayer() != null)
        {
            libUtils.getLanguageUtils().sendPlayerMessageMod(
                    context.getSource().getPlayer(),
                    CYANLIB_MODID,
                    "msg.translationsReloaded"
            );
        }
        return Command.SINGLE_SUCCESS;
    }

    /**
     * Called by the command {@code /modid <optionName> set [boolVal] [mode]}
     * <p>
     * Sets the value of the given {@code boolean option} to the given {@code boolean value} and executes the
     * {@code /modid get-config} command if {@code [mode]} is true, and the command {@code /modid config
     * <optionName>} otherwise.
     * This allows to see the changed option in the chat
     */
    public int setBoolOption(@NotNull CommandContext<ServerCommandSource> context)
    {
        ServerCommandSource source = context.getSource();
        // Command not send by a player
        if (source.getPlayer() == null)
        {
            source.getServer().sendMessage(Text.of("§cThis command can only be executed by a player"));
            return 0;
        }

        ServerPlayerEntity player = source.getPlayer();
        // Player has insufficient permissions
        if (!libUtils.hasPermission(player, MIN_OP_LVL_EDIT_CONFIG.getValue()))
        {
            libUtils.getLanguageUtils().sendPlayerMessageMod(player, CYANLIB_MODID, "error.notOp");
            return 0;
        }

        String option = StringArgumentType.getString(context, "optionName");
        // The option doesn't exist
        if (!libUtils.getOptionsStorage().optionExists(option))
        {
            libUtils.getLanguageUtils().sendPlayerMessageMod(player, CYANLIB_MODID, "error.optionNotFound");
            return 0;
        }

        boolean value = BoolArgumentType.getBool(context, "boolVal");
        // An error occurred while changing the option
        if (!libUtils.getOptionsStorage().setOption(option, value, true))
        {
            libUtils.getLanguageUtils().sendPlayerMessageMod(player, CYANLIB_MODID, "error.optionNotFound");
            return 0;
        }

        if (BoolArgumentType.getBool(context, "mode"))
        {
            source.getServer().getCommandManager().executeWithPrefix(
                    source,
                    "/%s get-config".formatted(modid)
            );
        }
        else
        {
            source.getServer().getCommandManager().executeWithPrefix(
                    source,
                    "/%s config %s".formatted(modid, option)
            );
        }
        return Command.SINGLE_SUCCESS;
    }

    /**
     * Called by the command {@code /modid <optionName> set [boolValue]}
     * <p>
     * Sets the value of the given {@code boolean option} to the given {@code boolean value}
     *
     * <ul><h2>Required translation path :</h2>
     *      <li>{@code "modid.msg.set.option"} (option is the command argument {@code StringArgumentType.getString
     *      (context, "optionName")})</li>
     * </ul>
     */
    public int setBoolOptionFromCommand(@NotNull CommandContext<ServerCommandSource> context)
    {
        ServerCommandSource source = context.getSource();
        // Command not send by a player
        if (source.getPlayer() == null)
        {
            source.getServer().sendMessage(Text.of("§cThis command can only be executed by a player"));
            return 0;
        }

        ServerPlayerEntity player = source.getPlayer();
        // Player has insufficient permissions
        if (!libUtils.hasPermission(player, MIN_OP_LVL_EDIT_CONFIG.getValue()))
        {
            libUtils.getLanguageUtils().sendPlayerMessageMod(player, CYANLIB_MODID, "error.notOp");
            return 0;
        }

        String option = StringArgumentType.getString(context, "optionName");
        // The option doesn't exist
        if (!libUtils.getOptionsStorage().optionExists(option))
        {
            libUtils.getLanguageUtils().sendPlayerMessageMod(player, CYANLIB_MODID, "error.optionNotFound");
            return 0;
        }

        boolean value = BoolArgumentType.getBool(context, "boolVal");
        // An error occurred while changing the option
        if (!libUtils.getOptionsStorage().setOption(option, value, true))
        {
            libUtils.getLanguageUtils().sendPlayerMessageMod(player, CYANLIB_MODID, "error.optionNotFound");
            return 0;
        }

        libUtils.getLanguageUtils().sendPlayerMessage(
                player,
                "msg.set.%s".formatted(option),
                value ? Formatting.GREEN + "ON" : Formatting.RED + "OFF"
        );
        return Command.SINGLE_SUCCESS;
    }

    /**
     * Called by the command {@code /modid <optionName> set [intValue] [mode]}
     * <p>
     * Sets the value of the given {@code int option} to the given {@code int value} and executes the
     * {@code /modid get-config} command if {@code [mode]} is true, and the command {@code /modid config
     * <optionName>} otherwise.
     * This allows to see the changed option in the chat
     */
    public int setIntOption(@NotNull CommandContext<ServerCommandSource> context)
    {
        ServerCommandSource source = context.getSource();
        // Command not send by a player
        if (source.getPlayer() == null)
        {
            source.getServer().sendMessage(Text.of("§cThis command can only be executed by a player"));
            return 0;
        }

        ServerPlayerEntity player = source.getPlayer();
        // Player has insufficient permissions
        if (!libUtils.hasPermission(player, MIN_OP_LVL_EDIT_CONFIG.getValue()))
        {
            libUtils.getLanguageUtils().sendPlayerMessageMod(player, CYANLIB_MODID, "error.notOp");
            return 0;
        }

        String option = StringArgumentType.getString(context, "optionName");
        // The option doesn't exist
        if (!libUtils.getOptionsStorage().optionExists(option))
        {
            libUtils.getLanguageUtils().sendPlayerMessageMod(player, CYANLIB_MODID, "error.optionNotFound");
            return 0;
        }

        int value = IntegerArgumentType.getInteger(context, "intVal");
        // An error occurred while changing the option
        if (!libUtils.getOptionsStorage().setOption(option, value, true))
        {
            libUtils.getLanguageUtils().sendPlayerMessageMod(player, CYANLIB_MODID, "error.incorrectInteger");
            return 0;
        }

        if (BoolArgumentType.getBool(context, "mode"))
        {
            source.getServer().getCommandManager().executeWithPrefix(
                    source,
                    "/%s get-config".formatted(modid)
            );
        }
        else
        {
            source.getServer().getCommandManager().executeWithPrefix(
                    source,
                    "/%s config %s".formatted(modid, option)
            );
        }
        return Command.SINGLE_SUCCESS;
    }

    /**
     * Called by the command {@code /modid <optionName> set [intValue]}
     * <p>
     * Sets the value of the given {@code int option} to the given {@code int value}
     *
     * <ul><h2>Required translations paths :</h2>
     *      <li>{@code "modid.msg.set.option"} (option is the command argument {@code StringArgumentType.getString
     *      (context, "optionName")})</li>
     *      <li>{@code "modid.msg.incorrectInteger"}</li>
     * </ul>
     */
    public int setIntOptionFromCommand(@NotNull CommandContext<ServerCommandSource> context)
    {
        ServerCommandSource source = context.getSource();
        // Command not send by a player
        if (source.getPlayer() == null)
        {
            source.getServer().sendMessage(Text.of("§cThis command can only be executed by a player"));
            return 0;
        }

        ServerPlayerEntity player = source.getPlayer();
        // Player has insufficient permissions
        if (!libUtils.hasPermission(player, MIN_OP_LVL_EDIT_CONFIG.getValue()))
        {
            libUtils.getLanguageUtils().sendPlayerMessageMod(player, CYANLIB_MODID, "error.notOp");
            return 0;
        }

        String option = StringArgumentType.getString(context, "optionName");
        // The option doesn't exist
        if (!libUtils.getOptionsStorage().optionExists(option))
        {
            libUtils.getLanguageUtils().sendPlayerMessageMod(player, CYANLIB_MODID, "error.optionNotFound");
            return 0;
        }

        int value = IntegerArgumentType.getInteger(context, "intVal");
        // An error occurred while changing the option
        if (!libUtils.getOptionsStorage().setOption(option, value, true))
        {
            libUtils.getLanguageUtils().sendPlayerMessageMod(player, CYANLIB_MODID, "error.incorrectInteger");
            return 0;
        }

        libUtils.getLanguageUtils().sendPlayerMessage(
                player,
                "msg.set.%s".formatted(option),
                Formatting.GOLD + String.valueOf(value)
        );
        return Command.SINGLE_SUCCESS;
    }

    /**
     * Called by the command {@code /modid config <optionName>}
     * <p>
     * Sends a message in the player's chat with a description of the option and its current value + some
     * presets the player can click on to change the value of the option
     *
     * <ul><h2>Required translation path :</h2>
     *      <li>{@code "modid.msg.getDesc.option"} (option is the command argument {@code StringArgumentType
     *      .getString(context, "optionName")})</li>
     * </ul>
     */
    public int getOptionChatConfig(@NotNull CommandContext<ServerCommandSource> context)
    {
        ServerCommandSource source = context.getSource();
        // Command not send by a player
        if (source.getPlayer() == null)
        {
            source.getServer().sendMessage(Text.of("§cThis command can only be executed by a player"));
            return 0;
        }

        ServerPlayerEntity player = source.getPlayer();
        // Player has insufficient permissions
        if (!libUtils.hasPermission(player, MIN_OP_LVL_EDIT_CONFIG.getValue()))
        {
            libUtils.getLanguageUtils().sendPlayerMessageMod(player, CYANLIB_MODID, "error.notOp");
            return 0;
        }

        String option = StringArgumentType.getString(context, "optionName");
        Object value = libUtils.getOptionsStorage().getOptionValue(option);
        // The option doesn't exist
        if (value == null)
        {
            libUtils.getLanguageUtils().sendPlayerMessageMod(player, CYANLIB_MODID, "error.optionNotFound");
            return 0;
        }

        player.sendMessage(Text.of("§6------------------------------------"), false);
        libUtils.getLanguageUtils().sendPlayerMessageActionBar(player, "msg.getDesc.%s".formatted(option), false);

        if (value instanceof Boolean)
        {
            libUtils.getLanguageUtils().sendPlayerMessageActionBarMod(
                    player, CYANLIB_MODID,
                    "msg.currentValue",
                    false,
                    getBooleanMessage((Boolean) value, option)
            );
        }
        else if (value instanceof Integer)
        {
            libUtils.getLanguageUtils().sendPlayerMessageActionBarMod(
                    player, CYANLIB_MODID,
                    "msg.currentValue",
                    false,
                    Formatting.GOLD + String.valueOf(value)
            );

            CyanLibOptionsStorage optionsStorage = libUtils.getOptionsStorage();

            if (optionsStorage.hasRule(option, RULES.OP_LEVELS))
            {
                sendIntSmallMessage(player, option);
            }
            else if (!optionsStorage.hasRule(option, RULES.MAX_VALUE)
                     && !optionsStorage.hasRule(option, RULES.MIN_VALUE)
                     && !optionsStorage.hasRule(option, RULES.NEGATIVE_VALUE)
            )
            {
                sendIntBigMessage(player, option);
            }
        }
        player.sendMessage(Text.of("§6------------------------------------"), false);
        return Command.SINGLE_SUCCESS;
    }

    private Text getBooleanMessage(boolean value, String option)
    {
        return value ?
               //? if =1.21.5 {
               Text.literal(Formatting.GREEN + "ON (click to change)")
                     .setStyle(Style.EMPTY.withClickEvent(
                             new ClickEvent.RunCommand("/%s config %s set false false".formatted(modid, option))
                     ))
               : Text.literal(Formatting.RED + "OFF (click to change)")
                     .setStyle(Style.EMPTY.withClickEvent(
                             new ClickEvent.RunCommand("/%s config %s set true false".formatted(modid, option)))
                     );
                //?} else {
               /*Text.literal(Formatting.GREEN + "ON (click to change)")
                   .setStyle(Style.EMPTY.withClickEvent(
                           new ClickEvent(
                                   ClickEvent.Action.RUN_COMMAND,
                                   "/%s config %s set false false".formatted(modid, option)
                           )
                   ))
                     : Text.literal(Formatting.RED + "OFF (click to change)")
                           .setStyle(Style.EMPTY.withClickEvent(
                                   new ClickEvent(
                                           ClickEvent.Action.RUN_COMMAND,
                                           "/%s config %s set true false".formatted(modid, option)
                                   ))
                           );
        *///?}
    }

    private void sendIntSmallMessage(ServerPlayerEntity player, String option)
    {
        libUtils.getLanguageUtils().sendPlayerMessageActionBarMod(
                player, CYANLIB_MODID,
                "msg.setValue",
                false,
                //? if =1.21.5 {
                Text.literal(Formatting.DARK_GREEN + (Formatting.BOLD + "0")).
                    setStyle(Style.EMPTY.withClickEvent(
                            new ClickEvent.RunCommand("/%s config %s set 0 false".formatted(modid, option)))
                    ),
                Text.literal(Formatting.DARK_GREEN + (Formatting.BOLD + "1")).
                    setStyle(Style.EMPTY.withClickEvent(
                            new ClickEvent.RunCommand("/%s config %s set 1 false".formatted(modid, option)))
                    ),
                Text.literal(Formatting.DARK_GREEN + (Formatting.BOLD + "2")).
                    setStyle(Style.EMPTY.withClickEvent(
                            new ClickEvent.RunCommand("/%s config %s set 2 false".formatted(modid, option)))
                    ),
                Text.literal(Formatting.DARK_GREEN + (Formatting.BOLD + "3")).
                    setStyle(Style.EMPTY.withClickEvent(
                            new ClickEvent.RunCommand("/%s config %s set 3 false".formatted(modid, option)))
                    ),
                Text.literal(Formatting.DARK_GREEN + (Formatting.BOLD + "4")).
                    setStyle(Style.EMPTY.withClickEvent(
                            new ClickEvent.RunCommand("/%s config %s set 4 false".formatted(modid, option)))
                    )
                //?} else {
                /*Text.literal(Formatting.DARK_GREEN + (Formatting.BOLD + "0")).
                    setStyle(Style.EMPTY.withClickEvent(
                            new ClickEvent(
                                    ClickEvent.Action.RUN_COMMAND,
                                    "/%s config %s set 0 false".formatted(modid, option)
                            ))
                    ),
                Text.literal(Formatting.DARK_GREEN + (Formatting.BOLD + "1")).
                    setStyle(Style.EMPTY.withClickEvent(
                            new ClickEvent(
                                    ClickEvent.Action.RUN_COMMAND,
                                    "/%s config %s set 1 false".formatted(modid, option)
                            ))
                    ),
                Text.literal(Formatting.DARK_GREEN + (Formatting.BOLD + "2")).
                    setStyle(Style.EMPTY.withClickEvent(
                            new ClickEvent(
                                    ClickEvent.Action.RUN_COMMAND,
                                    "/%s config %s set 2 false".formatted(modid, option)
                            ))
                    ),
                Text.literal(Formatting.DARK_GREEN + (Formatting.BOLD + "3")).
                    setStyle(Style.EMPTY.withClickEvent(
                            new ClickEvent(
                                    ClickEvent.Action.RUN_COMMAND,
                                    "/%s config %s set 3 false".formatted(modid, option)
                            ))
                    ),
                Text.literal(Formatting.DARK_GREEN + (Formatting.BOLD + "4")).
                    setStyle(Style.EMPTY.withClickEvent(
                            new ClickEvent(
                                    ClickEvent.Action.RUN_COMMAND,
                                    "/%s config %s set 4 false".formatted(modid, option)
                            ))
                    )
                *///?}
        );
    }

    private void sendIntBigMessage(ServerPlayerEntity player, String option)
    {
        libUtils.getLanguageUtils().sendPlayerMessageActionBarMod(
                player, CYANLIB_MODID,
                "msg.setValue",
                false,
                //? if =1.21.5 {
                Text.literal(Formatting.DARK_GREEN + (Formatting.BOLD + "8")).
                    setStyle(Style.EMPTY.withClickEvent(
                            new ClickEvent.RunCommand("/%s config %s set 8 false".formatted(modid, option)))
                    ),
                Text.literal(Formatting.DARK_GREEN + (Formatting.BOLD + "16")).
                    setStyle(Style.EMPTY.withClickEvent(
                            new ClickEvent.RunCommand("/%s config %s set 16 false".formatted(modid, option)))
                    ),
                Text.literal(Formatting.DARK_GREEN + (Formatting.BOLD + "32")).
                    setStyle(Style.EMPTY.withClickEvent(
                            new ClickEvent.RunCommand("/%s config %s set 32 false".formatted(modid, option)))
                    ),
                Text.literal(Formatting.DARK_GREEN + (Formatting.BOLD + "64")).
                    setStyle(Style.EMPTY.withClickEvent(
                            new ClickEvent.RunCommand("/%s config %s set 64 false".formatted(modid, option)))
                    ),
                Text.literal(Formatting.DARK_GREEN + (Formatting.BOLD + "128")).
                    setStyle(Style.EMPTY.withClickEvent(
                            new ClickEvent.RunCommand("/%s config %s set 128 false".formatted(modid, option)))
                    )
                //?} else {
                /*Text.literal(Formatting.DARK_GREEN + (Formatting.BOLD + "8")).
                    setStyle(Style.EMPTY.withClickEvent(
                            new ClickEvent(
                                    ClickEvent.Action.RUN_COMMAND,
                                    "/%s config %s set 8 false".formatted(modid, option)
                            ))
                    ),
                Text.literal(Formatting.DARK_GREEN + (Formatting.BOLD + "16")).
                    setStyle(Style.EMPTY.withClickEvent(
                            new ClickEvent(
                                    ClickEvent.Action.RUN_COMMAND,
                                    "/%s config %s set 16 false".formatted(modid, option)
                            ))
                    ),
                Text.literal(Formatting.DARK_GREEN + (Formatting.BOLD + "32")).
                    setStyle(Style.EMPTY.withClickEvent(
                            new ClickEvent(
                                    ClickEvent.Action.RUN_COMMAND,
                                    "/%s config %s set 32 false".formatted(modid, option)
                            ))
                    ),
                Text.literal(Formatting.DARK_GREEN + (Formatting.BOLD + "64")).
                    setStyle(Style.EMPTY.withClickEvent(
                            new ClickEvent(
                                    ClickEvent.Action.RUN_COMMAND,
                                    "/%s config %s set 64 false".formatted(modid, option)
                            ))
                    ),
                Text.literal(Formatting.DARK_GREEN + (Formatting.BOLD + "128")).
                    setStyle(Style.EMPTY.withClickEvent(
                            new ClickEvent(
                                    ClickEvent.Action.RUN_COMMAND,
                                    "/%s config %s set 128 false".formatted(modid, option)
                            ))
                    )
                *///?}
        );
    }

    /**
     * Called by the command {@code /modid get-config}
     * <p>
     * Sends a message in the player's chat with the current value of every option of your mod
     */
    public int getConfigOptions(@NotNull CommandContext<ServerCommandSource> context)
    {
        ServerCommandSource source = context.getSource();
        // Command not send by a player
        if (source.getPlayer() == null)
        {
            context.getSource().getServer().sendMessage(Text.of("§cThis command can only be executed by a player"));
            return 0;
        }

        ServerPlayerEntity player = source.getPlayer();
        // Player has insufficient permissions
        if (!libUtils.hasPermission(player, MIN_OP_LVL_EDIT_CONFIG.getValue()))
        {
            libUtils.getLanguageUtils().sendPlayerMessageMod(player, CYANLIB_MODID, "error.notOp");
            return 0;
        }

        player.sendMessage(Text.of("§6------------------------------------"), false);
        libUtils.getLanguageUtils().sendPlayerMessageActionBar(player, "msg.getCfg.header", false);

        for (String option : libUtils.getOptionsStorage().getOptionsNames())
        {
            if (!libUtils.getOptionsStorage().optionExists(option))
            {
                continue;
            }

            Object value = libUtils.getOptionsStorage().getOptionValue(option);

            if (value instanceof Boolean boolVal)
            {
                libUtils.getLanguageUtils().sendPlayerMessageActionBar(
                        player,
                        "msg.getCfg.%s".formatted(option),
                        false,
                        boolVal ?
                        //? if =1.21.5 {
                        Text.literal(Formatting.GREEN + "ON").
                            setStyle(Style.EMPTY.withClickEvent(
                                    new ClickEvent.RunCommand(
                                            "/%s config %s set false true".formatted(modid, option)
                                    ))
                            ) : Text.literal(Formatting.RED + "OFF").
                                    setStyle(Style.EMPTY.withClickEvent(
                                            new ClickEvent.RunCommand(
                                                    "/%s config %s set true true".formatted(modid, option)
                                            ))
                                    )
                        //?} else {
                        /*Text.literal(Formatting.GREEN + "ON").
                            setStyle(Style.EMPTY.withClickEvent(
                                    new ClickEvent(
                                            ClickEvent.Action.RUN_COMMAND,
                                            "/%s config %s set false true".formatted(modid, option)
                                    ))
                            ) : Text.literal(Formatting.RED + "OFF").
                                    setStyle(Style.EMPTY.withClickEvent(
                                            new ClickEvent(
                                                    ClickEvent.Action.RUN_COMMAND,
                                                    "/%s config %s set true true".formatted(modid, option)
                                            ))
                                    )
                        *///?}
                );
            }
            else if (value instanceof Integer intVal)
            {
                libUtils.getLanguageUtils().sendPlayerMessageActionBar(
                        player,
                        "msg.getCfg.%s".formatted(option),
                        false,
                        Formatting.GOLD + intVal.toString()
                );
            }
        }
        player.sendMessage(Text.of("§6------------------------------------"), false);
        return Command.SINGLE_SUCCESS;
    }
}
