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

import fr.aeldit.cyanlib.lib.config.CyanLibOptionsStorage;
import fr.aeldit.cyanlib.lib.utils.RULES;
import fr.aeldit.cyanlib.lib.utils.TranslationsPrefixes;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static fr.aeldit.cyanlib.lib.utils.TranslationsPrefixes.ERROR;

public class CyanLib
{
    private final String modid;
    private final CyanLibOptionsStorage optionsStorage;
    private final CyanLibLanguageUtils languageUtils;
    // This Map stores the Config class and OptionsStorage instance of each mod, the key in the map being the modid of the mod
    public static final Map<String, List<Object>> CONFIG_CLASS_INSTANCES = new HashMap<>();

    /**
     * Main class of this library
     *
     * @param modid          The modid of your mod
     * @param optionsStorage The instance of {@link CyanLibOptionsStorage}
     * @param languageUtils  The instance of {@link CyanLibLanguageUtils}
     */
    public CyanLib(String modid, @NotNull CyanLibOptionsStorage optionsStorage, CyanLibLanguageUtils languageUtils)
    {
        this.modid = modid;
        this.optionsStorage = optionsStorage;
        this.languageUtils = languageUtils;
    }

    /**
     * Main class of this library but without the language utils
     */
    public CyanLib(String modid, CyanLibOptionsStorage optionsStorage)
    {
        this.modid = modid;
        this.optionsStorage = optionsStorage;
        this.languageUtils = new CyanLibLanguageUtils(modid, optionsStorage, new HashMap<>());
    }

    public void init(String modid, @NotNull CyanLibOptionsStorage optionsStorageInstance, Class<?> configClass)
    {
        CONFIG_CLASS_INSTANCES.put(modid, Arrays.asList(optionsStorageInstance, configClass));
        optionsStorageInstance.init();
        ArrayList<String> option = optionsStorageInstance.getOptionsWithRule(RULES.LOAD_CUSTOM_TRANSLATIONS);

        if (!option.isEmpty())
        {
            if (optionsStorageInstance.getBooleanOption(option.get(0)))
            {
                languageUtils.loadLanguage();
            }
            else
            {
                languageUtils.unload();
            }
        }
    }

    public CyanLibOptionsStorage getOptionsStorage()
    {
        return optionsStorage;
    }

    /**
     * Returns whether the source is a player or not, and sends a message if this condition is {@code false}
     *
     * <ul><h2>Custom translations</h2> Required only if the option useCustomTranslations is set to true
     *      <li>{@link TranslationsPrefixes#ERROR} + {@code "playerOnlyCmd"}</li>
     * </ul>
     *
     * @param source the source of the command (usually {@code context.getSource()})
     */
    public boolean isPlayer(@NotNull ServerCommandSource source)
    {
        if (source.getPlayer() == null)
        {
            if (optionsStorage.getBooleanOption("useCustomTranslations"))
            {
                source.getServer().sendMessage(Text.of(languageUtils.getTranslation(ERROR + "playerOnlyCmd")));
            }
            else
            {
                source.getServer().sendMessage(Text.of("§cThis command can only be executed by a player"));
            }
            return false;
        }
        return true;
    }

    public CyanLibLanguageUtils getLanguageUtils()
    {
        return languageUtils;
    }

    /**
     * Returns whether the player has the required OP level or not, and sends a message if this condition is
     * {@code false}
     *
     * <ul><h2>Translations paths :</h2>
     *      <li>{@code "modid.msg.notOp"}</li>
     * </ul>
     *
     * <ul><h2>Custom translations :</h2> Required only if the option useCustomTranslations is set to true
     *      <li>{@link TranslationsPrefixes#GETCFG} + {@code "notOp"}</li>
     * </ul>
     *
     * @param player     the player
     * @param permission the OP level ({@code 0 <= permission <= 4})
     */
    public boolean hasPermission(@NotNull ServerPlayerEntity player, int permission)
    {
        if (!player.hasPermissionLevel(permission))
        {
            languageUtils.sendPlayerMessage(player,
                    languageUtils.getTranslation(ERROR + "notOp"),
                    "%s.msg.notOp".formatted(modid)
            );
            return false;
        }
        return true;
    }

    /**
     * Returns whether the player the option is allowed or not, and sends a message if this condition is {@code false}
     *
     * @param player  the player
     * @param option  the option you want to test
     * @param msgPath the path to the translation (in the method, the translations path is {@code "MODID.message.OPTION"},
     *                where {@code MODID} is the modid of your mod and {@code OPTION} is the {@code msgPath})
     */
    public boolean isOptionAllowed(@NotNull ServerPlayerEntity player, boolean option, String msgPath)
    {
        if (!option)
        {
            languageUtils.sendPlayerMessage(player,
                    languageUtils.getTranslation(ERROR + msgPath),
                    "%s.msg.%s".formatted(modid, msgPath)
            );
            return false;
        }
        return true;
    }
}
