package fr.aeldit.cyanlib;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CyanLibCore implements ModInitializer
{
    public static final String MODID = "cyanlib";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);
    public static final String MODNAME = "[CyanLib]";

    @Override
    // Initialize the differents parts of the mod when lauched on server
    public void onInitialize()
    {
        LOGGER.info("{} Successfuly initialized", MODNAME);
    }
}
