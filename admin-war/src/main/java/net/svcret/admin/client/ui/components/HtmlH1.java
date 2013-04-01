package net.svcret.admin.client.ui.components;

import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.ui.Widget;

public class HtmlH1 extends Widget {

    public HtmlH1(String theText) {
        setElement(Document.get().createHElement(1));
        getElement().setInnerText(theText);
    }

}
