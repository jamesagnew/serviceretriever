package net.svcret.admin.client.ui.components;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.LIElement;
import com.google.gwt.user.client.ui.Widget;

/**
 * HTML UL/OL implementation, based on code from
 * {@link "http://davidmaddison.blogspot.com/2009/01/creating-gwt-component.html"}
 */
public class HtmlList extends Widget {

	public HtmlList(ListType listType) {
		setElement(listType.createElement());
		setStylePrimaryName("html-list");
	}

	public void addItem(String text) {
		addItem(text, false);
	}

	public void addItem(String theText, boolean theHtml) {
		LIElement liElement = Document.get().createLIElement();
		if (theHtml) {
			liElement.setInnerHTML(theText);
		} else {
			liElement.setInnerText(theText);
		}
		getElement().appendChild(liElement);
	}

	public static enum ListType {
		ORDERED {
			@Override
			public Element createElement() {
				return Document.get().createOLElement();
			}
		},
		UNORDERED {
			@Override
			public Element createElement() {
				return Document.get().createULElement();
			}
		};

		public abstract Element createElement();
	}

}
