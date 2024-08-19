package fr.aeldit.cyanlib.core;

import fr.aeldit.cyanlib.lib.commands.CyanLibConfigCommands;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import static fr.aeldit.cyanlib.core.CyanLibCore.*;

public class CyanLibClientCore implements ClientModInitializer
{
    @Override
    public void onInitializeClient()
    {
        LIB_UTILS.init(CYANLIB_MODID, OPTS_STORAGE);

        CommandRegistrationCallback.EVENT.register(
                (dispatcher, dedicated, environment) -> new CyanLibConfigCommands(CYANLIB_MODID, LIB_UTILS).register(dispatcher)
        );
        LOGGER.info("[CyanLib] Successfully initialized");
    }
}
