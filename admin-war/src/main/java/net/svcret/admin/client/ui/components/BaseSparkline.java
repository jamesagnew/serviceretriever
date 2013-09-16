package net.svcret.admin.client.ui.components;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.LabelElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.user.client.ui.Widget;

public abstract class BaseSparkline extends Widget {

	private static int ourNextId = 0;

	private String myHeight = "20px";
	private String myId;
	private String myWidth = "35px";
	
	/**
	 * Constructor
	 * 
	 * @param theValues
	 *            The numberic values for the chart
	 */
	public BaseSparkline(String theText) {

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

	/**
	 * @return the height
	 */
	public String getHeight() {
		return myHeight;
	}

	

	public String getId() {
		return myId;
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
	public BaseSparkline withWidth(String theWidth) {
		myWidth = theWidth;
		return this;
	}

	


}
