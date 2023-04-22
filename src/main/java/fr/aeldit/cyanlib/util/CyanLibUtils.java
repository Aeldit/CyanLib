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

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import static fr.aeldit.cyanlib.util.ChatUtils.sendPlayerMessage;
import static fr.aeldit.cyanlib.util.Constants.ERROR;

public class CyanLibUtils
{
    private final String MODID;
    private final LanguageUtils languageUtils;
    private boolean errorToActionBar;
    private boolean useCustomTranslations;

    public CyanLibUtils(String modid, LanguageUtils languageUtils, boolean errorToActionBar, boolean useCustomTranslations)
    {
        this.MODID = modid;
        this.languageUtils = languageUtils;
        this.errorToActionBar = errorToActionBar;
        this.useCustomTranslations = useCustomTranslations;
    }

    public void setErrorToActionBar(boolean value)
    {
        this.errorToActionBar = value;
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
                    "%s.message.notOp".formatted(this.MODID),
                    this.errorToActionBar,
                    this.useCustomTranslations
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
                    "%s.message.%s".formatted(this.MODID, msgPath),
                    this.errorToActionBar,
                    this.useCustomTranslations
            );
            return false;
        }
        return true;
    }
}
