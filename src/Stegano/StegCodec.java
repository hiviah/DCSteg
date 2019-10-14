/*
 * StegCodec.java
 * 
 * Created on July 27, 2004, 3:20 PM
 *
 * Part of DCSteg steganographic/watermarking utility.
 * Distributed under GPL v2 license.
 */

package Stegano;

/**
 * StegCodes takes care of imprinting/extracting message to/from the medium
 * (image in YCbCr colorspace). The message is encoded in Hamming code before
 * being imprinted.
 *
 * @author Ondrej Mikle
 * @version 0.1
 */
public class StegCodec
{
    /**
     * Message terminator. We put four NULL bytes at the end of message. Just
     * one is necessary to mark the end of message, the redundancy is due to
     * possible corruption of the message.
     */
    private static final String terminator = new String(new byte[4]);

    /**
     * Computes the number of bytes of message that can be imprinted given the
     * number of bits available in medium.
     * 
     * @param bits of available bits on medium
     */
    public static int bits2bytesSpace(int bits)
    {
        return (bits / 14 - terminator.length());
    }

    /**
     * Imprints message into image.
     * 
     * @param ymg image to imprint into
     * @param msg the message (should not contain NULL character, it will be truncated at decoding time otherwise)
     * @param order imprint strength (imprint bit order)
     * 
     * @throws EndOfMediumException if message length exceeds space in the medium
     */
    public static void imprintMessage(YCbCrImage ymg, String msg, int order)
                    throws EndOfMediumException
    {
        String msgtailed = msg + terminator;
        CountedBitSet hc = HammingCode.Encode(msgtailed);

        ymg.imprintMedium(order, hc);
    }

    /**
     * Extracts message from image
     * 
     * @param ymg image to extract from
     * @param order bit order (imprint strength) of the imprinted message
     * @return extracted message
     */
    public static String extractMessage(YCbCrImage ymg, int order)
    {
        CountedBitSet hc = ymg.exportMedium(order);

        String msgtailed = HammingCode.Decode(hc);
        int terminator = msgtailed.indexOf(0); // we search for terminator, the NULL byte

        if (terminator > -1) // if found
            return msgtailed.substring(0, terminator);
        else
            return msgtailed; // if not, bad luck...
    }

}
