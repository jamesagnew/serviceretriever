package ca.uhn.sail.proxy.admin.client.nav;

import java.util.ArrayList;
import java.util.List;

import ca.uhn.sail.proxy.admin.client.ui.config.AddDomainPanel;
import ca.uhn.sail.proxy.admin.client.ui.config.AddDomainStep2Panel;
import ca.uhn.sail.proxy.admin.client.ui.config.AddServicePanel;
import ca.uhn.sail.proxy.admin.client.ui.config.AddServiceVersionPanel;
import ca.uhn.sail.proxy.admin.client.ui.config.EditDomainPanel;
import ca.uhn.sail.proxy.admin.client.ui.dash.ServiceDashboardPanel;
import ca.uhn.sail.proxy.admin.client.ui.layout.BodyPanel;
import ca.uhn.sail.proxy.admin.shared.model.Model;
import ca.uhn.sail.proxy.admin.shared.util.StringUtil;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Panel;

public class NavProcessor {

	static {
		History.addValueChangeHandler(new ValueChangeHandler<String>() {
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

		Panel panel = null;
		switch (page) {
		case DSH:
			panel = new ServiceDashboardPanel();
			break;
		case ADD:
			panel = new AddDomainPanel();
			break;
		case AD2:
			if (Model.getInstance().getDomainList().getDomainByPid(args) == null) {
				navigateToDefault();
			} else {
				panel = new AddDomainStep2Panel(args);
			}
			break;
		case EDO:
			if (Model.getInstance().getDomainList().getDomainByPid(args) == null) {
				navigateToDefault();
			} else {
				panel = new EditDomainPanel(args);
			}
			break;
		case ASE:
			if (Model.getInstance().getDomainList().getDomainByPid(args) == null) {
				navigateToDefault();
			} else {
				panel = new AddServicePanel(args);
			}
			break;
		case ASV:
			String[] argsSplit = args.split("_");
			if (argsSplit.length < 2 || StringUtil.isBlank(argsSplit[0]) || StringUtil.isBlank(argsSplit[1])) {
				panel = new AddServiceVersionPanel("", "");
			} else {
				panel = new AddServiceVersionPanel(argsSplit[0], argsSplit[1]);
			}
			break;
		}

		if (panel == null) {
			return;
		}

		BodyPanel.getInstance().setContents(panel);
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
			page = PagesEnum.DSH;
		}

		return page;
	}

	private static String getCurrentToken() {
		String token = History.getToken();
		token = StringUtil.defaultString(token);
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

}
