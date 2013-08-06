package net.svcret.admin.client.ui.config.svcver;

import static net.svcret.admin.client.AdminPortal.*;

import java.util.List;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.client.ui.components.HtmlH1;
import net.svcret.admin.client.ui.components.LoadingSpinner;
import net.svcret.admin.client.ui.stats.RecentMessagesGrid;
import net.svcret.admin.shared.IAsyncLoadCallback;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.BaseGServiceVersion;
import net.svcret.admin.shared.model.GRecentMessage;
import net.svcret.admin.shared.model.GRecentMessageLists;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TabPanel;

public class ServiceVersionRecentMessagePanel extends FlowPanel {

	private long myServiceVersionPid;
	private FlowPanel myTopPanel;
	private FlowPanel myContentPanel;
	private TabPanel myTransactionTypes;
	private LoadingSpinner myLoadingSpinner;
	private Label myTitleLabel;

	public ServiceVersionRecentMessagePanel(long theServiceVersionPid) {
		myServiceVersionPid = theServiceVersionPid;

		myTopPanel = new FlowPanel();
		add(myTopPanel);

		myTopPanel.setStylePrimaryName(CssConstants.MAIN_PANEL);

		Label titleLabel = new Label("Recent Transactions");
		titleLabel.setStyleName("mainPanelTitle");
		myTopPanel.add(titleLabel);

		myContentPanel = new FlowPanel();
		myContentPanel.addStyleName(CssConstants.CONTENT_INNER_PANEL);
		myTopPanel.add(myContentPanel);

		myLoadingSpinner = new LoadingSpinner();
		myContentPanel.add(myLoadingSpinner);
		
		AdminPortal.MODEL_SVC.loadRecentTransactionListForServiceVersion(myServiceVersionPid, new AsyncCallback<GRecentMessageLists>() {

			@Override
			public void onFailure(Throwable theCaught) {
				Model.handleFailure(theCaught);
			}

			@Override
			public void onSuccess(GRecentMessageLists theResult) {
				myLoadingSpinner.hideCompletely();
				setRecentMessages(theResult);
			}

		});

	}

	private void setRecentMessages(GRecentMessageLists theLists) {

		myTitleLabel = new Label();
		Model.getInstance().loadServiceVersion(myServiceVersionPid, new IAsyncLoadCallback<BaseGServiceVersion>() {
			@Override
			public void onSuccess(BaseGServiceVersion theResult) {
				myTitleLabel.setText("Displaying recent transactions for version " + theResult.getId());
			}
		});
		myContentPanel.add(myTitleLabel);
		
		myTransactionTypes = new TabPanel();
		myTransactionTypes.addStyleName(CssConstants.CONTENT_OUTER_TAB_PANEL);
		add(myTransactionTypes);

		if (theLists.getSuccessList() != null) {
			addRecentTransactionTable(MSGS.serviceVersionStats_RecentSuccessTitle(), theLists.getKeepSuccess(), theLists.getSuccessList(), "Success");
		}else {
			addNotKeptRecentTransactionTable(MSGS.serviceVersionStats_RecentSuccessTitle(), "Success");
		}

		if (theLists.getFaultList() != null) {
			addRecentTransactionTable(MSGS.serviceVersionStats_RecentFaultTitle(), theLists.getKeepFault(), theLists.getFaultList(), "Fault");
		}else {
			addNotKeptRecentTransactionTable(MSGS.serviceVersionStats_RecentFaultTitle(), "Fault");
		}

		if (theLists.getFailList() != null) {
			addRecentTransactionTable(MSGS.serviceVersionStats_RecentFailTitle(), theLists.getKeepFail(), theLists.getFailList(), "Failed");
		}else {
			addNotKeptRecentTransactionTable(MSGS.serviceVersionStats_RecentFailTitle(), "Failed");
		}

		if (theLists.getSecurityFailList() != null) {
			addRecentTransactionTable(MSGS.serviceVersionStats_RecentSecurityFailTitle(), theLists.getKeepSecurityFail(), theLists.getSecurityFailList(), "Security Failure");
		}else {
			addNotKeptRecentTransactionTable(MSGS.serviceVersionStats_RecentSecurityFailTitle(), "Security Failure");
		}

		myTransactionTypes.selectTab(0);
	}

	private void addRecentTransactionTable(String theTitle, int theNumToKeep, List<GRecentMessage> theList, String theTransactionType) {
		
		FlowPanel panel = new FlowPanel();
		panel.add(new HtmlH1(theTitle));
		panel.add(new Label(MSGS.serviceVersionStats_RecentConfigNum("Service Version", theNumToKeep, theTransactionType)));
		panel.add(new RecentMessagesGrid(theList));
		
		myTransactionTypes.add(panel, theTransactionType + "(" + theList.size() + ")");
		
	}

	private void addNotKeptRecentTransactionTable(String theTitle, String theTransactionType) {
		
		FlowPanel panel = new FlowPanel();
		panel.add(new HtmlH1(theTitle));
		panel.add(new Label("This Service Version is not configured to keep any " + theTransactionType + " transactions."));

		// TODO: add a link to configure the SV 
		
		myTransactionTypes.add(panel, theTransactionType);
		
	}
	
}
