package net.svcret.admin.client.nav;

import static net.svcret.admin.shared.util.StringUtil.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import net.svcret.admin.client.ui.catalog.ServiceCatalogPanel;
import net.svcret.admin.client.ui.config.ConfigPanel;
import net.svcret.admin.client.ui.config.HttpClientConfigsPanel;
import net.svcret.admin.client.ui.config.auth.AuthenticationHostsPanel;
import net.svcret.admin.client.ui.config.auth.EditUserPanel;
import net.svcret.admin.client.ui.config.auth.EditUsersPanel;
import net.svcret.admin.client.ui.config.domain.AddDomainPanel;
import net.svcret.admin.client.ui.config.domain.AddDomainStep2Panel;
import net.svcret.admin.client.ui.config.domain.DeleteDomainPanel;
import net.svcret.admin.client.ui.config.domain.EditDomainPanel;
import net.svcret.admin.client.ui.config.service.AddServicePanel;
import net.svcret.admin.client.ui.config.service.DeleteServicePanel;
import net.svcret.admin.client.ui.config.service.EditServicePanel;
import net.svcret.admin.client.ui.config.svcver.AddServiceVersionPanel;
import net.svcret.admin.client.ui.config.svcver.AddServiceVersionStep2Panel;
import net.svcret.admin.client.ui.config.svcver.EditServiceVersionPanel;
import net.svcret.admin.client.ui.dash.ServiceDashboardPanel;
import net.svcret.admin.client.ui.layout.BodyPanel;
import net.svcret.admin.client.ui.layout.BreadcrumbPanel;
import net.svcret.admin.shared.util.StringUtil;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Panel;

public class NavProcessor {

	public static final PagesEnum DEFAULT_PAGE = PagesEnum.DSH;
	public static final String SEPARATOR = "__";

