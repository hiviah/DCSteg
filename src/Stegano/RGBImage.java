/*
 * Part of DCSteg steganographic/watermarking utility.
 * Distributed under GPL v2 license.
 */
package Stegano;

import java.awt.image.*;
import java.awt.Image;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JApplet;

import java.io.File;
import java.net.URL;

/**
 * Class for representing images in RGB colorspace. Also implements reading and
 * writing of the images.
 *
 * @author Ondrej Mikle
 * @version 0.1
 */
public class RGBImage
{
    /**
     * Red component of image.
     */
    public short[] R;

    /**
     * Green component of image.
     */
    public short[] G;

    /**
     * Blue component of image.
     */
    public short[] B;

    /**
     * Image width and height.
     */
    private int wid, hgt;

    /**
     * Creates RGBImage instance by reading an image from file. Supported images
     * are those supported by JDK's {@link ImageIO}.
     * 
     * @param fname filename of the image
     */
    public RGBImage(String fname) throws IOException, InterruptedException
    {
        BufferedImage img = ImageIO.read(new File(fname));
        wid = img.getWidth();
        hgt = img.getHeight();
        int size = wid * hgt;

        int[] pixels = new int[size];

        // takes all the pixels from image
        PixelGrabber pg = new PixelGrabber(img, 0, 0, wid, hgt, pixels, 0, wid);

        // RGB decomposition
        R = new short[size];
        G = new short[size];
        B = new short[size];

        pg.grabPixels();

        for (int i = 0; i < size; i++)
        {
            R[i] = (short) (0xff & (pixels[i] >> 16));
            G[i] = (short) (0xff & (pixels[i] >> 8));
            B[i] = (short) (0xff & (pixels[i] >> 0));
        }
    }

    /**
     * Creates RGB image by conversion from image in YCbCr colorspace.
     */
    public RGBImage(YCbCrImage ymg)
    {
        int size = ymg.Y.length;

        R = new short[size];
        G = new short[size];
        B = new short[size];

        wid = ymg.getWidth();
        hgt = ymg.getHeight();

        for (int i = 0; i < size; i++)
        {
            double y = ymg.Y[i], cb = ymg.Cb[i], cr = ymg.Cr[i];

            // transition YCbCr -> RGB (linear operator)
            R[i] = norm(y - 0.001 * cb + 1.402 * cr);
            G[i] = norm(y - 0.344 * cb - 0.714 * cr);
            B[i] = norm(y + 1.772 * cb + 0.001 * cr);
        }
    }

    /**
     * Normalizes the value to range 0-255 (excessive values are set to 0 or
     * 255).
     * 
     * @param x value to normalize
     */
    protected static short norm(double x)
    {
        short s = (short) Math.round(x);
        if (s < 0)
            s = 0;
        if (s > 255)
            s = 255;

        return s;
    }

    /**
     * Returns image width.
     */
    public int getWidth()
    {
        return wid;
    }

    /**
     * Returns image height.
     */
    public int getHeight()
    {
        return hgt;
    }

    /**
     * Write RGB image to disk file.
     * 
     * @param fname filename of the output
     * @param format image format name, e.g. "jpg" or "png", supported are those
     *        supported by {@link ImageIO}
     */
    public void Save(String fname, String format) throws IOException
    {
        BufferedImage img = new BufferedImage(wid, hgt,
                        BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < hgt; y++)
            for (int x = 0; x < wid; x++)
            {
                int i = y * wid + x;
                img.setRGB(x, y, (R[i] << 16) | (G[i] << 8) | (B[i]));
            }
        ImageIO.write(img, format, new File(fname));
    }
}
