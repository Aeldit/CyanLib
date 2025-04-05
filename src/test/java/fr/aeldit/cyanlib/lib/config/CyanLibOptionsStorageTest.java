package fr.aeldit.cyanlib.lib.config;

import fr.aeldit.cyanlib.lib.utils.RULES;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings("UnnecessaryLocalVariable")
class CyanLibOptionsStorageTest
{
    private static final CyanLibOptionsStorage getOptions = new CyanLibOptionsStorage(
            "get", new GetTestingCyanLibConfigImpl()
    );
    private static final CyanLibOptionsStorage setOptions = new CyanLibOptionsStorage(
            "set", new SetTestingCyanLibConfigImpl()
    );
    private static final Map<String, Object> initialValues = setOptions.getOptionsNames().stream().collect(
            Collectors.toMap(opt -> opt, setOptions::getOptionValue, (a, b) -> b)
    );

    @Test
    void getOptionsNames()
    {
        Assertions.assertEquals(
                List.of("true", "op", "maxVal", "minVal", "posVal", "negVal", "range", "false"),
                getOptions.getOptionsNames()
        );
    }

    @Test
    void getOptionValueTrue()
    {
        Assertions.assertEquals(true, getOptions.getOptionValue("true"));
    }

    @Test
    void getOptionValueOp()
    {
        Assertions.assertEquals(4, getOptions.getOptionValue("op"));
    }

    @Test
    void getOptionValueMaxVal()
    {
        Assertions.assertEquals(28, getOptions.getOptionValue("maxVal"));
    }

    @Test
    void getOptionValueMinVal()
    {
        Assertions.assertEquals(90, getOptions.getOptionValue("minVal"));
    }

    @Test
    void getOptionValuePosVal()
    {
        Assertions.assertEquals(0, getOptions.getOptionValue("posVal"));
    }

    @Test
    void getOptionValueNegVal()
    {
        Assertions.assertEquals(-50, getOptions.getOptionValue("negVal"));
    }

    @Test
    void getOptionValueRange()
    {
        Assertions.assertEquals(125, getOptions.getOptionValue("range"));
    }

    @Test
    void getOptionValueFalse()
    {
        Assertions.assertEquals(false, getOptions.getOptionValue("false"));
    }

    @Test
    void setOptionBoolean()
    {
        String option = "true";
        boolean setValue = true;
        boolean expectedValue = (Boolean) setOptions.getOptionValue(option);

        Assertions.assertTrue(setOptions.setOption(option, setValue, false));
        Assertions.assertEquals(expectedValue, setOptions.getOptionValue(option));
    }

    @Test
    void setOptionIntOpTooHigh()
    {
        String option = "op";
        int setValue = 6;
        int expectedValue = (Integer) setOptions.getOptionValue(option);

        Assertions.assertFalse(setOptions.setOption(option, setValue, false));
        Assertions.assertEquals(expectedValue, setOptions.getOptionValue(option));
    }

    @Test
    void setOptionIntOpOk()
    {
        String option = "op";
        int setValue = 2;
        int expectedValue = setValue;

        Assertions.assertTrue(setOptions.setOption(option, setValue, false));
        Assertions.assertEquals(expectedValue, setOptions.getOptionValue(option));
    }

    @Test
    void setOptionIntMaxValTooBig()
    {
        String option = "maxVal";
        int setValue = 58;
        int expectedValue = (Integer) setOptions.getOptionValue(option);

        Assertions.assertFalse(setOptions.setOption(option, setValue, false));
        Assertions.assertEquals(expectedValue, setOptions.getOptionValue(option));
    }

    @Test
    void setOptionIntMaxValOk()
    {
        String option = "maxVal";
        int setValue = 50;
        int expectedValue = setValue;

        Assertions.assertTrue(setOptions.setOption(option, setValue, false));
        Assertions.assertEquals(expectedValue, setOptions.getOptionValue(option));
    }

    @Test
    void setOptionIntMinValTooSmall()
    {
        String option = "minVal";
        int setValue = -10;
        int expectedValue = (Integer) setOptions.getOptionValue(option);

        Assertions.assertFalse(setOptions.setOption(option, setValue, false));
        Assertions.assertEquals(expectedValue, setOptions.getOptionValue(option));
    }

    @Test
    void setOptionIntMinValOk()
    {
        String option = "minVal";
        int setValue = -7;
        int expectedValue = setValue;

        Assertions.assertTrue(setOptions.setOption(option, setValue, false));
        Assertions.assertEquals(expectedValue, setOptions.getOptionValue(option));
    }

    @Test
    void setOptionIntPosValNeg()
    {
        String option = "posVal";
        int setValue = -1;
        int expectedValue = (Integer) setOptions.getOptionValue(option);

        Assertions.assertFalse(setOptions.setOption(option, setValue, false));
        Assertions.assertEquals(expectedValue, setOptions.getOptionValue(option));
    }

