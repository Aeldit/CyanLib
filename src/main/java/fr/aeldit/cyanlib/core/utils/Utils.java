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

package fr.aeldit.cyanlib.core.utils;

import fr.aeldit.cyanlib.core.CoreConfig;
import fr.aeldit.cyanlib.lib.CyanLib;
import fr.aeldit.cyanlib.lib.CyanLibLanguageUtils;
import fr.aeldit.cyanlib.lib.commands.CyanLibConfigCommands;
import fr.aeldit.cyanlib.lib.config.CyanLibOptionsStorage;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class Utils
{
    public static final String MODID = "cyanlib";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);
    private static Map<String, String> defaultTranslations;

    public static CyanLibOptionsStorage LibOptionsStorage = new CyanLibOptionsStorage(MODID, CoreConfig.class);
    public static CyanLibLanguageUtils LanguageUtils = new CyanLibLanguageUtils(MODID, LibOptionsStorage);
    public static CyanLib LibUtils = new CyanLib(MODID, LibOptionsStorage, LanguageUtils);
    public static CyanLibConfigCommands LibConfigCommands = new CyanLibConfigCommands(MODID, LibUtils, getDefaultTranslations());

    public static @NotNull Map<String, String> getDefaultTranslations()
    {
        if (defaultTranslations == null)
        {
            defaultTranslations = new HashMap<>();

            defaultTranslations.put("error.optionNotFound", "§cThis option does not exist or you tried to set it to the wrong type");
            defaultTranslations.put("error.optionNotFoundOrWrongInt", "§cThis option does not exist or you tried to set it to an invalid number");
            defaultTranslations.put("error.incorrectInteger", "§cThe number you entered is invalid for this option");

            defaultTranslations.put("currentValue", "§7Current value : %s");
            defaultTranslations.put("setValue", "§7Set value to : %s  %s  %s  %s  %s");
            defaultTranslations.put("translationsReloaded", "§3Custom translations have been reloaded");

            defaultTranslations.put("set.useCustomTranslations", "§3Toggled custom translations %s");
            defaultTranslations.put("set.msgToActionBar", "§3Toggled messages to action bar %s");
            defaultTranslations.put("set.minOpLevelExeEditConfig", "§3The minimum OP level to edit the config is now %s");

            defaultTranslations.put("dashSeparation", "§6------------------------------------");
            defaultTranslations.put("headerDescCmd", "§6CyanLib - DESCRIPTION (commands)\n");
            defaultTranslations.put("headerDescOptions", "§6CyanLib - DESCRIPTION (options) :\n");

            defaultTranslations.put("desc.useCustomTranslations", "§3The §duseCustomTranslations §3option defines whether the custom translation will be used or not");
            defaultTranslations.put("desc.msgToActionBar", "§3The §dmsgToActionBar §3option defines whether the messages will be sent to the action bar or not");
            defaultTranslations.put("desc.minOpLevelExeEditConfig", "§3The §dminOpLevelExeEditConfig §3option defines the OP level required to edit the config");

            defaultTranslations.put("getCfg.header", "§6CyanLib - OPTIONS\n");
            defaultTranslations.put("getCfg.useCustomTranslations", "§6- §3Use custom translations : %s");
            defaultTranslations.put("getCfg.msgToActionBar", "§6- §3Messages to action bar : %s");
            defaultTranslations.put("getCfg.minOpLevelExeEditConfig", "§6- §3Minimum OP level required to edit the config : %s");
        }
        return defaultTranslations;
    }
}
