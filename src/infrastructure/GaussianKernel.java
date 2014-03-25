package infrastructure;

public class GaussianKernel 
{
	 /**
     * This class creates a gaussian kernel
     *
     * @param sigma Standard Derivation of the gaussian function
     * @param normalize Normalize integral of gaussian function to 1 or not...
     * @return float[] The gaussian kernel
     *
     * @author   Stephan Saalfeld
     */
    public static double[] createGaussianKernel1DDouble(double sigma, boolean normalize)
    {
            int size = 3;
            double[] gaussianKernel;

            if (sigma <= 0)
            {
                    gaussianKernel = new double[3];
                    gaussianKernel[1] = 1;
            }
            else
            {
                    size = Math.max(3, (int) (2 * (int) (3 * sigma + 0.5) + 1));

                    double two_sq_sigma = 2 * sigma * sigma;
                    gaussianKernel = new double[size];

                    for (int x = size / 2; x >= 0; --x)
                    {
                            double val = Math.exp( -(x * x) / two_sq_sigma);

                            gaussianKernel[size / 2 - x] = val;
                            gaussianKernel[size / 2 + x] = val;
                    }
            }

            if (normalize)
            {
                    double sum = 0;
                    for (double value : gaussianKernel)
                            sum += value;

                    for (int i = 0; i < gaussianKernel.length; i++)
                            gaussianKernel[i] /= sum;
            }
    
            return gaussianKernel;
    }
}
