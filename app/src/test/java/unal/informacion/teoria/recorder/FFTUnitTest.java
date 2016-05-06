package unal.informacion.teoria.recorder;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class FFTUnitTest {

    /*
        Tests developed with Matlab fft()
     */

    @Test
    public void fft_test1() throws Exception {

        double [] input = new double[]{1, 1, 1, 1, 1, 1, 5, 1};
        double [] outputImg = new double[]{0, 4, 0, -4, 0, 4, 0, -4};
        double [] outputReal = new double[]{12, 0, -4, 0, 4, 0 , -4, 0};

        FFT output = FFT.ditfft2(input);

        assertArrayEquals(outputReal, output.real, 0.00001);
        assertArrayEquals(outputImg, output.img, 0.00001);
    }

    @Test
    public void fft_test2() throws Exception {

        double [] input = new double[]{1, 0, 0, 0, 0, 0, 0, 0};
        double [] outputImg = new double[]{0, 0, 0, 0, 0, 0, 0, 0};
        double [] outputReal = new double[]{1, 1, 1, 1, 1, 1, 1, 1};

        FFT output = FFT.ditfft2(input);

        assertArrayEquals(outputReal, output.real, 0.00001);
        assertArrayEquals(outputImg, output.img, 0.00001);
    }

    @Test
    public void fft_test3() throws Exception {

        double [] input = new double[]{0.25, 54.0, 48.1, 0, 0, 0, 1, 54.56, 1.8, 0.001};
        double [] outputImg = new double[]{0, -23.296, -11.591, -55.260, 63.569,
                0, -63.596, 55.260, 111.591, 23.296};
        double [] outputReal = new double[]{159.711, 41.689, -67.264, -12.358, -11.967,
                -57.411, -11.967, -12.358, -67.264, 41.698};

        FFT output = FFT.ditfft2(input);

        assertArrayEquals(outputReal, output.real, 0.00001);
        assertArrayEquals(outputImg, output.img, 0.00001);
    }
}
