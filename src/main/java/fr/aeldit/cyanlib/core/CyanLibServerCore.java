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

import fr.aeldit.cyanlib.lib.commands.CyanLibConfigCommands;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import static fr.aeldit.cyanlib.core.CyanLibCore.*;

public class CyanLibServerCore implements DedicatedServerModInitializer
{
    @Override
    public void onInitializeServer()
    {
        LIB_UTILS.init(MODID, OPTS_STORAGE);

        CommandRegistrationCallback.EVENT.register(
                (dispatcher, dedicated, environment) -> new CyanLibConfigCommands(MODID, LIB_UTILS).register(dispatcher)
        );
        LOGGER.info("[CyanLib] Successfully initialized");
    }
}
