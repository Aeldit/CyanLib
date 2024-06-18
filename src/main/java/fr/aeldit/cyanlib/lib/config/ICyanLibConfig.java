package fr.aeldit.cyanlib.lib.config;

import java.util.Map;

public interface ICyanLibConfig
{
    /**
     * This is used because the mod being potentially server-side, the clients playing on the server wouldn't have
     * the translations
     *
     * @return The HashMap containing all the translations for your mod
     * @apiNote To add your translations, you can do as follows :
     *
     * <pre>{@code
     *      @Override
     *      public Map<String, String> getDefaultTranslations()
     *      {
     *          return Map.ofEntries(
     *              Map.entry("path.to.translation.one", "Translation 1"),
     *              Map.entry("path.to.translation.two", "Translation 2"),
     *              ...
     *          );
     *      }
     * }</pre>
     */
    Map<String, String> getDefaultTranslations();
}
