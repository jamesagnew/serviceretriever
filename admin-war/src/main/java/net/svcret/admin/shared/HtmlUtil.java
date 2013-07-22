package net.svcret.admin.shared;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class HtmlUtil {

	public static SafeHtml toSafeHtml(String theString) {
		return new SafeHtmlBuilder().appendHtmlConstant(theString).toSafeHtml();
	}

}
