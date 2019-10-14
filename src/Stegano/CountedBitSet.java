/*
 * CountedBitSet.java
 * 
 * Created on July 26, 2004, 11:04 PM
 *
 * Part of DCSteg steganographic/watermarking utility.
 * Distributed under GPL v2 license.
 */

package Stegano;

import java.util.BitSet;

/**
 * An extension of BitSet - we also stuff like counting of the bits, remembering
 * which of the set bits is the set. Methods like get/set/clear have the
 * same semantics like in java.util.BitSet.
 *
 * @author Ondrej Mikle
 * @version 0.1
 */
@SuppressWarnings("serial")
public class CountedBitSet extends BitSet
{

    /**
     * Index of the highest set bit, -1 at the beginning
     */
    private int bitsUsed = -1;

    /** Creates an empty bit set */
    public CountedBitSet()
    {
    }

    /**
     * Creates bit set by copying values from array ar (bit is set if the value
     * at the matching index is non-zero).
     * 
     * @param ar array to "copy"
     */
    public CountedBitSet(byte[] ar)
    {
        for (int i = 0; i < ar.length; i++)
        {
            set(i, (ar[i] != 0));
        }
    }

    /**
     * Same as in {@link java.util.BitSet#set}
     */
    public void set(int param)
    {
        super.set(param);
        if (param > bitsUsed)
            bitsUsed = param;
    }

    /**
     * Same as in {@link java.util.BitSet#set}
     */
    public void clear(int param)
    {
        super.clear(param);
        if (param > bitsUsed)
            bitsUsed = param;
    }

    /**
     * Same as in {@link java.util.BitSet#clear}
     */
    public void clear(int param, int param1)
    {
        super.clear(param, param1);
        if (param1 > bitsUsed)
            bitsUsed = param1;
    }

    /**
     * Same as in {@link java.util.BitSet#set}
     */
    public void set(int param, boolean param1)
    {
        super.set(param, param1);
        if (param > bitsUsed)
            bitsUsed = param;
    }

    /**
     * Same as in {@link java.util.BitSet#set}
     */
    public void set(int param, int param1)
    {
        super.set(param, param1);
        if (param1 > bitsUsed)
            bitsUsed = param1;
    }

    /**
     * Same as in {@link java.util.BitSet#set}
     */
    public void set(int param, int param1, boolean param2)
    {
        super.set(param, param1, param2);
        if (param1 > bitsUsed)
            bitsUsed = param1;
    }

    /**
     * Adds a bit at the next bit position (i.e. one order higher than the last
     * bit).
     * 
     * @param bit value to set for the new bit
     */
    public void add(boolean bit)
    {
        bitsUsed++;
        set(bitsUsed, bit);
    }

    /**
     * Appends the given set to this one, its bits become of higher order.
     * 
     * @param b bit set to append
     */
    public void append(CountedBitSet b)
    {
        for (int i = 0; i < b.bits(); i++)
            add(b.get(i));
    }

    /**
     * Returns the count of used bits
     */
    public int bits()
    {
        return bitsUsed + 1; // bitsUsed it the order of highest bit, so we
                                // add 1
    }

}
