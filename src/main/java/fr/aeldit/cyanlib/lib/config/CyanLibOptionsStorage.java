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

package fr.aeldit.cyanlib.lib.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import fr.aeldit.cyanlib.lib.utils.RULES;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.command.CommandSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static fr.aeldit.cyanlib.core.CyanLibCore.LOGGER;

public class CyanLibOptionsStorage
{
    private final String modid;
    private final Class<?> configClass;
    // Used for the auto-completion for commands
    private final ArrayList<String> optionsNames = new ArrayList<>();
    private final Map<String, Object> unsavedChangedOptions = new HashMap<>();
    private boolean isEditingFile = false;

    // We use synchronized lists because 2 players can edit the config at the same time
    private final List<BooleanOption> booleanOptionList = Collections.synchronizedList(new ArrayList<>());
    private final List<IntegerOption> integerOptionList = Collections.synchronizedList(new ArrayList<>());

    private final List<Option<?>> optionsList = Collections.synchronizedList(new ArrayList<>());

    public CyanLibOptionsStorage(String modid, Class<?> configClass)
    {
        this.modid = modid;
        this.configClass = configClass;
    }

    public void init()
    {
        readConfig();
    }

    public String getModid()
    {
        return modid;
    }

    public ArrayList<String> getOptionsNamesOld()
    {
        if (optionsNames.isEmpty())
        {
            booleanOptionList.forEach(option -> optionsNames.add(option.getOptionName()));
            integerOptionList.forEach(option -> optionsNames.add(option.getOptionName()));
        }
        return optionsNames;
    }

    public ArrayList<String> getOptionsNames()
    {
        if (optionsNames.isEmpty())
        {
            optionsList.forEach(option -> optionsNames.add(option.getOptionName()));
        }
        return optionsNames;
    }

    public Map<String, Object> getUnsavedChangedOptions()
    {
        return unsavedChangedOptions;
    }

    public void clearUnsavedChangedOptions()
    {
        unsavedChangedOptions.clear();
    }

    @Environment(EnvType.CLIENT)
    public static SimpleOption<?> @NotNull [] asConfigOptions(@NotNull Class<?> configClass)
    {
        ArrayList<SimpleOption<?>> options = new ArrayList<>();

        for (Field field : configClass.getDeclaredFields())
        {
            try
            {
                //options.add(((OptionConverter) field.get(null)).asConfigOption());
                options.add(((Option<?>) field.get(null)).asConfigOption());
            }
            catch (IllegalAccessException e)
            {
                throw new RuntimeException(e);
            }
        }
        return options.toArray(SimpleOption[]::new);
    }

    public boolean getBooleanOptionValue(String optionName)
    {
        for (BooleanOption option : booleanOptionList)
        {
            if (option.getOptionName().equals(optionName))
            {
                return option.getValue();
            }
        }
        return false;
    }

    public int getIntegerOptionValue(String optionName)
    {
        for (IntegerOption option : integerOptionList)
        {
            if (option.getOptionName().equals(optionName))
            {
                return option.getValue();
            }
        }
        return 0;
    }

    @Nullable
    public Object getOptionValue(String optionName, Class<?> expectedType)
    {
        for (Option<?> option : optionsList)
        {
            if (option.getOptionName().equals(optionName))
            {
                // If the given option is of the expected type
                if (option.getValue().getClass().equals(expectedType))
                {
                    return option.getValue();
                }
                break;
            }
        }
        return null;
    }

    @Nullable
    public Object getOptionValue(String optionName)
    {
        for (Option<?> option : optionsList)
        {
            if (option.getOptionName().equals(optionName))
            {
                return option.getValue();
            }
        }
        return null;
    }

    public boolean isBoolean(String optionName)
    {
        for (Option<?> option : optionsList)
        {
            if (option.getOptionName().equals(optionName))
            {
                return option instanceof BooleanOption;
            }
        }
        return false;
    }

    public boolean isInteger(String optionName)
    {
        for (Option<?> option : optionsList)
        {
            if (option.getOptionName().equals(optionName))
            {
                return option instanceof IntegerOption;
            }
        }
        return false;
    }

