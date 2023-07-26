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

package fr.aeldit.cyanlib.lib;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import fr.aeldit.cyanlib.lib.config.CyanLibOptionsStorage;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class CyanLibLanguageUtils
{
    private final String MODID;
    private final CyanLibOptionsStorage optionsStorage;
    private Map<String, String> translations;

    public CyanLibLanguageUtils(String modid, CyanLibOptionsStorage optionsStorage)
    {
        this.MODID = modid;
        this.optionsStorage = optionsStorage;
    }

    /**
     * Loads the custom translations into this class translations
     *
     * @param defaultTranslations The default translations if the file does not exist
     */
    public void loadLanguage(Map<String, String> defaultTranslations)
    {
        Path languagePath = FabricLoader.getInstance().getConfigDir().resolve(MODID + "/translations.json");

        if (this.translations == null)
        {
            this.translations = new HashMap<>();
        }

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
     * Sets the translations at null, so it doesn't use memory
     */
    public void unload()
    {
        this.translations = null;
    }

    /**
     * @param key the key of the translation
     * @return The {@code String "null"} if the translations are {@code null} or if the translations don't contain the {@code key},
     *         and the value associated with the {@code key} otherwise
     */
    public String getTranslation(String key)
    {
        return translations == null ? "null" : (translations.get(key) == null ? "null" : translations.get(key));
    }

    /**
     * Sends a message to the player but with the possibility of using the translation (which will require
     * the player to have the mod or the resource pack with translations installed), or use the default without needing
     * the player to have them installed
     *
     * <ul><h2>Required config options :</h2>
     *      <li>{@code useCustomTranslations}</li>
     *      <li>{@code msgToActionBar}</li>
     * </ul>
     *
     * @param player   the player to whom the message will be sent
     * @param msg      the default translation
     * @param tradPath the translation path (requires the player to have the mod/resource pack)
     * @param args     the arguments to pass to the message (can be null). (You can put more than 1 arg)
     */
    public void sendPlayerMessage(@NotNull ServerPlayerEntity player, String msg, String tradPath, Object... args)
    {
        if (this.optionsStorage.getBooleanOption("useCustomTranslations"))
        {
            player.sendMessage(Text.translatable(msg, args), this.optionsStorage.getBooleanOption("msgToActionBar"));
        }
        else
        {
            player.sendMessage(Text.translatable(tradPath, args), this.optionsStorage.getBooleanOption("msgToActionBar"));
        }
    }

    /**
     * Sends a message to the player but with the possibility of using the translation (which will require
     * the player to have the mod or the resource pack with translations installed), or use the default without needing
     * the player to have them installed.
     * <p>
     * This method allows to force the message to be or not in the action bar, independently of this class attributes
     *
     * <ul><h2>Required config options :</h2>
     *      <li>{@code useCustomTranslations}</li>
     * </ul>
     *
     * @param player      the player to whom the message will be sent
     * @param msg         the default translation
     * @param tradPath    the translation path (requires the player to have the mod/resource pack)
     * @param toActionBar whether the message will be sent in the action bar or not
     * @param args        the arguments to pass to the message (can be null). (You can put more than 1 arg)
     */
    public void sendPlayerMessageActionBar(@NotNull ServerPlayerEntity player, String msg, String tradPath, boolean toActionBar, Object... args)
    {
        if (this.optionsStorage.getBooleanOption("useCustomTranslations"))
        {
            player.sendMessage(Text.translatable(msg, args), toActionBar);
        }
        else
        {
            player.sendMessage(Text.translatable(tradPath, args), toActionBar);
        }
    }
}
