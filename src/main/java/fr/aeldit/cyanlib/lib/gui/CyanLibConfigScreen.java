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

package fr.aeldit.cyanlib.lib.gui;

import fr.aeldit.cyanlib.lib.config.CyanLibOptionsStorage;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.OptionListWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;

@Environment(EnvType.CLIENT)
public class CyanLibConfigScreen extends Screen
{
    private final CyanLibOptionsStorage cyanLibOptionsStorage;
    private final Screen parent;
    private final Class<?> configOptionsClass;
    private OptionListWidget optionList;
    // Used for when the player uses the escape key to exit the screen, which like the cancel button, reverts the modified but no saved options to their previous value
    private boolean save = false;

    public CyanLibConfigScreen(@NotNull CyanLibOptionsStorage cyanLibOptionsStorage, Screen parent, Class<?> configOptionsClass)
    {
        super(Text.translatable("%s.screen.options.title".formatted(cyanLibOptionsStorage.getModid())));
        this.cyanLibOptionsStorage = cyanLibOptionsStorage;
        this.parent = parent;
        this.configOptionsClass = configOptionsClass;
    }

    @Override
    public void close()
    {
        if (!save)
        {
            if (!cyanLibOptionsStorage.getUnsavedChangedOptions().isEmpty())
            {
                for (Map.Entry<String, Object> entry : cyanLibOptionsStorage.getUnsavedChangedOptions().entrySet())
                {
                    if (entry.getValue() instanceof Boolean)
                    {
                        cyanLibOptionsStorage.setBooleanOption(entry.getKey(), (Boolean) entry.getValue(), false);
                    }
                    else if (entry.getValue() instanceof Integer)
                    {
                        cyanLibOptionsStorage.setIntegerOption(entry.getKey(), (Integer) entry.getValue(), false);
                    }
                }
            }
        }
        save = false;
        cyanLibOptionsStorage.writeConfig();
        Objects.requireNonNull(client).setScreen(parent);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta)
    {
        super.render(context, mouseX, mouseY, delta);
        optionList.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 5, 0xffffff);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta)
    {
        super.renderBackgroundTexture(context);
    }

    @Override
    protected void init()
    {
        optionList = new OptionListWidget(client, width, height, 32, height - 32, 25);
        optionList.addAll(CyanLibOptionsStorage.asConfigOptions(configOptionsClass));
        addSelectableChild(optionList);

        addDrawableChild(
                ButtonWidget.builder(Text.translatable("cyanlib.screen.config.reset"), button -> {
                            cyanLibOptionsStorage.resetOptions();
                            save = true;
                            close();
                        })
                        .tooltip(Tooltip.of(Text.translatable("cyanlib.screen.config.reset.tooltip")))
                        .dimensions(10, 6, 100, 20)
                        .build()
        );

        addDrawableChild(
                ButtonWidget.builder(ScreenTexts.CANCEL, button -> {
                            save = false;
                            close();
                        })
                        .tooltip(Tooltip.of(Text.translatable("cyanlib.screen.config.cancel.tooltip")))
                        .dimensions(width / 2 - 154, height - 28, 150, 20)
                        .build()
        );
        addDrawableChild(
                ButtonWidget.builder(Text.translatable("cyanlib.screen.config.save&quit"), button -> {
                            save = true;
                            close();
                        })
                        .dimensions(width / 2 + 4, height - 28, 150, 20)
                        .build()
        );
    }
}
