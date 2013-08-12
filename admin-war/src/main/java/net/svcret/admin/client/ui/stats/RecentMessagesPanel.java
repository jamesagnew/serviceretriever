package net.svcret.admin.client.ui.stats;

import static net.svcret.admin.client.AdminPortal.*;

import java.util.List;

import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.client.ui.components.HtmlH1;
import net.svcret.admin.shared.model.GRecentMessage;
import net.svcret.admin.shared.model.GRecentMessageLists;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

public class RecentMessagesPanel extends FlowPanel {

	private String myUnitType;

	public RecentMessagesPanel(GRecentMessageLists theLists, String theUnitType) {
		addStyleName(CssConstants.CONTENT_INNER_PANEL);

		myUnitType = theUnitType;

		if (!theLists.hasAtLeastOneList()) {
			this.add(new Label(MSGS.serviceVersionStats_NoRecentMsgs(theUnitType)));
		}

		if (theLists.getSuccessList() != null) {
			addRecentTransactionTable(MSGS.serviceVersionStats_RecentSuccessTitle(), theLists.getKeepSuccess(), theLists.getSuccessList(), "Success");
		}

		if (theLists.getFaultList() != null) {
			addRecentTransactionTable(MSGS.serviceVersionStats_RecentFaultTitle(), theLists.getKeepFault(), theLists.getFaultList(), "Fault");
		}

		if (theLists.getFailList() != null) {
			addRecentTransactionTable(MSGS.serviceVersionStats_RecentFailTitle(), theLists.getKeepFail(), theLists.getFailList(), "Failed");
		}

		if (theLists.getSecurityFailList() != null) {
			addRecentTransactionTable(MSGS.serviceVersionStats_RecentSecurityFailTitle(), theLists.getKeepSecurityFail(), theLists.getSecurityFailList(), "Security Failure");
		}

	}

	private void addRecentTransactionTable(String theTitle, int theNumToKeep, List<GRecentMessage> theList, String theTransactionType) {
		this.add(new HtmlH1(theTitle));
		this.add(new Label(MSGS.serviceVersionStats_RecentConfigNum(myUnitType, theNumToKeep, theTransactionType)));
		this.add(new RecentMessagesGrid(theList));
	}

}
