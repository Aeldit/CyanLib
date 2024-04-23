package fr.aeldit.cyanlib.lib.config;

import fr.aeldit.cyanlib.lib.utils.RULES;
import net.minecraft.client.option.SimpleOption;
import org.jetbrains.annotations.Contract;

public class BooleanOption implements IOption<Boolean>
{
    private final String optionName;
    private final boolean defaultValue;
    private boolean value;

    @Contract(pure = true)
    public BooleanOption(String optionName, boolean value)
    {
        this.optionName = optionName;
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
        return RULES.NONE;
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

    @Override
    public SimpleOption<Boolean> asConfigOption()
    {
        return SimpleOption.ofBoolean("cyanlib.config.option.%s".formatted(optionName),
                getValue(),
                this::setValue
        );
    }
}