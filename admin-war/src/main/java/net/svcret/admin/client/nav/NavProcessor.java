package net.svcret.admin.client.nav;

import static net.svcret.admin.client.nav.PagesEnum.AV2;
import static net.svcret.admin.client.nav.PagesEnum.DDO;
import static net.svcret.admin.client.nav.PagesEnum.DSE;
import static net.svcret.admin.client.nav.PagesEnum.DSV;
import static net.svcret.admin.client.nav.PagesEnum.EDU;
import static net.svcret.admin.client.nav.PagesEnum.ESE;
import static net.svcret.admin.client.nav.PagesEnum.RSV;
import static net.svcret.admin.client.nav.PagesEnum.RUS;
import static net.svcret.admin.client.nav.PagesEnum.SRM;
import static net.svcret.admin.shared.util.StringUtil.positiveInt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;

import net.svcret.admin.client.ui.api.IDestroyable;
import net.svcret.admin.client.ui.catalog.ServiceCatalogPanel;
import net.svcret.admin.client.ui.config.ConfigPanel;
import net.svcret.admin.client.ui.config.auth.AddUserPanel;
import net.svcret.admin.client.ui.config.auth.AuthenticationHostsPanel;
import net.svcret.admin.client.ui.config.auth.EditUserPanel;
import net.svcret.admin.client.ui.config.auth.EditUsersPanel;
import net.svcret.admin.client.ui.config.domain.AddDomainPanel;
import net.svcret.admin.client.ui.config.domain.AddDomainStep2Panel;
import net.svcret.admin.client.ui.config.domain.DeleteDomainPanel;
import net.svcret.admin.client.ui.config.domain.DeleteServiceVersionPanel;
import net.svcret.admin.client.ui.config.domain.EditDomainPanel;
import net.svcret.admin.client.ui.config.http.HttpClientConfigsPanel;
import net.svcret.admin.client.ui.config.lib.CreateLibraryMessageBasedOnRecentTransactionPanel;
import net.svcret.admin.client.ui.config.lib.CreateNewLibraryMessagePanel;
import net.svcret.admin.client.ui.config.lib.EditLibraryMessagePanel;
import net.svcret.admin.client.ui.config.lib.MessageLibraryPanel;
import net.svcret.admin.client.ui.config.monitor.AddMonitorRulePanel;
import net.svcret.admin.client.ui.config.monitor.EditMonitorRulePanel;
import net.svcret.admin.client.ui.config.monitor.MonitorRulesPanel;
import net.svcret.admin.client.ui.config.monitor.ViewActiveCheckOutcomePanel;
import net.svcret.admin.client.ui.config.service.AddServicePanel;
import net.svcret.admin.client.ui.config.service.DeleteServicePanel;
import net.svcret.admin.client.ui.config.service.EditServicePanel;
import net.svcret.admin.client.ui.config.svcver.AddServiceVersionPanel;
import net.svcret.admin.client.ui.config.svcver.AddServiceVersionStep2Panel;
import net.svcret.admin.client.ui.config.svcver.CloneServiceVersionPanel;
import net.svcret.admin.client.ui.config.svcver.EditServiceVersionPanel;
import net.svcret.admin.client.ui.dash.ServiceDashboardPanel;
import net.svcret.admin.client.ui.dash.UrlDashboardPanel;
import net.svcret.admin.client.ui.layout.BodyPanel;
import net.svcret.admin.client.ui.layout.BreadcrumbPanel;
import net.svcret.admin.client.ui.log.ServiceVersionRecentMessagePanel;
import net.svcret.admin.client.ui.log.UserRecentMessagesPanel;
import net.svcret.admin.client.ui.log.ViewRecentMessageForServiceVersionPanel;
import net.svcret.admin.client.ui.log.ViewRecentMessageForUserPanel;
import net.svcret.admin.client.ui.stats.ServiceVersionStatsPanel;
import net.svcret.admin.client.ui.stats.UserStatsPanel;
import net.svcret.admin.client.ui.sticky.StickySessionListPanel;
import net.svcret.admin.client.ui.test.ReplayLibraryMessagePanel;
import net.svcret.admin.client.ui.test.ReplayMessagePanel;
import net.svcret.admin.client.ui.test.ServiceVersionTestPanel;
import net.svcret.admin.shared.enm.MonitorRuleTypeEnum;
import net.svcret.admin.shared.enm.RecentMessageTypeEnum;
import net.svcret.admin.shared.model.HierarchyEnum;
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

	private static List<String> ourInMemoryTokens = new ArrayList<String>();
	private static List<Panel> ourInMemoryPages = new ArrayList<Panel>();
	private static String ourCurrentTokenForUnitTest;

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

	public static String getBackToken() {
		String token = History.getToken();
		String[] parts = token.split(SEPARATOR);
		if (parts.length < 2) {
			return DEFAULT_PAGE.name();
		} else {
			StringBuilder b = new StringBuilder();
			int index = 0;
			for (String next : parts) {
				index++;
				if (index == parts.length) {
					break;
				}
				if (b.length() > 0) {
					b.append(SEPARATOR);
				}
				b.append(next);
			}
			return b.toString();
		}
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

	public static String getMonitorRules() {
		return createArgumentToken(PagesEnum.MRL);
	}

	public static String getTokenAddDomainStep2(long theId) {
		String token = getCurrentToken();
		if (!token.isEmpty()) {
			token = token + SEPARATOR;
		}
		token = token + PagesEnum.AD2 + "_" + theId;
		return token;
	}

	public static String getTokenAddMonitorRule(int theAddType) {
		return createArgumentToken(PagesEnum.AMR, theAddType);
	}

	public static String getTokenAddService(long theDomainPid) {
		return createArgumentToken(PagesEnum.ASE, theDomainPid);
	}

	public static String getTokenAddServiceVersion(Long theDomainId, Long theServiceId, Long theUncommittedSessionId) {
		if (theUncommittedSessionId != null) {
			return createArgumentToken(PagesEnum.ASV, theDomainId, theServiceId, theUncommittedSessionId);
		} else if (theDomainId != null && theServiceId != null) {
			return createArgumentToken(PagesEnum.ASV, theDomainId, theServiceId);
		} else {
			return createArgumentToken(PagesEnum.ASV);
		}
	}

	public static String getTokenAddServiceVersionStep2(long theVersionPid) {
		return createArgumentToken(AV2, theVersionPid);
	}

	public static String getTokenAddUser(long theAuthHostPid) {
		return createArgumentToken(PagesEnum.ADU, theAuthHostPid);
	}

	public static String getTokenDeleteDomain(long theDomainPid) {
		return createArgumentToken(DDO, theDomainPid);
	}

	public static String getTokenDeleteService(long theDomainPid, long theServicePid) {
		return createArgumentToken(DSE, theDomainPid, theServicePid);

	}

	public static String getTokenDeleteServiceVersion(long thePid) {
		return createArgumentToken(DSV, thePid);
	}

	public static String getTokenEditDomain(long theDomainPid) {
		return createArgumentToken(PagesEnum.EDO, theDomainPid);
	}

	public static String getTokenEditLibraryMessage(long theMessagePid) {
		return createArgumentToken(PagesEnum.ELM, theMessagePid);
	}

	public static String getTokenEditMonitorRule(long theRulePid) {
		return createArgumentToken(PagesEnum.EMR, theRulePid);
	}

	public static String getTokenEditService(long theDomainPid, long theServicePid) {
		return createArgumentToken(ESE, theDomainPid, theServicePid);
	}

	public static String getTokenEditServiceVersion(long theVersionPid) {
		return createArgumentToken(PagesEnum.ESV, theVersionPid);
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

	public static String getTokenEditUser(long theUserPid) {
		return createArgumentToken(EDU, theUserPid);
	}

	public static String getTokenReplayLibraryMessage(long theServiceVersionPid, long theLibraryMessagePid) {
		return createArgumentToken(PagesEnum.RLM, theServiceVersionPid, theLibraryMessagePid);
	}

	public static String getTokenReplayMessageForUser(long theMessagePid) {
		return createArgumentToken(PagesEnum.RPU, theMessagePid);
	}

	public static String getTokenReplayMessageForServiceVersion(long theMessagePid) {
		return createArgumentToken(PagesEnum.RPM, theMessagePid);
	}

	public static String getTokenSaveRecentMessageToLibrary(RecentMessageTypeEnum theType, long thePid) {
		return createArgumentToken(PagesEnum.SML, theType, Long.toString(thePid));
	}

	public static String getTokenServiceVersionStats(long theServiceVersionPid) {
		return createArgumentToken(PagesEnum.SVS, theServiceVersionPid);
	}

	public static String getTokenMessageLibrary() {
		return createArgumentToken(PagesEnum.MLB);
	}

	public static String getTokenMessageLibrary(HierarchyEnum theType, long thePid) {
		return createArgumentToken(PagesEnum.MLB, theType, thePid);
	}

	public static String getTokenMessageLibraryAdd() {
		return createArgumentToken(PagesEnum.CLM);
	}

	public static String getTokenMessageLibraryAdd(HierarchyEnum theType, long thePid) {
		return createArgumentToken(PagesEnum.CLM, theType, thePid);
	}

	public static String getTokenEditHttpClientConfig(long thePid) {
		return createArgumentToken(PagesEnum.HCC, thePid);
	}

	public static String getTokenTestServiceVersion(long theServiceVersionPid) {
		return createArgumentToken(PagesEnum.TSV, theServiceVersionPid);
	}

	public static String getTokenViewServiceVersionRecentMessage(long theSvcVerPid, long thePid) {
		return createArgumentToken(RSV, theSvcVerPid, thePid);
	}

	public static String getTokenServiceVersionRecentMessages(long thePid, boolean theFailedToLoadLast) {
		return createArgumentToken(SRM, thePid, Boolean.toString(theFailedToLoadLast));
	}

	public static String getTokenViewUserRecentMessage(long theUserPid, long thePid) {
		return createArgumentToken(RUS, theUserPid, thePid);
	}

	public static String getTokenUserRecentMessages(long theUserPid, boolean theFailedToLoadLast) {
		return createArgumentToken(PagesEnum.URM, theUserPid, Boolean.toString(theFailedToLoadLast));
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
		case UDS:
			panel = new UrlDashboardPanel();
			break;
		case SSL:
			panel = new StickySessionListPanel();
			break;
		case ADD:
			panel = new AddDomainPanel();
			break;
		case AD2:
			panel = new AddDomainStep2Panel(Long.parseLong(args));
			break;
		case CSV:
			panel = new CloneServiceVersionPanel(Long.parseLong(args));
			break;
		case UST:
			panel = new UserStatsPanel(Long.parseLong(args));
			break;
		case MLB:
			String[] argsSplit = args.split("_");
			if (argsSplit.length != 2) {
				panel = new MessageLibraryPanel();
			} else {
				panel = new MessageLibraryPanel(HierarchyEnum.valueOf(argsSplit[0]), Long.parseLong(argsSplit[1]));
			}
			break;
		case CLM:
			argsSplit = args.split("_");
			if (argsSplit.length != 2) {
				panel = new CreateNewLibraryMessagePanel();
			} else {
				panel = new CreateNewLibraryMessagePanel(HierarchyEnum.valueOf(argsSplit[0]), Long.parseLong(argsSplit[1]));
			}
			break;
		case MRL:
			panel = new MonitorRulesPanel();
			break;
		case ADU:
			panel = new AddUserPanel(Long.parseLong(args));
			break;
		case SRM:
			argsSplit = args.split("_");
			panel = new ServiceVersionRecentMessagePanel(Long.parseLong(argsSplit[0]), Boolean.parseBoolean(argsSplit[1]));
			break;
		case VAC:
			argsSplit = args.split("_");
			panel = new ViewActiveCheckOutcomePanel(Long.parseLong(argsSplit[0]), Long.parseLong(argsSplit[1]), Long.parseLong(argsSplit[2]));
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
			argsSplit = args.split("_");
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
			panel = new AddServiceVersionStep2Panel(Long.parseLong(args));
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
		case RPU:
			panel = new ReplayMessagePanel(ReplayMessagePanel.MSGTYPE_USER, Long.parseLong(args));
			break;
		case RPM:
			panel = new ReplayMessagePanel(ReplayMessagePanel.MSGTYPE_SVCVER, Long.parseLong(args));
			break;
		case DSH:
			panel = ServiceDashboardPanel.getInstance();
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
			argsSplit = args.split("_");
			if (argsSplit.length < 2) {
				navigateToDefault();
			} else {
			panel = new ViewRecentMessageForServiceVersionPanel(Long.parseLong(argsSplit[0]), Long.parseLong(argsSplit[1]));
			}
			break;
		case RUS:
			argsSplit = args.split("_");
			if (argsSplit.length < 2) {
				navigateToDefault();
			} else {
			panel = new ViewRecentMessageForUserPanel(Long.parseLong(argsSplit[0]), Long.parseLong(argsSplit[1]));
			}
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
			if (args.length() == 0) {
				panel = new HttpClientConfigsPanel();
			} else {
				panel = new HttpClientConfigsPanel(Long.parseLong(args));
			}
			break;
		case SEC:
			panel = new ServiceCatalogPanel();
			break;
		case SVS:
			panel = new ServiceVersionStatsPanel(Long.parseLong(args));
			break;
		case URM:
			argsSplit = args.split("_");
			panel = new UserRecentMessagesPanel(Long.parseLong(argsSplit[0]), Boolean.parseBoolean(argsSplit[1]));
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
			return;
		}

		String[] tokenParts = History.getToken().split(SEPARATOR);
		for (ListIterator<String> listIter = ourInMemoryTokens.listIterator(); listIter.hasNext();) {
			int nextIndex = listIter.nextIndex();
			String nextExistingToken = listIter.next();

			if (nextIndex >= tokenParts.length) {
				listIter.remove();
				Panel pageToDelete = ourInMemoryPages.remove(ourInMemoryPages.size() - 1);
				if (pageToDelete instanceof IDestroyable) {
					((IDestroyable) pageToDelete).destroy();
				}
			} else if (!nextExistingToken.equals(tokenParts[nextIndex])) {
				Panel pageToDelete = ourInMemoryPages.get(nextIndex);
				if (pageToDelete instanceof IDestroyable) {
					((IDestroyable) pageToDelete).destroy();
				}
				ourInMemoryPages.set(nextIndex, null);
			}
		}

		for (int nextIndex = 0; nextIndex < tokenParts.length; nextIndex++) {
			if (ourInMemoryTokens.size() <= nextIndex) {
				ourInMemoryTokens.add(tokenParts[nextIndex]);
				if (nextIndex == tokenParts.length - 1) {
					ourInMemoryPages.add(panel);
				}else {
				ourInMemoryPages.add(null);
				}
			} else if (!tokenParts[nextIndex].equals(ourInMemoryTokens.get(nextIndex))) {
				ourInMemoryTokens.set(nextIndex, tokenParts[nextIndex]);
				ourInMemoryPages.add(null);
			} else if (tokenParts[nextIndex].equals(ourInMemoryTokens.get(nextIndex))) {
				if (nextIndex == tokenParts.length - 1) {
					if (ourInMemoryPages.get(nextIndex) == null) {
						ourInMemoryPages.set(nextIndex, panel);
					}
				}
			}
		}

		Panel existingContents = BodyPanel.getInstance().getContents();
		String currentToken = getCurrentToken();
		currentToken = currentToken.replaceAll(".*__", "");

		if (ourInMemoryTokens.contains(currentToken)) {
			int index = ourInMemoryTokens.indexOf(currentToken);
			Panel newContents = ourInMemoryPages.get(index);
			if (newContents != null) {
				while (ourInMemoryTokens.size() > (index + 1)) {
					ourInMemoryTokens.remove(ourInMemoryTokens.size() - 1);
					Panel pageToDelete = ourInMemoryPages.remove(ourInMemoryPages.size() - 1);
					if (pageToDelete instanceof IDestroyable) {
						((IDestroyable) pageToDelete).destroy();
					}
				}
				BodyPanel.getInstance().setContents(newContents);
				return;
			}
		}

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


	private static String createArgumentToken(PagesEnum thePage, Object... theArgs) {
		StringBuilder b= new StringBuilder();
		
		String currentToken = getCurrentToken();
		String[] parts = currentToken.split(SEPARATOR);
		for (String nextToken : parts) {
			if (nextToken.length() < 3) {
				continue;
			}

			String nextType = nextToken.substring(0, 3);
			if (nextType.equals(thePage.name())) {
				break;
			}
			
			if (b.length() > 0) {
				b.append(SEPARATOR);
			}
			b.append(nextToken);
		}
		
		if (b.length() > 0) {
			b.append(SEPARATOR);
		}
		
		b.append(thePage.name());

		for (Object next : theArgs) {
			if (next != null) {
				b.append( "_").append( next.toString());
			}
		}
		
		return b.toString();
	}

	// private static String createArgumentToken(PagesEnum thePage, long... theArgument) {
	// String token = createArgumentToken(thePage);
	// for (long next : theArgument) {
	// token = token + "_" + next;
	// }
	// token = removeDuplicates(token);
	// return token;
	// }


	private static String getCurrentToken() {
		if (ourCurrentTokenForUnitTest != null) {
			return ourCurrentTokenForUnitTest;
		}
		
		String token = History.getToken();
		token = StringUtil.defaultString(token, DEFAULT_PAGE.name());
		return token;
	}

	static void setCurrentTokenForUnitTest(String theCurrentTokenForUnitTest) {
		ourCurrentTokenForUnitTest = theCurrentTokenForUnitTest;
	}

	private static void navigateToDefault() {
		History.newItem("");
	}

//	private static String removeDuplicatesAndFilterPages(String theToken, PagesEnum... thePagesToFilter) {
//
//		Set<String> toFilter = new HashSet<String>();
//		if (thePagesToFilter!=null) {
//			for (PagesEnum next : thePagesToFilter) {
//				toFilter.add(next.name());
//			}
//		}
//		
//		String[] parts = theToken.split(SEPARATOR);
//		List<String> newParts = new ArrayList<String>();
//
//		String prevType = null;
//		for (String nextToken : parts) {
//			if (nextToken.length() < 3) {
//				continue;
//			}
//			
//			String nextType = nextToken.substring(0, 3);
//			if (toFilter.contains(nextType)) {
//				continue;
//			}
//			
//			if (nextType.equals(prevType)) {
//				newParts.remove(newParts.size() - 1);
//			}
//
//			newParts.add(nextToken);
//			prevType = nextType;
//		}
//
//		StringBuilder retVal = new StringBuilder();
//		for (String next : newParts) {
//			if (retVal.length() > 0) {
//				retVal.append(SEPARATOR);
//			}
//			retVal.append(next);
//		}
//
//		return retVal.toString();
//
//	}

	public static String getTokenUserStats(long theUserPid) {
		return createArgumentToken(PagesEnum.UST, theUserPid);
	}

	public static String getTokenCloneServiceVersion(long thePid) {
		return createArgumentToken(PagesEnum.CSV, thePid);
	}

	public static String removeTokens(String theToken, PagesEnum... theTokensToRemove) {
		List<PagesEnum> tokensList = Arrays.asList(theTokensToRemove);
		
		String[] parts = theToken.split(SEPARATOR);
		List<String> newParts = new ArrayList<String>();

		for (String nextToken : parts) {
			if (nextToken.length() < 3) {
				continue;
			}

			String nextType = nextToken.substring(0, 3);
			PagesEnum nextTypeEnum = PagesEnum.valueOf(nextType);
			if (tokensList.contains(nextTypeEnum)) {
				continue;
			}
			
			newParts.add(nextToken);
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

	public static String getTokenViewActiveCheckOutcomes(long theRulePid, long theActiveCheckPid, long theUrlPid) {
		return createArgumentToken(PagesEnum.VAC, theRulePid, theActiveCheckPid, theUrlPid);
	}

}
