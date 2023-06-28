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

package fr.aeldit.cyanlib.lib;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;

import static fr.aeldit.cyanlib.lib.CyanLibLanguageUtils.sendPlayerMessage;
import static fr.aeldit.cyanlib.lib.CyanLibLanguageUtils.sendPlayerMessageActionBar;
import static fr.aeldit.cyanlib.lib.TranslationsPrefixes.*;

public class CyanLibCommands
{
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
     *
     * @param context             The command context
     * @param defaultTranslations The default translations of your mod
     * @param cyanLib             The CyanLib instance
     */
    public static void reloadTranslations(@NotNull CommandContext<ServerCommandSource> context, Map<String, String> defaultTranslations, @NotNull CyanLib cyanLib)
    {
        if (cyanLib.isPlayer(context.getSource()))
        {
            if (cyanLib.hasPermission(Objects.requireNonNull(context.getSource().getPlayer()), cyanLib.getConfigUtils().getIntOption("minOpLevelExeEditConfig")))
            {
                cyanLib.getLanguageUtils().loadLanguage(defaultTranslations);

                sendPlayerMessage(context.getSource().getPlayer(),
                        cyanLib.getLanguageUtils().getTranslation("translationsReloaded"),
                        "%s.msg.translationsReloaded".formatted(cyanLib.getMODID())
                );
            }
        }
    }

    /**
     * Sets the value of the given {@code boolean option} to the given {@code boolean value}
     *
     * <ul><h2>Translations paths :</h2>
     *      <li>{@code "modid.msg.set.option"} (option is the parameter of the function)</li>
     *      <li>{@code "modid.msg.error.wrongType"}</li>
     * </ul>
     *
     * <ul><h2>Required config options :</h2>
     *      <li>{@code minOpLevelExeEditConfig}</li>
     * </ul>
     *
     * @param context             The command context
     * @param defaultTranslations The default translations of your mod
     * @param cyanLib             The CyanLib instance
     * @param option              The option
     * @param value               The value
     * @param fromCmd             If the function is called from a command. If you don't have the {@code /modid getConfig} and {@code /modid config <optionName>} commands,
     *                            you have to put this parameter to true
     * @param mode                The mode to use (to call again {@code /modid getConfig} or {@code /modid config <optionName>}).
     *                            If {@code fromCmd} is {@code false}, you can put {@code mode} to either {@code true} or {@code false}, it won't change anything
     */
    public static void setBoolOption(@NotNull CommandContext<ServerCommandSource> context, Map<String, String> defaultTranslations, @NotNull CyanLib cyanLib, String option, Object value, boolean fromCmd, boolean mode)
    {
        ServerCommandSource source = context.getSource();

        if (cyanLib.isPlayer(source))
        {
            if (cyanLib.hasPermission(Objects.requireNonNull(source.getPlayer()), cyanLib.getConfigUtils().getIntOption("minOpLevelExeEditConfig")))
            {
                if (cyanLib.getConfigUtils().optionExists(option))
                {
                    if (cyanLib.getConfigUtils().isBoolean(option))
                    {
                        if (value instanceof Boolean)
                        {
                            cyanLib.getConfigUtils().setOption(option, value);

                            if (option.equals("useCustomTranslations"))
                            {
                                if ((Boolean) value)
                                {
                                    cyanLib.getLanguageUtils().loadLanguage(defaultTranslations);
                                }
                                else
                                {
                                    cyanLib.getLanguageUtils().unload();
                                }
                            }

                            if (!fromCmd)
                            {
                                if (mode)
                                {
                                    source.getServer().getCommandManager().executeWithPrefix(source, "/%s getConfig".formatted(cyanLib.getMODID()));
                                }
                                else
                                {
                                    source.getServer().getCommandManager().executeWithPrefix(source, "/%s config %s".formatted(cyanLib.getMODID(), option));
                                }
                            }
                            else
                            {
                                sendPlayerMessage(source.getPlayer(),
                                        cyanLib.getLanguageUtils().getTranslation(SET + option),
                                        "%s.msg.set.%s".formatted(cyanLib.getMODID(), option),
                                        (Boolean) value ? Formatting.GREEN + "ON" : Formatting.RED + "OFF"
                                );
                            }
                        }
                        else
                        {
                            sendPlayerMessage(Objects.requireNonNull(context.getSource().getPlayer()),
                                    cyanLib.getLanguageUtils().getTranslation(ERROR + "wrongType"),
                                    "%s.msg.error.wrongType".formatted(cyanLib.getMODID()),
                                    Formatting.YELLOW + "boolean"
                            );
                        }
                    }
                    else
                    {
                        sendPlayerMessage(Objects.requireNonNull(context.getSource().getPlayer()),
                                cyanLib.getLanguageUtils().getTranslation(ERROR + "wrongType"),
                                "%s.msg.error.wrongType".formatted(cyanLib.getMODID()),
                                Formatting.YELLOW + "integer"
                        );
                    }
                }
                else
                {
                    CyanLibLanguageUtils.sendPlayerMessage(source.getPlayer(),
                            cyanLib.getLanguageUtils().getTranslation(ERROR + "optionNotFound"),
                            "%s.msg.error.optionNotFound".formatted(cyanLib.getMODID())
                    );
                }
            }
        }
    }

