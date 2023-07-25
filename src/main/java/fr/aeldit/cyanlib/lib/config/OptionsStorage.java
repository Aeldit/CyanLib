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
import fr.aeldit.cyanlib.lib.utils.RULES;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.OptionListWidget;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
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

public class OptionsStorage
{
    protected String MODID;
    private final Class<?> configClass;
    private static final ArrayList<String> optionsNames = new ArrayList<>();

    public OptionsStorage(String modid, Class<?> configClass)
    {
        MODID = modid;
        this.configClass = configClass;
    }

    public class BooleanOption implements SimpleOptionConverter
    {
        private final String optionName;

        public BooleanOption(String optionName, boolean value)
        {
            this.optionName = optionName;
            setBooleanOption(value);
        }

        public String getOptionName()
        {
            return optionName;
        }

        public boolean getValue()
        {
            return booleanOptions.get(optionName);
        }

        public void setBooleanOption(boolean value)
        {
            booleanOptions.put(optionName, value);
        }

        @Override
        public SimpleOption<Boolean> asConfigOption()
        {
            return SimpleOption.ofBoolean("%s.config.option.%s".formatted(MODID, optionName),
                    booleanOptions.get(optionName),
                    this::setBooleanOption
                    //value -> setBooleanOption(value)
            );
        }
    }

    public class IntegerOption implements SimpleOptionConverter
    {
        private final String optionName;
        private final int min;
        private final int max;
        private final RULES rule;

        /**
         * Use when no rules are given (makes this integer option store just the value)
         */
        public IntegerOption(String optionName, int value)
        {
            this.optionName = optionName;
            this.min = 0;
            this.max = 4;
            this.rule = RULES.NONE;
            setIntegerOption(value);
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
            setIntegerOption(value);
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
            setIntegerOption(value);
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
            setIntegerOption(value);
        }

        public String getOptionName()
        {
            return optionName;
        }

        public int getValue()
        {
            return integerOptions.get(optionName);
        }

        public boolean setIntegerOption(int value)
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
                integerOptions.put(optionName, value);
                return true;
            }
            return false;
        }

        @Override
        public SimpleOption<Integer> asConfigOption()
        {
            return new SimpleOption<>("%s.config.option.%s".formatted(MODID, optionName),
                    SimpleOption.emptyTooltip(),
                    (optionText, value) -> Text.of(optionText.getString() + ": " + value),
                    new SimpleOption.ValidatingIntSliderCallbacks(min, max),
                    integerOptions.get(optionName),
                    this::setIntegerOption
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
            super(Text.translatable("%s.screen.options.title".formatted(MODID)));
            this.parent = parent;
            this.configOptionsClass = configOptionsClass;
        }

        @Override
        public void close()
        {
            Objects.requireNonNull(client).setScreen(parent);
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
                    ButtonWidget.builder(ScreenTexts.CANCEL, button -> {
                                close();
                            })
                            .dimensions(width / 2 - 154, height - 28, 150, 20)
                            .build()
            );
            addDrawableChild(
                    ButtonWidget.builder(Text.translatable("%s.screen.config.save&quit".formatted(MODID)), button -> {
                                saveConfig();
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
        saveConfig();
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

                        if (integerOption.getOptionName().equals(optionName))
                        {
                            if (integerOption.setIntegerOption(value))
                            {
                                saveConfig();
                                return true;
                            }
                            return false;
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

                        if (optionName.equals(integerOption.getOptionName()))
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

    public void saveConfig()
    {
        Map<String, Object> config = new HashMap<>();

        for (Field field : configClass.getDeclaredFields())
        {
            if (Modifier.isPublic(field.getModifiers()) && Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers()))
            {
                if (BooleanOption.class.isAssignableFrom(field.getType()))
                {
                    try
                    {
                        BooleanOption booleanOption = (BooleanOption) field.get(null);
                        config.put(booleanOption.getOptionName(), booleanOption.getValue());
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
                        config.put(integerOption.getOptionName(), integerOption.getValue());
                    }
                    catch (IllegalAccessException e)
                    {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        Path path = FabricLoader.getInstance().getConfigDir().resolve(MODID + ".json");

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
