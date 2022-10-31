package fr.aeldit.cyanlib.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

/**
 * @since 0.0.1
 */
public class ChatUtil
{
    public static void sendPlayerMessage(@NotNull ServerPlayerEntity player, String msg, Object args, String trad_path, boolean actionbar, boolean use_one_language)
    {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER)
        {
            if (use_one_language)
            {
                player.sendMessage(Text.translatable(msg, args), actionbar);
            } else
            {
                player.sendMessage(Text.translatable(trad_path, args), actionbar);
            }
        } else
        {
            player.sendMessage(Text.translatable(trad_path, args), actionbar);
        }
    }
}
