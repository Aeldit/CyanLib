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

package fr.aeldit.cyanlib.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import static fr.aeldit.cyanlib.util.Constants.ERROR;

public class CyanLibUtils
{
    private final String MODID;
    private final LanguageUtils languageUtils;
    private boolean msgToActionBar;
    private boolean useCustomTranslations;

    public CyanLibUtils(String modid, LanguageUtils languageUtils, boolean msgToActionBar, boolean useCustomTranslations)
    {
        this.MODID = modid;
        this.languageUtils = languageUtils;
        this.msgToActionBar = msgToActionBar;
        this.useCustomTranslations = useCustomTranslations;
    }

    public void setMsgToActionBar(boolean value)
    {
        this.msgToActionBar = value;
    }

    public void setUseCustomTranslations(boolean value)
    {
        this.useCustomTranslations = value;
    }

    // Redundant checks
    public boolean isPlayer(@NotNull ServerCommandSource source)
    {
        if (source.getPlayer() == null)
        {
            source.getServer().sendMessage(Text.of(this.languageUtils.getTranslation(ERROR + "playerOnlyCmd")));
            return false;
        }
        return true;
    }

    public boolean hasPermission(@NotNull ServerPlayerEntity player, int permission)
    {
        if (!player.hasPermissionLevel(permission))
        {
            sendPlayerMessage(player,
                    this.languageUtils.getTranslation(ERROR + "notOp"),
                    "%s.message.notOp".formatted(this.MODID)
            );
            return false;
        }
        return true;
    }

    public boolean isOptionAllowed(@NotNull ServerPlayerEntity player, boolean option, String msgPath)
    {
        if (!option)
        {
            sendPlayerMessage(player,
                    this.languageUtils.getTranslation(ERROR + msgPath),
                    "%s.message.%s".formatted(this.MODID, msgPath)
            );
            return false;
        }
        return true;
    }

    /**
     * Sends a message to the player but with the possibility of using the traductions (which will require
     * the player to have the mod or the resource pack with translations installed), or use the default without needing
     * the player to have them installed
     *
     * @param player   the player to whom the message will be sent
     * @param msg      the default translation
     * @param tradPath the traduction path (requires the player to have the mod/resource pack)
     * @param args     the arguments to pass to the message (can be null). (You can put more than 1 arg)
     */
    public void sendPlayerMessage(@NotNull ServerPlayerEntity player, String msg, String tradPath, Object... args)
    {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER)
        {
            if (this.useCustomTranslations)
            {
                player.sendMessage(Text.translatable(msg, args), this.msgToActionBar);
            }
            else
            {
                player.sendMessage(Text.translatable(tradPath, args), this.msgToActionBar);
            }
        }
        else
        {
            player.sendMessage(Text.translatable(tradPath, args), this.msgToActionBar);
        }
    }

    /**
     * Sends a message to the player but with the possibility of using the traductions (which will require
     * the player to have the mod or the resource pack with translations installed), or use the default without needing
     * the player to have them installed.
     * <p>
     * This method allows to force the message to be or not in the action bar
     *
     * @param player      the player to whom the message will be sent
     * @param msg         the default translation
     * @param tradPath    the traduction path (requires the player to have the mod/resource pack)
     * @param toActionBar whether the message will be sent in the action bar or not
     * @param args        the arguments to pass to the message (can be null). (You can put more than 1 arg)
     */
    public void sendPlayerMessageActionBar(@NotNull ServerPlayerEntity player, String msg, String tradPath, boolean toActionBar, Object... args)
    {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER)
        {
            if (this.useCustomTranslations)
            {
                player.sendMessage(Text.translatable(msg, args), toActionBar);
            }
            else
            {
                player.sendMessage(Text.translatable(tradPath, args), toActionBar);
            }
        }
        else
        {
            player.sendMessage(Text.translatable(tradPath, args), toActionBar);
        }
    }
}
