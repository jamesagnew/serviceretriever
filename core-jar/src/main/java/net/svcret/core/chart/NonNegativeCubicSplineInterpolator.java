package net.svcret.core.chart;

import org.rrd4j.data.CubicSplineInterpolator;

public class NonNegativeCubicSplineInterpolator extends CubicSplineInterpolator {

	public NonNegativeCubicSplineInterpolator(long[] theTimestamps, double[] theValues) {
		super(theTimestamps, theValues);
	}

	@Override
	public double getValue(double theXval) {
		return Math.max(0.0, super.getValue(theXval));
	}

	@Override
	public double getValue(long theTimestamp) {
		return Math.max(0L, super.getValue(theTimestamp));
	}

}
