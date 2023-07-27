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

import fr.aeldit.cyanlib.core.config.CoreConfig;
import fr.aeldit.cyanlib.lib.CyanLib;
import fr.aeldit.cyanlib.lib.CyanLibLanguageUtils;
import fr.aeldit.cyanlib.lib.commands.CyanLibConfigCommands;
import fr.aeldit.cyanlib.lib.config.CyanLibOptionsStorage;
import fr.aeldit.cyanlib.lib.config.ModMenuApiImpl;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class Utils
{
    public static final String MODID = "cyanlib";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);
    private static Map<String, String> DEFAULT_TRANSLATIONS;

    public static CyanLibOptionsStorage OPTIONS_STORAGE = new CyanLibOptionsStorage(MODID, CoreConfig.class);
    public static ModMenuApiImpl MODMENU_IMPL = new ModMenuApiImpl(CoreConfig.class, OPTIONS_STORAGE);
    public static CyanLibLanguageUtils LANGUAGE_UTILS = new CyanLibLanguageUtils(MODID, OPTIONS_STORAGE, getDefaultTranslations());
    public static CyanLib LIB_UTILS = new CyanLib(MODID, OPTIONS_STORAGE, LANGUAGE_UTILS);
    public static CyanLibConfigCommands CONFIG_COMMANDS = new CyanLibConfigCommands(MODID, LIB_UTILS);

    public static @NotNull Map<String, String> getDefaultTranslations()
    {
        if (DEFAULT_TRANSLATIONS == null)
        {
            DEFAULT_TRANSLATIONS = new HashMap<>();

            DEFAULT_TRANSLATIONS.put("error.optionNotFound", "§cThis option does not exist or you tried to set it to the wrong type");
            DEFAULT_TRANSLATIONS.put("error.optionNotFoundOrWrongInt", "§cThis option does not exist or you tried to set it to an invalid number");
            DEFAULT_TRANSLATIONS.put("error.incorrectInteger", "§cThe number you entered is invalid for this option");

            DEFAULT_TRANSLATIONS.put("currentValue", "§7Current value : %s");
            DEFAULT_TRANSLATIONS.put("setValue", "§7Set value to : %s  %s  %s  %s  %s");
            DEFAULT_TRANSLATIONS.put("translationsReloaded", "§3Custom translations have been reloaded");

            DEFAULT_TRANSLATIONS.put("set.useCustomTranslations", "§3Toggled custom translations %s");
            DEFAULT_TRANSLATIONS.put("set.msgToActionBar", "§3Toggled messages to action bar %s");
            DEFAULT_TRANSLATIONS.put("set.minOpLevelExeEditConfig", "§3The minimum OP level to edit the config is now %s");

            DEFAULT_TRANSLATIONS.put("dashSeparation", "§6------------------------------------");
            DEFAULT_TRANSLATIONS.put("headerDescCmd", "§6CyanLib - DESCRIPTION (commands)\n");
            DEFAULT_TRANSLATIONS.put("headerDescOptions", "§6CyanLib - DESCRIPTION (options) :\n");

            DEFAULT_TRANSLATIONS.put("desc.useCustomTranslations", "§3The §duseCustomTranslations §3option defines whether the custom translation will be used or not");
            DEFAULT_TRANSLATIONS.put("desc.msgToActionBar", "§3The §dmsgToActionBar §3option defines whether the messages will be sent to the action bar or not");
            DEFAULT_TRANSLATIONS.put("desc.minOpLevelExeEditConfig", "§3The §dminOpLevelExeEditConfig §3option defines the OP level required to edit the config");

            DEFAULT_TRANSLATIONS.put("getCfg.header", "§6CyanLib - OPTIONS\n");
            DEFAULT_TRANSLATIONS.put("getCfg.useCustomTranslations", "§6- §3Use custom translations : %s");
            DEFAULT_TRANSLATIONS.put("getCfg.msgToActionBar", "§6- §3Messages to action bar : %s");
            DEFAULT_TRANSLATIONS.put("getCfg.minOpLevelExeEditConfig", "§6- §3Minimum OP level required to edit the config : %s");
        }
        return DEFAULT_TRANSLATIONS;
    }
}
