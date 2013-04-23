package net.svcret.admin.client.ui.dash.model;

import net.svcret.admin.client.ui.components.CssConstants;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.HTML;

public class HeaderLabel extends HTML {

	public HeaderLabel(String theString) {
		setText(theString);
		addStyleName(CssConstants.DASHBOARD_ACTION_HEADER);
	}

	public HeaderLabel(SafeHtml theSafeHtml) {
		setHTML(theSafeHtml);
		addStyleName(CssConstants.DASHBOARD_ACTION_HEADER);
	}

}
