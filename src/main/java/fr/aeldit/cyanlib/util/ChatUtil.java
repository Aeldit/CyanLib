package fr.aeldit.cyanlib.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.NotNull;

/**
 * @since 0.0.1
 */
public class ChatUtil
{
    /**
     * Sends a message to the player but with the possibility of using the traductions (which will require
     * the player to have the mod or the resource pack with translations installed), or use the default without needing
     * the player to have them installed
     *
     * @param player the player to whom the message will be sent
     * @param msg the default translation
     * @param args the arguments to pass to the message (can be null)
     * @param tradPath the traduction path (requires the player to have the mod/resource pack)
     * @param actionBar if the message will be sent to the action bar
     * @param useTranslations if the translations will be used (if true, tradPath is used | if false, msg is used)
     */
    public static void sendPlayerMessage(@NotNull ServerPlayerEntity player, String msg, Object args, String tradPath, boolean actionBar, boolean useTranslations)
    {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER)
        {
            if (useTranslations)
            {
                player.sendMessage(new TranslatableText(tradPath, args), actionBar);
            } else
            {
                player.sendMessage(new TranslatableText(msg, args), actionBar);
            }
        } else
        {
            player.sendMessage(new TranslatableText(tradPath, args), actionBar);
        }
    }
}