    /**
     * Sets the value of the given {@code int option} to the given {@code int value}
     *
     * <ul><h2>Translations paths :</h2>
     *      <li>{@code "modid.msg.set.option"} (option is the parameter of the function)</li>
     *      <li>{@code "modid.msg.error.wrongType"}</li>
     *      <li>{@code "modid.msg.error.optionNotFound"}</li>
     * </ul>
     *
     * <ul><h2>Custom translations :</h2>
     *      <li>{@link TranslationsPrefixes#SET} + {@code "option"} (option is the parameter of the function)</li>
     *      <li>{@link TranslationsPrefixes#ERROR} + {@code "wrongType"}</li>
     *      <li>{@link TranslationsPrefixes#ERROR} + {@code "optionNotFound"}</li>
     * </ul>
     *
     * <ul><h2>Required config options :</h2>
     *      <li>{@code minOpLevelExeEditConfig}</li>
     * </ul>
     *
     * @param context The command context
     * @param cyanLib The CyanLib instance
     * @param option  The option
     * @param value   The value
     * @param fromCmd If the function is called from a command. If you don't have the {@code /modid getConfig} and {@code /modid config <optionName>} commands,
     *                you have to put this parameter to true
     * @param mode    The mode to use (to call again {@code /modid getConfig} or {@code /modid config <optionName>}).
     *                If {@code fromCmd} is {@code false}, you can put {@code mode} to either {@code true} or {@code false}, it won't change anything
     */
    public static void setIntOption(@NotNull CommandContext<ServerCommandSource> context, @NotNull CyanLib cyanLib, String option, Object value, boolean fromCmd, boolean mode)
    {
        ServerCommandSource source = context.getSource();

        if (cyanLib.isPlayer(source))
        {
            if (cyanLib.hasPermission(Objects.requireNonNull(source.getPlayer()), cyanLib.getConfigUtils().getIntOption("minOpLevelExeEditConfig")))
            {
                if (cyanLib.getConfigUtils().optionExists(option))
                {
                    if (cyanLib.getConfigUtils().isInteger(option))
                    {
                        if (value instanceof Integer)
                        {
                            cyanLib.getConfigUtils().setOption(option, value);

                            if (!fromCmd)
                            {
                                if (mode)
                                {
                                    source.getServer().getCommandManager().executeWithPrefix(source, "/%s getConfig".formatted(cyanLib.getMODID()));
                                }
                                else
                                {
                                    source.getServer().getCommandManager().executeWithPrefix(source, "/%s config %s".formatted(cyanLib.getMODID(), option));
                                }
                            }
                            else
                            {
                                sendPlayerMessage(source.getPlayer(),
                                        cyanLib.getLanguageUtils().getTranslation(SET + option),
                                        "%s.msg.set.%s".formatted(cyanLib.getMODID(), option),
                                        Formatting.GOLD + String.valueOf(value)
                                );
                            }
                        }
                        else
                        {
                            sendPlayerMessage(Objects.requireNonNull(context.getSource().getPlayer()),
                                    cyanLib.getLanguageUtils().getTranslation(ERROR + "wrongType"),
                                    "%s.msg.error.wrongType".formatted(cyanLib.getMODID()),
                                    Formatting.YELLOW + "integer"
                            );
                        }
                    }
                    else
                    {
                        sendPlayerMessage(Objects.requireNonNull(context.getSource().getPlayer()),
                                cyanLib.getLanguageUtils().getTranslation(ERROR + "wrongType"),
                                "%s.msg.error.wrongType".formatted(cyanLib.getMODID()),
                                Formatting.YELLOW + "boolean"
                        );
                    }
                }
                else
                {
                    CyanLibLanguageUtils.sendPlayerMessage(source.getPlayer(),
                            cyanLib.getLanguageUtils().getTranslation(ERROR + "optionNotFound"),
                            "%s.msg.error.optionNotFound".formatted(cyanLib.getMODID())
                    );
                }
            }
        }
    }

