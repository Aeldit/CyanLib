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
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class ChatUtils
{
    /**
     * Sends a message to the player but with the possibility of using the traductions (which will require
     * the player to have the mod or the resource pack with translations installed), or use the default without needing
     * the player to have them installed
     *
     * @param player                the player to whom the message will be sent
     * @param msg                   the default translation
     * @param tradPath              the traduction path (requires the player to have the mod/resource pack)
     * @param actionBar             if the message will be sent to the action bar
     * @param useCustomTranslations if the custom translations will be used (if false, tradPath is used | if true, msg is used)
     * @param args                  the arguments to pass to the message (can be null). (You can put more than 1 arg)
     */
    public static void sendPlayerMessage(@NotNull ServerPlayerEntity player, String msg, String tradPath, boolean actionBar, boolean useCustomTranslations, Object... args)
    {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER)
        {
            if (useCustomTranslations)
            {
                player.sendMessage(Text.translatable(msg, args), actionBar);
            } else
            {
                player.sendMessage(Text.translatable(tradPath, args), actionBar);
            }
        } else
        {
            player.sendMessage(Text.translatable(tradPath, args), actionBar);
        }
    }
}
