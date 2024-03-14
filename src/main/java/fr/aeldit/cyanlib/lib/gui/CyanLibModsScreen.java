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

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static fr.aeldit.cyanlib.lib.CyanLib.CONFIG_CLASS_INSTANCES;

@Environment(EnvType.CLIENT)
public class CyanLibModsScreen extends Screen
{
    private final Screen parent;

    public CyanLibModsScreen(Screen parent)
    {
        super(Text.translatable("cyanlib.screen.mods.title"));
        this.parent = parent;
    }

    @Override
    public void close()
    {
        Objects.requireNonNull(client).setScreen(parent);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta)
    {
        super.render(context, mouseX, mouseY, delta);
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
        int i = 0;
        List<String> sortedNames = new ArrayList<>(CONFIG_CLASS_INSTANCES.keySet());
        Collections.sort(sortedNames);

        for (String modId : sortedNames)
        {
            // Temporary, will add a page system at some point
            if (i < 6)
            {
                addDrawableChild(
                        ButtonWidget.builder(Text.translatable("%s.cyanlib.screen".formatted(modId)),
                                        button -> Objects.requireNonNull(client).setScreen(
                                                new CyanLibConfigScreen(
                                                        CONFIG_CLASS_INSTANCES.get(modId).getOptionsStorage(),
                                                        parent,
                                                        CONFIG_CLASS_INSTANCES.get(modId).getOptionsStorage().getConfigClass()
                                                )
                                        )
                                )
                                .dimensions(30, 30 + 20 * i + 10 * i, 150, 20)
                                .build()
                );
            }
            else
            {
                addDrawableChild(
                        ButtonWidget.builder(Text.translatable("%s.cyanlib.screen".formatted(modId)),
                                        button -> Objects.requireNonNull(client).setScreen(
                                                new CyanLibConfigScreen(
                                                        CONFIG_CLASS_INSTANCES.get(modId).getOptionsStorage(),
                                                        parent,
                                                        CONFIG_CLASS_INSTANCES.get(modId).getOptionsStorage().getConfigClass()
                                                )
                                        )
                                )
                                .dimensions(width - 180, 30 + 30 * (i - 6), 150, 20)
                                .build()
                );
            }
            i++;
        }

        addDrawableChild(
                ButtonWidget.builder(ScreenTexts.DONE, button -> close())
                        .dimensions(width / 2 - 100, height - 28, 200, 20)
                        .build()
        );
    }
}
