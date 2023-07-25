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
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static fr.aeldit.cyanlib.core.utils.Utils.LOGGER;

public class CyanLibConfig
{
    private final Path path;
    protected final ConcurrentHashMap<String, Object> options = new ConcurrentHashMap<>();
    private final Map<String, RULES> rules;
    private boolean isEditing = false;

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
    public CyanLibConfig(String modid, Map<String, Object> defaultOptions, Map<String, RULES> rules)
    {
        this.path = FabricLoader.getInstance().getConfigDir().resolve(modid + ".json");
        read();

        if (this.options.isEmpty())
        {
            this.options.putAll(defaultOptions);
        }

        for (String option : this.options.keySet())
        {
            if (!defaultOptions.containsKey(option))
            {
                this.options.remove(option);
                LOGGER.info("[%s] Removed the option %s from the options because it is no present in the default options".formatted(modid, option));
            }
        }

        defaultOptions.forEach((s, o) -> {
            if (!this.options.containsKey(s))
            {
                this.options.put(s, o);
                LOGGER.info("[%s] Added the option %s from the options because it is no present in the options file".formatted(modid, s));
            }
        });

        this.rules = rules;
    }

    public ArrayList<String> getOptionsWithRule(RULES rule)
    {
        ArrayList<String> validOptions = new ArrayList<>(this.rules.size());

        for (Map.Entry<String, RULES> entry : this.rules.entrySet())
        {
            if (entry.getValue().equals(rule))
            {
                validOptions.add(entry.getKey());
            }
        }
        return validOptions;
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
