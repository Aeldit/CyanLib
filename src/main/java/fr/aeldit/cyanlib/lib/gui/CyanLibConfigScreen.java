package fr.aeldit.cyanlib.lib.gui;

import fr.aeldit.cyanlib.lib.config.CyanLibOptionsStorage;
import fr.aeldit.cyanlib.lib.config.ICyanLibConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
#if MC_1_19_4
import net.minecraft.client.util.math.MatrixStack;
#else
import net.minecraft.client.gui.DrawContext;
#endif
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.OptionListWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Environment(EnvType.CLIENT)
public class CyanLibConfigScreen extends GameOptionsScreen
{
    private final CyanLibOptionsStorage cyanLibOptionsStorage;
    private final Screen parent;
    private final ICyanLibConfig configOptionsClass;
    private OptionListWidget optionList;

    public CyanLibConfigScreen(
            Screen previous, @NotNull CyanLibOptionsStorage cyanLibOptionsStorage, Screen parent,
            ICyanLibConfig configOptionsClass
    )
    {
        super(previous, MinecraftClient.getInstance().options,
                Text.translatable("%s.screen.options.title".formatted(cyanLibOptionsStorage.getModid()))
        );
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

    #if MC_1_19_4
    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta)
    {
        this.renderBackgroundTexture(matrices);
        drawCenteredTextWithShadow(matrices, this.textRenderer, this.title, this.width / 2, 5, 0xffffff);
        super.render(matrices, mouseX, mouseY, delta);
    }
    #else
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta)
    {
        super.render(context, mouseX, mouseY, delta);
        optionList.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 5, 0xffffff);
    }
    #endif

    #if MC_1_20_2
    /*@Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta)
    {
        super.renderBackgroundTexture(context);
    }*/
    #endif

    @Override
    protected void init()
    {
        #if MC_1_21
        optionList = new OptionListWidget(client, width, this);
        #elif MC_1_20_6
            optionList = new OptionListWidget(client, width, height, this);
        #elif MC_1_20_4
            optionList = new OptionListWidget(client, width, height - 66, 32, 32);
        #elif MC_1_20_2
            optionList = new OptionListWidget(client, width, height, 32, height - 66, 32);
        #elif MC_1_19_4
            optionList = new OptionListWidget(client, width, height, 32, height - 32, 25);
        #endif
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

    #if MC_1_21
    /*@Override
    protected void addOptions()
    {
    }*/
    #endif
}
