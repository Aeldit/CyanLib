package fr.aeldit.cyanlib.core.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import fr.aeldit.cyanlib.lib.gui.CyanLibConfigScreen;

import static fr.aeldit.cyanlib.core.CyanLibCore.CYANLIB_MODID;

public class ModMenuApiImpl implements ModMenuApi
{
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory()
    {
        return parent -> new CyanLibConfigScreen(null, parent, CYANLIB_MODID);
    }
}
