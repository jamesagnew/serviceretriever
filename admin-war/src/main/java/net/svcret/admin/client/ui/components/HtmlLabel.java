package net.svcret.admin.client.ui.components;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.LabelElement;
import com.google.gwt.user.client.ui.Widget;

public class HtmlLabel extends Widget {

	public HtmlLabel(String theText, String theLabelFor) {
		LabelElement elem = Document.get().createLabelElement();
		elem.setHtmlFor(theLabelFor);
		elem.setInnerText(theText);
		elem.addClassName("formLabel");
		setElement(elem);
	}

	
}