	static {
		History.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> theEvent) {
				navigate();
				BreadcrumbPanel.handleUpdate();
			}
		});
	}

	public static String currentTokenArgs() {
		String token = getCurrentToken();
		token = token.replaceAll(".*__", "");
		if (token.length() < 5) {
			return "";
		}
		return token.substring(4);
	}

	public static ClickHandler getBackHandler() {
		return new ClickHandler() {
			@Override
			public void onClick(ClickEvent theEvent) {
				String[] token = getCurrentToken().split(SEPARATOR);
				if (token.length < 2) {
					goHome();
				} else {
					StringBuilder b = new StringBuilder();
					for (int i = 0; i < token.length - 1; i++) {
						String string = token[i];
						if (b.length() > 0) {
							b.append(SEPARATOR);
						}
						b.append(string);
					}
					History.newItem(b.toString());
				}
			}
		};
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

	public static String getLastTokenBefore(PagesEnum... thePages) {
		HashSet<String> names = new HashSet<String>();
		for (PagesEnum pagesEnum : thePages) {
			names.add(pagesEnum.name());
		}

		String[] parts = getCurrentToken().split(SEPARATOR);
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

	public static String getTokenAddDomainStep2(long theId) {
		String token = getCurrentToken();
		if (!token.isEmpty()) {
			token = token + SEPARATOR;
		}
		token = token + PagesEnum.AD2 + "_" + theId;
		token = removeDuplicates(token);
		return token;
	}

	public static String getTokenAddService(boolean theAddToHistory, long theDomainPid) {
		String token = "";
		if (theAddToHistory) {
			token = getCurrentToken();
			if (!token.isEmpty()) {
				token = token + SEPARATOR;
			}
		}
		token = token + PagesEnum.ASE + "_" + theDomainPid;
		token = removeDuplicates(token);
		return token;
	}

	public static String getTokenAddServiceVersion(boolean theAddToHistory, Long theDomainId, Long theServiceId, Long theUncommittedSessionId) {
		String token = "";
		if (theAddToHistory) {
			token = getCurrentToken();
			if (!token.isEmpty()) {
				token = token + SEPARATOR;
			}
		}

		if (theUncommittedSessionId != null) {
			token = token + PagesEnum.ASV + "_" + theDomainId + "_" + theServiceId + "_" + theUncommittedSessionId;
		} else {
			token = token + PagesEnum.ASV;
		}

		token = removeDuplicates(token);
		return token;
	}

	public static String getTokenAddServiceVersionStep2(long theDomainPid, long theServicePid, long theVersionPid) {
		String token = "";
		token = getCurrentToken();
		if (!token.isEmpty()) {
			token = token + SEPARATOR;
		}

		token = token + PagesEnum.AV2 + "_" + theDomainPid + "_" + theServicePid + "_" + theVersionPid;

		token = removeDuplicates(token);
		return token;
	}

	public static String getTokenDeleteDomain(boolean theAddToHistory, long theDomainPid) {
		String token = "";
		if (theAddToHistory) {
			token = getCurrentToken();
			if (!token.isEmpty()) {
				token = token + SEPARATOR;
			}
		}
		token = token + PagesEnum.DDO + "_" + theDomainPid;
		token = removeDuplicates(token);
		return token;
	}

	public static String getTokenDeleteService(boolean theAddToHistory, long theDomainPid, long theServicePid) {
		String token = "";
		if (theAddToHistory) {
			token = getCurrentToken();
			if (!token.isEmpty()) {
				token = token + SEPARATOR;
			}
		}
		token = token + PagesEnum.DSE + "_" + theDomainPid + "_" + theServicePid;
		token = removeDuplicates(token);
		return token;
	}

	public static String getTokenEditService(boolean theAddToHistory, long theDomainPid, long theServicePid) {
		String token = "";
		if (theAddToHistory) {
			token = getCurrentToken();
			if (!token.isEmpty()) {
				token = token + SEPARATOR;
			}
		}
		token = token + PagesEnum.ESE + "_" + theDomainPid + "_" + theServicePid;
		token = removeDuplicates(token);
		return token;
	}

	public static String getTokenEditDomain(boolean theAddToHistory, long theDomainPid) {
		String token = "";
		if (theAddToHistory) {
			token = getCurrentToken();
			if (!token.isEmpty()) {
				token = token + SEPARATOR;
			}
		}
		token = token + PagesEnum.EDO + "_" + theDomainPid;
		token = removeDuplicates(token);
		return token;
	}

	// public static Long getParamAddServiceVersionUncommittedId() {
	// String current = getCurrentToken();
	// if (!current.startsWith(PagesEnum.ASV.name() + "_")) {
	// return null;
	// }
	//
	// String numStr = current.substring(4);
	// if (!numStr.matches("^[0-9]+$")) {
	// return null;
	// }
	//
	// return Long.parseLong(numStr);
	// }

	public static String getTokenEditServiceVersion(long theVersionPid) {
		String token = "";
		token = getCurrentToken();
		if (!token.isEmpty()) {
			token = token + SEPARATOR;
		}

		token = token + PagesEnum.ESV + "_" + theVersionPid;

		token = removeDuplicates(token);
		return token;
	}

	public static String getTokenEditUser(boolean theAddToHistory, long theUserPid) {
		String token = "";
		if (theAddToHistory) {
			token = getCurrentToken();
			if (!token.isEmpty()) {
				token = token + SEPARATOR;
			}
		}
		token = token + PagesEnum.EDU + "_" + theUserPid;
		token = removeDuplicates(token);
		return token;
	}


	public static void goHome() {
		navigateToDefault();
	}

	public static void navigate() {

		PagesEnum page = getCurrentPage();
		String args = currentTokenArgs();

		// try {
		Panel panel = null;
		switch (page) {
		case ADD:
			panel = new AddDomainPanel();
			break;
		case AD2:
			panel = new AddDomainStep2Panel(Long.parseLong(args));
			break;
		case AHL:
			panel = new AuthenticationHostsPanel();
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
			if (argsSplit.length == 3 && StringUtil.positiveInt(argsSplit[0]) && StringUtil.positiveInt(argsSplit[1]) && positiveInt(argsSplit[2])) {
				panel = new AddServiceVersionPanel(Long.parseLong(argsSplit[0]), Long.parseLong(argsSplit[1]), Long.parseLong(argsSplit[2]));
			} else if (argsSplit.length == 2 && StringUtil.positiveInt(argsSplit[0]) && StringUtil.positiveInt(argsSplit[1])) {
				panel = new AddServiceVersionPanel(Long.parseLong(argsSplit[0]), Long.parseLong(argsSplit[1]), null);
			} else {
				panel = new AddServiceVersionPanel(null, null, null);
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
		case CFG:
			panel=new ConfigPanel();
			break;
		case DDO:
			panel = new DeleteDomainPanel(Long.parseLong(args));
			break;
		case DSH:
			panel = new ServiceDashboardPanel();
			break;
		case DSE:
			argsSplit = args.split("_");
			if (argsSplit.length < 2) {
				navigateToDefault();
			} else {
				panel = new DeleteServicePanel(Long.parseLong(argsSplit[0]), Long.parseLong(argsSplit[1]));
			}
			break;
		case EDO:
			panel = new EditDomainPanel(Long.parseLong(args));
			break;
		case EDU:
			panel = new EditUserPanel(Long.parseLong(args));
			break;
		case ESE:
			argsSplit = args.split("_");
			if (argsSplit.length < 2) {
				navigateToDefault();
			} else {
				panel = new EditServicePanel(Long.parseLong(argsSplit[0]), Long.parseLong(argsSplit[1]));
			}
			break;
		case ESV:
			panel = new EditServiceVersionPanel(Long.parseLong(args));
			break;
		case EUL:
			panel = new EditUsersPanel();
			break;
		case HCC:
			panel = new HttpClientConfigsPanel();
			break;
		case SEC:
			panel= new ServiceCatalogPanel();
			break;
		}

		if (panel == null) {
			navigateToDefault();
		}

		BodyPanel.getInstance().setContents(panel);

		// } catch (Exception e) {
		// Model.handleFailure(e);
		// navigateToDefault();
		// }

	}

	private static void navigateToDefault() {
		History.newItem("");
	}

	public static void navRoot(PagesEnum thePage) {
		History.newItem(thePage.name(), true);
	}

	private static String removeDuplicates(String theToken) {

		String[] parts = theToken.split(SEPARATOR);
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
				retVal.append(SEPARATOR);
			}
			retVal.append(next);
		}

		return retVal.toString();

	}

}
