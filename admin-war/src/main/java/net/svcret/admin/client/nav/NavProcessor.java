package net.svcret.admin.client.nav;

import static net.svcret.admin.shared.util.StringUtil.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import net.svcret.admin.client.ui.catalog.ServiceCatalogPanel;
import net.svcret.admin.client.ui.config.ConfigPanel;
import net.svcret.admin.client.ui.config.HttpClientConfigsPanel;
import net.svcret.admin.client.ui.config.auth.AddUserPanel;
import net.svcret.admin.client.ui.config.auth.AuthenticationHostsPanel;
import net.svcret.admin.client.ui.config.auth.EditUserPanel;
import net.svcret.admin.client.ui.config.auth.EditUsersPanel;
import net.svcret.admin.client.ui.config.domain.AddDomainPanel;
import net.svcret.admin.client.ui.config.domain.AddDomainStep2Panel;
import net.svcret.admin.client.ui.config.domain.DeleteDomainPanel;
import net.svcret.admin.client.ui.config.domain.DeleteServiceVersionPanel;
import net.svcret.admin.client.ui.config.domain.EditDomainPanel;
import net.svcret.admin.client.ui.config.lib.CreateLibraryMessageBasedOnRecentTransactionPanel;
import net.svcret.admin.client.ui.config.lib.EditLibraryMessagePanel;
import net.svcret.admin.client.ui.config.monitor.AddMonitorRulePanel;
import net.svcret.admin.client.ui.config.monitor.EditMonitorRulePanel;
import net.svcret.admin.client.ui.config.monitor.MonitorRulesPanel;
import net.svcret.admin.client.ui.config.service.AddServicePanel;
import net.svcret.admin.client.ui.config.service.DeleteServicePanel;
import net.svcret.admin.client.ui.config.service.EditServicePanel;
import net.svcret.admin.client.ui.config.svcver.AddServiceVersionPanel;
import net.svcret.admin.client.ui.config.svcver.AddServiceVersionStep2Panel;
import net.svcret.admin.client.ui.config.svcver.EditServiceVersionPanel;
import net.svcret.admin.client.ui.config.svcver.ServiceVersionMessageLibraryPanel;
import net.svcret.admin.client.ui.config.svcver.ServiceVersionRecentMessagePanel;
import net.svcret.admin.client.ui.dash.IDestroyable;
import net.svcret.admin.client.ui.dash.ServiceDashboardPanel;
import net.svcret.admin.client.ui.layout.BodyPanel;
import net.svcret.admin.client.ui.layout.BreadcrumbPanel;
import net.svcret.admin.client.ui.stats.ServiceVersionStatsPanel;
import net.svcret.admin.client.ui.stats.UserStatsPanel;
import net.svcret.admin.client.ui.stats.ViewRecentMessageForServiceVersionPanel;
import net.svcret.admin.client.ui.stats.ViewRecentMessageForUserPanel;
import net.svcret.admin.client.ui.test.ReplayLibraryMessagePanel;
import net.svcret.admin.client.ui.test.ReplayMessagePanel;
import net.svcret.admin.client.ui.test.ServiceVersionTestPanel;
import net.svcret.admin.shared.enm.MonitorRuleTypeEnum;
import net.svcret.admin.shared.enm.RecentMessageTypeEnum;
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

	public static String getMonitorRules(boolean theAddToHistory) {
		return createArgumentToken(theAddToHistory, PagesEnum.MRL);
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

	public static String getTokenAddMonitorRule(boolean theAddToHistory, int theAddType) {
		return createArgumentToken(theAddToHistory, PagesEnum.AMR, theAddType);
	}

	public static String getTokenAddService(boolean theAddToHistory, long theDomainPid) {
		return createArgumentToken(theAddToHistory, PagesEnum.ASE, theDomainPid);
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

	public static String getTokenAddUser(boolean theAddToHistory, long theAuthHostPid) {
		String token = "";
		if (theAddToHistory) {
			token = getCurrentToken();
			if (!token.isEmpty()) {
				token = token + SEPARATOR;
			}
		}
		token = token + PagesEnum.ADU + "_" + theAuthHostPid;
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

	public static String getTokenDeleteServiceVersion(boolean theAddToHistory, long thePid) {
		String token = "";
		if (theAddToHistory) {
			token = getCurrentToken();
			if (!token.isEmpty()) {
				token = token + SEPARATOR;
			}
		}
		token = token + PagesEnum.DSV + "_" + thePid;
		token = removeDuplicates(token);
		return token;
	}

	public static String getTokenEditDomain(boolean theAddToHistory, long theDomainPid) {
		return createArgumentToken(theAddToHistory, PagesEnum.EDO, theDomainPid);
	}

	public static String getTokenEditLibraryMessage(boolean theAddToHistory, long theMessagePid) {
		return createArgumentToken(theAddToHistory, PagesEnum.ELM, theMessagePid);
	}

	public static String getTokenEditMonitorRule(boolean theAddToHistory, long theRulePid) {
		return createArgumentToken(theAddToHistory, PagesEnum.EMR, theRulePid);
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

	public static String getTokenEditServiceVersion(boolean theAddToHistory, long theVersionPid) {
		String token = "";
		if (theAddToHistory) {
			token = getCurrentToken();
			if (!token.isEmpty()) {
				token = token + SEPARATOR;
			}
		}

		token = token + PagesEnum.ESV + "_" + theVersionPid;

		token = removeDuplicates(token);

		return createArgumentToken(theAddToHistory, PagesEnum.ESV, theVersionPid);
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

	public static String getTokenReplayLibraryMessage(boolean theAddToHistory, long theServiceVersionPid, long theLibraryMessagePid) {
		return createArgumentToken(theAddToHistory, PagesEnum.RLM, theServiceVersionPid, theLibraryMessagePid);
	}

	public static String getTokenReplayMessage(boolean theAddToHistory, long theMessagePid) {
		return createArgumentToken(theAddToHistory, PagesEnum.RPM, theMessagePid);
	}

	public static String getTokenSaveRecentMessageToLibrary(boolean theAddToHistory, RecentMessageTypeEnum theType, long thePid) {
		return createArgumentToken(theAddToHistory, PagesEnum.SML, theType, Long.toString(thePid));
	}

	public static String getTokenServiceVersionStats(boolean theAddToHistory, long theServiceVersionPid) {
		return createArgumentToken(theAddToHistory, PagesEnum.SVS, theServiceVersionPid);
	}

	public static String getTokenServiceVersionMessageLibrary(boolean theAddToHistory, long theServiceVersionPid) {
		return createArgumentToken(theAddToHistory, PagesEnum.SVL, theServiceVersionPid);
	}

	public static String getTokenTestServiceVersion(boolean theAddToHistory, long theServiceVersionPid) {
		return createArgumentToken(theAddToHistory, PagesEnum.TSV, theServiceVersionPid);
	}

	public static String getTokenViewServiceVersionRecentMessage(boolean theAddToHistory, long thePid) {
		String token = "";
		if (theAddToHistory) {
			token = getCurrentToken();
			if (!token.isEmpty()) {
				token = token + SEPARATOR;
			}
		}
		token = token + PagesEnum.RSV + "_" + thePid;
		token = removeDuplicates(token);
		return token;
	}

	public static String getTokenServiceVersionRecentMessages(boolean theAddToHistory, long thePid) {
		String token = "";
		if (theAddToHistory) {
			token = getCurrentToken();
			if (!token.isEmpty()) {
				token = token + SEPARATOR;
			}
		}
		token = token + PagesEnum.SRM + "_" + thePid;
		token = removeDuplicates(token);
		return token;
	}

	public static String getTokenViewUserRecentMessage(boolean theAddToHistory, long thePid) {
		String token = "";
		if (theAddToHistory) {
			token = getCurrentToken();
			if (!token.isEmpty()) {
				token = token + SEPARATOR;
			}
		}
		token = token + PagesEnum.RUS + "_" + thePid;
		token = removeDuplicates(token);
		return token;
	}

	public static String getTokenViewUserStats(boolean theAddToHistory, long theUserPid) {
		String token = "";
		if (theAddToHistory) {
			token = getCurrentToken();
			if (!token.isEmpty()) {
				token = token + SEPARATOR;
			}
		}
		token = token + PagesEnum.VUS + "_" + theUserPid;
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
		case SVL:
			panel = new ServiceVersionMessageLibraryPanel(Long.parseLong(args));
			break;
		case MRL:
			panel = new MonitorRulesPanel();
			break;
		case ADU:
			panel = new AddUserPanel(Long.parseLong(args));
			break;
		case SRM:
			panel = new ServiceVersionRecentMessagePanel(Long.parseLong(args));
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
		case SML:
			String[] argsSplit = args.split("_");
			RecentMessageTypeEnum type = RecentMessageTypeEnum.valueOf(argsSplit[0]);
			panel = new CreateLibraryMessageBasedOnRecentTransactionPanel(type, Long.parseLong(argsSplit[1]));
			break;
		case ASV:
			argsSplit = args.split("_");
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
			panel = new ConfigPanel();
			break;
		case DDO:
			panel = new DeleteDomainPanel(Long.parseLong(args));
			break;
		case DSV:
			panel = new DeleteServiceVersionPanel(Long.parseLong(args));
			break;
		case RLM:
			argsSplit = args.split("_");
			if (argsSplit.length < 2) {
				navigateToDefault();
			} else {
				panel = new ReplayLibraryMessagePanel(Long.parseLong(argsSplit[0]), Long.parseLong(argsSplit[1]));
			}
			break;
		case TSV:
			if (StringUtil.isBlank(args)) {
				panel = new ServiceVersionTestPanel();
			} else {
				panel = new ServiceVersionTestPanel(Long.parseLong(args));
			}
			break;
		case RPM:
			panel = new ReplayMessagePanel(Long.parseLong(args));
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
		case RSV:
			panel = new ViewRecentMessageForServiceVersionPanel(Long.parseLong(args));
			break;
		case RUS:
			panel = new ViewRecentMessageForUserPanel(Long.parseLong(args));
			break;
		case EDO:
			panel = new EditDomainPanel(Long.parseLong(args));
			break;
		case ELM:
			panel = new EditLibraryMessagePanel(Long.parseLong(args));
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
			panel = new ServiceCatalogPanel();
			break;
		case SVS:
			panel = new ServiceVersionStatsPanel(Long.parseLong(args));
			break;
		case VUS:
			panel = new UserStatsPanel(Long.parseLong(args));
			break;
		case AMR:
			panel = new AddMonitorRulePanel(MonitorRuleTypeEnum.values()[Integer.parseInt(args)]);
			break;
		case EMR:
			panel = new EditMonitorRulePanel(Long.parseLong(args));
			break;
		}

		if (panel == null) {
			navigateToDefault();
		}

		Panel existingContents = BodyPanel.getInstance().getContents();
		if (existingContents != null) {
			if (existingContents instanceof IDestroyable) {
				((IDestroyable) existingContents).destroy();
			}
		}

		BodyPanel.getInstance().setContents(panel);

		// } catch (Exception e) {
		// Model.handleFailure(e);
		// navigateToDefault();
		// }

	}

	public static void navRoot(PagesEnum thePage) {
		History.newItem(thePage.name(), true);
	}

	private static String createArgumentToken(boolean theAddToHistory, PagesEnum thePage, Object... theArgs) {
		String token = createArgumentToken(theAddToHistory, thePage);
		for (Object next : theArgs) {
			token = token + "_" + next.toString();
		}
		token = removeDuplicates(token);
		return token;
	}


//	private static String createArgumentToken(boolean theAddToHistory, PagesEnum thePage, long... theArgument) {
//		String token = createArgumentToken(theAddToHistory, thePage);
//		for (long next : theArgument) {
//			token = token + "_" + next;
//		}
//		token = removeDuplicates(token);
//		return token;
//	}

	private static String createArgumentToken(boolean theAddToHistory, PagesEnum thePage) {
		String token = "";
		if (theAddToHistory) {
			token = getCurrentToken();
			if (!token.isEmpty()) {
				token = token + SEPARATOR;
			}
		}
		token = token + thePage;
		return token;
	}

	private static String getCurrentToken() {
		String token = History.getToken();
		token = StringUtil.defaultString(token, DEFAULT_PAGE.name());
		return token;
	}

	private static void navigateToDefault() {
		History.newItem("");
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
