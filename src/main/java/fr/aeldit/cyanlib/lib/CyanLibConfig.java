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
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import fr.aeldit.cyanlib.lib.utils.RULES;
import fr.aeldit.cyanlib.lib.utils.TranslationsPrefixes;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static fr.aeldit.cyanlib.core.utils.Utils.LOGGER;
import static fr.aeldit.cyanlib.lib.utils.TranslationsPrefixes.ERROR;
import static fr.aeldit.cyanlib.lib.utils.TranslationsPrefixes.RULE;

public class CyanLibConfig
{
    private final Path path;
    private final ConcurrentHashMap<String, Object> options = new ConcurrentHashMap<>();
    private final Map<String, Object> rules;
    private boolean isEditing = false;

    /**
     * Reads the options file {@code modid.json} and sets the options if it exists, sets the options to their default values
     *
     * @param modid          The modid of your mod
     * @param defaultOptions The default options to use if the options file doesn't exist
     */
    public CyanLibConfig(String modid, Map<String, Object> defaultOptions)
    {
        this.path = FabricLoader.getInstance().getConfigDir().resolve(modid + ".json");
        read();

        if (this.options.isEmpty())
        {
            this.options.putAll(defaultOptions);
        }

        this.rules = null;
    }

    /**
     * Reads the options file {@code modid.json} and sets the options if it exists, sets the options to their default values
     *
     * <ul>Rules can be set in 2 different ways :
     *     <li>If no argument is needed -> {@code rules.put("optionName", RULE)}</li>
     *     <li>If an argument is needed -> {@code rules.put("optionName", Arrays.asList(RULE, argument))}</li>
     * </ul>
     * <p>
     * ({@code RULE} is an element of the {@code enum} {@link RULES} and {@code argument} is an {@code int})
     *
     * @param modid          The modid of your mod
     * @param defaultOptions The default options to use if the options file doesn't exist
     * @param rules          The rules that take effect on some options
     */
    public CyanLibConfig(String modid, Map<String, Object> defaultOptions, Map<String, Object> rules)
    {
        this.path = FabricLoader.getInstance().getConfigDir().resolve(modid + ".json");
        read();

        if (this.options.isEmpty())
        {
            this.options.putAll(defaultOptions);
        }

        this.rules = rules;
    }

    /**
     * Sets {@code option} to the value {@code value} (without checking its type)
     */
    public void setOption(String option, Object value)
    {
        if (this.options.containsKey(option))
        {
            this.options.put(option, value);
            write();
        }
    }

    public ArrayList<String> getOptions()
    {
        return new ArrayList<>(this.options.keySet());
    }

    /**
     * @return The value of the {@code option} if it exists, the string "null" otherwise
     */
    public Object getOption(String option)
    {
        return this.options.getOrDefault(option, "null");
    }

    public boolean optionExists(String option)
    {
        return this.options.containsKey(option);
    }

    /**
     * Can only be called if the result of {@code this.rules.containsKey(option)} is {@code true}
     */
    public RULES getRule(String option)
    {
        if (this.rules.get(option) instanceof List<?>)
        {
            return (RULES) ((List<?>) this.rules.get(option)).get(0);
        }
        return (RULES) this.rules.get(option);
    }

    /**
     * Accepted arguments for :
     * <ul>
     *     <li>{@link RULES#MAX_VALUE} : the maximum value ({@code int})</li>
     *     <li>{@link RULES#MIN_VALUE} : the minimum value ({@code int})</li>
     *     <li>{@link RULES#OP_LEVELS} : none</li>
     *     <li>{@link RULES#POSITIVE_VALUE} : none</li>
     *     <li>{@link RULES#NEGATIVE_VALUE} : none</li>
     *     <li>{@link RULES#LOAD_CUSTOM_TRANSLATIONS} : none</li>
     * </ul>
     * <p>
     * For more info,
     *
     * @see CyanLibConfig#CyanLibConfig(String, Map, Map)
     */
    public int getRuleArguments(String option)
    {
        if (this.rules.get(option) instanceof List<?>)
        {
            return (Integer) ((List<?>) this.rules.get(option)).get(1);
        }
        return 0;
    }

