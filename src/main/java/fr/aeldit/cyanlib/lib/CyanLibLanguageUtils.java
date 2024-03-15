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

import static fr.aeldit.cyanlib.core.config.CoreCyanLibConfig.MSG_TO_ACTION_BAR;

public class CyanLibLanguageUtils
{
    private final String modid;
    // Map<modid, Map<translationKey, translation>>
    private static final Map<String, Map<String, String>> translations = new HashMap<>(0);

    public CyanLibLanguageUtils(String modid)
    {
        this.modid = modid;
    }

    public void loadCustomLanguage(Map<String, String> defaultTranslations)
    {
        Path customLangPath = FabricLoader.getInstance().getConfigDir().resolve(modid + "/custom_lang.json");

        if (!Files.exists(customLangPath))
        {
            translations.put(modid, defaultTranslations);
        }
        else
        {
            try
            {
                Gson gsonReader = new Gson();
                Reader reader = Files.newBufferedReader(customLangPath);
                TypeToken<Map<String, String>> mapType = new TypeToken<>()
                {
                };
                translations.put(modid, gsonReader.fromJson(reader, mapType));
                reader.close();
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }

            // If there are missing translations in the provided one, we add them from the default translations
            Map<String, String> currentModTranslations = translations.get(modid);

            for (String key : defaultTranslations.keySet())
            {
                if (!currentModTranslations.containsKey(key))
                {
                    translations.get(modid).put(key, defaultTranslations.get(key));
                }
            }
        }
    }

    private String getTranslation(String translationKey)
    {
        if (translations.containsKey(modid) && translations.get(modid).containsKey(translationKey))
        {
            return translations.get(modid).get(translationKey);
        }
        return "null";
    }

    /**
     * Sends a message to the player using the custom language if it is initialized, using the default translations
     * otherwise
     *
     * @param player          The player to whom the message will be sent
     * @param translationPath The translation key (ex: "cyanlib.error.notOp")
     * @param args            The arguments to pass to the message (can be omitted). (You can put more than 1 arg)
     */
    public void sendPlayerMessage(@NotNull ServerPlayerEntity player, String translationPath, Object... args)
    {
        player.sendMessage(Text.translatable(getTranslation(translationPath), args), MSG_TO_ACTION_BAR.getValue());
    }

    /**
     * Sends a message to the player but with the possibility of using the translation (which will require
     * the player to have the mod or the resource pack with translations installed), or use the default without needing
     * the player to have them installed.
     * <p>
     * This function forces the message to be or not in the action bar, independently of the {@code MSG_TO_ACTION_BAR}
     * option
     *
     * @param player          the player to whom the message will be sent
     * @param translationPath the translation key
     * @param toActionBar     whether the message will be sent in the action bar or not
     * @param args            the arguments to pass to the message (can be null). (You can put more than 1 arg)
     */
    public void sendPlayerMessageActionBar(
            @NotNull ServerPlayerEntity player, String translationPath, boolean toActionBar, Object... args
    )
    {
        player.sendMessage(Text.translatable(getTranslation(translationPath), args), toActionBar);
    }
}
