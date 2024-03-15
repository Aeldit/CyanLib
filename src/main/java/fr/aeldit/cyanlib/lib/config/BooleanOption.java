package fr.aeldit.cyanlib.lib.config;

import fr.aeldit.cyanlib.lib.utils.RULES;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.option.SimpleOption;

public class BooleanOption implements Option<Boolean>
{
    private final String optionName;
    private final boolean defaultValue;
    private final RULES rule;
    private boolean value;

    public BooleanOption(String optionName, boolean value)
    {
        this(optionName, value, RULES.NONE);
    }

    public BooleanOption(String optionName, boolean value, RULES rule)
    {
        this.optionName = optionName;
        this.rule = rule;
        this.defaultValue = value;
        this.value = value;
    }

    @Override
    public String getOptionName()
    {
        return optionName;
    }

    @Override
    public Boolean getDefaultValue()
    {
        return defaultValue;
    }

    @Override
    public RULES getRule()
    {
        return rule;
    }

    @Override
    public Boolean getValue()
    {
        return value;
    }

    @Override
    public boolean setValue(Object value)
    {
        if (value instanceof Boolean)
        {
            this.value = (Boolean) value;
            return true;
        }
        return false;
    }

    @Override
    public void reset()
    {
        this.value = defaultValue;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public SimpleOption<Boolean> asConfigOption()
    {
        return SimpleOption.ofBoolean("cyanlib.config.option.%s".formatted(optionName),
                getValue(),
                this::setValue
        );
    }
}