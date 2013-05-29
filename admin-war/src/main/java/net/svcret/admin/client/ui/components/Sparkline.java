package net.svcret.admin.client.ui.components;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.user.client.ui.Widget;

public class Sparkline extends Widget {

	private static int ourNextId = 0;
	private boolean myBar;
	private String myHeight = "20px";
	private String myId;
	private List<Integer> myValues;
	private String myWidth = "35px";

	public Sparkline(int[] theList, String theText) {
		this(toList(theList), theText);
	}

	/**
	 * Constructor
	 * 
	 * @param theValues
	 *            The numberic values for the chart
	 */
	public Sparkline(List<Integer> theValues) {
		this(theValues, null);
	}

	/**
	 * Constructor
	 * 
	 * @param theValues
	 *            The numberic values for the chart
	 */
	public Sparkline(List<Integer> theValues, String theText) {

		if (theValues != null) {
			myValues = theValues;
		} else {
			myValues = Collections.emptyList();
		}

		myId = "sparkline" + (ourNextId++);

		SpanElement rootElement = Document.get().createSpanElement();
		setElement(rootElement);

		SpanElement spanElement = Document.get().createSpanElement();
		spanElement.setId(myId);
		rootElement.insertFirst(spanElement);

		if (theText != null) {
			Node textNode = Document.get().createTextNode(theText);
			rootElement.insertAfter(textNode, spanElement);
		}
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
	public Sparkline withWidth(String theWidth) {
		myWidth = theWidth;
		return this;
	}

	private native void drawSparkline(final com.google.gwt.dom.client.Element theElement, String theValues, String theHeight, String theWidth, String theType) /*-{
		var sparkOptions = new Array();
		sparkOptions['chartRangeMin'] = 0;
		sparkOptions['height'] = theHeight;
		sparkOptions['width'] = theWidth;
		sparkOptions['type'] = theType;
		sparkOptions['tooltipFormat'] = '{{value:levels}} - {{value}}';
		if (theType == 'bar') {
			sparkOptions['barWidth'] = 1;
		}

		var splitValues = theValues.split(",");

		var values = new Array();
		for ( var i = 0; i < splitValues.length; i++) {
			values[i] = parseInt(splitValues[i]);
		}

		$wnd.jQuery(theElement).sparkline(values, sparkOptions);
	}-*/;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onLoad() {
		StringBuilder valuesBuilder = new StringBuilder();
		for (Iterator<Integer> iter = myValues.iterator(); iter.hasNext();) {
			valuesBuilder.append(iter.next());
			if (iter.hasNext()) {
				valuesBuilder.append(",");
			}
		}

		Element firstChild = (Element) getElement().getFirstChild();
		String valuesString = valuesBuilder.toString();
		String type = myBar ? "bar" : "line";
		drawSparkline(firstChild, valuesString, myHeight, myWidth, type);
	}

	private static List<Integer> toList(int[] theList) {
		ArrayList<Integer> retVal = new ArrayList<Integer>();
		for (int i : theList) {
			retVal.add(i);
		}
		return retVal;
	}

}
