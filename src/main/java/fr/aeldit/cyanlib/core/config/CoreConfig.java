package fr.aeldit.cyanlib.core.config;

import fr.aeldit.cyanlib.lib.config.BooleanOption;
import fr.aeldit.cyanlib.lib.config.IntegerOption;
import fr.aeldit.cyanlib.lib.utils.RULES;

public class CoreConfig
{
    public static final BooleanOption USE_CUSTOM_TRANSLATIONS = new BooleanOption("useCustomTranslations", false,
            RULES.LOAD_CUSTOM_TRANSLATIONS
    );
    public static final BooleanOption MSG_TO_ACTION_BAR = new BooleanOption("msgToActionBar", true);
    public static final IntegerOption MIN_OP_LVL_EDIT_CONFIG = new IntegerOption("minOpLvlEditConfig", 4,
            RULES.OP_LEVELS
    );
}
