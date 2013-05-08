package net.svcret.admin.client.ui.stats;

import static net.svcret.admin.client.AdminPortal.*;

import java.util.List;

import net.svcret.admin.client.ui.components.HtmlH1;
import net.svcret.admin.shared.model.GRecentMessage;
import net.svcret.admin.shared.model.GRecentMessageLists;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

public class RecentMessagesPanel extends FlowPanel {

	public RecentMessagesPanel(GRecentMessageLists theLists, boolean theIsUser) {

		if (!theLists.hasAtLeastOneList()) {
			this.add(new Label(MSGS.serviceVersionStats_NoRecentMsgs()));
		}

		if (theLists.getSuccessList() != null) {
			addRecentTransactionTable(MSGS.serviceVersionStats_RecentSuccessTitle(), theLists.getKeepSuccess(), theLists.getSuccessList(), theIsUser);
		}

		if (theLists.getFaultList() != null) {
			addRecentTransactionTable(MSGS.serviceVersionStats_RecentFaultTitle(), theLists.getKeepFault(), theLists.getFaultList(), theIsUser);
		}

		if (theLists.getFailList() != null) {
			addRecentTransactionTable(MSGS.serviceVersionStats_RecentFailTitle(), theLists.getKeepFail(), theLists.getFailList(), theIsUser);
		}

		if (theLists.getSecurityFailList() != null) {
			addRecentTransactionTable(MSGS.serviceVersionStats_RecentSecurityFailTitle(), theLists.getKeepSecurityFail(), theLists.getSecurityFailList(), theIsUser);
		}
	}

	private void addRecentTransactionTable(String theTitle, int theNumToKeep, List<GRecentMessage> theList, boolean theIsUsers) {
		this.add(new HtmlH1(theTitle));
		this.add(new Label(MSGS.serviceVersionStats_RecentConfigNum(Integer.toString(theNumToKeep))));
		this.add(new RecentMessagesGrid(theList, theIsUsers));
	}

}
