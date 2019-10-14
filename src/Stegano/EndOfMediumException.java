/*
 * Part of DCSteg steganographic/watermarking utility.
 * Distributed under GPL v2 license.
 */
package Stegano;

/**
 * This exception is thrown while iterating through the medium in
 * {@link Stegano.YCbCrImage.MediumIterator} if we run out of space and there
 * are still bytes of message left.
 *
 * @author Ondrej Mikle
 * @version 0.1
 */
@SuppressWarnings("serial")
public class EndOfMediumException extends Exception
{

    /** Creates instance of the exception */
    public EndOfMediumException()
    {
    }

    /**
     * Creates instance of the exception
     * 
     * @param what what happened
     */
    public EndOfMediumException(String what)
    {
        super(what);
    }

}
