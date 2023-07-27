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

package fr.aeldit.cyanlib.lib.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class ModMenuApiImpl implements ModMenuApi
{
    private final Class<?> configClass;
    private final CyanLibOptionsStorage optionsStorage;

    public ModMenuApiImpl(Class<?> configClass, CyanLibOptionsStorage optionsStorage)
    {
        this.configClass = configClass;
        this.optionsStorage = optionsStorage;
    }

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory()
    {
        return parent -> optionsStorage.new CyanLibConfigScreen(parent, configClass);
    }
}
