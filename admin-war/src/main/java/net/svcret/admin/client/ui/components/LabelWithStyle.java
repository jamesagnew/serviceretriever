package net.svcret.admin.client.ui.components;

import com.google.gwt.user.client.ui.Label;

public class LabelWithStyle extends Label {

	public LabelWithStyle(String theText, String theClassName) {
		super(theText);
		addStyleName(theClassName);
	}

}
