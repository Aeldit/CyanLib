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

public class CyanLibOptionsStorage
{
    private final String modid;
    private final ICyanLibConfig cyanLibConfigClass;
    // Used for the auto-completion for commands
    private final ArrayList<String> optionsNames = new ArrayList<>();

    // We use a synchronized list because 2 players can edit the config at the same time when in multiplayer
    private final List<IOption<?>> optionsList = Collections.synchronizedList(new ArrayList<>());

    public CyanLibOptionsStorage(String modid, ICyanLibConfig configClass)
    {
        this.modid = modid;
        this.cyanLibConfigClass = configClass;
    }

    public void init()
    {
        readConfig();
        for (IOption<?> option : optionsList)
        {
            optionsNames.add(option.getOptionName());
        }
    }

    public ICyanLibConfig getConfigClass()
    {
        return cyanLibConfigClass;
    }

    public ArrayList<String> getOptionsNames()
    {
        return optionsNames;
    }

    @Environment(EnvType.CLIENT)
    public static SimpleOption<?> @NotNull [] asConfigOptions(@NotNull ICyanLibConfig configClass)
    {
        ArrayList<SimpleOption<?>> options = new ArrayList<>(configClass.getClass().getDeclaredFields().length);

        for (Field field : configClass.getClass().getDeclaredFields())
        {
            try
            {
                options.add(((IOption<?>) field.get(null)).asConfigOption());
            }
            catch (IllegalAccessException e)
            {
                throw new RuntimeException(e);
            }
        }
        return options.toArray(SimpleOption[]::new);
    }

    public @Nullable Object getOptionValue(String optionName)
    {
        IOption<?> option = getOption(optionName);
        return option == null ? null : option.getValue();
    }

    public boolean setOption(String optionName, Object value, boolean save)
    {
        IOption<?> option = getOption(optionName);
        if (option != null)
        {
            boolean success = option.setValue(value);
            if (save)
            {
                writeConfig();
            }
            return success;
        }
        return false;
    }

    public void resetOptions()
    {
        optionsList.forEach(IOption::reset);
    }

    public boolean optionExists(String optionName)
    {
        return getOption(optionName) != null;
    }

    /**
     * Called for the command {@code /modid config <optionName>}
     *
     * @return a suggestion with the available options
     */
    public static CompletableFuture<Suggestions> getOptionsSuggestions(
            @NotNull SuggestionsBuilder builder,
            @NotNull CyanLibOptionsStorage optionsStorage
    )
    {
        return CommandSource.suggestMatching(optionsStorage.getOptionsNames(), builder);
    }

    public boolean hasRule(String optionName, RULES rule)
    {
        IOption<?> option = getOption(optionName);
        return option != null && option.getRule() == rule;
    }

    private @Nullable IOption<?> getOption(String optionName)
    {
        for (IOption<?> option : optionsList)
        {
            if (option.getOptionName().equals(optionName))
            {
                return option;
            }
        }
        return null;
    }

    private void readConfig()
    {
        Path path = FabricLoader.getInstance().getConfigDir().resolve("%s.json".formatted(modid));

        // If the file does not exist, we simply load the class in memory
        if (!Files.exists(path))
        {
            for (Field field : cyanLibConfigClass.getClass().getDeclaredFields())
            {
                if (Modifier.isPublic(field.getModifiers()) && Modifier.isStatic(field.getModifiers())
                    && Modifier.isFinal(field.getModifiers()))
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
            Map<String, Object> config;

            try
            {
                Gson gson = new Gson();
                Reader reader = Files.newBufferedReader(path);
                TypeToken<Map<String, Object>> mapType = new TypeToken<>()
                {
                };
                config = gson.fromJson(reader, mapType);
                reader.close();
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }

            if (config != null && !config.isEmpty())
            {
                // If there are options present in teh code but not in the config file, we need to save the new options
                boolean fileNeedsUpdate = false;

                // Puts doubles with 0 as decimal as integers
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

                // Remove options present in the config file but not in the code
                ArrayList<String> toRemove = new ArrayList<>();
                for (String option : config.keySet())
                {
                    boolean exists = false;

                    for (Field field : cyanLibConfigClass.getClass().getDeclaredFields())
                    {
                        if (Modifier.isPublic(field.getModifiers()) && Modifier.isStatic(field.getModifiers())
                            && Modifier.isFinal(field.getModifiers()))
                        {
                            if (BooleanOption.class.isAssignableFrom(field.getType()))
                            {
                                try
                                {
                                    if (((BooleanOption) field.get(null)).getOptionName().equals(option))
                                    {
                                        exists = true;
                                        break;
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
                                    if (((IntegerOption) field.get(null)).getOptionName().equals(option))
                                    {
                                        exists = true;
                                        break;
                                    }
                                }
                                catch (IllegalAccessException e)
                                {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                    }

                    if (!exists)
                    {
                        toRemove.add(option);
                    }
                }

                for (String option : toRemove)
                {
                    config.remove(option);
                }

                if (!toRemove.isEmpty())
                {
                    fileNeedsUpdate = true;
                    toRemove.clear();
                }

                // For each option found in the config file, update the value of the option object
                // If an option object is not present in the file, it is added to the options and the file is updated
                for (Field field : cyanLibConfigClass.getClass().getDeclaredFields())
                {
                    if (Modifier.isPublic(field.getModifiers()) && Modifier.isStatic(field.getModifiers())
                        && Modifier.isFinal(field.getModifiers()))
                    {
                        if (BooleanOption.class.isAssignableFrom(field.getType()))
                        {
                            try
                            {
                                BooleanOption booleanOption = (BooleanOption) field.get(null);

                                if (config.containsKey(booleanOption.getOptionName()))
                                {
                                    boolean configFileValue = (Boolean) config.get(booleanOption.getOptionName());
                                    // If the value in the config file is different from the default one, we change
                                    // its value in the class
                                    if (configFileValue != booleanOption.getValue())
                                    {
                                        booleanOption.setValue(configFileValue);
                                    }
                                }
                                else
                                {
                                    fileNeedsUpdate = true;
                                }
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

                                if (config.containsKey(integerOption.getOptionName()))
                                {
                                    int configFileValue = (Integer) config.get(integerOption.getOptionName());
                                    // If the value in the config file is different from the default one, we change
                                    // its value in the class
                                    if (configFileValue != integerOption.getValue())
                                    {
                                        integerOption.setValue(configFileValue);
                                    }
                                }
                                else
                                {
                                    fileNeedsUpdate = true;
                                }
                                optionsList.add(integerOption);
                            }
                            catch (IllegalAccessException e)
                            {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }

                if (fileNeedsUpdate)
                {
                    writeConfig();
                }
            }
        }
    }

    public void writeConfig()
    {
        Map<String, Object> config = new HashMap<>();
        for (IOption<?> option : optionsList)
        {
            config.put(option.getOptionName(), option.getValue());
        }

        Path path = FabricLoader.getInstance().getConfigDir().resolve("%s.json".formatted(modid));
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
    }
}
