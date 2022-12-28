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
