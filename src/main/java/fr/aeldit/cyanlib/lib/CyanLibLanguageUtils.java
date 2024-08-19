package fr.aeldit.cyanlib.lib;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static fr.aeldit.cyanlib.core.config.CyanLibConfigImpl.MSG_TO_ACTION_BAR;

@SuppressWarnings("unused")
public class CyanLibLanguageUtils
{
    // Map<modKey, Map<translationKey, translation>>
    // Contains for keys each mod that implements this Library using the CyanLibLanguageUtils,
    // and for values the translations of each mod
    private static final ConcurrentHashMap<String, Map<String, String>> modsTranslations = new ConcurrentHashMap<>();
    private final String modid;

    @Contract(pure = true)
    public CyanLibLanguageUtils(String modid)
    {
        this.modid = modid;
    }

    public void loadCustomLanguage(Map<String, String> defaultTranslations)
    {
        Path customLangPath =
                FabricLoader.getInstance().getConfigDir().resolve(Path.of("%s/custom_lang.json".formatted(modid)));

        if (!Files.exists(customLangPath))
        {
            modsTranslations.put(modid, defaultTranslations);
        }
        else
        {
            try
            {
                Gson gsonReader = new Gson();
                Reader reader = Files.newBufferedReader(customLangPath);
                TypeToken<HashMap<String, String>> mapType = new TypeToken<>()
                {
                };
                modsTranslations.put(modid, gsonReader.fromJson(reader, mapType));
                reader.close();
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }

            if (!modsTranslations.containsKey(modid) || modsTranslations.get(modid).isEmpty())
            {
                modsTranslations.put(modid, defaultTranslations);
            }
            else
            {
                // If there are missing translations in the provided one, we add them from the default translations
                for (String key : defaultTranslations.keySet())
                {
                    if (!modsTranslations.get(modid).containsKey(key))
                    {
                        modsTranslations.get(modid).put(key, defaultTranslations.get(key));
                    }
                }
            }
        }
    }

    private String getTranslation(String translationKey)
    {
        return getTranslation(modid, translationKey);
    }

    private String getTranslation(String modKey, String translationKey)
    {
        if (!modsTranslations.containsKey(modKey) || !modsTranslations.get(modKey).containsKey(translationKey))
        {
            return "The translation key '%s' doesn't exist for the mod %s".formatted(translationKey, modKey);
        }
        return modsTranslations.get(modKey).get(translationKey);
    }

    /**
     * Sends a message to the player using the custom language if it is initialized, using the default translations
     * otherwise
     *
     * @param player          The player to whom the message will be sent
     * @param modKey          The modid of the mod of which we want the translation
     * @param translationPath The translation key (ex: "cyanlib.error.notOp")
     * @param args            The arguments to pass to the message (can be omitted). (You can put more than 1 arg)
     */
    public void sendPlayerMessageMod(
            @NotNull ServerPlayerEntity player, String modKey, String translationPath, Object... args
    )
    {
        player.sendMessage(
                Text.translatable(getTranslation(modKey, translationPath), args),
                MSG_TO_ACTION_BAR.getValue()
        );
    }

    /**
     * @see #sendPlayerMessageMod(ServerPlayerEntity, String, String, Object...)
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
     * @param modKey          The modid of the mod of which we want the translation
     * @param translationPath the translation key
     * @param toActionBar     whether the message will be sent in the action bar or not
     * @param args            the arguments to pass to the message (can be null). (You can put more than 1 arg)
     */
    public void sendPlayerMessageActionBarMod(
            @NotNull ServerPlayerEntity player, String modKey, String translationPath, boolean toActionBar,
            Object... args
    )
    {
        player.sendMessage(Text.translatable(getTranslation(modKey, translationPath), args), toActionBar);
    }

    /**
     * @see #sendPlayerMessageActionBarMod(ServerPlayerEntity, String, String, boolean, Object...)
     */
    public void sendPlayerMessageActionBar(
            @NotNull ServerPlayerEntity player, String translationPath, boolean toActionBar, Object... args
    )
    {
        player.sendMessage(Text.translatable(getTranslation(translationPath), args), toActionBar);
    }
}
