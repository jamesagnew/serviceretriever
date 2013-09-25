package net.svcret.admin.client.ui.components;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.JsArrayInteger;
import com.google.gwt.core.client.JsArrayUtils;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.LabelElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.user.client.ui.Widget;

public class Sparkline extends Widget {

	private static int ourNextId = 0;
	private String myHeight = "20px";
	private String myId;
	private int[] myValues;
	private String myWidth = "35px";
	private int myPeakValue;

	public Sparkline(int[] theValues, String theText, int thePeakValue) {
		myValues = theValues;
		myId = "sparkline" + (ourNextId++);
		myPeakValue = thePeakValue;
		
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

	

	private native void drawSparkline(final com.google.gwt.dom.client.Element theElement, JsArrayInteger theValues, String theHeight, String theWidth, int thePeakValue) /*-{
		var sparkOptions = new Array();
		sparkOptions['chartRangeMin'] = 0;
		sparkOptions['chartRangeMax'] = thePeakValue;
		sparkOptions['height'] = theHeight;
		sparkOptions['width'] = theWidth;
		sparkOptions['type'] = 'line';
		sparkOptions['disableTooltips'] = true;
		sparkOptions['disableInteraction'] = true;

		$wnd.jQuery(theElement).sparkline(theValues, sparkOptions);
	}-*/;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onLoad() {
		Element firstChild = (Element) getElement().getFirstChild();
		JsArrayInteger values = JsArrayUtils.readOnlyJsArray(myValues);

		drawSparkline(firstChild, values, myHeight, myWidth, myPeakValue);
	}



	private static List<Integer> toList(int[] theList) {
		ArrayList<Integer> retVal = new ArrayList<Integer>();
		for (int i : theList) {
			retVal.add(i);
		}
		return retVal;
	}

	public String getId() {
		return myId;
	}

}
