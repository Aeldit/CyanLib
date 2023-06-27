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

import fr.aeldit.cyanlib.lib.CyanLib;
import fr.aeldit.cyanlib.lib.CyanLibConfig;
import fr.aeldit.cyanlib.lib.CyanLibLanguageUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class Utils
{
    public static final String MODID = "cyanlib";
    public static Logger LOGGER = LoggerFactory.getLogger(MODID);
    public static Map<String, String> defaultTranslations = new HashMap<>();

    public static CyanLibConfig LibConfig = new CyanLibConfig(MODID, getOptions());
    public static CyanLibLanguageUtils LanguageUtils = new CyanLibLanguageUtils(MODID);
    public static CyanLib LibUtils = new CyanLib(MODID, LibConfig, LanguageUtils);

    public static @NotNull Map<String, Object> getOptions()
    {
        Map<String, Object> options = new HashMap<>();

        options.put("useCustomTranslations", false);
        options.put("msgToActionBar", true);

        options.put("minOpLevelExeEditConfig", 4);

        return options;
    }

    public static @NotNull Map<String, String> getDefaultTranslations()
    {
        defaultTranslations.put("error.wrongType", "§cThis option can only be set to the %s §ctype");

        defaultTranslations.put("currentValue", "§7Current value : %s");
        defaultTranslations.put("setValue", "§7Set value to : %s  %s  %s  %s  %s");
        defaultTranslations.put("translationsReloaded", "§3Custom translations have been reloaded");

        defaultTranslations.put("set.useCustomTranslations", "§3Toogled custom translations %s");
        defaultTranslations.put("set.msgToActionBar", "§3Toogled messages to action bar %s");
        defaultTranslations.put("set.minOpLevelExeEditConfig", "§3The minimum OP level to edit the config is now %s");

        defaultTranslations.put("dashSeparation", "§6------------------------------------");
        defaultTranslations.put("headerDescCmd", "§6CyanLib - DESCRIPTION (commands)\n");
        defaultTranslations.put("headerDescOptions", "§6CyanLib - DESCRIPTION (options) :\n");

        defaultTranslations.put("desc.useCustomTranslations", "§3The §duseCustomTranslations §3option defines wether the custom translation will be used or not");
        defaultTranslations.put("desc.msgToActionBar", "§3The §dmsgToActionBar §3option defines wether the messages will be sent to the action bar or not");
        defaultTranslations.put("desc.minOpLevelExeEditConfig", "§3The §dminOpLevelExeEditConfig §3option defines the OP level required to edit the config");

        defaultTranslations.put("getCfg.header", "§6CyanLib - OPTIONS\n");
        defaultTranslations.put("getCfg.useCustomTranslations", "§6- §3Use custom translations : %s");
        defaultTranslations.put("getCfg.msgToActionBar", "§6- §3Messages to action bar : %s");
        defaultTranslations.put("getCfg.minOpLevelExeEditConfig", "§6- §3Minimum OP level required to edit the config : %s");

        return defaultTranslations;
    }
}
