package net.svcret.admin.client.ui.components;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;

public class PButton extends Button {

	public PButton(String theText) {
		super(theText);
		setStylePrimaryName("pushButton");
	}

	public PButton(String theText, ClickHandler theHandler) {
		this(theText);
		addClickHandler(theHandler);
	}
	
}
