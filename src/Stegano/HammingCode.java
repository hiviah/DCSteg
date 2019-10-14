/*
 * HammingCode.java
 * 
 * Created on July 27, 2004, 12:43 AM
 *
 * Part of DCSteg steganographic/watermarking utility.
 * Distributed under GPL v2 license.
 */

package Stegano;

import java.util.BitSet;

/**
 * Codec for Hamming(7,4) self-correcting code (7 bits of code encode 4 bits of
 * actual data). It works, although it is definitely not very optimal
 * implementation of Hamming code.
 *
 * @author Ondrej Mikle
 * @version 0.1
 */
public class HammingCode
{
    /**
     * Kernel of generator matrix H creates the Hamming code.
     */
    private static byte[][] H = { { 1, 0, 1, 0, 1, 0, 1 },
                    { 0, 1, 1, 0, 0, 1, 1 }, { 0, 0, 0, 1, 1, 1, 1 } };

    /**
     * Matrix G, its rows are base of kernel of matrix H.
     */
    private static byte[][] base = { { 1, 0, 0, 0, 0, 1, 1 },
                    { 0, 1, 0, 0, 1, 0, 1 }, { 0, 0, 1, 0, 1, 1, 0 },
                    { 0, 0, 0, 1, 1, 1, 1 } };

    /**
     * Encodes the lower nybble (4 bits) into respective Hamming codeword.
     */
    private static CountedBitSet encodeNybble(byte b)
    {
        CountedBitSet out = new CountedBitSet();

        for (int i = 0; i < 7; i++)
        {
            int sum = 0;

            // matrix multiplication u*G
            for (int j = 0; j < 4; j++)
            {
                int bit = ((b & (1 << j)) != 0) ? 1 : 0;
                sum += bit * base[j][i];
            }

            sum &= 1; // computation in GF(2)
            out.set(i, (sum != 0));

        }
        return out;
    }

    /**
     * Converts boolean (bit) to byte.
     */
    public static byte tobit(boolean b)
    {
        return (b) ? (byte) 1 : 0;
    }

    /**
     * Decodes Hamming codeword into data. The assumption is that there is at
     * most one error, otherwise the code cannot correct the error (though
     * you'll still get some result).
     * 
     * @param w Hamming codeword
     */
    private static byte decodeNybble(BitSet w)
    {
        byte[] c = new byte[3];

        // we'll compute H*w at first and see how it looks like
        for (int i = 0; i < 3; i++)
        {
            int sum = 0;

            for (int j = 0; j < 7; j++)
            {
                sum += H[i][j] * tobit(w.get(j));
            }
            c[i] = (byte) (sum & 1); // back into GF(2)
        }

        // test whether w belongs to kernel of matrix H <=> H*w==0
        boolean inKern = true;
        for (int i = 0; i < 3; i++)
            if (c[i] != 0)
            {
                inKern = false;
                break;
            }

        if (inKern) // is in kernel, no error occurred
        {
            byte out = 0;

            // first four bits of w are the data bits
            for (int i = 0; i < 4; i++)
            {
                out |= (byte) (tobit(w.get(i)) << i);
            }
            return out;
        }

        // otherwise we have to find the error and correct it

        // search the column of H matching c
        for (int i = 0; i < 7; i++)
        {
            boolean found = true;
            for (int j = 0; j < 3; j++)
                if (c[j] != H[j][i])
                {
                    found = false;
                    break;
                }

            if (found) // index i of the column matching c determines the error
            {
                byte out = 0;

                w.flip(i); // flip given bit
                for (int k = 0; k < 4; k++)
                {
                    // first four bits are the data bits
                    out |= (byte) (tobit(w.get(k)) << k);
                }
                return out;
            }
        }

        // we never reach this, but the compiler complains about missing return
        // value
        return -1;
    }

    /**
     * Takes message and encodes it into Hamming codewords.
     * 
     * This method will not work correctly with special Unicode strings, because
     * only one byte of each character is encoded (legacy code).
     * 
     * @param msg message to encode
     * @return message encoded with Hamming code
     */
    public static CountedBitSet Encode(String msg)
    {
        byte[] mb = msg.getBytes();
        CountedBitSet hc = new CountedBitSet();

        for (int i = 0; i < mb.length; i++)
        {
            // store lower nybble, then higher nybble
            hc.append(encodeNybble((byte) (mb[i] & 0x0f)));
            hc.append(encodeNybble((byte) (mb[i] >> 4)));
        }

        return hc;
    }

    /**
     * Decodes Hamming codewords into message. This method will not work
     * correctly with special Unicode strings, because only one byte of each
     * character is encoded (legacy code).
     * 
     * @param code message in Hamming code
     * @return decoded message
     */
    public static String Decode(CountedBitSet code)
    {
        // we take bits in multiples of 14, rest is ignored (because they just
        // align the 8-bit byte boundary)
        int usedbits = code.bits() / 14;
        byte[] out = new byte[usedbits];

        // loop steps two nybbles each time, i.e. we decode one data byte in
        // each iteration
        for (int i = 0, j = 0; j < usedbits; i += 14, j++)
        {
            BitSet lo = code.get(i, i + 7), hi = code.get(i + 7, i + 14);

            out[j] = (byte) (decodeNybble(lo) | (byte) ((decodeNybble(hi) << 4)));
        }

        return new String(out);
    }

}
