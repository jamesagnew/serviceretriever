package net.svcret.admin.client.ui.layout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import net.svcret.admin.client.nav.NavProcessor;
import net.svcret.admin.client.nav.PagesEnum;
import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.shared.util.StringUtil;

import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Image;

public class BreadcrumbPanel extends HorizontalPanel {

	private static BreadcrumbPanel INSTANCE;

	public BreadcrumbPanel() {
		
		if (INSTANCE != null) {
			throw new IllegalStateException("Already have singleton instance");
		}
		INSTANCE = this;

		update();
	}

	public static void handleUpdate() {
		INSTANCE.update();
	}

	private void update() {
		clear();

		String token = History.getToken();
		if (StringUtil.isBlank(token)) {
			addLink(NavProcessor.DEFAULT_PAGE, NavProcessor.DEFAULT_PAGE.name(), true);
			return;
		}

		ArrayList<String> parts = new ArrayList<>(Arrays.asList(token.split(NavProcessor.SEPARATOR)));
		for (Iterator<String> iter = parts.iterator(); iter.hasNext();) {
			String next = iter.next();
			if (next == null || next.length() < 3) {
				iter.remove();
			} else {
				try {
					PagesEnum.valueOf(next.substring(0, 3));
				} catch (Exception e) {
					iter.remove();
				}
			}
		}

		if (parts.isEmpty()) {
			addLink(NavProcessor.DEFAULT_PAGE, NavProcessor.DEFAULT_PAGE.name(), true);
			return;
		}
		
		StringBuilder tokenBuilder = new StringBuilder();
		int index = 0;
		for (String nextToken : parts) {
			if (tokenBuilder.length() > 0) {
				tokenBuilder.append(NavProcessor.SEPARATOR);
			}
			tokenBuilder.append(nextToken);
			
			index++;
			boolean lastLink = index == parts.size();
			addLink(PagesEnum.valueOf(nextToken.substring(0,3)), tokenBuilder.toString(), lastLink);
		}
		
	}

	private void addLink(PagesEnum thePage, String theToken, boolean theIsLastLink) {
		if (!theIsLastLink) {
			Hyperlink link = new Hyperlink(thePage.getBreadcrumb(), theToken);
			link.setStyleName(CssConstants.BREADCRUMB_LINK);
			add(link);

			add(new Image("images/breadcrumb_divider.png"));
		} else {
			Hyperlink link = new Hyperlink(thePage.getBreadcrumb(), theToken);
			link.setStyleName(CssConstants.BREADCRUMB_LINK);
			add(link);
		}
	}

}