    /**
     * Sends a message in the player's chat with a description of the option and its current value + some
     * presets the player can click on to change the value of the option
     *
     * <ul><h2>Translations paths :</h2>
     *      <li>{@code "modid.msg.dashSeparation"}</li>
     *      <li>{@code "modid.msg.getDescription.option"} (option is the parameter of the function)</li>
     *      <li>{@code "modid.msg.currentValue"}</li>
     *      <li>{@code "modid.msg.setValue"}</li>
     *      <li>{@code "modid.msg.error.optionNotFound"}</li>
     * </ul>
     *
     * <ul><h2>Custom translations :</h2>
     *      <li>{@code "dashSeparation"}</li>
     *      <li>{@link TranslationsPrefixes#DESC} + {@code "option"} (option is the parameter of the function)</li>
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
     *
     * @param context The command context
     * @param cyanLib The CyanLib instance
     * @param option  The option
     */
    public static void getOptionChatConfig(@NotNull CommandContext<ServerCommandSource> context, @NotNull CyanLib cyanLib, String option)
    {
        ServerPlayerEntity player = context.getSource().getPlayer();

        if (cyanLib.isPlayer(context.getSource()))
        {
            if (cyanLib.hasPermission(Objects.requireNonNull(player), cyanLib.getConfigUtils().getIntOption("minOpLevelExeEditConfig")))
            {
                if (cyanLib.getConfigUtils().optionExists(option))
                {
                    Object value = cyanLib.getConfigUtils().getOption(option);

                    sendPlayerMessageActionBar(player,
                            cyanLib.getLanguageUtils().getTranslation("dashSeparation"),
                            "%s.msg.dashSeparation".formatted(cyanLib.getMODID()),
                            false
                    );
                    sendPlayerMessageActionBar(player,
                            cyanLib.getLanguageUtils().getTranslation(DESC + option),
                            "%s.msg.getDescription.%s".formatted(cyanLib.getMODID(), option),
                            false
                    );

                    if (value instanceof Boolean)
                    {
                        sendPlayerMessageActionBar(player,
                                cyanLib.getLanguageUtils().getTranslation("currentValue"),
                                "%s.msg.currentValue".formatted(cyanLib.getMODID()),
                                false,
                                (Boolean) value ? Text.literal(Formatting.GREEN + "ON (click to change)").
                                        setStyle(Style.EMPTY.withClickEvent(
                                                new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/%s config %s set false false".formatted(cyanLib.getMODID(), option)))
                                        ) : Text.literal(Formatting.RED + "OFF (click to change)").
                                        setStyle(Style.EMPTY.withClickEvent(
                                                new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/%s config %s set true false".formatted(cyanLib.getMODID(), option)))
                                        )
                        );
                    }
                    else if (value instanceof Integer)
                    {
                        sendPlayerMessageActionBar(player,
                                cyanLib.getLanguageUtils().getTranslation("currentValue"),
                                "%s.msg.currentValue".formatted(cyanLib.getMODID()),
                                false,
                                Formatting.GOLD + String.valueOf(value)
                        );

                        if (option.startsWith("minOpLevelExe"))
                        {
                            sendPlayerMessageActionBar(player,
                                    cyanLib.getLanguageUtils().getTranslation("setValue"),
                                    "%s.msg.setValue".formatted(cyanLib.getMODID()),
                                    false,
                                    Text.literal(Formatting.DARK_GREEN + (Formatting.BOLD + "0")).
                                            setStyle(Style.EMPTY.withClickEvent(
                                                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/%s config %s set 0 false".formatted(cyanLib.getMODID(), option)))
                                            ),
                                    Text.literal(Formatting.DARK_GREEN + (Formatting.BOLD + "1")).
                                            setStyle(Style.EMPTY.withClickEvent(
                                                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/%s config %s set 1 false".formatted(cyanLib.getMODID(), option)))
                                            ),
                                    Text.literal(Formatting.DARK_GREEN + (Formatting.BOLD + "2")).
                                            setStyle(Style.EMPTY.withClickEvent(
                                                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/%s config %s set 2 false".formatted(cyanLib.getMODID(), option)))
                                            ),
                                    Text.literal(Formatting.DARK_GREEN + (Formatting.BOLD + "3")).
                                            setStyle(Style.EMPTY.withClickEvent(
                                                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/%s config %s set 3 false".formatted(cyanLib.getMODID(), option)))
                                            ),
                                    Text.literal(Formatting.DARK_GREEN + (Formatting.BOLD + "4")).
                                            setStyle(Style.EMPTY.withClickEvent(
                                                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/%s config %s set 4 false".formatted(cyanLib.getMODID(), option)))
                                            )
                            );
                        }
                        else
                        {
                            sendPlayerMessageActionBar(player,
                                    cyanLib.getLanguageUtils().getTranslation("setValue"),
                                    "%s.msg.setValue".formatted(cyanLib.getMODID()),
                                    false,
                                    Text.literal(Formatting.DARK_GREEN + (Formatting.BOLD + "8")).
                                            setStyle(Style.EMPTY.withClickEvent(
                                                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/%s config %s set 8 false".formatted(cyanLib.getMODID(), option)))
                                            ),
                                    Text.literal(Formatting.DARK_GREEN + (Formatting.BOLD + "16")).
                                            setStyle(Style.EMPTY.withClickEvent(
                                                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/%s config %s set 16 false".formatted(cyanLib.getMODID(), option)))
                                            ),
                                    Text.literal(Formatting.DARK_GREEN + (Formatting.BOLD + "32")).
                                            setStyle(Style.EMPTY.withClickEvent(
                                                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/%s config %s set 32 false".formatted(cyanLib.getMODID(), option)))
                                            ),
                                    Text.literal(Formatting.DARK_GREEN + (Formatting.BOLD + "64")).
                                            setStyle(Style.EMPTY.withClickEvent(
                                                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/%s config %s set 64 false".formatted(cyanLib.getMODID(), option)))
                                            ),
                                    Text.literal(Formatting.DARK_GREEN + (Formatting.BOLD + "128")).
                                            setStyle(Style.EMPTY.withClickEvent(
                                                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/%s config %s set 128 false".formatted(cyanLib.getMODID(), option)))
                                            )
                            );
                        }
                    }
                    sendPlayerMessageActionBar(player,
                            cyanLib.getLanguageUtils().getTranslation("dashSeparation"),
                            "%s.msg.dashSeparation".formatted(cyanLib.getMODID()),
                            false
                    );
                }
                else
                {
                    CyanLibLanguageUtils.sendPlayerMessage(Objects.requireNonNull(context.getSource().getPlayer()),
                            cyanLib.getLanguageUtils().getTranslation(ERROR + "optionNotFound"),
                            "%s.msg.error.optionNotFound".formatted(cyanLib.getMODID())
                    );
                }
            }
        }
    }

    /**
     * Sends a message in the player's chat with the current value of every option of your mod
     *
     * <ul><h2>Translations paths :</h2>
     *      <li>{@code "modid.msg.dashSeparation"}</li>
     *      <li>{@code "modid.msg.getCfg.header"}</li>
     *      <li>{@code "modid.msg.getCfg.option"} (option is the parameter of the function)</li>
     * </ul>
     *
     * <ul><h2>Custom translations :</h2>
     *      <li>{@code "dashSeparation"}</li>
     *      <li>{@link TranslationsPrefixes#GETCFG} + {@code "header"}</li>
     *      <li>{@link TranslationsPrefixes#GETCFG} + {@code "option"} (option is the parameter of the function)</li>
     * </ul>
     *
     * <ul><h2>Required config options :</h2>
     *      <li>{@code minOpLevelExeEditConfig}</li>
     * </ul>
     *
     * <ul><h2>Required config commands :</h2>
     *      <li>{@code /modid config <optionName> set [(boolean) value] [(boolean) mode]}</li>
     * </ul>
     *
     * @param context The command context
     * @param cyanLib The CyanLib instance
     */
    public static void getConfigOptions(@NotNull CommandContext<ServerCommandSource> context, @NotNull CyanLib cyanLib)
    {
        ServerPlayerEntity player = context.getSource().getPlayer();

        if (cyanLib.isPlayer(context.getSource()))
        {
            if (cyanLib.hasPermission(Objects.requireNonNull(player), cyanLib.getConfigUtils().getIntOption("minOpLevelExeEditConfig")))
            {
                sendPlayerMessageActionBar(player,
                        cyanLib.getLanguageUtils().getTranslation("dashSeparation"),
                        "%s.msg.dashSeparation".formatted(cyanLib.getMODID()),
                        false
                );
                sendPlayerMessageActionBar(player,
                        cyanLib.getLanguageUtils().getTranslation(GETCFG + "header"),
                        "%s.msg.getCfg.header".formatted(cyanLib.getMODID()),
                        false
                );

                for (String option : cyanLib.getConfigUtils().getOptions())
                {
                    if (cyanLib.getConfigUtils().isBoolean(option))
                    {
                        sendPlayerMessageActionBar(player,
                                cyanLib.getLanguageUtils().getTranslation(GETCFG + option),
                                "%s.msg.getCfg.%s".formatted(cyanLib.getMODID(), option),
                                false,
                                cyanLib.getConfigUtils().getBoolOption(option) ? Text.literal(Formatting.GREEN + "ON").
                                        setStyle(Style.EMPTY.withClickEvent(
                                                new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/%s config %s set false true".formatted(cyanLib.getMODID(), option)))
                                        ) : Text.literal(Formatting.RED + "OFF").
                                        setStyle(Style.EMPTY.withClickEvent(
                                                new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/%s config %s set true true".formatted(cyanLib.getMODID(), option)))
                                        )
                        );
                    }
                    else if (cyanLib.getConfigUtils().isInteger(option))
                    {
                        sendPlayerMessageActionBar(player,
                                cyanLib.getLanguageUtils().getTranslation(GETCFG + option),
                                "%s.msg.getCfg.%s".formatted(cyanLib.getMODID(), option),
                                false,
                                Formatting.GOLD + Integer.toString(cyanLib.getConfigUtils().getIntOption(option))
                        );
                    }
                }

                sendPlayerMessageActionBar(player,
                        cyanLib.getLanguageUtils().getTranslation("dashSeparation"),
                        "%s.msg.dashSeparation".formatted(cyanLib.getMODID()),
                        false
                );
            }
        }
    }
}
