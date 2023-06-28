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

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import static fr.aeldit.cyanlib.lib.CyanLibLanguageUtils.sendPlayerMessage;
import static fr.aeldit.cyanlib.lib.TranslationsPrefixes.ERROR;

public class CyanLib
{
    private final String MODID;
    private final CyanLibConfig configUtils;
    private final CyanLibLanguageUtils languageUtils;

    /**
     * Main class of this library
     *
     * @param modid         The modid of your mod
     * @param configUtils   The instance of {@link CyanLibConfig}
     * @param languageUtils The instance of {@link CyanLibLanguageUtils}
     */
    public CyanLib(String modid, @NotNull CyanLibConfig configUtils, CyanLibLanguageUtils languageUtils)
    {
        this.MODID = modid;
        this.configUtils = configUtils;

        if (this.configUtils.optionExists("useCustomTranslations"))
        {
            this.languageUtils = languageUtils;

            if (!this.configUtils.getBoolOption("useCustomTranslations"))
            {
                this.languageUtils.unload();
            }
        }
        else
        {
            this.languageUtils = null;
        }
    }

    public String getMODID()
    {
        return this.MODID;
    }

    public CyanLibConfig getConfigUtils()
    {
        return this.configUtils;
    }

    public CyanLibLanguageUtils getLanguageUtils()
    {
        return this.languageUtils;
    }

    /**
     * Returns whether the source is a player or not, and sends a message if this condition is {@code false}
     *
     * <ul><h2>Custom translations</h2> Required only if the option useCustomTranslations is set to true
     *      <li>{@link TranslationsPrefixes#ERROR} + {@code "playerOnlyCmd"}</li>
     * </ul>
     *
     * @param source the source of the command (usually {@code context.getSource()})
     */
    public boolean isPlayer(@NotNull ServerCommandSource source)
    {
        if (source.getPlayer() == null)
        {
            if (this.configUtils.getBoolOption("useCustomTranslations"))
            {
                source.getServer().sendMessage(Text.of(this.languageUtils.getTranslation(ERROR + "playerOnlyCmd")));
            }
            else
            {
                source.getServer().sendMessage(Text.of("§cThis command can only be executed by a player"));
            }
            return false;
        }
        return true;
    }

    /**
     * Returns whether the player has the required OP level or not, and sends a message if this condition is
     * {@code false}
     *
     * <ul><h2>Translations paths :</h2>
     *      <li>{@code "modid.msg.notOp"}</li>
     * </ul>
     *
     * <ul><h2>Custom translations :</h2> Required only if the option useCustomTranslations is set to true
     *      <li>{@link TranslationsPrefixes#GETCFG} + {@code "notOp"}</li>
     * </ul>
     *
     * @param player     the player
     * @param permission the OP level ({@code 0 <= permission <= 4})
     */
    public boolean hasPermission(@NotNull ServerPlayerEntity player, int permission)
    {
        if (!player.hasPermissionLevel(permission))
        {
            sendPlayerMessage(player,
                    this.languageUtils.getTranslation(ERROR + "notOp"),
                    "%s.msg.notOp".formatted(this.MODID)
            );
            return false;
        }
        return true;
    }

    /**
     * Returns whether the player the option is allowed or not, and sends a message if this condition is {@code false}
     *
     * @param player  the player
     * @param option  the option you want to test
     * @param msgPath the path to the translation (in the method, the translations path is {@code "MODID.message.OPTION"},
     *                where {@code MODID} is the modid of your mod and {@code OPTION} is the {@code msgPath})
     */
    public boolean isOptionAllowed(@NotNull ServerPlayerEntity player, boolean option, String msgPath)
    {
        if (!option)
        {
            sendPlayerMessage(player,
                    this.languageUtils.getTranslation(ERROR + msgPath),
                    "%s.msg.%s".formatted(this.MODID, msgPath)
            );
            return false;
        }
        return true;
    }
}
