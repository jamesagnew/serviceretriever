package net.svcret.admin.client.ui.components;


public class RecentMonitorTestsSparkline extends BaseSparkline {

	private int[] myValues;

	/**
	 * Constructor
	 * 
	 * @param theValues
	 *            The numberic values for the chart
	 */
	public RecentMonitorTestsSparkline(int[] theValues, String theText) {
		super(theText);
		myValues = theValues;
	}

	public String getNativeInvocation(String theElementId) {
		StringBuilder b = new StringBuilder();
		b.append("jsDrawRecentMonitorTestsSparkline('#");
		b.append(theElementId);
		b.append("', [");

		for (int col = 0; col < myValues.length; col++) {
			if (col > 0) {
				b.append(", ");
			}
			b.append(myValues[col]);
		}

		b.append("], '");
		b.append(getHeight());
		b.append("', '");
		b.append(getWidth());
		b.append("');");
		return b.toString();
	}

}
