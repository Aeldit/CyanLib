package fr.aeldit.cyanlib.core.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import fr.aeldit.cyanlib.lib.gui.CyanLibModsScreen;

public class ModMenuApiImpl implements ModMenuApi
{
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory()
    {
        return CyanLibModsScreen::new;
    }
}
