package fr.aeldit.cyanlib.core.config;

import fr.aeldit.cyanlib.lib.config.BooleanOption;
import fr.aeldit.cyanlib.lib.config.CyanLibConfig;
import fr.aeldit.cyanlib.lib.config.IntegerOption;
import fr.aeldit.cyanlib.lib.utils.RULES;

import java.util.HashMap;
import java.util.Map;

public class CoreCyanLibConfig implements CyanLibConfig
{
    public static final BooleanOption MSG_TO_ACTION_BAR = new BooleanOption("msgToActionBar", true);
    public static final IntegerOption MIN_OP_LVL_EDIT_CONFIG = new IntegerOption("minOpLvlEditConfig", 4,
            RULES.OP_LEVELS
    );

    @Override
    public Map<String, String> getDefaultTranslations()
    {
        Map<String, String> translations = new HashMap<>();

        // ERRORS
        translations.put("cyanlib.error.optionNotFound", "§cThis option does not exist or you tried to set it to the " +
                "wrong type");
        translations.put("cyanlib.error.optionNotFoundOrWrongInt", "§cThis option does not exist or you tried to set " +
                "it to an invalid number");
        translations.put("cyanlib.error.incorrectInteger", "§cThe number you entered is invalid for this option");

        // MESSAGES
        translations.put("cyanlib.msg.currentValue", "§7Current value , %s");
        translations.put("cyanlib.msg.setValue", "§7Set value to , %s  %s  %s  %s  %s");
        translations.put("cyanlib.msg.translationsReloaded", "§3Custom translations have been reloaded");

        // SETS
        translations.put("cyanlib.msg.set.msgToActionBar", "§3Toggled messages to action bar %s");
        translations.put("cyanlib.msg.set.minOpLvlEditConfig", "§3The minimum OP level required to edit the config is" +
                " now %s");

        // SEPARATIONS
        translations.put("cyanlib.msg.dashSeparation", "§6------------------------------------");

        // CONFIG
        translations.put("cyanlib.msg.getDesc.msgToActionBar", "§3The §dmsgToActionBar §3option defines whether the " +
                "messages will be sent to the action bar or not");
        translations.put("cyanlib.msg.getDesc.minOpLvlEditConfig", "§3The §dminOpLevelExeEditConfig §3option defines " +
                "the OP level required to edit the config");

        // GET_CFG
        translations.put("cyanlib.msg.getCfg.header", "§6CyanLib - OPTIONS\n");
        translations.put("cyanlib.msg.getCfg.msgToActionBar", "§6- §3Messages to action bar : %s");
        translations.put("cyanlib.msg.getCfg.minOpLvlEditConfig", "§6- §3Minimum OP level required to edit the config" +
                " : %s");

        return translations;
    }
}
