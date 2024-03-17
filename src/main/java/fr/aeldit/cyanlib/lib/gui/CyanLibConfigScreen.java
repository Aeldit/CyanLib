package fr.aeldit.cyanlib.lib.gui;

import fr.aeldit.cyanlib.lib.config.CyanLibConfig;
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

import java.util.Objects;

@Environment(EnvType.CLIENT)
public class CyanLibConfigScreen extends Screen
{
    private final CyanLibOptionsStorage cyanLibOptionsStorage;
    private final Screen parent;
    private final CyanLibConfig configOptionsClass;
    private OptionListWidget optionList;

    public CyanLibConfigScreen(
            @NotNull CyanLibOptionsStorage cyanLibOptionsStorage, Screen parent, CyanLibConfig configOptionsClass
    )
    {
        super(Text.translatable("%s.screen.options.title".formatted(cyanLibOptionsStorage.getModid())));
        this.cyanLibOptionsStorage = cyanLibOptionsStorage;
        this.parent = parent;
        this.configOptionsClass = configOptionsClass;
    }

    @Override
    public void close()
    {
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
        optionList = new OptionListWidget(client, width, height - 66, 32, 32);
        optionList.addAll(CyanLibOptionsStorage.asConfigOptions(configOptionsClass));
        addSelectableChild(optionList);

        addDrawableChild(
                ButtonWidget.builder(Text.translatable("cyanlib.screen.config.reset"), button -> {
                            cyanLibOptionsStorage.resetOptions();
                            close();
                        })
                        .tooltip(Tooltip.of(Text.translatable("cyanlib.screen.config.reset.tooltip")))
                        .dimensions(10, 6, 100, 20)
                        .build()
        );

        addDrawableChild(
                ButtonWidget.builder(ScreenTexts.DONE, button -> close())
                        .dimensions(width / 2 - 100, height - 28, 200, 20)
                        .build()
        );
    }
}
