package fr.aeldit.cyanlib.core;

import fr.aeldit.cyanlib.core.config.CyanLibConfigImpl;
import fr.aeldit.cyanlib.lib.CyanLib;
import fr.aeldit.cyanlib.lib.commands.CyanLibConfigCommands;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CyanLibCore implements ModInitializer
{
    public static final String CYANLIB_MODID = "cyanlib";
    public static final Logger LOGGER = LoggerFactory.getLogger(CYANLIB_MODID);

    public static final CyanLib LIB_UTILS = new CyanLib(CYANLIB_MODID, new CyanLibConfigImpl());

    @Override
    public void onInitialize()
    {
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, dedicated, environment) -> new CyanLibConfigCommands(CYANLIB_MODID, LIB_UTILS).register(
                        dispatcher)
        );
        LOGGER.info("[CyanLib] Successfully initialized");
    }
}
