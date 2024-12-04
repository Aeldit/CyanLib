package fr.aeldit.cyanlib.core;

import fr.aeldit.cyanlib.core.config.CyanLibConfigImpl;
import fr.aeldit.cyanlib.lib.CyanLib;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CyanLibCore
{
    public static final String CYANLIB_MODID = "cyanlib";
    public static final Logger LOGGER = LoggerFactory.getLogger(CYANLIB_MODID);

    public static final CyanLib LIB_UTILS = new CyanLib(CYANLIB_MODID, new CyanLibConfigImpl());
}
