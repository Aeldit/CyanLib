package fr.aeldit.cyanlib.lib;

import fr.aeldit.cyanlib.lib.config.CyanLibOptionsStorage;
import fr.aeldit.cyanlib.lib.config.ICyanLibConfig;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

import static fr.aeldit.cyanlib.core.CyanLibCore.CYANLIB_MODID;

// TODO -> Add multi world support
public class CyanLib
{
    private final CyanLibOptionsStorage optionsStorage;
    private final CyanLibLanguageUtils languageUtils;
    // This Map stores the CyanLib instance of each mod using this library, the key in the map being the modid
    // of the mod
    public static final HashMap<String, CyanLib> CONFIG_CLASS_INSTANCES = new HashMap<>();

    /**
     * Main class of this library
     *
     * @param modid             The modid of your mod
     * @param cyanLibConfigImpl The {@link ICyanLibConfig} implementation
     */
    public CyanLib(String modid, ICyanLibConfig cyanLibConfigImpl)
    {
        this.optionsStorage = new CyanLibOptionsStorage(modid, cyanLibConfigImpl);
        this.languageUtils = new CyanLibLanguageUtils(modid);

        CONFIG_CLASS_INSTANCES.put(modid, this);
        this.optionsStorage.init();
        languageUtils.loadCustomLanguage(this.optionsStorage.getConfigClass().getDefaultTranslations());
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
            languageUtils.sendPlayerMessageMod(player, CYANLIB_MODID, "error.notOp");
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
            languageUtils.sendPlayerMessage(player, "error.%s".formatted(translationKey));
        }
        return option;
    }
}
