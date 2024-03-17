package fr.aeldit.cyanlib.core;

import fr.aeldit.cyanlib.lib.commands.CyanLibConfigCommands;
import fr.aeldit.cyanlib.lib.gui.CyanLibModsScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import static fr.aeldit.cyanlib.core.CyanLibCore.*;

public class CyanLibClientCore implements ClientModInitializer
{
    @Override
    public void onInitializeClient()
    {
        LIB_UTILS.init(MODID, OPTS_STORAGE);

        KeyBinding mainScreenKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "cyanlib.keybindings.openScreen.config",
                InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_I,
                "cyanlib.keybindings.category"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (mainScreenKey.wasPressed())
            {
                client.setScreen(new CyanLibModsScreen(null));
            }
        });

        CommandRegistrationCallback.EVENT.register(
                (dispatcher, dedicated, environment) -> new CyanLibConfigCommands(MODID, LIB_UTILS).register(dispatcher)
        );
        LOGGER.info("[CyanLib] Successfully initialized");
    }
}
