package fr.aeldit.cyanlib.lib.config;

import fr.aeldit.cyanlib.lib.utils.RULES;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.option.SimpleOption;

@SuppressWarnings("unused")
public interface IOption<T>
{
    String getOptionName();

    T getDefaultValue();

    RULES getRule();

    T getValue();

    boolean setValue(Object value);

    void reset();

    @Environment(EnvType.CLIENT)
    SimpleOption<?> asConfigOption();
}
