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

import net.fabricmc.loader.api.FabricLoader;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Properties;

public class LanguageUtils
{
    String MODID;
    LinkedHashMap<String, String> translations = new LinkedHashMap<>();

    public LanguageUtils(String modid)
    {
        this.MODID = modid;
    }

    public void loadLanguage(LinkedHashMap<String, String> defaultTranslations)
    {
        if (!Files.exists(FabricLoader.getInstance().getConfigDir().resolve(this.MODID)))
        {
            try
            {
                Files.createDirectory(FabricLoader.getInstance().getConfigDir().resolve(this.MODID));
            } catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
        Path languagePath = FabricLoader.getInstance().getConfigDir().resolve(this.MODID + "/translations.properties");
        if (!Files.exists(languagePath))
        {
            try
            {
                Files.createFile(languagePath);
                Properties properties = new Properties();
                properties.putAll(defaultTranslations);
                properties.store(new FileOutputStream(languagePath.toFile()), null);
            } catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
        try
        {
            Properties properties = new Properties();
            properties.load(new FileInputStream(languagePath.toFile()));
            for (String key : properties.stringPropertyNames())
            {
                translations.put(key, properties.getProperty(key));
            }
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public String getTranslation(String key)
    {
        return translations.get(key) != null ? translations.get(key) : "null";
    }
}
