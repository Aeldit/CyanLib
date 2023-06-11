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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class LanguageUtils
{
    private final String MODID;
    private Map<String, String> translations;

    public LanguageUtils(String modid)
    {
        this.MODID = modid;
    }

    /**
     * Loads the custom translations into this class translations
     *
     * @param defaultTranslations The default translations if the file does not exist
     */
    public void loadLanguage(Map<String, String> defaultTranslations)
    {
        Path languagePath = FabricLoader.getInstance().getConfigDir().resolve(this.MODID + "/translations.json");

        if (!Files.exists(languagePath))
        {
            this.translations = defaultTranslations;
        }
        else
        {
            try
            {
                Gson gsonReader = new Gson();
                Reader reader = Files.newBufferedReader(languagePath);
                TypeToken<Map<String, String>> mapType = new TypeToken<>() {};
                this.translations = gsonReader.fromJson(reader, mapType);
                reader.close();
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Returns the value associated with the key {@code key} if it exists, the String "null" otherwise
     *
     * @param key the key of the translation
     */
    public String getTranslation(String key)
    {
        return translations.get(key) != null ? translations.get(key) : "null";
    }

    public void setTranslations(Map<String, String> translations)
    {
        this.translations = translations;
    }
}
