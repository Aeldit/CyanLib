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

package fr.aeldit.cyanlib.core.config;

import fr.aeldit.cyanlib.lib.config.BooleanOption;
import fr.aeldit.cyanlib.lib.config.IntegerOption;
import fr.aeldit.cyanlib.lib.utils.RULES;

public class CoreConfig
{
    public static final BooleanOption USE_CUSTOM_TRANSLATIONS = new BooleanOption("useCustomTranslations", false, RULES.LOAD_CUSTOM_TRANSLATIONS);
    public static final BooleanOption MSG_TO_ACTION_BAR = new BooleanOption("msgToActionBar", true);
    public static final IntegerOption MIN_OP_LVL_EDIT_CONFIG = new IntegerOption("minOpLvlEditConfig", 4, RULES.OP_LEVELS);
}
