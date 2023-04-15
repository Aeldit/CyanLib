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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Properties;

public class LanguageUtils
{
    private final String MODID;
    public LinkedHashMap<String, String> translations = new LinkedHashMap<>();

    public LanguageUtils(String modid)
    {
        this.MODID = modid;
    }

    /**
     * Loads the custom translations into this class translations
     *
     * @param defaultTranslations The default translations if the file does not exist
     */
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
            this.translations = defaultTranslations;
        } else
        {
            FileInputStream fis = null;
            try
            {
                Properties properties = new Properties();
                fis = new FileInputStream(languagePath.toFile());
                properties.load(fis);
                for (String key : properties.stringPropertyNames())
                {
                    this.translations.put(key, properties.getProperty(key));
                }
            } catch (IOException e)
            {
                throw new RuntimeException(e);
            } finally
            {
                if (fis != null)
                {
                    try
                    {
                        fis.close();
                    } catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public String getTranslation(String key)
    {
        return translations.get(key) != null ? translations.get(key) : "null";
    }
}
