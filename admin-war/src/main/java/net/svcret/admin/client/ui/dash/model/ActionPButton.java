package net.svcret.admin.client.ui.dash.model;

import net.svcret.admin.client.ui.components.PButton;

import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.resources.client.ImageResource;

public class ActionPButton extends PButton {

	public ActionPButton(ImageResource theIcon, String theText) {
		super(theIcon, theText);
		
		getElement().getStyle().setWidth(100, Unit.PCT);
		getElement().getStyle().setDisplay(Display.BLOCK);
	}

}
