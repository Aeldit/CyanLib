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
    private final ICyanLibConfig cyanLibConfigClass;
    // Used for the auto-completion for commands
    private final ArrayList<String> optionsNames = new ArrayList<>();
    private boolean isEditingFile = false;

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
        optionsList.forEach(option -> optionsNames.add(option.getOptionName()));
    }

    public String getModid()
    {
        return modid;
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
            if (save)
            {
                writeConfig();
            }
            return option.setValue(value);
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
        Path path = FabricLoader.getInstance().getConfigDir().resolve(modid + ".json");

        // If the file does not exist, we simply load the class in memory
        if (!Files.exists(path))
        {
            for (Field field : cyanLibConfigClass.getClass().getDeclaredFields())
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
            Map<String, Object> config = new HashMap<>();

            try
            {
                Gson gson = new Gson();
                Reader reader = Files.newBufferedReader(path);
                TypeToken<Map<String, Object>> mapType = new TypeToken<>()
                {
                };
                config.putAll(gson.fromJson(reader, mapType));
                reader.close();
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }

            if (!config.isEmpty())
            {
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

                for (Field field : cyanLibConfigClass.getClass().getDeclaredFields())
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
                                    // If the value in the config file is different from the default one, we change
                                    // its value in the class
                                    if (configFileValue != booleanOption.getValue())
                                    {
                                        booleanOption.setValue(configFileValue);
                                    }
                                    optionsList.add(booleanOption);
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
                                    // If the value in the config file is different from the default one, we change
                                    // its value in the class
                                    if (configFileValue != integerOption.getValue())
                                    {
                                        integerOption.setValue(configFileValue);
                                    }
                                    optionsList.add(integerOption);
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
        }
    }

    public void writeConfig()
    {
        Map<String, Object> config = new HashMap<>();

        for (IOption<?> option : optionsList)
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
                LOGGER.info(("[CyanLibCore] Could not write the file %s because it is already being written (for more" +
                        " " +
                        "than 1 sec)").formatted(path.getFileName().toString()));
            }
        }
    }
}
