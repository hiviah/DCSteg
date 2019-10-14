/*
 * QuanTable.java
 * 
 * Created on July 25, 2004, 3:06 PM
 *
 * Part of DCSteg steganographic/watermarking utility.
 * Distributed under GPL v2 license.
 */

package Stegano;

/**
 * Static class representing image quantizing table of 50% quality (actually
 * this is the reference JPEG 50% quality quantification table).
 *
 * @author Ondrej Mikle
 * @version 0.1
 */
public class QuanTable
{
    /**
     * Quantizing matrix
     */
    private static final int table[][] = { { 16, 11, 10, 16, 24, 40, 51, 61 },
                    { 12, 12, 14, 19, 26, 58, 60, 55 },
                    { 14, 13, 16, 24, 40, 57, 69, 56 },
                    { 14, 17, 22, 29, 51, 87, 80, 62 },
                    { 18, 22, 37, 56, 68, 109, 103, 77 },
                    { 24, 35, 55, 64, 81, 104, 113, 92 },
                    { 49, 64, 78, 87, 103, 121, 120, 101 },
                    { 72, 92, 95, 98, 112, 100, 103, 99 } };

    /**
     * Quantizes the block with this matrix (divides each pixel value by the
     * respective coefficient in the table
     * 
     * @param in block for quantizing
     * @return quantized block
     */
    public Block8x8 quantize(Block8x8 in)
    {
        Block8x8 out = new Block8x8();

        for (int y = 0; y < 8; y++)
            for (int x = 0; x < 8; x++)
            {
                out.data[y][x] = in.data[y][x] / table[y][x];
            }
        return out;
    }

    /**
     * Dequantization (inverse quantization). Takes quantized block and returns
     * dequantized block as it were before quantization (there may and will be
     * numeric imprecision).
     * 
     * @param in quantized block
     * @return reconstructed (dequantized block)
     */
    public Block8x8 dequantize(Block8x8 in)
    {
        Block8x8 out = new Block8x8();

        for (int y = 0; y < 8; y++)
            for (int x = 0; x < 8; x++)
            {
                out.data[y][x] = in.data[y][x] * table[y][x];
            }
        return out;
    }
}
