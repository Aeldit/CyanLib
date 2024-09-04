package fr.aeldit.cyanlib.lib.gui;

import fr.aeldit.cyanlib.lib.config.CyanLibOptionsStorage;
import fr.aeldit.cyanlib.lib.config.ICyanLibConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
//? if =1.19.4 {
/*import net.minecraft.client.util.math.MatrixStack;
 *///?} else {
import net.minecraft.client.gui.DrawContext;
//?}
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.OptionListWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

import java.util.Objects;

import static fr.aeldit.cyanlib.lib.CyanLib.CONFIG_CLASS_INSTANCES;

@Environment(EnvType.CLIENT)
public class CyanLibConfigScreen extends GameOptionsScreen
{
    private final CyanLibOptionsStorage cyanLibOptionsStorage;
    private final Screen parent;
    private final ICyanLibConfig configOptionsClass;
    private OptionListWidget optionList;

    public CyanLibConfigScreen(Screen previous, Screen parent, String modid)
    {
        super(previous, MinecraftClient.getInstance().options,
              Text.translatable("%s.screen.options.title".formatted(modid))
        );
        this.parent = parent;
        this.cyanLibOptionsStorage = CONFIG_CLASS_INSTANCES.get(modid).getOptionsStorage();
        this.configOptionsClass = this.cyanLibOptionsStorage.getConfigClass();
    }

    @Override
    public void close()
    {
        cyanLibOptionsStorage.writeConfig();
        Objects.requireNonNull(client).setScreen(parent);
    }

    //? if =1.19.4 {
    /*@Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta)
    {
        this.renderBackgroundTexture(matrices);
        optionList.render(matrices, mouseX, mouseY, delta);
        drawCenteredTextWithShadow(matrices, this.textRenderer, this.title, this.width / 2, 5, 0xffffff);
        super.render(matrices, mouseX, mouseY, delta);
    }
    *///?} elif =1.20.1 {
    /*@Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta)
    {
        optionList.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 5, 0xffffff);
        super.render(context, mouseX, mouseY, delta);
    }
    *///?} else {
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta)
    {
        super.render(context, mouseX, mouseY, delta);
        optionList.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 5, 0xffffff);
    }
    //?}

    //? if =1.20.2 {
    /*@Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta)
    {
        super.renderBackgroundTexture(context);
    }
    *///?}

    @Override
    protected void init()
    {
        //? if >=1.21 {
        optionList = new OptionListWidget(client, width, this);
        //?} elif =1.20.6 {
        /*optionList = new OptionListWidget(client, width, height, this);
         *///?} elif =1.20.4 {
        /*optionList = new OptionListWidget(client, width, height - 66, 32, 32);
         *///?} elif =1.20.2 || =1.20.1 {
        /*optionList = new OptionListWidget(client, width, height, 32, height - 32, 32);
         *///?} elif =1.19.4 {
        /*optionList = new OptionListWidget(client, width, height, 32, height - 32, 25);
         *///?}
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

    //? if =1.21.0 {
    @Override
    protected void addOptions()
    {
    }
    //?}
}
