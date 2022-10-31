package fr.aeldit.cyanlib.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class LanguageFilesUtils
{

    /**
     * Create a lang file (ex: en.txt) with the {@code langName},
     * and a folder with the name {@code folderName} if it doesn't exists (this folder is created/located in the config directory of the MC server)
     *
     * @param player         the player
     * @param folderName     the name of the folder, most likely the modid
     * @param langName       the name of the language (ex: en, fr, ...)
     * @param useOneLanguage if the use of one language is used (here it is used when a message is send to the player
     */
    public static void setupLangFile(ServerPlayerEntity player, String folderName, String langName, boolean useOneLanguage)
    {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER)
        {
            if (! Files.exists(FabricLoader.getInstance().getConfigDir().resolve(folderName)))
            {
                try
                {
                    Files.createDirectory(Paths.get(String.valueOf(FabricLoader.getInstance().getConfigDir().resolve(folderName))));
                    ChatUtil.sendPlayerMessage(player,
                            "Folder created : config/" + folderName + "/",
                            folderName,
                            "cyanlib.message.folderCreated",
                            false,
                            useOneLanguage);
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            try
            {
                File langFile = new File(String.valueOf(FabricLoader.getInstance().getConfigDir().resolve(folderName + "\\" + langName + ".properties")));
                if (langFile.createNewFile())
                {
                    ChatUtil.sendPlayerMessage(player,
                            "File created : " + langFile.getName(),
                            langFile.getName(),
                            "cyanlib.message.fileCreated",
                            false,
                            useOneLanguage);
                } else
                {
                    player.sendMessage(Text.translatable("File already exists."), false);
                    ChatUtil.sendPlayerMessage(player,
                            "File already exists.",
                            null,
                            "cyanlib.message.fileAlreadyExists",
                            false,
                            useOneLanguage);
                }
            } catch (IOException e)
            {
                ChatUtil.sendPlayerMessage(player,
                        "Could not create the language file",
                        null,
                        "cyanlib.message.error.createFile",
                        false,
                        useOneLanguage);
                e.printStackTrace();
            }
        } else
        {
            ChatUtil.sendPlayerMessage(player,
                    "This functionnality only works if you are on a server",
                    null,
                    "cyanlib.message.error.notOnServer",
                    false,
                    useOneLanguage);
        }
    }

}
