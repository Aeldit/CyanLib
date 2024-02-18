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

import fr.aeldit.cyanlib.core.config.CoreConfig;
import fr.aeldit.cyanlib.lib.gui.CyanLibModsScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import static fr.aeldit.cyanlib.core.CyanLibCore.*;

public class CyanLibClientCore implements ClientModInitializer
{
    @Override
    public void onInitializeClient()
    {
        LIB_UTILS.init(MODID, OPTS_STORAGE, CoreConfig.class);

        KeyBinding mainScreenKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "cyanlib.keybindings.openScreen.config",
                InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_I,
                "cyanlib.keybindings.category"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (mainScreenKey.wasPressed())
            {
                client.setScreen(new CyanLibModsScreen(null));
            }
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated, environment) -> CONFIG_COMMANDS.register(dispatcher));
        LOGGER.info("[CyanLib] Successfully initialized");
    }
}
