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

package fr.aeldit.cyanlib.lib.utils;

/**
 * {@link #NONE} is used to indicate that the option has no rule (you should not use it. If you want an option to have no rule, simply don't specify one)
 * <p>
 * {@link #MAX_VALUE} indicates a maximum value
 * <p>
 * {@link #MIN_VALUE} indicates a minimum value
 * <p>
 * {@link #OP_LEVELS} is a particular type of range, which can take integer values between 0 and 4, both included
 * <p>
 * {@link #POSITIVE_VALUE} indicates that the option must be positive and != 0
 * <p>
 * {@link #NEGATIVE_VALUE} indicates that the option must be negative and != 0
 * <p>
 * {@link #RANGE} indicates that the option can be between 2 values, which must both be specified in the option definition
 * <p>
 * {@link #LOAD_CUSTOM_TRANSLATIONS} can be held by only 1 option and indicates the code that when true, the custom translations will be loaded and used
 */
public enum RULES
{
    NONE,
    MAX_VALUE,
    MIN_VALUE,
    OP_LEVELS,
    POSITIVE_VALUE,
    NEGATIVE_VALUE,
    RANGE,
    LOAD_CUSTOM_TRANSLATIONS
}
