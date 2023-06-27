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
 * in the repo of this mod (https://github.com/Aeldit/CyanSetHome)
 */

package fr.aeldit.cyanlib.core.commands.arguments;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static fr.aeldit.cyanlib.core.utils.Utils.LibConfig;

public final class ArgumentSuggestion
{
    /**
     * Called for the command {@code /cyansh config <optionName>}
     *
     * @return a suggestion with the available options
     */
    public static CompletableFuture<Suggestions> getOptions(@NotNull SuggestionsBuilder builder)
    {
        return CommandSource.suggestMatching(LibConfig.getOptions(), builder);
    }

    /**
     * Called for the command {@code /cyansh config optionName [integer]}
     *
     * @return a suggestion with all the available integers for the configurations
     */
    public static CompletableFuture<Suggestions> getOPLevels(@NotNull SuggestionsBuilder builder)
    {
        List<String> ints = new ArrayList<>();
        ints.add("0");
        ints.add("1");
        ints.add("2");
        ints.add("3");
        ints.add("4");

        return CommandSource.suggestMatching(ints, builder);
    }
}
