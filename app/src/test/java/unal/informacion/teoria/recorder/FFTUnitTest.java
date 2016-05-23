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

        FFT output = new FFT(input);
        output = output.ditfft2(output);

        assertArrayEquals(outputReal, output.real, 0.003);
        assertArrayEquals(outputImg, output.img, 0.003);
    }

    @Test
    public void fft_test2() throws Exception {

        double [] input = new double[]{1, 0, 0, 0, 0, 0, 0, 0};
        double [] outputImg = new double[]{0, 0, 0, 0, 0, 0, 0, 0};
        double [] outputReal = new double[]{1, 1, 1, 1, 1, 1, 1, 1};

        FFT output = new FFT(input);
        output = output.ditfft2(output);

        assertArrayEquals(outputReal, output.real, 0.003);
        assertArrayEquals(outputImg, output.img, 0.003);
    }


    @Test
    public void fft_test4() throws Exception {

        double [] input = new double[]{0.001, -0.001, 0.003, -0.001, 0.002, -0.003,
                0.001, -0.002};
        double [] outputImg = new double[]{0, -0.0041, 0.0010, -0.0001, 0, 0.0001,
                -0.0010, 0.0041};
        double [] outputReal = new double[]{0, -0.0003, -0.0010, -0.0017, 0.0140,
                -0.0017, -0.0010, -0.003};

        FFT output = new FFT(input);
        output = output.ditfft2(output);

        assertArrayEquals(outputReal, output.real, 0.003);
        assertArrayEquals(outputImg, output.img, 0.003);
    }
}
