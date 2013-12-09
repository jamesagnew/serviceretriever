package net.svcret.admin.client.ui.log;

import static net.svcret.admin.client.AdminPortal.MSGS;

import java.util.ArrayList;
import java.util.List;

import net.svcret.admin.client.MyResources;
import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.client.ui.components.HtmlH1;
import net.svcret.admin.client.ui.components.LoadingSpinner;
import net.svcret.admin.shared.model.GRecentMessage;
import net.svcret.admin.shared.model.GRecentMessageLists;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.Widget;

public abstract class BaseRecentMessagesPanel extends FlowPanel {
	private FlowPanel myTopPanel;
	private FlowPanel myContentPanel;
	private TabPanel myTransactionTypes;
	private LoadingSpinner myLoadingSpinner;
	private Label myFailedToLoadLastTransaction;

	public BaseRecentMessagesPanel() {

		myTopPanel = new FlowPanel();
		add(myTopPanel);

		myFailedToLoadLastTransaction = new Label();
		myFailedToLoadLastTransaction.setVisible(false);
		
		myTopPanel.setStylePrimaryName(CssConstants.MAIN_PANEL);

		Label titleLabel = new Label(getDialogTitle());
		titleLabel.setStyleName(CssConstants.MAIN_PANEL_TITLE);
		myTopPanel.add(titleLabel);

		myContentPanel = new FlowPanel();
		myContentPanel.addStyleName(CssConstants.CONTENT_INNER_PANEL);
		myContentPanel.add(myFailedToLoadLastTransaction);
		myTopPanel.add(myContentPanel);

		myLoadingSpinner = new LoadingSpinner();
		myLoadingSpinner.show();
		myContentPanel.add(myLoadingSpinner);
		
		myTransactionTypes = new TabPanel();
		myTransactionTypes.addStyleName(CssConstants.CONTENT_OUTER_TAB_PANEL);
		add(myTransactionTypes);

	}

	protected void showFailedToLoadLastTransaction() {
		myFailedToLoadLastTransaction.setVisible(true);
		myFailedToLoadLastTransaction.setText("Could not find transaction! It has probably been purged from the database. Please select another.");
		myFailedToLoadLastTransaction.addStyleName(MyResources.CSS.innerAlertPane());
	}
	
	protected abstract String getDialogTitle();

	protected void setRecentMessages(GRecentMessageLists theLists, String theUnit, Widget theHeaderPanel) {
		myLoadingSpinner.hideCompletely();

		myContentPanel.clear();
		myContentPanel.add(myFailedToLoadLastTransaction);
		myContentPanel.add(theHeaderPanel);

		myTransactionTypes.clear();
		
		if (theLists.getSuccessList() != null || theLists.getFaultList() != null || theLists.getFailList() != null || theLists.getSecurityFailList() != null) {
			List<GRecentMessage> list = new ArrayList<GRecentMessage>();
			if (theLists.getSuccessList() != null) {
				list.addAll(theLists.getSuccessList());
			}
			if (theLists.getFaultList() != null) {
				list.addAll(theLists.getFaultList());
			}
			if (theLists.getFailList() != null) {
				list.addAll(theLists.getFailList());
			}
			if (theLists.getSecurityFailList() != null) {
				list.addAll(theLists.getSecurityFailList());
			}
			addRecentTransactionTable(true, MSGS.serviceVersionStats_CombinedTitle(), 0, list, "Combined");
		}

		if (theLists.getSuccessList() != null) {
			addRecentTransactionTable(false, MSGS.serviceVersionStats_RecentSuccessTitle(), theLists.getKeepSuccess(), theLists.getSuccessList(), "Success");
		} else {
			addNotKeptRecentTransactionTable(theUnit, MSGS.serviceVersionStats_RecentSuccessTitle(), "Success");
		}

		if (theLists.getFaultList() != null) {
			addRecentTransactionTable(false, MSGS.serviceVersionStats_RecentFaultTitle(), theLists.getKeepFault(), theLists.getFaultList(), "Fault");
		} else {
			addNotKeptRecentTransactionTable(theUnit, MSGS.serviceVersionStats_RecentFaultTitle(), "Fault");
		}

		if (theLists.getFailList() != null) {
			addRecentTransactionTable(false, MSGS.serviceVersionStats_RecentFailTitle(), theLists.getKeepFail(), theLists.getFailList(), "Failed");
		} else {
			addNotKeptRecentTransactionTable(theUnit, MSGS.serviceVersionStats_RecentFailTitle(), "Failed");
		}

		if (theLists.getSecurityFailList() != null) {
			addRecentTransactionTable(false, MSGS.serviceVersionStats_RecentSecurityFailTitle(), theLists.getKeepSecurityFail(), theLists.getSecurityFailList(), "Security Failure");
		} else {
			addNotKeptRecentTransactionTable(theUnit, MSGS.serviceVersionStats_RecentSecurityFailTitle(), "Security Failure");
		}

		myTransactionTypes.selectTab(0);
	}

	private void addRecentTransactionTable(boolean isCombined, String theTitle, int theNumToKeep, List<GRecentMessage> theList, String theTransactionType) {

		FlowPanel panel = new FlowPanel();
		panel.add(new HtmlH1(theTitle));
		if (!isCombined) {
			panel.add(new Label(MSGS.serviceVersionStats_RecentConfigNum(getCatalogItemTypeThisPanelIsDisplaying(), theNumToKeep, theTransactionType)));
		}
		panel.add(new RecentMessagesGrid(theList));

		myTransactionTypes.add(panel, theTransactionType + "(" + theList.size() + ")");

	}

	protected abstract String getCatalogItemTypeThisPanelIsDisplaying();

	private void addNotKeptRecentTransactionTable(String theUnit, String theTitle, String theTransactionType) {

		FlowPanel panel = new FlowPanel();
		panel.add(new HtmlH1(theTitle));
		panel.add(new Label("This " + theUnit + " is not configured to keep any " + theTransactionType + " transactions."));

		// TODO: add a link to configure the SV

		myTransactionTypes.add(panel, theTransactionType);

	}

}
