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

package fr.aeldit.cyanlib.util;

import fr.aeldit.cyanlib.CyanLibCore;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileUtils
{
    /**
     * Removes the empty files
     *
     * @param paths The paths to the files
     */
    public static void removeEmptyFiles(Path @NotNull ... paths)
    {
        for (Path path : paths)
        {
            try
            {
                if (Files.exists(path) && Files.readAllLines(path).size() <= 1)
                {
                    Files.delete(path);
                    CyanLibCore.LOGGER.info("{} Deleted the file {} because it was empty", CyanLibCore.MODNAME, path);
                }
            } catch (IOException e)
            {
                throw new RuntimeException(e);
            }

        }
    }
}
