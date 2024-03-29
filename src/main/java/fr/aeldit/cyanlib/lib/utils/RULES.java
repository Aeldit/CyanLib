package fr.aeldit.cyanlib.lib.utils;

/**
 * {@link #NONE} is used to indicate that the option has no rule (you should not use it. If you want an option to
 * have no rule, simply don't specify one)
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
 * {@link #RANGE} indicates that the option can be between 2 values, which must both be specified in the option
 * definition
 */
public enum RULES
{
    NONE,
    MAX_VALUE,
    MIN_VALUE,
    OP_LEVELS,
    POSITIVE_VALUE,
    NEGATIVE_VALUE,
    RANGE
}
