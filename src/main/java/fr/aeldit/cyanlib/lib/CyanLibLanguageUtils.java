package fr.aeldit.cyanlib.lib;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static fr.aeldit.cyanlib.core.config.CoreConfig.MSG_TO_ACTION_BAR;
import static fr.aeldit.cyanlib.core.config.CoreConfig.USE_CUSTOM_TRANSLATIONS;

public class CyanLibLanguageUtils
{
    private final String modid;
    private final Map<String, String> translations = new HashMap<>();
    private final Map<String, String> defaultTranslations;

    public CyanLibLanguageUtils(String modid, Map<String, String> defaultTranslations)
    {
        this.modid = modid;
        this.defaultTranslations = defaultTranslations;
    }

    /**
     * Loads the custom translations into this class translations
     */
    public void loadLanguage()
    {
        Path languagePath = FabricLoader.getInstance().getConfigDir().resolve(modid + "/translations.json");

        if (!Files.exists(languagePath))
        {
            translations.putAll(defaultTranslations);
        }
        else
        {
            try
            {
                Gson gsonReader = new Gson();
                Reader reader = Files.newBufferedReader(languagePath);
                TypeToken<Map<String, String>> mapType = new TypeToken<>()
                {
                };
                translations.putAll(gsonReader.fromJson(reader, mapType));
                reader.close();
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Clears the translations map, so it doesn't use too much memory
     */
    public void unload()
    {
        translations.clear();
    }

    /**
     * @param key the key of the translation
     * @return The {@code String "null"} if the translations are {@code null} or if the translations don't contain
     * the {@code key},
     * and the value associated with the {@code key} otherwise
     */
    public String getTranslation(String key)
    {
        if (translations.isEmpty() || translations.get(key) == null)
        {
            return "null";
        }
        return translations.get(key);
    }

    /**
     * Sends a message to the player but with the possibility of using the translation (which will require
     * the player to have the mod or the resource pack with translations installed), or use the default without needing
     * the player to have them installed
     *
     * <ul><h2>Required config options :</h2>
     *      <li>{@code useCustomTranslations}</li>
     * </ul>
     *
     * @param player   the player to whom the message will be sent
     * @param msg      the default translation
     * @param tradPath the translation path (requires the player to have the mod/resource pack)
     * @param args     the arguments to pass to the message (can be null). (You can put more than 1 arg)
     */
    public void sendPlayerMessage(@NotNull ServerPlayerEntity player, String msg, String tradPath, Object... args)
    {
        if (USE_CUSTOM_TRANSLATIONS.getValue())
        {
            player.sendMessage(Text.translatable(msg, args), MSG_TO_ACTION_BAR.getValue());
        }
        else
        {
            player.sendMessage(Text.translatable(tradPath, args), MSG_TO_ACTION_BAR.getValue());
        }
    }

    /**
     * Sends a message to the player but with the possibility of using the translation (which will require
     * the player to have the mod or the resource pack with translations installed), or use the default without needing
     * the player to have them installed.
     * <p>
     * This function forces the message to be or not in the action bar, independently of the {@code MSG_TO_ACTION_BAR}
     * option
     *
     * <ul><h2>Required config options :</h2>
     *      <li>{@code useCustomTranslations}</li>
     * </ul>
     *
     * @param player      the player to whom the message will be sent
     * @param msg         the default translation
     * @param tradPath    the translation path (requires the player to have the mod/resource pack)
     * @param toActionBar whether the message will be sent in the action bar or not
     * @param args        the arguments to pass to the message (can be null). (You can put more than 1 arg)
     */
    public void sendPlayerMessageActionBar(
            @NotNull ServerPlayerEntity player, String msg, String tradPath,
            boolean toActionBar, Object... args
    )
    {
        if (USE_CUSTOM_TRANSLATIONS.getValue())
        {
            player.sendMessage(Text.translatable(msg, args), toActionBar);
        }
        else
        {
            player.sendMessage(Text.translatable(tradPath, args), toActionBar);
        }
    }
}