    /**
     * <ul><h2>Translations paths :</h2>
     *      <li>{@code "modid.msg.rule.maxValue"}</li>
     *      <li>{@code "modid.msg.rule.minValue"}</li>
     *      <li>{@code "modid.msg.rule.opLevels"}</li>
     *      <li>{@code "modid.msg.rule.positiveValue"}</li>
     *      <li>{@code "modid.msg.rule.negativeValue"}</li>
     * </ul>
     *
     * <ul><h2>Custom translations :</h2> Required only if the option useCustomTranslations is set to true
     *      <li>{@link TranslationsPrefixes#ERROR} +{@link TranslationsPrefixes#RULE} + {@code "maxValue"}</li>
     *      <li>{@link TranslationsPrefixes#ERROR} +{@link TranslationsPrefixes#RULE} + {@code "minValue"}</li>
     *      <li>{@link TranslationsPrefixes#ERROR} +{@link TranslationsPrefixes#RULE} + {@code "opLevels"}</li>
     *      <li>{@link TranslationsPrefixes#ERROR} +{@link TranslationsPrefixes#RULE} + {@code "positiveValue"}</li>
     *      <li>{@link TranslationsPrefixes#ERROR} +{@link TranslationsPrefixes#RULE} + {@code "negativeValue"}</li>
     * </ul>
     *
     * @return Whether the rule is respected for the option if the option has a rule, {@code true} otherwise
     */
    public boolean isIntegerRuleValid(String option, int value, CyanLib cyanLib, ServerPlayerEntity player)
    {
        if (this.rules.containsKey(option))
        {
            if (getRule(option).equals(RULES.MAX_VALUE) && (value > getRuleArguments(option)))
            {
                cyanLib.getLanguageUtils().sendPlayerMessage(player,
                        cyanLib.getLanguageUtils().getTranslation(ERROR + RULE + "maxValue"),
                        "%s.msg.rule.maxValue".formatted(cyanLib.getMODID()),
                        Formatting.YELLOW + String.valueOf(getRuleArguments(option))
                );
                return false;
            }
            else if (getRule(option).equals(RULES.MIN_VALUE) && (value < getRuleArguments(option)))
            {
                cyanLib.getLanguageUtils().sendPlayerMessage(player,
                        cyanLib.getLanguageUtils().getTranslation(ERROR + RULE + "minValue"),
                        "%s.msg.rule.minValue".formatted(cyanLib.getMODID()),
                        Formatting.YELLOW + String.valueOf(getRuleArguments(option))
                );
                return false;
            }
            else if (getRule(option).equals(RULES.OP_LEVELS) && (value < 0 || value > 4))
            {
                cyanLib.getLanguageUtils().sendPlayerMessage(player,
                        cyanLib.getLanguageUtils().getTranslation(ERROR + RULE + "opLevels"),
                        "%s.msg.rule.opLevels".formatted(cyanLib.getMODID())
                );
                return false;
            }
            else if (getRule(option).equals(RULES.POSITIVE_VALUE) && (value < 0))
            {
                cyanLib.getLanguageUtils().sendPlayerMessage(player,
                        cyanLib.getLanguageUtils().getTranslation(ERROR + RULE + "positiveValue"),
                        "%s.msg.rule.positiveValue".formatted(cyanLib.getMODID())
                );
                return false;
            }
            else if (getRule(option).equals(RULES.NEGATIVE_VALUE) && (value > 0))
            {
                cyanLib.getLanguageUtils().sendPlayerMessage(player,
                        cyanLib.getLanguageUtils().getTranslation(ERROR + RULE + "negativeValue"),
                        "%s.msg.rule.negativeValue".formatted(cyanLib.getMODID())
                );
                return false;
            }
        }
        return true;
    }

    /**
     * @return Whether the given {@code option} has the given {@code rule}
     */
    public boolean hasRule(String option, RULES rule)
    {
        if (this.rules.containsKey(option))
        {
            return this.rules.get(option).equals(rule);
        }
        return false;
    }

    /**
     * @return Whether the {@code option} is a {@code boolean} or not | {@code false} if the {@code option} does not exist
     */
    public boolean isBoolean(String option)
    {
        if (this.options.containsKey(option))
        {
            return this.options.get(option) instanceof Boolean;
        }
        return false;
    }

    /**
     * @return Whether the {@code option} is an {@code int} or not | {@code false} if the {@code option} does not exist
     */
    public boolean isInteger(String option)
    {
        if (this.options.containsKey(option))
        {
            return this.options.get(option) instanceof Integer;
        }
        return false;
    }

    /**
     * @return The {@code boolean} value associated with {@code option} if it is a {@code boolean} | {@code false} if the {@code option} does not exist
     */
    public boolean getBoolOption(String option)
    {
        if (this.options.containsKey(option))
        {
            if (this.options.get(option) instanceof Boolean)
            {
                return (Boolean) this.options.get(option);
            }
        }
        return false;
    }

    /**
     * @return The {@code int} value associated with {@code option} if it is an {@code int} | {@code 0} if the {@code option} does not exist
     */
    public int getIntOption(String option)
    {
        if (this.options.containsKey(option))
        {
            if (this.options.get(option) instanceof Integer)
            {
                return (Integer) this.options.get(option);
            }
        }
        return 0;
    }

    private void read()
    {
        if (Files.exists(path))
        {
            try
            {
                Gson gsonReader = new Gson();
                Reader reader = Files.newBufferedReader(this.path);
                TypeToken<Map<String, Object>> mapType = new TypeToken<>() {};
                this.options.putAll(gsonReader.fromJson(reader, mapType));
                reader.close();
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }

        // Casts the 'double' values to 'int' or 'float'
        if (!this.options.isEmpty())
        {
            for (String key : this.options.keySet())
            {
                Object value = this.options.get(key);

                if (value instanceof Double)
                {
                    if (((Double) value).intValue() == (Double) value)
                    {
                        this.options.put(key, ((Double) value).intValue());
                    }
                    else if (((Double) value).floatValue() == (Double) value)
                    {
                        this.options.put(key, ((Double) value).floatValue());
                    }
                }
            }
        }
    }

    private void write()
    {
        if (!this.isEditing)
        {
            this.isEditing = true;

            try
            {
                Gson gsonWriter = new GsonBuilder().setPrettyPrinting().create();
                Writer writer = Files.newBufferedWriter(this.path);
                gsonWriter.toJson(this.options, writer);
                writer.close();
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }

            this.isEditing = false;
        }
        else
        {
            long end = System.currentTimeMillis() + 1000; // 1 s
            boolean couldWrite = false;

            while (System.currentTimeMillis() < end)
            {
                if (!this.isEditing)
                {
                    this.isEditing = true;

                    try
                    {
                        Gson gsonWriter = new GsonBuilder().setPrettyPrinting().create();
                        Writer writer = Files.newBufferedWriter(this.path);
                        gsonWriter.toJson(this.options, writer);
                        writer.close();
                    }
                    catch (IOException e)
                    {
                        throw new RuntimeException(e);
                    }

                    couldWrite = true;
                    this.isEditing = false;
                    break;
                }
            }

            if (!couldWrite)
            {
                LOGGER.info("[CyanLib] Could not write the file %s because it is already being written (for more than 1 sec)".formatted(path.getFileName().toString()));
            }
        }
    }
}
