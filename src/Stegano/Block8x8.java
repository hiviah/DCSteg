/*
 * Part of DCSteg steganographic/watermarking utility.
 * Distributed under GPL v2 license.
 */
package Stegano;

/**
 * Class for manipulating 8x8 pixel blocks - discrete cosine transform (DCT) and
 * quantizing (forward and inverse transforms). Usually the block is obtained
 * from {@link Stegano.YCbCrImage#subImage}.
 *
 * @author Ondrej Mikle
 * @version 0.1
 */
public class Block8x8
{
    /**
     * Computes forward and inverse DCT. The algorithm is the simple
     * non-optimized version of DCT.
     */
    public static class CosineTransform
    {
        /**
         * Default block size
         */
        private final static int N = 8;

        /**
         * Precomputed DCT coefficients.
         */
        private double[][] c;

        /**
         * Creates CosineTransform object and precomputes coefficients.
         */
        public CosineTransform()
        {
            c = new double[N][N];
            initMatrix();
        }

        /**
         * Forward DCT.
         * 
         * @param input input pixel block
         * @return transformed pixel block (frequency domain)
         */
        public Block8x8 forward(Block8x8 input)
        {
            Block8x8 output = new Block8x8(), temp = new Block8x8();
            double temp1;

            for (int i = 0; i < N; i++)
            {
                for (int j = 0; j < N; j++)
                {
                    temp.data[i][j] = 0.0;
                    for (int k = 0; k < N; k++)
                    {
                        temp.data[i][j] += (((input.data[i][k]) - 128) * c[j][k]);
                    }
                }
            }

            for (int i = 0; i < N; i++)
            {
                for (int j = 0; j < N; j++)
                {
                    temp1 = 0.0;

                    for (int k = 0; k < N; k++)
                    {
                        temp1 += (c[i][k] * temp.data[k][j]);
                    }

                    output.data[i][j] = temp1;
                }
            }

            return output;
        }

        /**
         * Inverse DCT.
         * 
         * @param input input block of DCT coefficients
         * @return inversely transformed (spatial domain) pixel block
         */
        public Block8x8 reverse(Block8x8 input)
        {
            Block8x8 output = new Block8x8(), temp = new Block8x8();
            double temp1;

            for (int i = 0; i < N; i++)
            {
                for (int j = 0; j < N; j++)
                {
                    temp.data[i][j] = 0.0;

                    for (int k = 0; k < N; k++)
                    {
                        temp.data[i][j] += input.data[i][k] * c[k][j];
                    }
                }
            }

            for (int i = 0; i < N; i++)
            {
                for (int j = 0; j < N; j++)
                {
                    temp1 = 0.0;

                    for (int k = 0; k < N; k++)
                    {
                        temp1 += c[k][i] * temp.data[k][j];
                    }

                    temp1 += 128.0;

                    output.data[i][j] = temp1;
                }
            }

            return output;
        }

        /**
         * Precomputes coefficients matrix for DCT speedup.
         */
        private void initMatrix()
        {
            for (int i = 0; i < N; i++)
            {
                double nn = (double) (N);
                c[0][i] = 1.0 / Math.sqrt(nn);
            }

            for (int i = 1; i < N; i++)
            {
                for (int j = 0; j < N; j++)
                {
                    double jj = (double) j;
                    double ii = (double) i;
                    c[i][j] = Math.sqrt(2.0 / 8.0)
                        * Math.cos(((2.0 * jj + 1.0) * ii * Math.PI) / (2.0 * 8.0));
                }
            }
        }
    }

    /**
     * Block data, publicly accessible.
     */
    public double data[][];

    /**
     * Static quantization table.
     */
    public static final QuanTable qt50 = new QuanTable();

    /**
     * Static DCT transform object.
     */

    public static final CosineTransform DCT = new CosineTransform();

    /**
     * Specifies the maximal DC coefficient after quantizing that can bear
     * information (otherwise the limited-integer transformations would cut off
     * high bits).
     */
    private static double maxDCcoef = 20;

    /**
     * Creates pixel block initialized with zeros.
     */
    public Block8x8()
    {
        data = new double[8][8];
    }

    /**
     * Creates a copy of block.
     */
    public Block8x8(int[][] matrix)
    {
        data = new double[8][8];

        for (int y = 0; y < 8; y++)
            for (int x = 0; x < 8; x++)
            {
                data[y][x] = matrix[y][x];
            }
    }

    /**
     * Creates copy of block.
     */
    public Block8x8(double[][] matrix)
    {
        data = new double[8][8];

        for (int y = 0; y < 8; y++)
        {
            System.arraycopy(matrix[y], 0, data[y], 0, 8);
        }
    }

    /**
     * Returns true iff num is of order at least order+1, i.e. there exists a
     * set bit higher than order and absolute value of num is less or equal
     * {@link Stegano.Block8x8#maxDCcoef}. The purpose is not to get the
     * brightness too high to avoid cutting off high bits after conversion to
     * RGB colorspace.
     * 
     * @param num number to test
     * @param order order to test it against
     */
    public static boolean isOfOrder(double num, int order)
    {
        double absnum = Math.abs(num);
        // test if large enough
        return (Math.floor(absnum) >= (1 << (order + 1)) 
            && (((int) absnum | (1 << order)) <= maxDCcoef)); //but not too bright
    }

    /**
     * Computes forward DCT, quantizes and returns the transformed block
     */
    public Block8x8 forwardTransform()
    {
        return qt50.quantize(DCT.forward(this));
    }

    /**
     * Dequantizes the block, computes inverse DCT and returns the transformed
     * block
     */
    public Block8x8 reverseTransform()
    {
        return DCT.reverse(qt50.dequantize(this));
    }
}
