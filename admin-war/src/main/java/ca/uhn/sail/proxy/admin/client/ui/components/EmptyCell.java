package ca.uhn.sail.proxy.admin.client.ui.components;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.user.client.ui.Widget;

public class EmptyCell extends Widget {

	public EmptyCell() {
		SpanElement span = Document.get().createSpanElement();
		span.appendChild(Document.get().createTextNode("&nbsp;"));
		span.setInnerHTML("&nbsp;");
		setElement(span);
	}

	public static Widget defaultWidget(Widget theWidget) {
		return theWidget != null ? theWidget : new EmptyCell();
	}
	
}
