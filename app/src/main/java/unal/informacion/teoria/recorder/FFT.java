package unal.informacion.teoria.recorder;


public class FFT {

    double[] real;
    double[] img;

    public FFT(double[] input) {
        this.real = new double[input.length];
        this.img = new double[input.length];

        for (int i = 0; i < input.length; i++) {
            img[i] = 0;
            real[i] = input[i];
        }
    }

    public FFT(int size) {
        this.real = new double[size];
        this.img = new double[size];
    }

    public static FFT ditfft2(double[] input) {

        if (input.length % 2 != 0) {
            throw new IllegalArgumentException("The size of input should be divisible by 2");
        }

        FFT fft = new FFT(input);

        return ditfft2(fft);
    }

    /*
    Implementation of Cooley-Tukey FFT algorithm using radix-2
    decimation-in-time
     */
    public static FFT ditfft2(FFT fft) {

        int length = fft.real.length;
        if (length == 1) {
            FFT s = new FFT(1);
            s.real[0] = fft.real[0];
            s.img[0] = fft.img[0];
            return s;
        }
        FFT temp = new FFT(length / 2);

        for (int i = 0; i < length / 2; i++) {
            temp.real[i] = fft.real[i * 2];
            temp.img[i] = fft.img[i * 2];
        }

        FFT fftLHS = ditfft2(temp);

        for (int i = 0; i < length / 2; i++) {
            temp.real[i] = fft.real[i * 2 + 1];
            temp.img[i] = fft.img[i * 2 + 1];
        }

        FFT fftRHS = ditfft2(temp);
        FFT fftFull = new FFT(length);

        double arg, real, img, fftRhsReal, fftRhsImg;

        for (int i = 0; i < length / 2; i++) {
            arg = -2.0 * Math.PI * i / length;
            real = Math.cos(arg);
            img = Math.sin(arg);
            fftRhsReal = fftRHS.real[i] * real - fftRHS.img[i] * img;
            fftRhsImg = fftRHS.real[i] * img + fftRHS.img[i] * real;

            fftFull.real[i] = fftLHS.real[i] + fftRhsReal;
            fftFull.img[i] = fftLHS.img[i] + fftRhsImg;

            fftFull.real[i + length / 2] = fftLHS.real[i] - fftRhsReal;
            fftFull.img[i + length / 2] = fftLHS.img[i] - fftRhsImg;
        }

        return fftFull;
    }
}