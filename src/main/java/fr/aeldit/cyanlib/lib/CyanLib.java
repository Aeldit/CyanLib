package fr.aeldit.cyanlib.lib;

import fr.aeldit.cyanlib.lib.config.CyanLibOptionsStorage;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

// TODO -> Add multi world support
public class CyanLib
{
    private final String modid;
    private final CyanLibOptionsStorage optionsStorage;
    private final CyanLibLanguageUtils languageUtils;
    // This Map stores the CyanLib instance of each mod using this library, the key in the map being the modid
    // of the mod
    public static final HashMap<String, CyanLib> CONFIG_CLASS_INSTANCES = new HashMap<>();

    /**
     * Main class of this library
     *
     * @param modid          The modid of your mod
     * @param optionsStorage The {@link CyanLibOptionsStorage} object
     * @param languageUtils  The {@link CyanLibLanguageUtils} object
     */
    @Contract(pure = true)
    public CyanLib(String modid, CyanLibOptionsStorage optionsStorage, CyanLibLanguageUtils languageUtils)
    {
        this.modid = modid;
        this.optionsStorage = optionsStorage;
        this.languageUtils = languageUtils;
    }

    public void init(String modid, @NotNull CyanLibOptionsStorage optionsStorageInstance)
    {
        CONFIG_CLASS_INSTANCES.put(modid, this);
        optionsStorageInstance.init();
        languageUtils.loadCustomLanguage(optionsStorage.getConfigClass().getDefaultTranslations());
    }

    public CyanLibOptionsStorage getOptionsStorage()
    {
        return optionsStorage;
    }

    public CyanLibLanguageUtils getLanguageUtils()
    {
        return languageUtils;
    }

    /**
     * Returns whether the player has the required OP level or not, and sends a message if this condition is
     * {@code false}
     *
     * @param player     the player
     * @param permission the OP level ({@code 0 <= permission <= 4})
     */
    public boolean hasPermission(@NotNull ServerPlayerEntity player, int permission)
    {
        if (!player.hasPermissionLevel(permission))
        {
            languageUtils.sendPlayerMessageMod(player, "cyanlib", "cyanlib.msg.notOp");
            return false;
        }
        return true;
    }

    /**
     * Returns whether the player the option is allowed or not, and sends a message if this condition is {@code false}
     *
     * @param player         the player
     * @param option         the option you want to test
     * @param translationKey the path to the translation (in the method, the translations path is {@code "MODID.message
     *                       .OPTION"},
     *                       where {@code MODID} is the modid of your mod and {@code OPTION} is the {@code
     *                       translationKey})
     */
    @SuppressWarnings("unused")
    public boolean isOptionEnabled(ServerPlayerEntity player, boolean option, String translationKey)
    {
        if (!option)
        {
            languageUtils.sendPlayerMessage(player, "%s.error.%s".formatted(modid, translationKey));
        }
        return option;
    }
}
