package fr.aeldit.cyanlib.lib;

import fr.aeldit.cyanlib.lib.config.IntegerOption;

import java.util.concurrent.ConcurrentHashMap;

public abstract class CombatTracking
{
    private static final ConcurrentHashMap<String, Long> lastHurtTime = new ConcurrentHashMap<>();

    public static void addEntry(String playerName, long hurtTime)
    {
        lastHurtTime.put(playerName, hurtTime);
    }

    public static boolean isPlayerInCombat(String playerName, IntegerOption combatTimeoutSeconds)
    {
        if (lastHurtTime.containsKey(playerName))
        {
            return System.currentTimeMillis() - lastHurtTime.get(playerName)
                    < combatTimeoutSeconds.getValue() * 1000;
        }
        return false;
    }

    public static void removePlayerOnPlayerQuit(String playerName)
    {
        lastHurtTime.remove(playerName);
    }
}
