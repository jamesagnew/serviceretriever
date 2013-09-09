package net.svcret.admin.client.ui.components;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayInteger;
import com.google.gwt.core.client.JsArrayUtils;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.LabelElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.ui.Widget;

public class UsageSparkline extends Widget {

	private static int ourNextId = 0;
	private boolean myBar;
	private String myHeight = "20px";
	private String myId;
	private String myWidth = "35px";
	private DateTimeFormat ourTimeFormat = DateTimeFormat.getFormat(PredefinedFormat.TIME_SHORT);
	private int[] mySuccessValues;
	private int[] myFaultValues;
	private int[] myFailValues;
	private int[] mySecurityFailValues;

	/**
	 * Constructor
	 * 
	 * @param theValues
	 *            The numberic values for the chart
	 */
	public UsageSparkline(int[] theSuccessValues, int[] theFaultValues, int[] theFailValues, int[] theSecurityFailValues, String theText) {

		mySuccessValues = theSuccessValues;
		myFaultValues = theFaultValues;
		myFailValues = theFailValues;
		mySecurityFailValues = theSecurityFailValues;

		myId = "sparkline" + (ourNextId++);

		SpanElement rootElement = Document.get().createSpanElement();
		setElement(rootElement);

		SpanElement spanElement = Document.get().createSpanElement();
		spanElement.setId(myId);
		rootElement.insertFirst(spanElement);

		if (theText != null) {
			Element textNode = Document.get().createElement("nobr");
			textNode.setInnerText(theText);
			rootElement.insertAfter(textNode, spanElement);
		} else {
			LabelElement textNode = Document.get().createLabelElement();
			textNode.setInnerHTML("&nbsp;");
			rootElement.insertAfter(textNode, spanElement);
		}
	}

	public UsageSparkline asBar(boolean theBar) {
		setBar(theBar);
		return this;
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

	public void setBar(boolean theBar) {
		myBar = theBar;
	}

	/**
	 * @param theHeight
	 *            the height to set
	 */
	@Override
	public void setHeight(String theHeight) {
		myHeight = theHeight;
	}

	/**
	 * @param theWidth
	 *            the width to set
	 */
	@Override
	public void setWidth(String theWidth) {
		myWidth = theWidth;
	}

	/**
	 * @param theWidth
	 *            the width to set
	 * @return
	 */
	public UsageSparkline withWidth(String theWidth) {
		myWidth = theWidth;
		return this;
	}

	public String getNativeInvocation(String theElementId) {
		StringBuilder b = new StringBuilder();
		b.append("jsDrawUsageSparkline('#");
		b.append(theElementId);
		b.append("', [");

		ensureArrays();
		for (int col = 0; col < mySuccessValues.length; col++) {
			if (col>0) {
				b.append(", ");
			}
			b.append("[");
			b.append(mySuccessValues[col]).append(", ");
			b.append(myFaultValues[col]).append(", ");
			b.append(myFailValues[col]).append(", ");
			b.append(mySecurityFailValues[col]);
			b.append("]");
		}

		b.append("], '");
		b.append(myHeight);
		b.append("', '");
		b.append(myWidth);
		b.append("');");
		return b.toString();
	}

	private native void drawSparkline(final com.google.gwt.dom.client.Element theElement, JsArray<JsArrayInteger> theValues, String theHeight, String theWidth) /*-{
		var sparkOptions = new Array();
		sparkOptions['chartRangeMin'] = 0;
		sparkOptions['height'] = theHeight;
		sparkOptions['width'] = theWidth;
		sparkOptions['type'] = 'bar';
		sparkOptions['stackedBarColor'] = [ '#0C0', '#66C', '#F00', '#FA0' ];
		sparkOptions['barWidth'] = 2;
		sparkOptions['barSpacing'] = 0;
		sparkOptions['zeroColor'] = '#CCF';
		sparkOptions['disableTooltips'] = true;
		sparkOptions['disableInteraction'] = true;
		$wnd.jQuery(theElement).sparkline(theValues, sparkOptions);
	}-*/;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onLoad() {
		ensureArrays();

		JsArrayInteger[] columns = new JsArrayInteger[mySuccessValues.length];
		for (int i = 0; i < columns.length; i++) {
			columns[i] = JsArrayUtils.readOnlyJsArray(new int[] { mySuccessValues[i], myFaultValues[i], myFailValues[i], mySecurityFailValues[i] });
		}

		JsArray<JsArrayInteger> values = JsArrayUtils.readOnlyJsArray(columns);

		Element firstChild = (Element) getElement().getFirstChild();
		drawSparkline(firstChild, values, myHeight, myWidth);
	}

	private void ensureArrays() {
		int maxLength = 0;
		if (mySuccessValues != null) {
			maxLength = Math.max(maxLength, mySuccessValues.length);
		}
		if (myFailValues != null) {
			maxLength = Math.max(maxLength, myFailValues.length);
		}
		if (myFaultValues != null) {
			maxLength = Math.max(maxLength, myFaultValues.length);
		}
		if (mySecurityFailValues != null) {
			maxLength = Math.max(maxLength, mySecurityFailValues.length);
		}

		if (mySuccessValues == null) {
			GWT.log("No success values!");
			mySuccessValues = new int[maxLength];
		}
		if (myFailValues == null) {
			GWT.log("No fail values!");
			myFailValues = new int[maxLength];
		}
		if (myFaultValues == null) {
			GWT.log("No fault values!");
			myFaultValues = new int[maxLength];
		}
		if (mySecurityFailValues == null) {
			GWT.log("No secfail values!");
			mySecurityFailValues = new int[maxLength];
		}

	}

	private String createTypeString() {
		String type = myBar ? "bar" : "line";
		return type;
	}

	public String getId() {
		return myId;
	}

}
