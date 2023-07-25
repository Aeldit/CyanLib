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

import fr.aeldit.cyanlib.lib.config.OptionsStorage;

import static fr.aeldit.cyanlib.core.utils.Utils.LibOptionsStorage;

public class CoreConfig
{
    public static final OptionsStorage.BooleanOption USE_CUSTOM_TRANSLATIONS = LibOptionsStorage.new BooleanOption("useCustomTranslations", false);
    public static final OptionsStorage.BooleanOption MSG_TO_ACTION_BAR = LibOptionsStorage.new BooleanOption("msgToActionBar", true);
    public static final OptionsStorage.IntegerOption MIN_OP_LVL_EDIT_CONFIG = LibOptionsStorage.new IntegerOption("minOpLvlEditConfig", 4);
}
