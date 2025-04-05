package fr.aeldit.cyanlib.lib.config;

import fr.aeldit.cyanlib.lib.utils.RULES;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class GetTestingCyanLibConfigImpl implements ICyanLibConfig
{
    public static final BooleanOption TRUE_OPT = new BooleanOption("true", true);
    public static final IntegerOption OP_OPT = new IntegerOption("op", 4, RULES.OP_LEVELS);
    public static final IntegerOption MAX_VAL = new IntegerOption("maxVal", 28, RULES.MAX_VALUE, 50);
    public static final IntegerOption MIN_VAL = new IntegerOption("minVal", 90, RULES.MIN_VALUE, -7);
    public static final IntegerOption POS_VAL = new IntegerOption("posVal", 0, RULES.POSITIVE_VALUE);
    public static final IntegerOption NEG_VAL = new IntegerOption("negVal", -50, RULES.NEGATIVE_VALUE);
    public static final IntegerOption RANGE_OPT = new IntegerOption("range", 125, RULES.RANGE, -128, 127);
    public static final BooleanOption FALSE_OPT = new BooleanOption("false", false);

    @Override
    public Map<String, String> getDefaultTranslations()
    {
        return new HashMap<>(0);
    }
}
