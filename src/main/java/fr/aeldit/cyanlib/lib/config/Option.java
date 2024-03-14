package fr.aeldit.cyanlib.lib.config;

import fr.aeldit.cyanlib.lib.utils.RULES;
import net.minecraft.client.option.SimpleOption;

public interface Option<T>
{
    String getOptionName();

    T getDefaultValue();

    RULES getRule();

    T getValue();

    boolean setValue(Object value);

    void reset();

    SimpleOption<?> asConfigOption();
}
