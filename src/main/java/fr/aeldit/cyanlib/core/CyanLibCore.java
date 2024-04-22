package fr.aeldit.cyanlib.core;

import fr.aeldit.cyanlib.core.config.CyanLibConfigImpl;
import fr.aeldit.cyanlib.lib.CyanLib;
import fr.aeldit.cyanlib.lib.CyanLibLanguageUtils;
import fr.aeldit.cyanlib.lib.config.CyanLibOptionsStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CyanLibCore
{
    public static final String MODID = "cyanlib";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public static final CyanLibOptionsStorage OPTS_STORAGE = new CyanLibOptionsStorage(MODID, new CyanLibConfigImpl());
    public static final CyanLib LIB_UTILS = new CyanLib(MODID, OPTS_STORAGE, new CyanLibLanguageUtils(MODID));
}
