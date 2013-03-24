package net.svcret.admin.client.ui.layout;

import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.ScrollPanel;

public class BodyPanel extends ScrollPanel {

	private static BodyPanel ourInstance;

	public BodyPanel() {
		assert ourInstance == null;

		ourInstance = this;
		addStyleName("outerLayoutBody");
	}

	/**
	 * @return the instance
	 */
	public static BodyPanel getInstance() {
		return ourInstance;
	}

	public void setContents(Panel thePanel) {
		clear();
		add(thePanel);
	}
}
