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
import fr.aeldit.cyanlib.lib.utils.RULES;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.OptionListWidget;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static fr.aeldit.cyanlib.core.utils.Utils.LOGGER;

public class CyanLibOptionsStorage
{
    protected String modid;
    private final Class<?> configClass;
    private final ArrayList<String> optionsNames = new ArrayList<>();
    private final Map<String, Object> changedUnsavedOptions = new HashMap<>();
    private boolean isEditingFile = false;

    public CyanLibOptionsStorage(String modid, Class<?> configClass)
    {
        this.modid = modid;
        this.configClass = configClass;
    }

    public void init()
    {
        readConfig();
    }

    public class BooleanOption implements SimpleOptionConverter
    {
        private final String optionName;
        private final RULES rule;
        private final boolean defaultValue;

        public BooleanOption(String optionName, boolean value)
        {
            setBooleanOption(optionName, value);
            this.optionName = optionName;
            this.rule = RULES.NONE;
            this.defaultValue = value;
        }

        public BooleanOption(String optionName, boolean value, RULES rule)
        {
            setBooleanOption(optionName, value);
            this.optionName = optionName;
            this.rule = rule;
            this.defaultValue = value;
        }

        public String getOptionName()
        {
            return optionName;
        }

        public boolean getValue()
        {
            return getBooleanOption(optionName);
        }

        public void setValue(boolean value)
        {
            if (!changedUnsavedOptions.containsKey(optionName))
            {
                changedUnsavedOptions.put(optionName, booleanOptions.get(optionName));
            }
            booleanOptions.put(optionName, value);
        }

        @Override
        public SimpleOption<Boolean> asConfigOption()
        {
            return SimpleOption.ofBoolean("%s.config.option.%s".formatted(modid, optionName),
                    getValue(),
                    this::setValue
            );
        }
    }

    public class IntegerOption implements SimpleOptionConverter
    {
        private final String optionName;
        private final int min;
        private final int max;
        private final RULES rule;
        private final int defaultValue;

        /**
         * Use when no rules are given (makes this integer option store just the value)
         */
        public IntegerOption(String optionName, int value)
        {
            this.optionName = optionName;
            this.min = 0;
            this.max = 4;
            this.rule = RULES.NONE;
            this.defaultValue = value;
            setValue(value);
        }

        /**
         * Used with :
         * <ul>
         *     <li>{@link RULES#POSITIVE_VALUE}</li>
         *     <li>{@link RULES#NEGATIVE_VALUE}</li>
         *     <li>{@link RULES#OP_LEVELS}</li>
         * </ul>
         */
        public IntegerOption(String optionName, int value, @NotNull RULES rule)
        {
            this.optionName = optionName;
            if (rule.equals(RULES.OP_LEVELS))
            {
                this.min = 0;
                this.max = 4;
            }
            else
            {
                this.min = 0;
                this.max = 0;
            }
            this.rule = rule;
            this.defaultValue = value;
            setValue(value);
        }

        /**
         * Used with :
         * <ul>
         *     <li>{@link RULES#MAX_VALUE}</li>
         *     <li>{@link RULES#MIN_VALUE}</li>
         * </ul>
         */
        public IntegerOption(String optionName, int value, @NotNull RULES rule, int minOrMax)
        {
            this.optionName = optionName;

            if (rule.equals(RULES.MIN_VALUE))
            {
                this.min = minOrMax;
                this.max = 0;
            }
            else if (rule.equals(RULES.MAX_VALUE))
            {
                this.min = 0;
                this.max = minOrMax;
            }
            else
            {
                this.min = 0;
                this.max = 0;
            }
            this.rule = rule;
            this.defaultValue = value;
            setValue(value);
        }

        /**
         * Used with :
         * <ul>
         *     <li>{@link RULES#RANGE}</li>
         * </ul>
         */
        public IntegerOption(String optionName, int value, RULES rule, int min, int max)
        {
            this.optionName = optionName;
            this.min = min;
            this.max = max;
            this.rule = rule;
            this.defaultValue = value;
            setValue(value);
        }

        public int getValue()
        {
            return getIntegerOption(optionName);
        }

        public boolean setValue(int value)
        {
            if (rule.equals(RULES.NONE)
                    || (rule.equals(RULES.POSITIVE_VALUE) && value > 0)
                    || (rule.equals(RULES.NEGATIVE_VALUE) && value < 0)
                    || (rule.equals(RULES.OP_LEVELS) && value >= 0 && value <= 4)
                    || (rule.equals(RULES.MAX_VALUE) && value <= max)
                    || (rule.equals(RULES.MIN_VALUE) && value >= min)
                    || (rule.equals(RULES.RANGE) && value >= min && value <= max)
            )
            {
                if (!changedUnsavedOptions.containsKey(optionName))
                {
                    changedUnsavedOptions.put(optionName, integerOptions.get(optionName));
                }
                integerOptions.put(optionName, value);
                return true;
            }
            return false;
        }

