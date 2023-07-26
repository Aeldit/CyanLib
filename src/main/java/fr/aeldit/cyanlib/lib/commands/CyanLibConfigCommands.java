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
import fr.aeldit.cyanlib.lib.commands.arguments.ArgumentSuggestion;
import fr.aeldit.cyanlib.lib.utils.RULES;
import fr.aeldit.cyanlib.lib.utils.TranslationsPrefixes;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;

import static fr.aeldit.cyanlib.lib.utils.TranslationsPrefixes.*;

public class CyanLibConfigCommands
{
    private final String modid;
    private final CyanLib libUtils;
    private final Map<String, String> defaultTranslations;

    public CyanLibConfigCommands(String modid, CyanLib libUtils, Map<String, String> defaultTranslations)
    {
        this.modid = modid;
        this.libUtils = libUtils;
        this.defaultTranslations = defaultTranslations;
    }

    public void register(@NotNull CommandDispatcher<ServerCommandSource> dispatcher)
    {
        dispatcher.register(CommandManager.literal(this.modid)
                .then(CommandManager.literal("config")
                        .then(CommandManager.argument("optionName", StringArgumentType.string())
                                .suggests((context, builder) -> ArgumentSuggestion.getOptions(builder, this.libUtils.getOptionsStorage()))
                                .then(CommandManager.literal("set")
                                        .then(CommandManager.argument("booleanValue", BoolArgumentType.bool())
                                                .then(CommandManager.argument("mode", BoolArgumentType.bool())
                                                        .executes(this::setBoolOption)
                                                )
                                                .executes(this::setBoolOptionFromCommand)
                                        )
                                        .then(CommandManager.argument("integerValue", IntegerArgumentType.integer())
                                                .suggests((context, builder) -> ArgumentSuggestion.getOPLevels(builder))
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
     *
     * <ul><h2>Required config options :</h2>
     *      <li>{@code minOpLevelExeEditConfig}</li>
     * </ul>
     */
    public int reloadTranslations(@NotNull CommandContext<ServerCommandSource> context)
    {
        if (this.libUtils.isPlayer(context.getSource()))
        {
            if (this.libUtils.hasPermission(Objects.requireNonNull(context.getSource().getPlayer()), libUtils.getOptionsStorage().getIntegerOption("minOpLvlEditConfig")))
            {
                this.libUtils.getLanguageUtils().loadLanguage(defaultTranslations);

                this.libUtils.getLanguageUtils().sendPlayerMessage(context.getSource().getPlayer(),
                        this.libUtils.getLanguageUtils().getTranslation("translationsReloaded"),
                        "%s.msg.translationsReloaded".formatted(this.modid)
                );
            }
        }
        return Command.SINGLE_SUCCESS;
    }

    /**
     * Called by the command {@code /modid <optionName> set [booleanValue] [mode]}
     * <p>
     * Sets the value of the given {@code boolean option} to the given {@code boolean value} and executes the
     * {@code /modid get-config} command if {@code [mode]} is true, and the command {@code /modid config <optionName>} otherwise.
     * This allows to see the changed option in the chat
     *
     * <ul><h2>Translations paths :</h2>
     *      <li>{@code "modid.msg.set.option"} (option is the parameter of the function)</li>
     *      <li>{@code "modid.msg.wrongType"}</li>
     *      <li>{@code "modid.msg.optionNotFound"}</li>
     * </ul>
     *
     * <ul><h2>Custom translations :</h2> Required only if the option useCustomTranslations is set to true
     *      <li>{@link TranslationsPrefixes#SET} + {@code option} (option is the parameter of the function)</li>
     *      <li>{@link TranslationsPrefixes#ERROR} + {@code "wrongType"}</li>
     *      <li>{@link TranslationsPrefixes#ERROR} + {@code "optionNotFound"}</li>
     * </ul>
     *
     * <ul><h2>Required config options :</h2>
     *      <li>{@code minOpLevelExeEditConfig}</li>
     * </ul>
     */
    public int setBoolOption(@NotNull CommandContext<ServerCommandSource> context)
    {
        ServerCommandSource source = context.getSource();

        if (this.libUtils.isPlayer(source))
        {
            if (this.libUtils.hasPermission(Objects.requireNonNull(source.getPlayer()), libUtils.getOptionsStorage().getIntegerOption("minOpLvlEditConfig")))
            {
                String option = StringArgumentType.getString(context, "optionName");

                if (this.libUtils.getOptionsStorage().booleanOptionExists(option))
                {
                    boolean value = BoolArgumentType.getBool(context, "booleanValue");
                    this.libUtils.getOptionsStorage().setAndSaveBooleanOption(option, value);

                    if (this.libUtils.getOptionsStorage().hasRule(option, RULES.LOAD_CUSTOM_TRANSLATIONS))
                    {
                        if (value)
                        {
                            this.libUtils.getLanguageUtils().loadLanguage(defaultTranslations);
                        }
                        else
                        {
                            this.libUtils.getLanguageUtils().unload();
                        }
                    }

                    if (BoolArgumentType.getBool(context, "mode"))
                    {
                        source.getServer().getCommandManager().executeWithPrefix(source, "/%s get-config".formatted(this.modid));
                    }
                    else
                    {
                        source.getServer().getCommandManager().executeWithPrefix(source, "/%s config %s".formatted(this.modid, option));
                    }
                }
                else
                {
                    this.libUtils.getLanguageUtils().sendPlayerMessage(source.getPlayer(),
                            this.libUtils.getLanguageUtils().getTranslation(ERROR + "optionNotFound"),
                            "%s.msg.optionNotFound".formatted(this.modid)
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
     *      <li>{@code "modid.msg.set.option"} (option is the parameter of the function)</li>
     *      <li>{@code "modid.msg.wrongType"}</li>
     *      <li>{@code "modid.msg.optionNotFound"}</li>
     * </ul>
     *
     * <ul><h2>Custom translations :</h2> Required only if the option useCustomTranslations is set to true
     *      <li>{@link TranslationsPrefixes#SET} + {@code option} (option is the parameter of the function)</li>
     *      <li>{@link TranslationsPrefixes#ERROR} + {@code "wrongType"}</li>
     *      <li>{@link TranslationsPrefixes#ERROR} + {@code "optionNotFound"}</li>
     * </ul>
     *
     * <ul><h2>Required config options :</h2>
     *      <li>{@code minOpLevelExeEditConfig}</li>
     * </ul>
     */
    public int setBoolOptionFromCommand(@NotNull CommandContext<ServerCommandSource> context)
    {
        ServerCommandSource source = context.getSource();

        if (this.libUtils.isPlayer(source))
        {
            if (this.libUtils.hasPermission(Objects.requireNonNull(source.getPlayer()), libUtils.getOptionsStorage().getIntegerOption("minOpLvlEditConfig")))
            {
                String option = StringArgumentType.getString(context, "optionName");

                if (this.libUtils.getOptionsStorage().booleanOptionExists(option))
                {
                    boolean value = BoolArgumentType.getBool(context, "booleanValue");
                    this.libUtils.getOptionsStorage().setAndSaveBooleanOption(option, value);

                    if (this.libUtils.getOptionsStorage().hasRule(option, RULES.LOAD_CUSTOM_TRANSLATIONS))
                    {
                        if (value)
                        {
                            this.libUtils.getLanguageUtils().loadLanguage(defaultTranslations);
                        }
                        else
                        {
                            this.libUtils.getLanguageUtils().unload();
                        }
                    }

                    this.libUtils.getLanguageUtils().sendPlayerMessage(source.getPlayer(),
                            this.libUtils.getLanguageUtils().getTranslation(SET + option),
                            "%s.msg.set.%s".formatted(this.modid, option),
                            value ? Formatting.GREEN + "ON" : Formatting.RED + "OFF"
                    );
                }
                else
                {
                    this.libUtils.getLanguageUtils().sendPlayerMessage(source.getPlayer(),
                            this.libUtils.getLanguageUtils().getTranslation(ERROR + "optionNotFound"),
                            "%s.msg.optionNotFound".formatted(this.modid)
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
     * {@code /modid get-config} command if {@code [mode]} is true, and the command {@code /modid config <optionName>} otherwise.
     * This allows to see the changed option in the chat
     *
     * <ul><h2>Translations paths :</h2>
     *      <li>{@code "modid.msg.set.option"} (option is the parameter of the function)</li>
     *      <li>{@code "modid.msg.wrongType"}</li>
     *      <li>{@code "modid.msg.optionNotFound"}</li>
     * </ul>
     *
     * <ul><h2>Custom translations :</h2> Required only if the option useCustomTranslations is set to true
     *      <li>{@link TranslationsPrefixes#SET} + {@code option} (option is the parameter of the function)</li>
     *      <li>{@link TranslationsPrefixes#ERROR} + {@code "wrongType"}</li>
     *      <li>{@link TranslationsPrefixes#ERROR} + {@code "optionNotFound"}</li>
     * </ul>
     *
     * <ul><h2>Required config options :</h2>
     *      <li>{@code minOpLevelExeEditConfig}</li>
     * </ul>
     */
    public int setIntOption(@NotNull CommandContext<ServerCommandSource> context)
    {
        ServerCommandSource source = context.getSource();

        if (this.libUtils.isPlayer(source))
        {
            if (this.libUtils.hasPermission(Objects.requireNonNull(source.getPlayer()), libUtils.getOptionsStorage().getIntegerOption("minOpLvlEditConfig")))
            {
                String option = StringArgumentType.getString(context, "optionName");

                if (this.libUtils.getOptionsStorage().integerOptionExists(option))
                {
                    int value = IntegerArgumentType.getInteger(context, "integerValue");

                    if (this.libUtils.getOptionsStorage().setAndSaveIntegerOption(option, value))
                    {
                        if (BoolArgumentType.getBool(context, "mode"))
                        {
                            source.getServer().getCommandManager().executeWithPrefix(source, "/%s get-config".formatted(this.modid));
                        }
                        else
                        {
                            source.getServer().getCommandManager().executeWithPrefix(source, "/%s config %s".formatted(this.modid, option));
                        }
                    }
                    else
                    {
                        this.libUtils.getLanguageUtils().sendPlayerMessage(source.getPlayer(),
                                this.libUtils.getLanguageUtils().getTranslation(ERROR + "incorrectInteger"),
                                "%s.msg.incorrectInteger".formatted(this.modid)
                        );
                    }
                }
                else
                {
                    this.libUtils.getLanguageUtils().sendPlayerMessage(source.getPlayer(),
                            this.libUtils.getLanguageUtils().getTranslation(ERROR + "optionNotFound"),
                            "%s.msg.optionNotFound".formatted(this.modid)
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
     *      <li>{@code "modid.msg.set.option"} (option is the parameter of the function)</li>
     *      <li>{@code "modid.msg.wrongType"}</li>
     *      <li>{@code "modid.msg.optionNotFound"}</li>
     * </ul>
     *
     * <ul><h2>Custom translations :</h2> Required only if the option useCustomTranslations is set to true
     *      <li>{@link TranslationsPrefixes#SET} + {@code option} (option is the parameter of the function)</li>
     *      <li>{@link TranslationsPrefixes#ERROR} + {@code "wrongType"}</li>
     *      <li>{@link TranslationsPrefixes#ERROR} + {@code "optionNotFound"}</li>
     * </ul>
     *
     * <ul><h2>Required config options :</h2>
     *      <li>{@code minOpLevelExeEditConfig}</li>
     * </ul>
     */
    public int setIntOptionFromCommand(@NotNull CommandContext<ServerCommandSource> context)
    {
        ServerCommandSource source = context.getSource();

        if (this.libUtils.isPlayer(source))
        {
            if (this.libUtils.hasPermission(Objects.requireNonNull(source.getPlayer()), libUtils.getOptionsStorage().getIntegerOption("minOpLvlEditConfig")))
            {
                String option = StringArgumentType.getString(context, "optionName");

                if (this.libUtils.getOptionsStorage().integerOptionExists(option))
                {
                    int value = IntegerArgumentType.getInteger(context, "integerValue");

                    if (this.libUtils.getOptionsStorage().setAndSaveIntegerOption(option, value))
                    {
                        this.libUtils.getLanguageUtils().sendPlayerMessage(source.getPlayer(),
                                this.libUtils.getLanguageUtils().getTranslation(SET + option),
                                "%s.msg.set.%s".formatted(this.modid, option),
                                Formatting.GOLD + String.valueOf(value)
                        );
                    }
                    else
                    {
                        this.libUtils.getLanguageUtils().sendPlayerMessage(source.getPlayer(),
                                this.libUtils.getLanguageUtils().getTranslation(ERROR + "incorrectInteger"),
                                "%s.msg.incorrectInteger".formatted(this.modid)
                        );
                    }
                }
                else
                {
                    this.libUtils.getLanguageUtils().sendPlayerMessage(source.getPlayer(),
                            this.libUtils.getLanguageUtils().getTranslation(ERROR + "optionNotFound"),
                            "%s.msg.optionNotFound".formatted(this.modid)
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
     *      <li>{@code "modid.msg.getDesc.option"} (option is the parameter of the function)</li>
     *      <li>{@code "modid.msg.currentValue"}</li>
     *      <li>{@code "modid.msg.setValue"}</li>
     *      <li>{@code "modid.msg.optionNotFound"}</li>
     * </ul>
     *
     * <ul><h2>Custom translations :</h2> Required only if the option useCustomTranslations is set to true
     *      <li>{@code "dashSeparation"}</li>
     *      <li>{@link TranslationsPrefixes#DESC} + {@code option} (option is the parameter of the function)</li>
     *      <li>{@code "currentValue"}</li>
     *      <li>{@code "setValue"}</li>
     *      <li>{@link TranslationsPrefixes#ERROR} + {@code "optionNotFound"}</li>
     * </ul>
     *
     * <ul><h2>Required config options :</h2>
     *      <li>{@code minOpLevelExeEditConfig}</li>
     * </ul>
     *
     * <ul><h2>Required config commands :</h2>
     *      <li>{@code /modid config <optionName> set [(boolean) value] [(boolean) mode]}</li>
     *      <li>{@code /modid config <optionName> set [(int) value] [(boolean) mode]}</li>
     * </ul>
     */
    public int getOptionChatConfig(@NotNull CommandContext<ServerCommandSource> context)
    {
        ServerPlayerEntity player = context.getSource().getPlayer();

        if (this.libUtils.isPlayer(context.getSource()))
        {
            if (this.libUtils.hasPermission(Objects.requireNonNull(player), libUtils.getOptionsStorage().getIntegerOption("minOpLvlEditConfig")))
            {
                String option = StringArgumentType.getString(context, "optionName");

                if (this.libUtils.getOptionsStorage().optionExists(option))
                {
                    Object value = this.libUtils.getOptionsStorage().getOption(option);
                    System.out.println(value);

                    this.libUtils.getLanguageUtils().sendPlayerMessageActionBar(player,
                            this.libUtils.getLanguageUtils().getTranslation("dashSeparation"),
                            "%s.msg.dashSeparation".formatted(this.modid),
                            false
                    );
                    this.libUtils.getLanguageUtils().sendPlayerMessageActionBar(player,
                            this.libUtils.getLanguageUtils().getTranslation(DESC + option),
                            "%s.msg.getDesc.%s".formatted(this.modid, option),
                            false
                    );

                    if (value instanceof Boolean)
                    {
                        this.libUtils.getLanguageUtils().sendPlayerMessageActionBar(player,
                                this.libUtils.getLanguageUtils().getTranslation("currentValue"),
                                "%s.msg.currentValue".formatted(this.modid),
                                false,
                                (Boolean) value ? Text.literal(Formatting.GREEN + "ON (click to change)").
                                        setStyle(Style.EMPTY.withClickEvent(
                                                new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/%s config %s set false false".formatted(this.modid, option)))
                                        ) : Text.literal(Formatting.RED + "OFF (click to change)").
                                        setStyle(Style.EMPTY.withClickEvent(
                                                new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/%s config %s set true false".formatted(this.modid, option)))
                                        )
                        );
                    }
                    else if (value instanceof Integer)
                    {
                        this.libUtils.getLanguageUtils().sendPlayerMessageActionBar(player,
                                this.libUtils.getLanguageUtils().getTranslation("currentValue"),
                                "%s.msg.currentValue".formatted(this.modid),
                                false,
                                Formatting.GOLD + String.valueOf(value)
                        );

                        if (this.libUtils.getOptionsStorage().hasRule(option, RULES.OP_LEVELS))
                        {
                            this.libUtils.getLanguageUtils().sendPlayerMessageActionBar(player,
                                    this.libUtils.getLanguageUtils().getTranslation("setValue"),
                                    "%s.msg.setValue".formatted(this.modid),
                                    false,
                                    Text.literal(Formatting.DARK_GREEN + (Formatting.BOLD + "0")).
                                            setStyle(Style.EMPTY.withClickEvent(
                                                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/%s config %s set 0 false".formatted(this.modid, option)))
                                            ),
                                    Text.literal(Formatting.DARK_GREEN + (Formatting.BOLD + "1")).
                                            setStyle(Style.EMPTY.withClickEvent(
                                                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/%s config %s set 1 false".formatted(this.modid, option)))
                                            ),
                                    Text.literal(Formatting.DARK_GREEN + (Formatting.BOLD + "2")).
                                            setStyle(Style.EMPTY.withClickEvent(
                                                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/%s config %s set 2 false".formatted(this.modid, option)))
                                            ),
                                    Text.literal(Formatting.DARK_GREEN + (Formatting.BOLD + "3")).
                                            setStyle(Style.EMPTY.withClickEvent(
                                                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/%s config %s set 3 false".formatted(this.modid, option)))
                                            ),
                                    Text.literal(Formatting.DARK_GREEN + (Formatting.BOLD + "4")).
                                            setStyle(Style.EMPTY.withClickEvent(
                                                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/%s config %s set 4 false".formatted(this.modid, option)))
                                            )
                            );
                        }
                        else if (!this.libUtils.getOptionsStorage().hasRule(option, RULES.MAX_VALUE)
                                && !this.libUtils.getOptionsStorage().hasRule(option, RULES.MIN_VALUE)
                                && !this.libUtils.getOptionsStorage().hasRule(option, RULES.NEGATIVE_VALUE)
                        )
                        {
                            this.libUtils.getLanguageUtils().sendPlayerMessageActionBar(player,
                                    this.libUtils.getLanguageUtils().getTranslation("setValue"),
                                    "%s.msg.setValue".formatted(this.modid),
                                    false,
                                    Text.literal(Formatting.DARK_GREEN + (Formatting.BOLD + "8")).
                                            setStyle(Style.EMPTY.withClickEvent(
                                                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/%s config %s set 8 false".formatted(this.modid, option)))
                                            ),
                                    Text.literal(Formatting.DARK_GREEN + (Formatting.BOLD + "16")).
                                            setStyle(Style.EMPTY.withClickEvent(
                                                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/%s config %s set 16 false".formatted(this.modid, option)))
                                            ),
                                    Text.literal(Formatting.DARK_GREEN + (Formatting.BOLD + "32")).
                                            setStyle(Style.EMPTY.withClickEvent(
                                                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/%s config %s set 32 false".formatted(this.modid, option)))
                                            ),
                                    Text.literal(Formatting.DARK_GREEN + (Formatting.BOLD + "64")).
                                            setStyle(Style.EMPTY.withClickEvent(
                                                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/%s config %s set 64 false".formatted(this.modid, option)))
                                            ),
                                    Text.literal(Formatting.DARK_GREEN + (Formatting.BOLD + "128")).
                                            setStyle(Style.EMPTY.withClickEvent(
                                                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/%s config %s set 128 false".formatted(this.modid, option)))
                                            )
                            );
                        }
                    }
                    this.libUtils.getLanguageUtils().sendPlayerMessageActionBar(player,
                            this.libUtils.getLanguageUtils().getTranslation("dashSeparation"),
                            "%s.msg.dashSeparation".formatted(this.modid),
                            false
                    );
                }
                else
                {
                    this.libUtils.getLanguageUtils().sendPlayerMessage(Objects.requireNonNull(context.getSource().getPlayer()),
                            this.libUtils.getLanguageUtils().getTranslation(ERROR + "optionNotFound"),
                            "%s.msg.optionNotFound".formatted(this.modid)
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
     *      <li>{@link TranslationsPrefixes#GETCFG} + {@code "header"}</li>
     *      <li>{@link TranslationsPrefixes#GETCFG} + {@code option} (option is the parameter of the function)</li>
     * </ul>
     *
     * <ul><h2>Required config options :</h2>
     *      <li>{@code minOpLevelExeEditConfig}</li>
     * </ul>
     *
     * <ul><h2>Required config commands :</h2>
     *      <li>{@code /modid config <optionName> set [(boolean) value] [(boolean) mode]}</li>
     * </ul>
     */
    public int getConfigOptions(@NotNull CommandContext<ServerCommandSource> context)
    {
        ServerPlayerEntity player = context.getSource().getPlayer();

        if (this.libUtils.isPlayer(context.getSource()))
        {
            if (this.libUtils.hasPermission(Objects.requireNonNull(player), libUtils.getOptionsStorage().getIntegerOption("minOpLvlEditConfig")))
            {
                this.libUtils.getLanguageUtils().sendPlayerMessageActionBar(player,
                        this.libUtils.getLanguageUtils().getTranslation("dashSeparation"),
                        "%s.msg.dashSeparation".formatted(this.modid),
                        false
                );
                this.libUtils.getLanguageUtils().sendPlayerMessageActionBar(player,
                        this.libUtils.getLanguageUtils().getTranslation(GETCFG + "header"),
                        "%s.msg.getCfg.header".formatted(this.modid),
                        false
                );

                for (String option : this.libUtils.getOptionsStorage().getOptionsNames())
                {
                    if (this.libUtils.getOptionsStorage().booleanOptionExists(option))
                    {
                        this.libUtils.getLanguageUtils().sendPlayerMessageActionBar(player,
                                this.libUtils.getLanguageUtils().getTranslation(GETCFG + option),
                                "%s.msg.getCfg.%s".formatted(this.modid, option),
                                false,
                                this.libUtils.getOptionsStorage().getBooleanOption(option) ? Text.literal(Formatting.GREEN + "ON").
                                        setStyle(Style.EMPTY.withClickEvent(
                                                new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/%s config %s set false true".formatted(this.modid, option)))
                                        ) : Text.literal(Formatting.RED + "OFF").
                                        setStyle(Style.EMPTY.withClickEvent(
                                                new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/%s config %s set true true".formatted(this.modid, option)))
                                        )
                        );
                    }
                    else if (this.libUtils.getOptionsStorage().integerOptionExists(option))
                    {
                        this.libUtils.getLanguageUtils().sendPlayerMessageActionBar(player,
                                this.libUtils.getLanguageUtils().getTranslation(GETCFG + option),
                                "%s.msg.getCfg.%s".formatted(this.modid, option),
                                false,
                                Formatting.GOLD + Integer.toString(this.libUtils.getOptionsStorage().getIntegerOption(option))
                        );
                    }
                }

                this.libUtils.getLanguageUtils().sendPlayerMessageActionBar(player,
                        this.libUtils.getLanguageUtils().getTranslation("dashSeparation"),
                        "%s.msg.dashSeparation".formatted(this.modid),
                        false
                );
            }
        }
        return Command.SINGLE_SUCCESS;
    }
}