    public void setBooleanOption(String optionName, boolean value, boolean save)
    {
        for (BooleanOption option : booleanOptionList)
        {
            if (option.getOptionName().equals(optionName))
            {
                option.setValue(value);
                break;
            }
        }

        if (save)
        {
            writeConfig();
        }
    }

    public boolean setIntegerOption(String optionName, int value, boolean save)
    {
        for (IntegerOption option : integerOptionList)
        {
            if (option.getOptionName().equals(optionName))
            {
                return option.setValue(value);
            }
        }

        if (save)
        {
            writeConfig();
        }
        return false;
    }

    public boolean setOption(String optionName, Object value, boolean save)
    {
        for (Option<?> option : optionsList)
        {
            if (option.getOptionName().equals(optionName))
            {
                return option.setValue(value);
            }
        }

        if (save)
        {
            writeConfig();
        }
        return false;
    }

    public void resetOptions()
    {
        booleanOptionList.forEach(BooleanOption::reset);
        integerOptionList.forEach(IntegerOption::reset);
    }

    public boolean booleanOptionExists(String optionName)
    {
        for (BooleanOption option : booleanOptionList)
        {
            if (option.getOptionName().equals(optionName))
            {
                return true;
            }
        }
        return false;
    }

    public boolean integerOptionExists(String optionName)
    {
        for (IntegerOption option : integerOptionList)
        {
            if (option.getOptionName().equals(optionName))
            {
                return true;
            }
        }
        return false;
    }

