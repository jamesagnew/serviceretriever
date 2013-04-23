package com.google.gwt.user.client.ui;

import java.util.ArrayList;
import java.util.List;

public class ComplexPanel extends Widget {

	private List<Widget> myChildren = new ArrayList<Widget>();

	public void add(Widget theChild) {
		myChildren.add(theChild);
	}
	
}