        @Override
        public SimpleOption<Integer> asConfigOption()
        {
            return new SimpleOption<>("%s.config.option.%s".formatted(modid, optionName),
                    SimpleOption.emptyTooltip(),
                    (optionText, value) -> Text.of(optionText.getString() + ": " + value),
                    new SimpleOption.ValidatingIntSliderCallbacks(min, max),
                    getValue(),
                    this::setValue
            );
        }
    }

    @Environment(EnvType.CLIENT)
    public class CyanLibConfigScreen extends Screen
    {
        private final Screen parent;
        private final Class<?> configOptionsClass;
        private OptionListWidget optionList;

        public CyanLibConfigScreen(Screen parent, Class<?> configOptionsClass)
        {
            super(Text.translatable("%s.screen.options.title".formatted(modid)));
            this.parent = parent;
            this.configOptionsClass = configOptionsClass;
        }

        @Override
        public void close()
        {
            Objects.requireNonNull(client).setScreen(parent);
        }

        public void closeWithoutSaving()
        {
            if (!changedUnsavedOptions.isEmpty())
            {
                for (Map.Entry<String, Object> entry : changedUnsavedOptions.entrySet())
                {
                    if (entry.getValue() instanceof Boolean)
                    {
                        booleanOptions.put(entry.getKey(), (Boolean) entry.getValue());
                    }
                    else if (entry.getValue() instanceof Integer)
                    {
                        integerOptions.put(entry.getKey(), (Integer) entry.getValue());
                    }
                }
                changedUnsavedOptions.clear();
                writeConfig();
            }
            close();
        }

        @Override
        public void render(DrawContext DrawContext, int mouseX, int mouseY, float delta)
        {
            this.renderBackgroundTexture(DrawContext);
            optionList.render(DrawContext, mouseX, mouseY, delta);
            DrawContext.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 5, 0xffffff);
            super.render(DrawContext, mouseX, mouseY, delta);
        }

