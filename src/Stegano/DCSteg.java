/*
 * Part of DCSteg steganographic/watermarking utility.
 * Distributed under GPL v2 license.
 */
package Stegano;

import java.io.*;

/**
 * Main runnable class DCSteg, allows image analysis, message imprint and
 * decoding message from image.
 *
 * @author Ondrej Mikle
 * @version 0.1
 */
public class DCSteg
{
    /**
     * Constructs a new instance.
     */
    public DCSteg ()
    {
    }

    /**
     * Analyzes given image and reports how many bits of what order are there to
     * hold the message.
     * 
     * @param fname filename of the image file to analyze
     */
    public static void Analysis(String fname) throws IOException,
                    InterruptedException
    {
        YCbCrImage ymg = new YCbCrImage(new RGBImage(fname));

        ymg.forwardTransform();
        int orders[] = ymg.analysis();

        for (int i = 0; i < orders.length; i++)
        {
            // if number of bits available for the given order is zero, all
            // higher orders will be zero
            if (orders[i] == 0)
                break;
            System.out.println(i + ": " + orders[i] + " bits, after encoding "
                            + StegCodec.bits2bytesSpace(orders[i])
                            + " bytes available for the message");
        }
    }

    /**
     * Imprints the message into the image. Output image is saved in png format.
     * 
     * @param in name of the input image filename
     * @param out name of the output image filename
     * @param order strength of imprint (bit order used to imprint)
     * @param msg message itself
     */
    public static void Encode(String in, String out, int order, String msg)
                    throws IOException, InterruptedException,
                    EndOfMediumException
    {
        YCbCrImage ymg = new YCbCrImage(new RGBImage(in));
        ymg.forwardTransform();
        StegCodec.imprintMessage(ymg, msg, order);
        ymg.reverseTransform();
        RGBImage rmg = new RGBImage(ymg);
        rmg.Save(out, "png");
    }

    /**
     * Decodes message from image.
     * 
     * @param in input image filename
     * @param order strength of imprint (bit order where to look for the
     *        message)
     */
    public static void Decode(String in, int order) throws IOException,
                    InterruptedException
    {
        YCbCrImage ymg = new YCbCrImage(new RGBImage(in));
        ymg.forwardTransform();

        String msg = StegCodec.extractMessage(ymg, order);
        System.out.println(msg);
    }

    /**
     * Main method. Without sufficient arguments just prints out help.
     * 
     * @param args see help text in the body below
     */
    public static void main(String[] args)
    {
        if (args.length < 2)
        {
            System.err.println("DCSteg <command> <image_in> [imprint_strength] [image_out] [message]\n"
                + "(imprint_strength is mandatory for 'e' and 'd' commands)\n\n"
                + "Commands:\n"
                + "a  - image analysis, computes how many bits/bytes are available for message in given image\n"
                + "e  - encode message in the image\n"
                + "d  - decode message from image\n\n"
                + "image_in, image_out - input and output image filenames (output is in png format)\n"
                + "imprint_strength - what bit order to use to imprint the message, run analysis first to find out the available space\n");
            System.exit(255);
        }
        try
        {
            switch (args[0].toLowerCase().charAt(0))
            {
                case 'a':
                    System.err
                              .println("Analyzing image, it may take a while if the image is large");
                    Analysis(args[1]);
                    break;
                case 'e':
                    System.err.println("Encoding message...");
                    Encode(args[1], args[3], Integer.valueOf(args[2])
                                                    .intValue(), args[4]);
                    break;
                case 'd':
                    System.err.println("Decoding message...");
                    Decode(args[1], Integer.valueOf(args[2]).intValue());
                    break;
            }
        } catch (NumberFormatException e)
        {
            System.err
                      .println("Argument passed as imprint strength cannot be converted to integer");
            System.exit(1);
        } catch (IOException e)
        {
            System.err.println("Error loading image: " + e);
            System.exit(3);
        } catch (InterruptedException e)
        {
            System.err.println("Image loading interrupted: " + e);
            System.exit(3);
        } catch (EndOfMediumException e)
        {
            System.err.println("Not enough space for the message in the image");
            System.exit(4);
        }
    }
}