    @Test
    void setOptionIntPosValOk()
    {
        String option = "posVal";
        int setValue = 5;
        int expectedValue = setValue;

        Assertions.assertTrue(setOptions.setOption(option, setValue, false));
        Assertions.assertEquals(expectedValue, setOptions.getOptionValue(option));
    }

    @Test
    void setOptionIntNegValPos()
    {
        String option = "negVal";
        int setValue = 20;
        int expectedValue = (Integer) setOptions.getOptionValue(option);

        Assertions.assertFalse(setOptions.setOption(option, setValue, false));
        Assertions.assertEquals(expectedValue, setOptions.getOptionValue(option));
    }

    @Test
    void setOptionIntNegValOk()
    {
        String option = "negVal";
        int setValue = -3;
        int expectedValue = setValue;

        Assertions.assertTrue(setOptions.setOption(option, setValue, false));
        Assertions.assertEquals(expectedValue, setOptions.getOptionValue(option));
    }

    @Test
    void setOptionIntRangeTooHigh()
    {
        String option = "range";
        int setValue = 256;
        int expectedValue = (Integer) setOptions.getOptionValue(option);

        Assertions.assertFalse(setOptions.setOption(option, setValue, false));
        Assertions.assertEquals(expectedValue, setOptions.getOptionValue(option));
    }

    @Test
    void setOptionIntRangeOkTop()
    {
        String option = "range";
        int setValue = 127;
        int expectedValue = setValue;

        Assertions.assertTrue(setOptions.setOption(option, setValue, false));
        Assertions.assertEquals(expectedValue, setOptions.getOptionValue(option));
    }

    @Test
    void setOptionIntRangeTooLow()
    {
        String option = "range";
        int setValue = -200;
        int expectedValue = (Integer) setOptions.getOptionValue(option);

        Assertions.assertFalse(setOptions.setOption(option, setValue, false));
        Assertions.assertEquals(expectedValue, setOptions.getOptionValue(option));
    }

    @Test
    void setOptionIntRangeOkBottom()
    {
        String option = "range";
        int setValue = -128;
        int expectedValue = setValue;

        Assertions.assertTrue(setOptions.setOption(option, setValue, false));
        Assertions.assertEquals(expectedValue, setOptions.getOptionValue(option));
    }

    @Test
    void resetOptions()
    {
        setOptions.resetOptions();
        for (Map.Entry<String, Object> e : initialValues.entrySet())
        {
            Assertions.assertEquals(e.getValue(), setOptions.getOptionValue(e.getKey()));
        }
    }

    @Test
    void optionExistsTrue()
    {
        Assertions.assertTrue(getOptions.optionExists("true"));
    }

    @Test
    void optionExistsFalse()
    {
        Assertions.assertTrue(getOptions.optionExists("false"));
    }

    @Test
    void optionExistsOp()
    {
        Assertions.assertTrue(getOptions.optionExists("op"));
    }

    @Test
    void optionExistsMaxVal()
    {
        Assertions.assertTrue(getOptions.optionExists("maxVal"));
    }

    @Test
    void optionExistsMinVal()
    {
        Assertions.assertTrue(getOptions.optionExists("minVal"));
    }

    @Test
    void optionExistsPosVal()
    {
        Assertions.assertTrue(getOptions.optionExists("posVal"));
    }

    @Test
    void optionExistsNegVal()
    {
        Assertions.assertTrue(getOptions.optionExists("negVal"));
    }

    @Test
    void optionExistsRange()
    {
        Assertions.assertTrue(getOptions.optionExists("range"));
    }

    @Test
    void optionDoesNotExist()
    {
        Assertions.assertFalse(getOptions.optionExists("notAnOption"));
    }

    @Test
    void hasRuleTrue()
    {
        Assertions.assertTrue(getOptions.hasRule("true", RULES.NONE));
    }

    @Test
    void hasRuleFalse()
    {
        Assertions.assertTrue(getOptions.hasRule("false", RULES.NONE));
    }

    @Test
    void hasRuleOp()
    {
        Assertions.assertTrue(getOptions.hasRule("op", RULES.OP_LEVELS));
    }

    @Test
    void hasRuleMaxVal()
    {
        Assertions.assertTrue(getOptions.hasRule("maxVal", RULES.MAX_VALUE));
    }

    @Test
    void hasRuleMinVal()
    {
        Assertions.assertTrue(getOptions.hasRule("minVal", RULES.MIN_VALUE));
    }

    @Test
    void hasRulePosVal()
    {
        Assertions.assertTrue(getOptions.hasRule("posVal", RULES.POSITIVE_VALUE));
    }

    @Test
    void hasRuleNegVal()
    {
        Assertions.assertTrue(getOptions.hasRule("negVal", RULES.NEGATIVE_VALUE));
    }

    @Test
    void hasRuleRange()
    {
        Assertions.assertTrue(getOptions.hasRule("range", RULES.RANGE));
    }
}
