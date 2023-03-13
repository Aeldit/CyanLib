package fr.aeldit.cyanlib.util;

import net.fabricmc.loader.api.FabricLoader;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Properties;

public class LanguageUtils
{
    public static LinkedHashMap<String, LinkedHashMap<String, String>> translations = new LinkedHashMap<>();
    public static final String DESC = "desc.";
    public static final String GETCFG = "getCfg.";
    public static final String SET = "set.";
    public static final String ERROR = "error.";

    public static void loadLanguage(String modid, LinkedHashMap<String, String> defaultTranslations)
    {
        if (!Files.exists(FabricLoader.getInstance().getConfigDir().resolve(modid)))
        {
            try
            {
                Files.createDirectory(FabricLoader.getInstance().getConfigDir().resolve(modid));
            } catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
        Path languagePath = FabricLoader.getInstance().getConfigDir().resolve(modid + "/translations.properties");
        if (!Files.exists(languagePath))
        {
            try
            {
                Files.createFile(languagePath);
                Properties properties = new Properties();
                properties.putAll(defaultTranslations);
                properties.store(new FileOutputStream(languagePath.toFile()), null);
            } catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
        try
        {
            Properties properties = new Properties();
            properties.load(new FileInputStream(languagePath.toFile()));
            for (String key : properties.stringPropertyNames())
            {
                translations.get(modid).put(key, properties.getProperty(key));
            }
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static String getTranslation(String modid, String key)
    {
        return translations.get(modid).get(key) != null ? translations.get(modid).get(key) : "null";
    }
}
