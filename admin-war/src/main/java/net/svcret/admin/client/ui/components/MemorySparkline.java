package net.svcret.admin.client.ui.components;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class MemorySparkline {

	private static int ourNextId = 0;
	private String myHeight = "20px";
	private String myId;
	private String myWidth = "35px";
	private int[] myUsedValues;
	private int[] myMaxValues;

	/**
	 * Constructor
	 * 
	 * @param theValues
	 *            The numberic values for the chart
	 */
	public MemorySparkline(int[] theUsedValues, int[] theMaxValues) {

		myUsedValues = theUsedValues;

		// Store only deltas since we'll be stacking them
		myMaxValues = new int[myUsedValues.length];
		for (int i = 0; i < theMaxValues.length; i++) {
			myMaxValues[i] = theMaxValues[i] - theUsedValues[i];
		}

		myId = "memsparkline" + (ourNextId++);
	}

	/**
	 * @return the height
	 */
	public String getHeight() {
		return myHeight;
	}

	/**
	 * @return the width
	 */
	public String getWidth() {
		return myWidth;
	}

	/**
	 * @param theHeight
	 *            the height to set
	 */
	public void setHeight(String theHeight) {
		myHeight = theHeight;
	}

	/**
	 * @param theWidth
	 *            the width to set
	 */
	public void setWidth(String theWidth) {
		myWidth = theWidth;
	}

	/**
	 * @param theWidth
	 *            the width to set
	 * @return
	 */
	public MemorySparkline withWidth(String theWidth) {
		myWidth = theWidth;
		return this;
	}

	public String getNativeInvocation(String theElementId) {
		StringBuilder b = new StringBuilder();
		b.append("jsDrawMemorySparkline('#");
		b.append(theElementId);
		b.append("', [");

		ensureArrays();
		for (int col = 0; col < myUsedValues.length; col++) {
			if (col > 0) {
				b.append(", ");
			}
			b.append("[");
			b.append(myUsedValues[col]).append(", ");
			b.append(myMaxValues[col]);
			b.append("]");
		}

		b.append("], '");
		b.append(myHeight);
		b.append("', '");
		b.append(myWidth);
		b.append("');");
		return b.toString();
	}

	private void ensureArrays() {
		int maxLength = 0;
		if (myUsedValues != null) {
			maxLength = Math.max(maxLength, myUsedValues.length);
		}
		if (myMaxValues != null) {
			maxLength = Math.max(maxLength, myMaxValues.length);
		}

		if (myUsedValues == null) {
			GWT.log("No success values!");
			myUsedValues = new int[maxLength];
		}
		if (myMaxValues == null) {
			GWT.log("No fail values!");
			myMaxValues = new int[maxLength];
		}
	}

	public String getId() {
		return myId;
	}

	public static void renderTransactionGraphsAsHtml(SafeHtmlBuilder b, int[] theUsedValues, int[] theMaxValues, String theWidth, String theHeight) {
		MemorySparkline sparkline = new MemorySparkline(theUsedValues, theMaxValues);
		sparkline.setWidth(theWidth);
		sparkline.setHeight(theHeight);
		b.appendHtmlConstant("<span id='" + sparkline.getId() + "'></span>");
		b.appendHtmlConstant("<img src='images/empty.png' onload=\"" + sparkline.getNativeInvocation(sparkline.getId()) + "\" />");
	}

}
