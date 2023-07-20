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

package fr.aeldit.cyanlib.core;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import static fr.aeldit.cyanlib.core.utils.Utils.*;

public class CyanLibClientCore implements ClientModInitializer
{
    @Override
    public void onInitializeClient()
    {
        if (LibConfig.getBoolOption("useCustomTranslations"))
        {
            LanguageUtils.loadLanguage(getDefaultTranslations());
        }

        /*KeyBinding mainScreenKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "cyan.key.openScreen.locations",
                InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_C,
                "category.cyan"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (mainScreenKey.wasPressed())
            {
                CyanScreen.open();
            }
        });*/

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated, environment) -> LibConfigCommands.register(dispatcher));
        LOGGER.info("[CyanLib] Successfully initialized");
    }
}
