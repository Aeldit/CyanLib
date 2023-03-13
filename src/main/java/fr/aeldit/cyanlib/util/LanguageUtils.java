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
    String MODID;
    LinkedHashMap<String, String> translations = new LinkedHashMap<>();

    public LanguageUtils(String modid)
    {
        this.MODID = modid;
    }

    public void loadLanguage(LinkedHashMap<String, String> defaultTranslations)
    {
        if (!Files.exists(FabricLoader.getInstance().getConfigDir().resolve(this.MODID)))
        {
            try
            {
                Files.createDirectory(FabricLoader.getInstance().getConfigDir().resolve(this.MODID));
            } catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
        Path languagePath = FabricLoader.getInstance().getConfigDir().resolve(this.MODID + "/translations.properties");
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
                translations.put(key, properties.getProperty(key));
            }
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public String getTranslation(String key)
    {
        return translations.get(key) != null ? translations.get(key) : "null";
    }
}
