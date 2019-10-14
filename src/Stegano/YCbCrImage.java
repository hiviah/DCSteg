package Stegano;

import java.util.BitSet;

/**
 * Class YCbCrImage serves for manipulating image in YCbCr colorspace.
 * Luma component is split into blockx 8x8 pixels, transformed via DCT,
 * quantized, and imprinted. After quantizing it is possible to find out how
 * many available bytes are there for the message.
 *
 * @author Ondrej Mikle
 * @version 0.1
 */
public class YCbCrImage
{
    /**
     * Iterates over all bits of medium where bits of encoded message can be stored.
     * Has sense only if the image has gone through DCT and quantization.
     *
     * Blocks are iterated in top-to-bottom, left-to-right order. Blocks smaller
     * than 8x8 are skipped.
     */
    private class MediumIterator
    {
        int order;

        int bx = 0, by = 0;

        int x = -1, y = 0;

        /**
	 * Numbers of encountered and changed bits while imprinting. Suitable for statistics.
         */
        int encountered, changed;

        /**
         * Current block we iterate through.
         */
        Block8x8 curblock;

	/**
	 * Creates medium iterator for given bit order.
	 * @param ord order/imprint strength
	 */
        MediumIterator(int ord)
        {
            order = ord;
            curblock = subImage(0, 0);
        }

        /**
	 * Does one iteration and return next index from medium where the next
	 * bit should be placed.
	 * @throws EndOfMediumException
         */
        int iterate() throws EndOfMediumException
        {
            while (true)
            {
                // walk the rest of 8x8 block
                for (int cy = y; cy < 8; cy++)
                {
                    for (int cx = x + 1; cx < 8; cx++)
                    {
                        double num = curblock.data[cy][cx];
			//we choose only DC coefficients, they are more stable than any AC coefficients
                        if (cx == 0 && cy == 0 && Block8x8.isOfOrder(num, order))
                        {
                            x = cx;
                            y = cy;
                            return ((by + y) * wid + (bx + x));
                        }
                    }
                    x = -1; // return to beginning of line
                    // x = -1, so that we begin with cx=0
                }

                // end of block, choose next one
                y = 0;
                bx += 8;
                if (bx + 8 > wid) // would we enter block smaller than 8x8?
                {
                    bx = 0;
                    by += 8; // move to next row of blocks
                    if (by + 8 > hgt) // no space left
                        throw new EndOfMediumException(
                                        "Not enough space in the image");
                }

                curblock = subImage(bx, by);
            }
        }

        /**
	 * Reads the next bit in the medium.
	 * @throws EndOfMediumException when there are no more bits left
	 * @return the read bit
         */
        boolean read() throws EndOfMediumException
        {
            int pos = iterate();
            double val = Y[pos];

            int retbit = ((int) Math.floor(Math.abs(val)) & (1 << order));

            return (retbit != 0);
        }

        /**
	 * Writes the next bit in the medium.
	 * @throws EndOfMediumException when there is no more space left
	 * @param bit bit to write
         */
        void write(boolean bit) throws EndOfMediumException
        {
            int pos = iterate();
            double val = Y[pos];
            boolean changebit;

	    // decompose number so that bit operations are nicer
            int sign = (val >= 0) ? 1 : -1;
            val = Math.abs(val);
            int intval = (int) Math.floor(val);
            double frac = val - intval;

            changebit = ((intval & (1 << order)) != 0) ? true : false;
            if (changebit != bit)
                changed++; // changed bit statistics
            encountered++;

            intval = (bit) ? intval | (1 << order) : intval & (~(1 << order));

            // decomposed number reconstruction
            Y[pos] = sign * (intval + frac);
        }

	/** Resets position in the image */
        void reset()
        {
            bx = -8;
            by = 0;
            changed = encountered = 0;
        }

        /** Returns the ratio of changed bits:written bits */
	double stats()
        {
            return ((double) changed / (double) encountered);
        }
    }

    /**
     * Luma component of image.
     */
    public double[] Y;

    /**
     * Blue chroma component of image.
     */
    public double[] Cb;

    /**
     * Red chroma component of image.
     */
    public double[] Cr;

    /**
     * Image width.
     */
    private int wid;

    /**
     * Image height.
     */
    private int hgt;

