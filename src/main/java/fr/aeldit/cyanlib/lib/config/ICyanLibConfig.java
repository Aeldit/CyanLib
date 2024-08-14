package fr.aeldit.cyanlib.lib.config;

import java.util.Map;

public interface ICyanLibConfig
{
    /**
     * This function's purpose is to store and return the mod's default translations. We don't use the traditional way,
     * because with the traditional way requires the client to have the translations loaded (either by a resource pack
     * or by the mod). By storing the translations in json files in the server's config and here in the code,
     * we can guarantee that the client users won't need to have the translations loaded.
     * <p>
     * This is used because the mod being potentially server-side, the clients playing on the server wouldn't have
     * the translations
     *
     * @return The HashMap containing all the translations for your mod
     * @apiNote To add your translations, you can do as follows :
     *
     * <pre>{@code
     *      import static java.util.Map.entry;
     *      ...
     *      @Override
     *      public Map<String, String> getDefaultTranslations()
     *      {
     *          return Map.ofEntries(
     *              entry("path.to.translation.one", "Translation 1"),
     *              entry("path.to.translation.two", "Translation 2"),
     *              ...
     *          );
     *      }
     * }</pre>
     */
    Map<String, String> getDefaultTranslations();
}
