package net.svcret.admin.client.nav;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.ui.config.AddDomainPanel;
import net.svcret.admin.client.ui.config.AddDomainStep2Panel;
import net.svcret.admin.client.ui.config.AddServicePanel;
import net.svcret.admin.client.ui.config.AddServiceVersionPanel;
import net.svcret.admin.client.ui.config.AddServiceVersionStep2Panel;
import net.svcret.admin.client.ui.config.EditDomainPanel;
import net.svcret.admin.client.ui.config.HttpClientConfigsPanel;
import net.svcret.admin.client.ui.dash.ServiceDashboardPanel;
import net.svcret.admin.client.ui.layout.BodyPanel;
import net.svcret.admin.shared.util.StringUtil;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Panel;

public class NavProcessor {

	private static final PagesEnum DEFAULT_PAGE = PagesEnum.DSH;

	static {
		History.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> theEvent) {
				navigate();
			}
		});
	}

	public static void navRoot(PagesEnum thePage) {
		History.newItem(thePage.name(), true);
	}

	public static void navigate() {

		PagesEnum page = getCurrentPage();
		String args = currentTokenArgs();

		try {
			Panel panel = null;
			switch (page) {
			case DSH:
				panel = new ServiceDashboardPanel();
				break;
			case ADD:
				panel = new AddDomainPanel();
				break;
			case AD2:
				panel = new AddDomainStep2Panel(Long.parseLong(args));
				break;
			case EDO:
				panel = new EditDomainPanel(Long.parseLong(args));
				break;
			case ASE:
				if (StringUtil.isBlank(args)) {
					panel = new AddServicePanel(null);
				} else {
					panel = new AddServicePanel(Long.parseLong(args));
				}
				break;
			case ASV:
				String[] argsSplit = args.split("_");
				if (argsSplit.length < 2 || StringUtil.isBlank(argsSplit[0]) || StringUtil.isBlank(argsSplit[1])) {
					panel = new AddServiceVersionPanel(null, null);
				} else {
					panel = new AddServiceVersionPanel(Long.parseLong(argsSplit[0]), Long.parseLong(argsSplit[1]));
				}
				break;
			case AV2:
				argsSplit = args.split("_");
				if (argsSplit.length < 3) {
					navigateToDefault();
				} else {
					panel = new AddServiceVersionStep2Panel(Long.parseLong(argsSplit[0]), Long.parseLong(argsSplit[1]), Long.parseLong(argsSplit[2]));
				}
				break;
			case HCC:
				panel = new HttpClientConfigsPanel();
			}

			if (panel == null) {
				return;
			}

			BodyPanel.getInstance().setContents(panel);
			
		} catch (Exception e) {
			GWT.log("Failed to navigate!", e);
			AdminPortal.reportError("Failed to navigate!", e);
			navigateToDefault();
		}

	}

	private static void navigateToDefault() {
		History.newItem("");
	}

	public static String currentTokenArgs() {
		String token = getCurrentToken();
		token = token.replaceAll(".*__", "");
		if (token.length() < 5) {
			return "";
		}
		return token.substring(4);
	}

	public static PagesEnum getCurrentPage() {
		String token = getCurrentToken();
		token = token.replaceAll(".*__", "");

		if (token.length() > 3) {
			token = token.substring(0, 3);
		}

		PagesEnum page;
		try {
			page = PagesEnum.valueOf(token);
		} catch (IllegalArgumentException e) {
			page = DEFAULT_PAGE;
		}

		return page;
	}

	private static String getCurrentToken() {
		String token = History.getToken();
		token = StringUtil.defaultString(token, DEFAULT_PAGE.name());
		return token;
	}

	public static String getTokenAddDomainStep2(long theId) {
		String token = getCurrentToken();
		if (!token.isEmpty()) {
			token = token + "__";
		}
		token = token + PagesEnum.AD2 + "_" + theId;
		token = removeDuplicates(token);
		return token;
	}

	public static String getTokenEditDomain(boolean theAddToHistory, long theDomainPid) {
		String token = "";
		if (theAddToHistory) {
			token = getCurrentToken();
			if (!token.isEmpty()) {
				token = token + "__";
			}
		}
		token = token + PagesEnum.EDO + "_" + theDomainPid;
		token = removeDuplicates(token);
		return token;
	}

	public static String getTokenAddService(boolean theAddToHistory, long theDomainPid) {
		String token = "";
		if (theAddToHistory) {
			token = getCurrentToken();
			if (!token.isEmpty()) {
				token = token + "__";
			}
		}
		token = token + PagesEnum.ASE + "_" + theDomainPid;
		token = removeDuplicates(token);
		return token;
	}

	public static String getTokenAddServiceVersion(boolean theAddToHistory, Long theUncommittedSessionId) {
		String token = "";
		if (theAddToHistory) {
			token = getCurrentToken();
			if (!token.isEmpty()) {
				token = token + "__";
			}
		}

		if (theUncommittedSessionId != null) {
			token = token + PagesEnum.ASV + "_" + theUncommittedSessionId;
		} else {
			token = token + PagesEnum.ASV;
		}

		token = removeDuplicates(token);
		return token;
	}

	private static String removeDuplicates(String theToken) {

		String[] parts = theToken.split("__");
		List<String> newParts = new ArrayList<String>();

		String prevType = null;
		for (String nextToken : parts) {
			if (nextToken.length() < 3) {
				continue;
			}
			String nextType = nextToken.substring(0, 3);
			if (nextType.equals(prevType)) {
				newParts.remove(newParts.size() - 1);
			}

			newParts.add(nextToken);
			prevType = nextType;
		}

		StringBuilder retVal = new StringBuilder();
		for (String next : newParts) {
			if (retVal.length() > 0) {
				retVal.append("__");
			}
			retVal.append(next);
		}

		return retVal.toString();

	}

	public static Long getParamAddServiceVersionUncommittedId() {
		String current = getCurrentToken();
		if (!current.startsWith(PagesEnum.ASV.name() + "_")) {
			return null;
		}

		String numStr = current.substring(4);
		if (!numStr.matches("^[0-9]+$")) {
			return null;
		}

		return Long.parseLong(numStr);
	}

	public static String getTokenAddServiceVersionStep2(long theDomainPid, long theServicePid, long theVersionPid) {
		String token = "";
		token = getCurrentToken();
		if (!token.isEmpty()) {
			token = token + "__";
		}

		token = token + PagesEnum.AV2 + "_" + theDomainPid + "_" + theServicePid + "_" + theVersionPid;

		token = removeDuplicates(token);
		return token;
	}

	public static String getLastTokenBefore(PagesEnum... thePages) {
		HashSet<String> names = new HashSet<String>();
		for (PagesEnum pagesEnum : thePages) {
			names.add(pagesEnum.name());
		}

		String[] parts = getCurrentToken().split("__");
		String prev = DEFAULT_PAGE.name();
		for (String next : parts) {
			if (next.length() >= 3 && names.contains(next.substring(0, 3))) {
				break;
			} else {
				prev = next;
			}
		}
		return prev;
	}

	public static void goHome() {
		navigateToDefault();
	}

}
