/*
 * Copyright (c) 2023  -  Made by Aeldit
 *
 *              GNU LESSER GENERAL PUBLIC LICENSE
 *                  Version 3, 29 June 2007
 *
 *  Copyright (C) 2007 Free Software Foundation, Inc. <https://fsf.org/>
 *  Everyone is permitted to copy and distribute verbatim copies
 *  of this license document, but changing it is not allowed.
 *
 *
 * This version of the GNU Lesser General Public License incorporates
 * the terms and conditions of version 3 of the GNU General Public
 * License, supplemented by the additional permissions listed in the LICENSE.txt file
 * in the repo of this mod (https://github.com/Aeldit/CyanLib)
 */

package fr.aeldit.cyanlib.lib.commands.arguments;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import fr.aeldit.cyanlib.lib.CyanLibConfig;
import net.minecraft.command.CommandSource;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public final class ArgumentSuggestion
{
    /**
     * Called for the command {@code /modid config <optionName>}
     *
     * @return a suggestion with the available options
     */
    public static CompletableFuture<Suggestions> getOptions(@NotNull SuggestionsBuilder builder, @NotNull CyanLibConfig libConfig)
    {
        return CommandSource.suggestMatching(libConfig.getOptions(), builder);
    }

    /**
     * Called for the command {@code /modid config <optionName> [integer]}
     *
     * @return a suggestion with all the available OP levels
     */
    public static CompletableFuture<Suggestions> getOPLevels(@NotNull SuggestionsBuilder builder)
    {
        return CommandSource.suggestMatching(Arrays.asList("0", "1", "2", "3", "4"), builder);
    }
}