        @Override
        protected void init()
        {
            optionList = new OptionListWidget(client, width, height, 32, height - 32, 25);
            optionList.addAll(asConfigOptions(configOptionsClass));
            addSelectableChild(optionList);

            addDrawableChild(
                    ButtonWidget.builder(Text.translatable("%s.screen.config.reset".formatted(modid)), button -> {
                                resetOptions();
                                writeConfig();
                                close();
                            })
                            .tooltip(Tooltip.of(Text.translatable("%s.screen.config.reset.tooltip".formatted(modid))))
                            .dimensions(10, 6, 100, 20)
                            .build()
            );

            addDrawableChild(
                    ButtonWidget.builder(ScreenTexts.CANCEL, button -> closeWithoutSaving())
                            .dimensions(width / 2 - 154, height - 28, 150, 20)
                            .build()
            );
            addDrawableChild(
                    ButtonWidget.builder(Text.translatable("%s.screen.config.save&quit".formatted(modid)), button -> {
                                writeConfig();
                                close();
                            })
                            .dimensions(width / 2 + 4, height - 28, 150, 20)
                            .build()
            );
        }
    }

    public static SimpleOption<?> @NotNull [] asConfigOptions(@NotNull Class<?> configClass)
    {
        ArrayList<SimpleOption<?>> options = new ArrayList<>();

        for (Field field : configClass.getDeclaredFields())
        {
            try
            {
                options.add(((SimpleOptionConverter) field.get(null)).asConfigOption());
            }
            catch (IllegalAccessException e)
            {
                throw new RuntimeException(e);
            }
        }
        return options.toArray(SimpleOption[]::new);
    }

    private final ConcurrentHashMap<String, Boolean> booleanOptions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Integer> integerOptions = new ConcurrentHashMap<>();

    public boolean getBooleanOption(String optionName)
    {
        return booleanOptions.get(optionName);
    }

    public int getIntegerOption(String optionName)
    {
        return integerOptions.get(optionName);
    }

    public void setBooleanOption(String optionName, boolean value)
    {
        booleanOptions.put(optionName, value);
    }

    public boolean setIntegerOption(String optionName, int value)
    {
        for (Field field : configClass.getDeclaredFields())
        {
            if (Modifier.isPublic(field.getModifiers()) && Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers()))
            {
                if (IntegerOption.class.isAssignableFrom(field.getType()))
                {
                    try
                    {
                        IntegerOption integerOption = (IntegerOption) field.get(null);

                        if (integerOption.optionName.equals(optionName))
                        {
                            return integerOption.setValue(value);
                        }
                    }
                    catch (IllegalAccessException e)
                    {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return false;
    }

    public void setAndSaveBooleanOption(String optionName, boolean value)
    {
        setBooleanOption(optionName, value);
        writeConfig();
    }

    public boolean setAndSaveIntegerOption(String optionName, int value)
    {
        if (setIntegerOption(optionName, value))
        {
            writeConfig();
            return true;
        }
        return false;
    }

    public void resetOptions()
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
                        booleanOptions.put(booleanOption.optionName, booleanOption.defaultValue);
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
                        integerOptions.put(integerOption.optionName, integerOption.defaultValue);
                    }
                    catch (IllegalAccessException e)
                    {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    public boolean booleanOptionExists(String optionName)
    {
        for (String option : booleanOptions.keySet())
        {
            if (option.equals(optionName))
            {
                return true;
            }
        }
        return false;
    }

    public boolean integerOptionExists(String optionName)
    {
        for (String option : integerOptions.keySet())
        {
            if (option.equals(optionName))
            {
                return true;
            }
        }
        return false;
    }

    public boolean optionExists(String optionName)
    {
        return booleanOptionExists(optionName) || integerOptionExists(optionName);
    }

    public Object getOption(String optionName)
    {
        if (booleanOptions.containsKey(optionName))
        {
            return booleanOptions.get(optionName);
        }
        else if (integerOptions.containsKey(optionName))
        {
            return integerOptions.get(optionName);
        }
        return null;
    }

    public ArrayList<String> getOptionsNames()
    {
        if (optionsNames.isEmpty())
        {
            optionsNames.addAll(booleanOptions.keySet());
            optionsNames.addAll(integerOptions.keySet());
        }
        return optionsNames;
    }

    public boolean hasRule(String optionName, RULES rule)
    {
        for (Field field : configClass.getDeclaredFields())
        {
            if (Modifier.isPublic(field.getModifiers()) && Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers()))
            {
                if (IntegerOption.class.isAssignableFrom(field.getType()))
                {
                    try
                    {
                        IntegerOption integerOption = (IntegerOption) field.get(null);

                        if (optionName.equals(integerOption.optionName))
                        {
                            return rule.equals(integerOption.rule);
                        }
                    }
                    catch (IllegalAccessException e)
                    {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return false;
    }

    public ArrayList<String> getOptionsWithRule(RULES rule)
    {
        ArrayList<String> validOptions = new ArrayList<>();

        for (Field field : configClass.getDeclaredFields())
        {
            if (Modifier.isPublic(field.getModifiers()) && Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers()))
            {
                if (IntegerOption.class.isAssignableFrom(field.getType()))
                {
                    try
                    {
                        IntegerOption integerOption = (IntegerOption) field.get(null);

                        if (integerOption.rule.equals(rule))
                        {
                            validOptions.add(integerOption.optionName);
                        }
                    }
                    catch (IllegalAccessException e)
                    {
                        throw new RuntimeException(e);
                    }
                }
                else if (BooleanOption.class.isAssignableFrom(field.getType()))
                {
                    try
                    {
                        BooleanOption booleanOption = (BooleanOption) field.get(null);

                        if (booleanOption.rule.equals(rule))
                        {
                            validOptions.add(booleanOption.optionName);
                        }
                    }
                    catch (IllegalAccessException e)
                    {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return validOptions;
    }

    public void readConfig()
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
                            booleanOptions.put(booleanOption.optionName, booleanOption.getValue());
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
                            integerOptions.put(integerOption.optionName, integerOption.getValue());
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
                TypeToken<Map<String, Object>> mapType = new TypeToken<>() {};
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

                                if (config.containsKey(booleanOption.optionName))
                                {
                                    if ((Boolean) config.get(booleanOption.optionName) != booleanOption.getValue())
                                    {
                                        booleanOptions.put(booleanOption.optionName, (Boolean) config.get(booleanOption.optionName));
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

                                if (config.containsKey(integerOption.optionName))
                                {
                                    if ((Integer) config.get(integerOption.optionName) != integerOption.getValue())
                                    {
                                        integerOptions.put(integerOption.optionName, (Integer) config.get(integerOption.optionName));
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

    public void writeConfig()
    {
        Map<String, Object> config = new HashMap<>(booleanOptions);
        config.putAll(integerOptions);

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
                LOGGER.info("[CyanLib] Could not write the file %s because it is already being written (for more than 1 sec)".formatted(path.getFileName().toString()));
            }
        }
    }
}