    /**
     * Creates YCbCrImage from {@link RGBImage} by transferring it into YCbCr
     * colorspace.
     * @param img the RGB image
     */
    public YCbCrImage(RGBImage img)
    {
        Y = new double[img.R.length];
        Cb = new double[img.R.length];
        Cr = new double[img.R.length];

        wid = img.getWidth();
        hgt = img.getHeight();

        for (int i = 0; i < img.R.length; i++)
        {
            int r = img.R[i], g = img.G[i], b = img.B[i];

            Y[i] = 0.299 * r + 0.587 * g + 0.114 * b;
            Cb[i] = -0.1687 * r - 0.3313 * g + 0.5 * b;
            Cr[i] = 0.5 * r - 0.4187 * g - 0.0813 * b;
        }
    }

    /**
     * Constructs a new instance.
     */
    public YCbCrImage ()
    {
    }

    /**
     * Reads a block of 8x8 pixels from given position from luma component. If the
     * coords would be out of bounds (e.g. borders of image), fills in zeros.
     * @param x x coord
     * @param y y coord
     * @return read block
     */
    public Block8x8 subImage(int x, int y)
    {
        Block8x8 matrix = new Block8x8();

        for (int i = y, my = 0; i < y + 8; i++, my++)
            for (int j = x, mx = 0; j < x + 8; j++, mx++)
                if (i < hgt && j < wid)
                    matrix.data[my][mx] = Y[i * wid + j];

        return matrix;
    }

    /**
     * Writes given block into luma component of image at given coordinates.
     * @param x x coords
     * @param y y coord
     * @param b b block to write
     */
    public void update(int x, int y, Block8x8 b)
    {

        for (int i = y, my = 0; i < y + 8; i++, my++)
            for (int j = x, mx = 0; j < x + 8; j++, mx++)
            {
                if (i < hgt && j < wid)
                    Y[i * wid + j] = b.data[my][mx];
            }
    }

    /**
     * Iterates over image and computes the analysis. Returns an array where
     * the integer at each index represents how many bits there are available
     * for the given imprint strength (=index).
     * Tests DC coefficients in each block.
     */
    public int[] analysis()
    {
        int[] res = new int[32], subres;

        for (int y = 0; y < hgt; y += 8)
            for (int x = 0; x < wid; x += 8)
            {
                /*
                 * Testing orders 3 and higher is of no use, since
		 * a) they are way too visible
		 * b) the space available is very low
		 * c) we can cross maximal brightness boundary very easily
                 */
                for (int i = 0; i < 3; i++)
                    if (Block8x8.isOfOrder(subImage(x, y).data[0][0], i))
                        res[i]++;
            }

        return res;
    }

    /**
     * Returns height.
     */
    public int getHeight()
    {
        return hgt;
    }

    /**
     * Returns width.
     */
    public int getWidth()
    {
        return wid;
    }

    /**
     * Returns array of bits containing the encoded message. Only
     * DCT coefficients large enough to hold the bit are considered.
     * Blocks smaller than 8x8 are ignored as well.
     * @param order bit order/strength of imprint
     * @return encoded message bits
     */
    public CountedBitSet exportMedium(int order)
    {
        CountedBitSet medium = new CountedBitSet();
        MediumIterator it = new MediumIterator(order);
        int i = 0;

        try
        {
            while (true)
            {
                medium.set(i, it.read());
                i++;
            }
        } catch (EndOfMediumException e)
        {
            // finished reading the message
        }

        return medium;
    }

    /**
     * Imprints the bit array into image's DCT coefficients.
     * 
     * @param order bit order
     * @param medium bits to write
     * 
     * @return ratio of changed bits
     */
    public double imprintMedium(int order, CountedBitSet medium)
                    throws EndOfMediumException
    {
        MediumIterator it = new MediumIterator(order);

        for (int i = 0; i < medium.bits(); i++)
            it.write(medium.get(i));

        return it.stats();
    }

    /**
     * Computes DCT and quantizes the luma component.
     */
    public void forwardTransform()
    {
        // y < hgt & ~7 znamena aby netransformovalo okrajove bloky nezarovnane
        // na 8
        for (int y = 0; y < (hgt & ~7); y += 8)
            for (int x = 0; x < (wid & ~7); x += 8)
                update(x, y, subImage(x, y).forwardTransform());
    }

    /**
     * Computes dequantization and inverse DCT of luma component.
     */
    public void reverseTransform()
    {
        // y < hgt & ~7 znamena aby netransformovalo okrajove bloky nezarovnane
        // na 8
        for (int y = 0; y < (hgt & ~7); y += 8)
            for (int x = 0; x < (wid & ~7); x += 8)
                update(x, y, subImage(x, y).reverseTransform());
    }
}

