package net.svcret.admin.client;

import com.google.gwt.core.shared.GWT;

public class MyResources {

	public static final MyCss CSS;
	static
	{
		CSS = ((MyCssBundle) GWT.create(MyCssBundle.class)).css();
		CSS.ensureInjected();
	}

}
