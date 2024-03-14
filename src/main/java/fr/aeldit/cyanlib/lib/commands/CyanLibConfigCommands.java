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
 * in the repo of this mod (https://github.com/Aeldit/CyanLib)
 */

package fr.aeldit.cyanlib.lib.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import fr.aeldit.cyanlib.lib.CyanLib;
import fr.aeldit.cyanlib.lib.utils.RULES;
import fr.aeldit.cyanlib.lib.utils.TranslationsPrefixes;
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
import java.util.Objects;

import static fr.aeldit.cyanlib.core.config.CoreConfig.MIN_OP_LVL_EDIT_CONFIG;
import static fr.aeldit.cyanlib.lib.config.CyanLibOptionsStorage.getOptionsSuggestions;
import static fr.aeldit.cyanlib.lib.utils.TranslationsPrefixes.*;

public class CyanLibConfigCommands
{
    private final String modid;
    private final CyanLib libUtils;

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

                .then(CommandManager.literal("reload-translations")
                        .executes(this::reloadTranslations)
                )
        );
    }

    /**
     * Reloads the custom translations for the given modid
     *
     * <ul><h2>Translations paths :</h2>
     *      <li>{@code "modid.msg.translationsReloaded"}</li>
     * </ul>
     */
    public int reloadTranslations(@NotNull CommandContext<ServerCommandSource> context)
    {
        if (libUtils.isPlayer(context.getSource()))
        {
            ServerPlayerEntity player = context.getSource().getPlayer();

            if (libUtils.hasPermission(player, MIN_OP_LVL_EDIT_CONFIG.getValue()))
            {
                libUtils.getLanguageUtils().loadLanguage();

                libUtils.getLanguageUtils().sendPlayerMessage(
                        player,
                        libUtils.getLanguageUtils().getTranslation("translationsReloaded"),
                        "%s.msg.translationsReloaded".formatted(modid)
                );
            }
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
     *
     * <ul><h2>Translations paths :</h2>
     *      <li>{@code "modid.msg.optionNotFound"}</li>
     * </ul>
     *
     * <ul><h2>Custom translations :</h2> Required only if the option useCustomTranslations is set to true
     *      <li>{@link TranslationsPrefixes#ERROR} + {@code "optionNotFound"}</li>
     * </ul>
     */
    public int setBoolOption(@NotNull CommandContext<ServerCommandSource> context)
    {
        ServerCommandSource source = context.getSource();

        if (libUtils.isPlayer(source))
        {
            if (libUtils.hasPermission(Objects.requireNonNull(source.getPlayer()), MIN_OP_LVL_EDIT_CONFIG.getValue()))
            {
                String option = StringArgumentType.getString(context, "optionName");

                if (libUtils.getOptionsStorage().optionExists(option))
                {
                    boolean value = BoolArgumentType.getBool(context, "booleanValue");
                    libUtils.getOptionsStorage().setOption(option, value, true);

                    if (libUtils.getOptionsStorage().hasRule(option, RULES.LOAD_CUSTOM_TRANSLATIONS))
                    {
                        if (value)
                        {
                            libUtils.getLanguageUtils().loadLanguage();
                        }
                        else
                        {
                            libUtils.getLanguageUtils().unload();
                        }
                    }

                    if (BoolArgumentType.getBool(context, "mode"))
                    {
                        source.getServer().getCommandManager().executeWithPrefix(source,
                                "/%s get-config".formatted(modid)
                        );
                    }
                    else
                    {
                        source.getServer().getCommandManager().executeWithPrefix(source,
                                "/%s config %s".formatted(modid, option)
                        );
                    }
                }
                else
                {
                    libUtils.getLanguageUtils().sendPlayerMessage(source.getPlayer(),
                            libUtils.getLanguageUtils().getTranslation(ERROR + "optionNotFound"),
                            "%s.msg.optionNotFound".formatted(modid)
                    );
                }
            }
        }
        return Command.SINGLE_SUCCESS;
    }

    /**
     * Called by the command {@code /modid <optionName> set [boolValue]}
     * <p>
     * Sets the value of the given {@code boolean option} to the given {@code boolean value}
     *
     * <ul><h2>Translations paths :</h2>
     *      <li>{@code "modid.msg.set.option"} (option is the command argument {@code StringArgumentType.getString
     *      (context, "optionName")})</li>
     *      <li>{@code "modid.msg.optionNotFound"}</li>
     * </ul>
     *
     * <ul><h2>Custom translations :</h2> Required only if the option useCustomTranslations is set to true
     *      <li>{@link TranslationsPrefixes#SET} + {@code option} (option is the command argument {@code
     *      StringArgumentType.getString(context, "optionName")})</li>
     *      <li>{@link TranslationsPrefixes#ERROR} + {@code "optionNotFound"}</li>
     * </ul>
     */
    public int setBoolOptionFromCommand(@NotNull CommandContext<ServerCommandSource> context)
    {
        ServerCommandSource source = context.getSource();

        if (libUtils.isPlayer(source))
        {
            if (libUtils.hasPermission(Objects.requireNonNull(source.getPlayer()), MIN_OP_LVL_EDIT_CONFIG.getValue()))
            {
                String option = StringArgumentType.getString(context, "optionName");

                if (libUtils.getOptionsStorage().optionExists(option))
                {
                    boolean value = BoolArgumentType.getBool(context, "booleanValue");
                    libUtils.getOptionsStorage().setOption(option, value, true);

                    if (libUtils.getOptionsStorage().hasRule(option, RULES.LOAD_CUSTOM_TRANSLATIONS))
                    {
                        if (value)
                        {
                            libUtils.getLanguageUtils().loadLanguage();
                        }
                        else
                        {
                            libUtils.getLanguageUtils().unload();
                        }
                    }

                    libUtils.getLanguageUtils().sendPlayerMessage(source.getPlayer(),
                            libUtils.getLanguageUtils().getTranslation(SET + option),
                            "%s.msg.set.%s".formatted(modid, option),
                            value ? Formatting.GREEN + "ON" : Formatting.RED + "OFF"
                    );
                }
                else
                {
                    libUtils.getLanguageUtils().sendPlayerMessage(source.getPlayer(),
                            libUtils.getLanguageUtils().getTranslation(ERROR + "optionNotFound"),
                            "%s.msg.optionNotFound".formatted(modid)
                    );
                }
            }
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
     *
     * <ul><h2>Translations paths :</h2>
     *      <li>{@code "modid.msg.set.option"} (option is the command argument {@code StringArgumentType.getString
     *      (context, "optionName")})</li>
     *      <li>{@code "modid.msg.incorrectInteger"}</li>
     *      <li>{@code "modid.msg.optionNotFound"}</li>
     * </ul>
     *
     * <ul><h2>Custom translations :</h2> Required only if the option useCustomTranslations is set to true
     *      <li>{@link TranslationsPrefixes#SET} + {@code option} (option is the command argument {@code
     *      StringArgumentType.getString(context, "optionName")})</li>
     *      <li>{@link TranslationsPrefixes#ERROR} + {@code "incorrectInteger"}</li>
     *      <li>{@link TranslationsPrefixes#ERROR} + {@code "optionNotFound"}</li>
     * </ul>
     */
    public int setIntOption(@NotNull CommandContext<ServerCommandSource> context)
    {
        ServerCommandSource source = context.getSource();

        if (libUtils.isPlayer(source))
        {
            if (libUtils.hasPermission(Objects.requireNonNull(source.getPlayer()), MIN_OP_LVL_EDIT_CONFIG.getValue()))
            {
                String option = StringArgumentType.getString(context, "optionName");

                if (libUtils.getOptionsStorage().optionExists(option))
                {
                    int value = IntegerArgumentType.getInteger(context, "integerValue");

                    if (libUtils.getOptionsStorage().setOption(option, value, true))
                    {
                        if (BoolArgumentType.getBool(context, "mode"))
                        {
                            source.getServer().getCommandManager().executeWithPrefix(source,
                                    "/%s get-config".formatted(modid)
                            );
                        }
                        else
                        {
                            source.getServer().getCommandManager().executeWithPrefix(source,
                                    "/%s config %s".formatted(modid, option)
                            );
                        }
                    }
                    else
                    {
                        libUtils.getLanguageUtils().sendPlayerMessage(source.getPlayer(),
                                libUtils.getLanguageUtils().getTranslation(ERROR + "incorrectInteger"),
                                "%s.msg.incorrectInteger".formatted(modid)
                        );
                    }
                }
                else
                {
                    libUtils.getLanguageUtils().sendPlayerMessage(source.getPlayer(),
                            libUtils.getLanguageUtils().getTranslation(ERROR + "optionNotFound"),
                            "%s.msg.optionNotFound".formatted(modid)
                    );
                }
            }
        }
        return Command.SINGLE_SUCCESS;
    }

    /**
     * Called by the command {@code /modid <optionName> set [intValue]}
     * <p>
     * Sets the value of the given {@code int option} to the given {@code int value}
     *
     * <ul><h2>Translations paths :</h2>
     *      <li>{@code "modid.msg.set.option"} (option is the command argument {@code StringArgumentType.getString
     *      (context, "optionName")})</li>
     *      <li>{@code "modid.msg.incorrectInteger"}</li>
     *      <li>{@code "modid.msg.optionNotFound"}</li>
     * </ul>
     *
     * <ul><h2>Custom translations :</h2> Required only if the option useCustomTranslations is set to true
     *      <li>{@link TranslationsPrefixes#SET} + {@code option} (option is the command argument {@code
     *      StringArgumentType.getString(context, "optionName")})</li>
     *      <li>{@link TranslationsPrefixes#ERROR} + {@code "incorrectInteger"}</li>
     *      <li>{@link TranslationsPrefixes#ERROR} + {@code "optionNotFound"}</li>
     * </ul>
     */
    public int setIntOptionFromCommand(@NotNull CommandContext<ServerCommandSource> context)
    {
        ServerCommandSource source = context.getSource();

        if (libUtils.isPlayer(source))
        {
            if (libUtils.hasPermission(Objects.requireNonNull(source.getPlayer()), MIN_OP_LVL_EDIT_CONFIG.getValue()))
            {
                String option = StringArgumentType.getString(context, "optionName");

                if (libUtils.getOptionsStorage().optionExists(option))
                {
                    int value = IntegerArgumentType.getInteger(context, "integerValue");

                    if (libUtils.getOptionsStorage().setOption(option, value, true))
                    {
                        libUtils.getLanguageUtils().sendPlayerMessage(source.getPlayer(),
                                libUtils.getLanguageUtils().getTranslation(SET + option),
                                "%s.msg.set.%s".formatted(modid, option),
                                Formatting.GOLD + String.valueOf(value)
                        );
                    }
                    else
                    {
                        libUtils.getLanguageUtils().sendPlayerMessage(source.getPlayer(),
                                libUtils.getLanguageUtils().getTranslation(ERROR + "incorrectInteger"),
                                "%s.msg.incorrectInteger".formatted(modid)
                        );
                    }
                }
                else
                {
                    libUtils.getLanguageUtils().sendPlayerMessage(source.getPlayer(),
                            libUtils.getLanguageUtils().getTranslation(ERROR + "optionNotFound"),
                            "%s.msg.optionNotFound".formatted(modid)
                    );
                }
            }
        }
        return Command.SINGLE_SUCCESS;
    }

    /**
     * Called by the command {@code /modid config <optionName>}
     * <p>
     * Sends a message in the player's chat with a description of the option and its current value + some
     * presets the player can click on to change the value of the option
     *
     * <ul><h2>Translations paths :</h2>
     *      <li>{@code "modid.msg.dashSeparation"}</li>
     *      <li>{@code "modid.msg.getDesc.option"} (option is the command argument {@code StringArgumentType
     *      .getString(context, "optionName")})</li>
     *      <li>{@code "modid.msg.currentValue"}</li>
     *      <li>{@code "modid.msg.setValue"}</li>
     *      <li>{@code "modid.msg.optionNotFound"}</li>
     * </ul>
     *
     * <ul><h2>Custom translations :</h2> Required only if the option useCustomTranslations is set to true
     *      <li>{@code "dashSeparation"}</li>
     *      <li>{@link TranslationsPrefixes#DESC} + {@code option} (option is the command argument {@code
     *      StringArgumentType.getString(context, "optionName")})</li>
     *      <li>{@code "currentValue"}</li>
     *      <li>{@code "setValue"}</li>
     *      <li>{@link TranslationsPrefixes#ERROR} + {@code "optionNotFound"}</li>
     * </ul>
     */
    public int getOptionChatConfig(@NotNull CommandContext<ServerCommandSource> context)
    {
        ServerPlayerEntity player = context.getSource().getPlayer();

        if (libUtils.isPlayer(context.getSource()))
        {
            if (libUtils.hasPermission(Objects.requireNonNull(player), MIN_OP_LVL_EDIT_CONFIG.getValue()))
            {
                String option = StringArgumentType.getString(context, "optionName");

                Object value = libUtils.getOptionsStorage().getOptionValue(option);
                if (value != null)
                {
                    libUtils.getLanguageUtils().sendPlayerMessageActionBar(player,
                            libUtils.getLanguageUtils().getTranslation("dashSeparation"),
                            "%s.msg.dashSeparation".formatted(modid),
                            false
                    );
                    libUtils.getLanguageUtils().sendPlayerMessageActionBar(player,
                            libUtils.getLanguageUtils().getTranslation(DESC + option),
                            "%s.msg.getDesc.%s".formatted(modid, option),
                            false
                    );

                    if (value instanceof Boolean)
                    {
                        libUtils.getLanguageUtils().sendPlayerMessageActionBar(player,
                                libUtils.getLanguageUtils().getTranslation("currentValue"),
                                "%s.msg.currentValue".formatted(modid),
                                false,
                                (Boolean) value ? Text.literal(Formatting.GREEN + "ON (click to change)").
                                        setStyle(Style.EMPTY.withClickEvent(
                                                new ClickEvent(ClickEvent.Action.RUN_COMMAND, ("/%s config %s set " +
                                                        "false false").formatted(modid, option)))
                                        ) : Text.literal(Formatting.RED + "OFF (click to change)").
                                        setStyle(Style.EMPTY.withClickEvent(
                                                new ClickEvent(ClickEvent.Action.RUN_COMMAND, ("/%s config %s set " +
                                                        "true" +
                                                        " false").formatted(modid, option)))
                                        )
                        );
                    }
                    else if (value instanceof Integer)
                    {
                        libUtils.getLanguageUtils().sendPlayerMessageActionBar(player,
                                libUtils.getLanguageUtils().getTranslation("currentValue"),
                                "%s.msg.currentValue".formatted(modid),
                                false,
                                Formatting.GOLD + String.valueOf(value)
                        );

                        if (libUtils.getOptionsStorage().hasRule(option, RULES.OP_LEVELS))
                        {
                            libUtils.getLanguageUtils().sendPlayerMessageActionBar(player,
                                    libUtils.getLanguageUtils().getTranslation("setValue"),
                                    "%s.msg.setValue".formatted(modid),
                                    false,
                                    Text.literal(Formatting.DARK_GREEN + (Formatting.BOLD + "0")).
                                            setStyle(Style.EMPTY.withClickEvent(
                                                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, ("/%s config %s set" +
                                                            " " +
                                                            "0 false").formatted(modid, option)))
                                            ),
                                    Text.literal(Formatting.DARK_GREEN + (Formatting.BOLD + "1")).
                                            setStyle(Style.EMPTY.withClickEvent(
                                                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, ("/%s config %s set" +
                                                            " " +
                                                            "1 false").formatted(modid, option)))
                                            ),
                                    Text.literal(Formatting.DARK_GREEN + (Formatting.BOLD + "2")).
                                            setStyle(Style.EMPTY.withClickEvent(
                                                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, ("/%s config %s set" +
                                                            " " +
                                                            "2 false").formatted(modid, option)))
                                            ),
                                    Text.literal(Formatting.DARK_GREEN + (Formatting.BOLD + "3")).
                                            setStyle(Style.EMPTY.withClickEvent(
                                                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, ("/%s config %s set" +
                                                            " " +
                                                            "3 false").formatted(modid, option)))
                                            ),
                                    Text.literal(Formatting.DARK_GREEN + (Formatting.BOLD + "4")).
                                            setStyle(Style.EMPTY.withClickEvent(
                                                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, ("/%s config %s set" +
                                                            " " +
                                                            "4 false").formatted(modid, option)))
                                            )
                            );
                        }
                        else if (!libUtils.getOptionsStorage().hasRule(option, RULES.MAX_VALUE)
                                && !libUtils.getOptionsStorage().hasRule(option, RULES.MIN_VALUE)
                                && !libUtils.getOptionsStorage().hasRule(option, RULES.NEGATIVE_VALUE)
                        )
                        {
                            libUtils.getLanguageUtils().sendPlayerMessageActionBar(player,
                                    libUtils.getLanguageUtils().getTranslation("setValue"),
                                    "%s.msg.setValue".formatted(modid),
                                    false,
                                    Text.literal(Formatting.DARK_GREEN + (Formatting.BOLD + "8")).
                                            setStyle(Style.EMPTY.withClickEvent(
                                                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, ("/%s config %s set" +
                                                            " " +
                                                            "8 false").formatted(modid, option)))
                                            ),
                                    Text.literal(Formatting.DARK_GREEN + (Formatting.BOLD + "16")).
                                            setStyle(Style.EMPTY.withClickEvent(
                                                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, ("/%s config %s set" +
                                                            " " +
                                                            "16 false").formatted(modid, option)))
                                            ),
                                    Text.literal(Formatting.DARK_GREEN + (Formatting.BOLD + "32")).
                                            setStyle(Style.EMPTY.withClickEvent(
                                                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, ("/%s config %s set" +
                                                            " " +
                                                            "32 false").formatted(modid, option)))
                                            ),
                                    Text.literal(Formatting.DARK_GREEN + (Formatting.BOLD + "64")).
                                            setStyle(Style.EMPTY.withClickEvent(
                                                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, ("/%s config %s set" +
                                                            " " +
                                                            "64 false").formatted(modid, option)))
                                            ),
                                    Text.literal(Formatting.DARK_GREEN + (Formatting.BOLD + "128")).
                                            setStyle(Style.EMPTY.withClickEvent(
                                                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, ("/%s config %s set" +
                                                            " " +
                                                            "128 false").formatted(modid, option)))
                                            )
                            );
                        }
                    }
                    libUtils.getLanguageUtils().sendPlayerMessageActionBar(player,
                            libUtils.getLanguageUtils().getTranslation("dashSeparation"),
                            "%s.msg.dashSeparation".formatted(modid),
                            false
                    );
                }
                else
                {
                    libUtils.getLanguageUtils().sendPlayerMessage(Objects.requireNonNull(context.getSource().getPlayer()),
                            libUtils.getLanguageUtils().getTranslation(ERROR + "optionNotFound"),
                            "%s.msg.optionNotFound".formatted(modid)
                    );
                }
            }
        }
        return Command.SINGLE_SUCCESS;
    }

    /**
     * Called by the command {@code /modid get-config}
     * <p>
     * Sends a message in the player's chat with the current value of every option of your mod
     *
     * <ul><h2>Translations paths :</h2>
     *      <li>{@code "modid.msg.dashSeparation"}</li>
     *      <li>{@code "modid.msg.getCfg.header"}</li>
     *      <li>{@code "modid.msg.getCfg.option"} (option is the parameter of the function)</li>
     * </ul>
     *
     * <ul><h2>Custom translations :</h2> Required only if the option useCustomTranslations is set to true
     *      <li>{@code "dashSeparation"}</li>
     *      <li>{@link TranslationsPrefixes#GET_CFG} + {@code "header"}</li>
     *      <li>{@link TranslationsPrefixes#GET_CFG} + {@code option} (option is the parameter of the function)</li>
     * </ul>
     */
    public int getConfigOptions(@NotNull CommandContext<ServerCommandSource> context)
    {
        ServerPlayerEntity player = context.getSource().getPlayer();

        if (libUtils.isPlayer(context.getSource()))
        {
            if (libUtils.hasPermission(Objects.requireNonNull(player), MIN_OP_LVL_EDIT_CONFIG.getValue()))
            {
                libUtils.getLanguageUtils().sendPlayerMessageActionBar(player,
                        libUtils.getLanguageUtils().getTranslation("dashSeparation"),
                        "%s.msg.dashSeparation".formatted(modid),
                        false
                );
                libUtils.getLanguageUtils().sendPlayerMessageActionBar(player,
                        libUtils.getLanguageUtils().getTranslation(GET_CFG + "header"),
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
                            libUtils.getLanguageUtils().sendPlayerMessageActionBar(player,
                                    libUtils.getLanguageUtils().getTranslation(GET_CFG + option),
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
                            libUtils.getLanguageUtils().sendPlayerMessageActionBar(player,
                                    libUtils.getLanguageUtils().getTranslation(GET_CFG + option),
                                    "%s.msg.getCfg.%s".formatted(modid, option),
                                    false,
                                    Formatting.GOLD + integerValue.toString()
                            );
                        }
                    }
                }

                libUtils.getLanguageUtils().sendPlayerMessageActionBar(player,
                        libUtils.getLanguageUtils().getTranslation("dashSeparation"),
                        "%s.msg.dashSeparation".formatted(modid),
                        false
                );
            }
        }
        return Command.SINGLE_SUCCESS;
    }
}
