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
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import static fr.aeldit.cyanlib.core.config.CyanLibConfigImpl.MIN_OP_LVL_EDIT_CONFIG;
import static fr.aeldit.cyanlib.lib.config.CyanLibOptionsStorage.getOptionsSuggestions;

public class CyanLibConfigCommands
{
    private final String modid;
    private final CyanLib libUtils;

    @Contract(pure = true)
    public CyanLibConfigCommands(String modid, CyanLib libUtils)
    {
        this.modid = modid;
        this.libUtils = libUtils;
    }

    public void register(@NotNull CommandDispatcher<ServerCommandSource> dispatcher)
    {
        dispatcher.register(CommandManager.literal(modid)
                .then(CommandManager.literal("config")
                        .then(CommandManager.argument("optionName", StringArgumentType.string())
                                .suggests((context, builder) -> getOptionsSuggestions(builder,
                                        libUtils.getOptionsStorage()
                                ))
                                .then(CommandManager.literal("set")
                                        .then(CommandManager.argument("booleanValue", BoolArgumentType.bool())
                                                .then(CommandManager.argument("mode", BoolArgumentType.bool())
                                                        .executes(this::setBoolOption)
                                                )
                                                .executes(this::setBoolOptionFromCommand)
                                        )
                                        .then(CommandManager.argument("integerValue", IntegerArgumentType.integer())
                                                .suggests((context, builder) -> CommandSource.suggestMatching(
                                                        Arrays.asList("0", "1", "2", "3", "4"), builder)
                                                )
                                                .then(CommandManager.argument("mode", BoolArgumentType.bool())
                                                        .executes(this::setIntOption)
                                                )
                                                .executes(this::setIntOptionFromCommand)
                                        )
                                )
                                .executes(this::getOptionChatConfig)
                        )
                )
                .then(CommandManager.literal("get-config")
                        .executes(this::getConfigOptions)
                )

                .then(CommandManager.literal("reloadTranslations")
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
            libUtils.getLanguageUtils().sendPlayerMessageMod(context.getSource().getPlayer(), "cyanlib",
                    "cyanlib.msg.translationsReloaded"
            );
        }
        return Command.SINGLE_SUCCESS;
    }

    /**
     * Called by the command {@code /modid <optionName> set [booleanValue] [mode]}
     * <p>
     * Sets the value of the given {@code boolean option} to the given {@code boolean value} and executes the
     * {@code /modid get-config} command if {@code [mode]} is true, and the command {@code /modid config
     * <optionName>} otherwise.
     * This allows to see the changed option in the chat
     */
    public int setBoolOption(@NotNull CommandContext<ServerCommandSource> context)
    {
        ServerCommandSource source = context.getSource();
        if (source.getPlayer() != null)
        {
            ServerPlayerEntity player = source.getPlayer();

            if (libUtils.hasPermission(player, MIN_OP_LVL_EDIT_CONFIG.getValue()))
            {
                String option = StringArgumentType.getString(context, "optionName");
                boolean value = BoolArgumentType.getBool(context, "booleanValue");

                if (libUtils.getOptionsStorage().setOption(option, value, true))
                {
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
                }
                else
                {
                    libUtils.getLanguageUtils().sendPlayerMessageMod(player, "cyanlib", "cyanlib.msg.optionNotFound");
                }
            }
        }
        else
        {
            source.getServer().sendMessage(Text.of("§cThis command can only be executed by a player"));
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
        if (context.getSource().getPlayer() != null)
        {
            ServerPlayerEntity player = context.getSource().getPlayer();

            if (libUtils.hasPermission(player, MIN_OP_LVL_EDIT_CONFIG.getValue()))
            {
                String option = StringArgumentType.getString(context, "optionName");
                boolean value = BoolArgumentType.getBool(context, "booleanValue");

                if (libUtils.getOptionsStorage().setOption(option, value, true))
                {
                    libUtils.getLanguageUtils().sendPlayerMessage(player,
                            "%s.msg.set.%s".formatted(modid, option),
                            value ? Formatting.GREEN + "ON" : Formatting.RED + "OFF"
                    );
                }
                else
                {
                    libUtils.getLanguageUtils().sendPlayerMessageMod(player, "cyanlib", "cyanlib.msg.optionNotFound");
                }
            }
        }
        else
        {
            context.getSource().getServer().sendMessage(Text.of("§cThis command can only be executed by a player"));
        }
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
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player != null)
        {
            if (libUtils.hasPermission(player, MIN_OP_LVL_EDIT_CONFIG.getValue()))
            {
                String option = StringArgumentType.getString(context, "optionName");

                if (libUtils.getOptionsStorage().optionExists(option))
                {
                    int value = IntegerArgumentType.getInteger(context, "integerValue");

                    if (libUtils.getOptionsStorage().setOption(option, value, true))
                    {
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
                    }
                    else
                    {
                        libUtils.getLanguageUtils().sendPlayerMessageMod(player, "cyanlib",
                                "cyanlib.msg.incorrectInteger"
                        );
                    }
                }
                else
                {
                    libUtils.getLanguageUtils().sendPlayerMessageMod(player, "cyanlib", "cyanlib.msg.optionNotFound");
                }
            }
        }
        else
        {
            source.getServer().sendMessage(Text.of("§cThis command can only be executed by a player"));
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
        if (context.getSource().getPlayer() != null)
        {
            ServerPlayerEntity player = context.getSource().getPlayer();

            if (libUtils.hasPermission(player, MIN_OP_LVL_EDIT_CONFIG.getValue()))
            {
                String option = StringArgumentType.getString(context, "optionName");

                if (libUtils.getOptionsStorage().optionExists(option))
                {
                    int value = IntegerArgumentType.getInteger(context, "integerValue");

                    if (libUtils.getOptionsStorage().setOption(option, value, true))
                    {
                        libUtils.getLanguageUtils().sendPlayerMessage(
                                player,
                                "%s.msg.set.%s".formatted(modid, option),
                                Formatting.GOLD + String.valueOf(value)
                        );
                    }
                    else
                    {
                        libUtils.getLanguageUtils().sendPlayerMessageMod(player, "cyanlib",
                                "cyanlib.msg.incorrectInteger"
                        );
                    }
                }
                else
                {
                    libUtils.getLanguageUtils().sendPlayerMessageMod(player, "cyanlib", "cyanlib.msg.optionNotFound");
                }
            }
        }
        else
        {
            context.getSource().getServer().sendMessage(Text.of("§cThis command can only be executed by a player"));
        }
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
        if (context.getSource().getPlayer() != null)
        {
            ServerPlayerEntity player = context.getSource().getPlayer();

            if (libUtils.hasPermission(player, MIN_OP_LVL_EDIT_CONFIG.getValue()))
            {
                String option = StringArgumentType.getString(context, "optionName");
                Object value = libUtils.getOptionsStorage().getOptionValue(option);

                if (value != null)
                {
                    player.sendMessage(Text.of("§6------------------------------------"), false);
                    libUtils.getLanguageUtils().sendPlayerMessageActionBar(player,
                            "%s.msg.getDesc.%s".formatted(modid, option),
                            false
                    );

                    if (value instanceof Boolean)
                    {
                        libUtils.getLanguageUtils().sendPlayerMessageActionBarMod(player, "cyanlib",
                                "cyanlib.msg.currentValue",
                                false,
                                (Boolean) value ? Text.literal(Formatting.GREEN + "ON (click to change)").
                                        setStyle(Style.EMPTY.withClickEvent(
                                                new ClickEvent(ClickEvent.Action.RUN_COMMAND, ("/%s config %s set " +
                                                        "false false").formatted(modid, option)))
                                        ) : Text.literal(Formatting.RED + "OFF (click to change)").
                                        setStyle(Style.EMPTY.withClickEvent(
                                                new ClickEvent(ClickEvent.Action.RUN_COMMAND, ("/%s config %s set " +
                                                        "true false").formatted(modid, option)))
                                        )
                        );
                    }
                    else if (value instanceof Integer)
                    {
                        libUtils.getLanguageUtils().sendPlayerMessageActionBarMod(player, "cyanlib",
                                "cyanlib.msg.currentValue",
                                false,
                                Formatting.GOLD + String.valueOf(value)
                        );

                        CyanLibOptionsStorage optionsStorage = libUtils.getOptionsStorage();

                        if (optionsStorage.hasRule(option, RULES.OP_LEVELS))
                        {
                            libUtils.getLanguageUtils().sendPlayerMessageActionBarMod(player, "cyanlib",
                                    "cyanlib.msg.setValue",
                                    false,
                                    Text.literal(Formatting.DARK_GREEN + (Formatting.BOLD + "0")).
                                            setStyle(Style.EMPTY.withClickEvent(
                                                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, ("/%s config %s set" +
                                                            " 0 false").formatted(modid, option)))
                                            ),
                                    Text.literal(Formatting.DARK_GREEN + (Formatting.BOLD + "1")).
                                            setStyle(Style.EMPTY.withClickEvent(
                                                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, ("/%s config %s set" +
                                                            " 1 false").formatted(modid, option)))
                                            ),
                                    Text.literal(Formatting.DARK_GREEN + (Formatting.BOLD + "2")).
                                            setStyle(Style.EMPTY.withClickEvent(
                                                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, ("/%s config %s set" +
                                                            " 2 false").formatted(modid, option)))
                                            ),
                                    Text.literal(Formatting.DARK_GREEN + (Formatting.BOLD + "3")).
                                            setStyle(Style.EMPTY.withClickEvent(
                                                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, ("/%s config %s set" +
                                                            " 3 false").formatted(modid, option)))
                                            ),
                                    Text.literal(Formatting.DARK_GREEN + (Formatting.BOLD + "4")).
                                            setStyle(Style.EMPTY.withClickEvent(
                                                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, ("/%s config %s set" +
                                                            " 4 false").formatted(modid, option)))
                                            )
                            );
                        }
                        else if (!optionsStorage.hasRule(option, RULES.MAX_VALUE)
                                && !optionsStorage.hasRule(option, RULES.MIN_VALUE)
                                && !optionsStorage.hasRule(option, RULES.NEGATIVE_VALUE)
                        )
                        {
                            libUtils.getLanguageUtils().sendPlayerMessageActionBarMod(
                                    player,
                                    "cyanlib",
                                    "cyanlib.msg.setValue",
                                    false,
                                    Text.literal(Formatting.DARK_GREEN + (Formatting.BOLD + "8")).
                                            setStyle(Style.EMPTY.withClickEvent(
                                                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, ("/%s config %s set" +
                                                            " 8 false").formatted(modid, option)))
                                            ),
                                    Text.literal(Formatting.DARK_GREEN + (Formatting.BOLD + "16")).
                                            setStyle(Style.EMPTY.withClickEvent(
                                                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, ("/%s config %s set" +
                                                            " 16 false").formatted(modid, option)))
                                            ),
                                    Text.literal(Formatting.DARK_GREEN + (Formatting.BOLD + "32")).
                                            setStyle(Style.EMPTY.withClickEvent(
                                                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, ("/%s config %s set" +
                                                            " 32 false").formatted(modid, option)))
                                            ),
                                    Text.literal(Formatting.DARK_GREEN + (Formatting.BOLD + "64")).
                                            setStyle(Style.EMPTY.withClickEvent(
                                                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, ("/%s config %s set" +
                                                            " 64 false").formatted(modid, option)))
                                            ),
                                    Text.literal(Formatting.DARK_GREEN + (Formatting.BOLD + "128")).
                                            setStyle(Style.EMPTY.withClickEvent(
                                                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, ("/%s config %s set" +
                                                            " 128 false").formatted(modid, option)))
                                            )
                            );
                        }
                    }
                    player.sendMessage(Text.of("§6------------------------------------"), false);
                }
                else
                {
                    libUtils.getLanguageUtils().sendPlayerMessageMod(player, "cyanlib", "cyanlib.msg.optionNotFound");
                }
            }
        }
        else
        {
            context.getSource().getServer().sendMessage(Text.of("§cThis command can only be executed by a player"));
        }
        return Command.SINGLE_SUCCESS;
    }

    /**
     * Called by the command {@code /modid get-config}
     * <p>
     * Sends a message in the player's chat with the current value of every option of your mod
     */
    public int getConfigOptions(@NotNull CommandContext<ServerCommandSource> context)
    {
        if (context.getSource().getPlayer() != null)
        {
            ServerPlayerEntity player = context.getSource().getPlayer();

            if (libUtils.hasPermission(player, MIN_OP_LVL_EDIT_CONFIG.getValue()))
            {
                player.sendMessage(Text.of("§6------------------------------------"), false);
                libUtils.getLanguageUtils().sendPlayerMessageActionBar(
                        player,
                        "%s.msg.getCfg.header".formatted(modid),
                        false
                );

                for (String option : libUtils.getOptionsStorage().getOptionsNames())
                {
                    if (libUtils.getOptionsStorage().optionExists(option))
                    {
                        Object value = libUtils.getOptionsStorage().getOptionValue(option);

                        if (value instanceof Boolean booleanValue)
                        {
                            libUtils.getLanguageUtils().sendPlayerMessageActionBar(
                                    player,
                                    "%s.msg.getCfg.%s".formatted(modid, option),
                                    false,
                                    booleanValue ? Text.literal(Formatting.GREEN + "ON").
                                            setStyle(Style.EMPTY.withClickEvent(
                                                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, ("/%s config %s set" +
                                                            " " +
                                                            "false true").formatted(modid, option)))
                                            ) : Text.literal(Formatting.RED + "OFF").
                                            setStyle(Style.EMPTY.withClickEvent(
                                                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, ("/%s config %s set" +
                                                            " " +
                                                            "true true").formatted(modid, option)))
                                            )
                            );
                        }
                        else if (value instanceof Integer integerValue)
                        {
                            libUtils.getLanguageUtils().sendPlayerMessageActionBar(
                                    player,
                                    "%s.msg.getCfg.%s".formatted(modid, option),
                                    false,
                                    Formatting.GOLD + integerValue.toString()
                            );
                        }
                    }
                }
                player.sendMessage(Text.of("§6------------------------------------"), false);
            }
        }
        else
        {
            context.getSource().getServer().sendMessage(Text.of("§cThis command can only be executed by a player"));
        }
        return Command.SINGLE_SUCCESS;
    }
}