    public boolean optionExists(String optionName)
    {
        for (Option<?> option : optionsList)
        {
            if (option.getOptionName().equals(optionName))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Called for the command {@code /modid config <optionName>}
     *
     * @return a suggestion with the available options
     */
    public static CompletableFuture<Suggestions> getOptionsSuggestions(@NotNull SuggestionsBuilder builder, @NotNull CyanLibOptionsStorage optionsStorage)
    {
        return CommandSource.suggestMatching(optionsStorage.getOptionsNames(), builder);
    }

    public boolean hasRule(String optionName, RULES rule)
    {
        for (BooleanOption option : booleanOptionList)
        {
            if (option.getOptionName().equals(optionName))
            {
                return option.getRule() == rule;
            }
        }

        for (IntegerOption option : integerOptionList)
        {
            if (option.getOptionName().equals(optionName))
            {
                return option.getRule() == rule;
            }
        }
        return false;
    }

    public ArrayList<String> getOptionsWithRuleOld(RULES rule)
    {
        ArrayList<String> validOptions = new ArrayList<>();

        for (BooleanOption option : booleanOptionList)
        {
            if (option.getRule() == rule)
            {
                validOptions.add(option.getOptionName());
            }
        }

        for (IntegerOption option : integerOptionList)
        {
            if (option.getRule() == rule)
            {
                validOptions.add(option.getOptionName());
            }
        }
        return validOptions;
    }

    public ArrayList<String> getOptionsWithRule(RULES rule)
    {
        ArrayList<String> validOptions = new ArrayList<>();

        for (Option<?> option : optionsList)
        {
            if (option.getRule() == rule)
            {
                validOptions.add(option.getOptionName());
            }
        }
        return validOptions;
    }

    private void readConfigOld()
    {
        Path path = FabricLoader.getInstance().getConfigDir().resolve(modid + ".json");

        if (!Files.exists(path))
        {
            for (Field field : configClass.getDeclaredFields())
            {
                if (Modifier.isPublic(field.getModifiers()) && Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers()))
                {
                    if (BooleanOption.class.isAssignableFrom(field.getType()))
                    {
                        try
                        {
                            BooleanOption booleanOption = (BooleanOption) field.get(null);
                            booleanOptionList.add(booleanOption);
                        }
                        catch (IllegalAccessException e)
                        {
                            throw new RuntimeException(e);
                        }
                    }
                    else if (IntegerOption.class.isAssignableFrom(field.getType()))
                    {
                        try
                        {
                            IntegerOption integerOption = (IntegerOption) field.get(null);
                            integerOptionList.add(integerOption);
                        }
                        catch (IllegalAccessException e)
                        {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
        else
        {
            try
            {
                Gson gson = new Gson();
                Reader reader = Files.newBufferedReader(path);
                TypeToken<Map<String, Object>> mapType = new TypeToken<>()
                {
                };
                Map<String, Object> config = new HashMap<>(gson.fromJson(reader, mapType));
                reader.close();

                for (Map.Entry<String, Object> entry : config.entrySet())
                {
                    if (entry.getValue() instanceof Double)
                    {
                        // Integer values are stored as double in the gson file, so by doing this we can put it back to an int
                        if (((Double) entry.getValue()).intValue() == (Double) entry.getValue())
                        {
                            config.put(entry.getKey(), ((Double) entry.getValue()).intValue());
                        }
                    }
                }

                for (Field field : configClass.getDeclaredFields())
                {
                    if (Modifier.isPublic(field.getModifiers()) && Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers()))
                    {
                        if (BooleanOption.class.isAssignableFrom(field.getType()))
                        {
                            try
                            {
                                BooleanOption booleanOption = (BooleanOption) field.get(null);

                                if (config.containsKey(booleanOption.getOptionName()))
                                {
                                    boolean configFilevalue = (Boolean) config.get(booleanOption.getOptionName());
                                    // If the value in the config file is different from the default one, we change its value in the class
                                    if (configFilevalue != booleanOption.getValue())
                                    {
                                        setOption(booleanOption.getOptionName(), configFilevalue, false);
                                    }
                                }
                            }
                            catch (IllegalAccessException e)
                            {
                                throw new RuntimeException(e);
                            }
                        }
                        else if (IntegerOption.class.isAssignableFrom(field.getType()))
                        {
                            try
                            {
                                IntegerOption integerOption = (IntegerOption) field.get(null);

                                if (config.containsKey(integerOption.getOptionName()))
                                {
                                    int configFileValue = (Integer) config.get(integerOption.getOptionName());
                                    // If the value in the config file is different from the default one, we change its value in the class
                                    if (configFileValue != integerOption.getValue())
                                    {
                                        setOption(integerOption.getOptionName(), configFileValue, false);
                                    }
                                }
                            }
                            catch (IllegalAccessException e)
                            {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    private void readConfig()
    {
        Path path = FabricLoader.getInstance().getConfigDir().resolve(modid + ".json");

        // If the file does not exist, we simply load the class in memory
        if (!Files.exists(path))
        {
            for (Field field : configClass.getDeclaredFields())
            {
                if (Modifier.isPublic(field.getModifiers()) && Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers()))
                {
                    if (BooleanOption.class.isAssignableFrom(field.getType()))
                    {
                        try
                        {
                            BooleanOption booleanOption = (BooleanOption) field.get(null);
                            optionsList.add(booleanOption);
                        }
                        catch (IllegalAccessException e)
                        {
                            throw new RuntimeException(e);
                        }
                    }
                    else if (IntegerOption.class.isAssignableFrom(field.getType()))
                    {
                        try
                        {
                            IntegerOption integerOption = (IntegerOption) field.get(null);
                            optionsList.add(integerOption);
                        }
                        catch (IllegalAccessException e)
                        {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
        // Otherwise, we load the config from the file
        else
        {
            try
            {
                Gson gson = new Gson();
                Reader reader = Files.newBufferedReader(path);
                TypeToken<Map<String, Object>> mapType = new TypeToken<>()
                {
                };
                Map<String, Object> config = new HashMap<>(gson.fromJson(reader, mapType));
                reader.close();

                for (Map.Entry<String, Object> entry : config.entrySet())
                {
                    if (entry.getValue() instanceof Double)
                    {
                        // Integer values are stored as double in the gson file, so by doing this we can put them back
                        // to an int
                        if (((Double) entry.getValue()).intValue() == (Double) entry.getValue())
                        {
                            config.put(entry.getKey(), ((Double) entry.getValue()).intValue());
                        }
                    }
                }

                //
                for (Field field : configClass.getDeclaredFields())
                {
                    if (Modifier.isPublic(field.getModifiers()) && Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers()))
                    {
                        if (BooleanOption.class.isAssignableFrom(field.getType()))
                        {
                            try
                            {
                                BooleanOption booleanOption = (BooleanOption) field.get(null);

                                if (config.containsKey(booleanOption.getOptionName()))
                                {
                                    boolean configFileValue = (Boolean) config.get(booleanOption.getOptionName());
                                    // If the value in the config file is different from the default one, we change its value in the class
                                    if (configFileValue != booleanOption.getValue())
                                    {
                                        setOption(booleanOption.getOptionName(), configFileValue, false);
                                    }
                                }
                            }
                            catch (IllegalAccessException e)
                            {
                                throw new RuntimeException(e);
                            }
                        }
                        else if (IntegerOption.class.isAssignableFrom(field.getType()))
                        {
                            try
                            {
                                IntegerOption integerOption = (IntegerOption) field.get(null);

                                if (config.containsKey(integerOption.getOptionName()))
                                {
                                    int configFileValue = (Integer) config.get(integerOption.getOptionName());
                                    // If the value in the config file is different from the default one, we change its value in the class
                                    if (configFileValue != integerOption.getValue())
                                    {
                                        setOption(integerOption.getOptionName(), configFileValue, false);
                                    }
                                }
                            }
                            catch (IllegalAccessException e)
                            {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    public void writeConfigOld()
    {
        clearUnsavedChangedOptions();
        Map<String, Object> config = new HashMap<>();

        for (BooleanOption option : booleanOptionList)
        {
            config.put(option.getOptionName(), option.getValue());
        }

        for (IntegerOption option : integerOptionList)
        {
            config.put(option.getOptionName(), option.getValue());
        }

        Path path = FabricLoader.getInstance().getConfigDir().resolve(modid + ".json");

        if (!Files.exists(path))
        {
            try
            {
                Files.createFile(path);
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }

        if (!this.isEditingFile)
        {
            this.isEditingFile = true;

            try
            {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                Writer writer = Files.newBufferedWriter(path);
                gson.toJson(config, writer);
                writer.close();
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }

            this.isEditingFile = false;
        }
        else
        {
            long end = System.currentTimeMillis() + 1000; // 1 s
            boolean couldWrite = false;

            while (System.currentTimeMillis() < end)
            {
                if (!this.isEditingFile)
                {
                    this.isEditingFile = true;

                    try
                    {
                        Gson gson = new GsonBuilder().setPrettyPrinting().create();
                        Writer writer = Files.newBufferedWriter(path);
                        gson.toJson(config, writer);
                        writer.close();
                    }
                    catch (IOException e)
                    {
                        throw new RuntimeException(e);
                    }

                    couldWrite = true;
                    this.isEditingFile = false;
                    break;
                }
            }

            if (!couldWrite)
            {
                LOGGER.info("[CyanLibCore] Could not write the file %s because it is already being written (for more than 1 sec)".formatted(path.getFileName().toString()));
            }
        }
    }

    public void writeConfig()
    {
        clearUnsavedChangedOptions();
        Map<String, Object> config = new HashMap<>();

        for (Option<?> option : optionsList)
        {
            config.put(option.getOptionName(), option.getValue());
        }

        Path path = FabricLoader.getInstance().getConfigDir().resolve(modid + ".json");

        if (!Files.exists(path))
        {
            try
            {
                Files.createFile(path);
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }

        if (!this.isEditingFile)
        {
            this.isEditingFile = true;

            try
            {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                Writer writer = Files.newBufferedWriter(path);
                gson.toJson(config, writer);
                writer.close();
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }

            this.isEditingFile = false;
        }
        else
        {
            long end = System.currentTimeMillis() + 1000; // 1 s
            boolean couldWrite = false;

            while (System.currentTimeMillis() < end)
            {
                if (!this.isEditingFile)
                {
                    this.isEditingFile = true;

                    try
                    {
                        Gson gson = new GsonBuilder().setPrettyPrinting().create();
                        Writer writer = Files.newBufferedWriter(path);
                        gson.toJson(config, writer);
                        writer.close();
                    }
                    catch (IOException e)
                    {
                        throw new RuntimeException(e);
                    }

                    couldWrite = true;
                    this.isEditingFile = false;
                    break;
                }
            }

            if (!couldWrite)
            {
                LOGGER.info("[CyanLibCore] Could not write the file %s because it is already being written (for more than 1 sec)".formatted(path.getFileName().toString()));
            }
        }
    }
}
