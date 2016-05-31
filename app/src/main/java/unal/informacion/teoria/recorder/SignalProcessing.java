package unal.informacion.teoria.recorder;

import java.util.List;

public class SignalProcessing {
	
	public static double[] filter(List<Short> x) {
        double[] filter;
        double a = 1;
        double b =  0.0500;
        int sx = x.size();
        filter = new double[sx];
        filter[0] = b*x.get(0);
        for (int i = 1; i < sx; i++) {
            filter[i] = 0.0;
            for (int j = 0; j <= i; j++) {
                int k = i-j;
                if (j > 0) {
                    if ((k < 1) && (j < x.size())) {
                        filter[i] += b*x.get(j);
                    }
                    if ((k < filter.length) && (j < 1)) {
                        filter[i] -= a*filter[k];
                    }
                } else {
                    if ((k < 1) && (j < x.size())) {
                        filter[i] += (b*x.get(j));
                    }
                }
            }
        }
        return filter;
    }
	
	public static double jaccardCoefficient(double[] x, double[] y) {
		int minSize = Math.min(x.length, y.length);
		double minSum = 0.0;
		double maxSum = 0.0;
		
		for(int i = 0; i < minSize; i++) {
			minSum += Math.min(x[i], y[i]);
			maxSum += Math.max(x[i], y[i]);
		}
		
		return minSum / maxSum;
	}


    public static double[] normalize(List<Short> x) {

        double max = 0;
        double[] norm = new double[x.size()];
        for (Short d : x) {
            max = Math.max(max, d);
        }

        for (int i = 0; i < x.size(); i++) {
            norm[i] = x.get(i) / max;
        }

        return norm;
    }

    public static double[] normalize(double[] x) {

        double max = 0;
        double[] norm = new double[x.length];
        for (double d : x) {
            max = Math.max(max, d);
        }

        for (int i = 0; i < x.length; i++) {
            norm[i] = x[i] / max;
        }

        return norm;
    }
}
