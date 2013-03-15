package ca.uhn.sail.proxy.admin.client.ui.layout;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;

public class BodyPanel extends FlowPanel {

	private static BodyPanel ourInstance;

	public BodyPanel() {
		assert ourInstance == null;

		ourInstance = this;
		setStylePrimaryName("outerLayoutBody");
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
