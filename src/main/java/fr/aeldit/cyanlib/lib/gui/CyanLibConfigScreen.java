package fr.aeldit.cyanlib.lib.gui;

import fr.aeldit.cyanlib.lib.config.CyanLibConfig;
import fr.aeldit.cyanlib.lib.config.CyanLibOptionsStorage;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.OptionListWidget;
import net.minecraft.client.util.math.MatrixStack;
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
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta)
    {
        this.renderBackgroundTexture(matrices);
        optionList.render(matrices, mouseX, mouseY, delta);
        drawCenteredTextWithShadow(matrices, this.textRenderer, this.title, this.width / 2, 5, 0xffffff);
        super.render(matrices, mouseX, mouseY, delta);
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
