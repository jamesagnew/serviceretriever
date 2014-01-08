package net.svcret.admin.client.ui.components;

import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.ui.Widget;

public class HtmlPre extends Widget {

	public HtmlPre(String theText) {
		this();
		setText(theText);
	}

	public HtmlPre() {
		setElement(Document.get().createPreElement());
	}

	public void setText(String theText) {
		getElement().setInnerText(theText);
	}

	public String getText() {
		return getElement().getInnerText();
	}

}
