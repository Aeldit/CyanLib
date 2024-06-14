package fr.aeldit.cyanlib.lib.config;

import fr.aeldit.cyanlib.lib.utils.RULES;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class IntegerOption implements IOption<Integer>
{
    private final String optionName;
    private final int defaultValue;
    private final RULES rule;
    private final int min, max;
    private int value;

    /**
     * Use when no rules are given (makes this integer option store just the value)
     */
    public IntegerOption(String optionName, int value)
    {
        this(optionName, value, RULES.NONE, 0, 4);
    }

    /**
     * Used with :
     * <ul>
     *     <li>{@link RULES#POSITIVE_VALUE}</li>
     *     <li>{@link RULES#NEGATIVE_VALUE}</li>
     *     <li>{@link RULES#OP_LEVELS}</li>
     * </ul>
     */
    public IntegerOption(String optionName, int value, @NotNull RULES rule)
    {
        this.optionName = optionName;

        if (rule.equals(RULES.POSITIVE_VALUE))
        {
            this.min = 0;
            this.max = 512;
        }
        else if (rule.equals(RULES.NEGATIVE_VALUE))
        {
            this.min = -512;
            this.max = 0;
        }
        else if (rule.equals(RULES.OP_LEVELS))
        {
            this.min = 0;
            this.max = 4;
        }
        else
        {
            this.min = 0;
            this.max = 4;
        }
        this.rule = rule;
        this.defaultValue = value;
        setValue(value);
    }

    /**
     * Used with :
     * <ul>
     *     <li>{@link RULES#MAX_VALUE}</li>
     *     <li>{@link RULES#MIN_VALUE}</li>
     * </ul>
     */
    public IntegerOption(String optionName, int value, @NotNull RULES rule, int minOrMax)
    {
        this(optionName, value, rule,
                rule.equals(RULES.MIN_VALUE) ? minOrMax : -512, rule.equals(RULES.MIN_VALUE) ? 512 : minOrMax
        );
    }

    /**
     * Used with :
     * <ul>
     *     <li>{@link RULES#RANGE}</li>
     * </ul>
     * <p>
     * and called by other constructors of this class
     */
    public IntegerOption(String optionName, int value, RULES rule, int min, int max)
    {
        this.optionName = optionName;
        this.min = min;
        this.max = max;
        this.rule = rule;
        this.defaultValue = value;
        this.value = value;
        setValue(value);
    }

    @Override
    public String getOptionName()
    {
        return optionName;
    }

    @Override
    public Integer getDefaultValue()
    {
        return defaultValue;
    }

    @Override
    public RULES getRule()
    {
        return rule;
    }

    @Override
    public Integer getValue()
    {
        return value;
    }

    @Override
    public boolean setValue(Object valueArg)
    {
        if (valueArg instanceof Integer)
        {
            int value = (Integer) valueArg;
            if (rule.equals(RULES.NONE)
                    || (rule.equals(RULES.POSITIVE_VALUE) && value > 0)
                    || (rule.equals(RULES.NEGATIVE_VALUE) && value < 0)
                    || (rule.equals(RULES.OP_LEVELS) && value >= 0 && value <= 4)
                    || (rule.equals(RULES.MAX_VALUE) && value <= max)
                    || (rule.equals(RULES.MIN_VALUE) && value >= min)
                    || (rule.equals(RULES.RANGE) && value >= min && value <= max)
            )
            {
                this.value = value;
                return true;
            }
        }
        return false;
    }

    @Override
    public void reset()
    {
        this.value = defaultValue;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public SimpleOption<Integer> asConfigOption()
    {
        return new SimpleOption<>("cyanlib.config.option.%s".formatted(optionName),
                SimpleOption.emptyTooltip(),
                (optionText, value) -> Text.of("%s: %s".formatted(optionText.getString(), value)),
                new SimpleOption.ValidatingIntSliderCallbacks(min, max),
                getValue(),
                this::setValue
        );
    }
}
