package fr.aeldit.cyanlib.lib.config;

import java.util.Map;

public interface CyanLibConfig
{
    /**
     * This is used because the mod being potentially server-side, the clients playing on the server wouldn't have
     * the translations
     *
     * @return The HashMap containing all the translations for your mod
     */
    Map<String, String> getDefaultTranslations();
}
