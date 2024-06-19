package fr.aeldit.cyanlib.core.config;

import fr.aeldit.cyanlib.lib.config.BooleanOption;
import fr.aeldit.cyanlib.lib.config.ICyanLibConfig;
import fr.aeldit.cyanlib.lib.config.IntegerOption;
import fr.aeldit.cyanlib.lib.utils.RULES;

import java.util.Map;

import static java.util.Map.entry;

public class CyanLibConfigImpl implements ICyanLibConfig
{
    public static final BooleanOption MSG_TO_ACTION_BAR = new BooleanOption("msgToActionBar", true);
    public static final IntegerOption MIN_OP_LVL_EDIT_CONFIG = new IntegerOption("minOpLvlEditConfig", 4,
            RULES.OP_LEVELS
    );

    @Override
    public Map<String, String> getDefaultTranslations()
    {
        return Map.ofEntries(
                // ERRORS
                entry("cyanlib.msg.notOp", "§cYou don't have the required permission to execute this command"),
                entry("cyanlib.error.optionNotFound",
                        "§cThis option does not exist or you tried to set it to the wrong type"
                ),
                entry("cyanlib.error.optionNotFoundOrWrongInt",
                        "§cThis option does not exist or you tried to set it to an invalid number"
                ),
                entry("cyanlib.error.incorrectInteger", "§cThe number you entered is invalid for this option"),
                // MESSAGES
                entry("cyanlib.msg.currentValue", "§7Current value , %s"),
                entry("cyanlib.msg.setValue", "§7Set value to , %s  %s  %s  %s  %s"),
                entry("cyanlib.msg.translationsReloaded", "§3Custom translations have been reloaded"),
                // SETS
                entry("cyanlib.msg.set.msgToActionBar", "§3Toggled messages to action bar %s"),
                entry("cyanlib.msg.set.minOpLvlEditConfig",
                        "§3The minimum OP level required to edit the config is now %s"
                ),
                // SEPARATIONS
                entry("cyanlib.msg.dashSeparation", "§6------------------------------------"),
                // CONFIG
                entry("cyanlib.msg.getDesc.msgToActionBar",
                        "§3The §dmsgToActionBar §3option defines whether the messages will be sent to the action bar " +
                                "or not"
                ),
                entry("cyanlib.msg.getDesc.minOpLvlEditConfig",
                        "§3The §dminOpLevelExeEditConfig §3option defines the OP level required to edit the config"
                ),
                // GET_CFG
                entry("cyanlib.msg.getCfg.header", "§6CyanLib - OPTIONS\n"),
                entry("cyanlib.msg.getCfg.msgToActionBar", "§6- §3Messages to action bar : %s"),
                entry("cyanlib.msg.getCfg.minOpLvlEditConfig",
                        "§6- §3Minimum OP level required to edit the config : %s"
                )
        );
    }
}
