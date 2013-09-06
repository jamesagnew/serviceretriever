package net.svcret.admin.client.ui.stats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.client.ui.components.HtmlH1;
import net.svcret.admin.client.ui.components.LoadingSpinner;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.GPartialUserList;
import net.svcret.admin.shared.model.GUser;
import net.svcret.admin.shared.model.PartialUserListRequest;
import net.svcret.admin.shared.util.ChartParams;
import net.svcret.admin.shared.util.ChartTypeEnum;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;

public class UserStatsPanel extends FlowPanel {

	private FlowPanel myTopPanel;
	private FlowPanel myContentPanel;
	private LoadingSpinner myLoadingSpinner;
	private GPartialUserList myUserList;
	private ListBox myUserPicker;
	private Long myInitialUserPid;
	private FlowPanel myGraphsPanel;
	private TimeRangeSelectorPanel myGraphsTimePicker;
	private FlowPanel myGraphsContentPanel;

	public UserStatsPanel(Long theInitialUserPid) {
		myInitialUserPid = theInitialUserPid;

		myTopPanel = new FlowPanel();
		add(myTopPanel);

		myTopPanel.setStylePrimaryName(CssConstants.MAIN_PANEL);

		Label titleLabel = new Label("User Statistics");
		titleLabel.setStyleName(CssConstants.MAIN_PANEL_TITLE);
		myTopPanel.add(titleLabel);

		myContentPanel = new FlowPanel();
		myContentPanel.addStyleName(CssConstants.CONTENT_INNER_PANEL);
		myTopPanel.add(myContentPanel);

		myLoadingSpinner = new LoadingSpinner();
		myLoadingSpinner.show();
		myContentPanel.add(myLoadingSpinner);

		PartialUserListRequest request = new PartialUserListRequest();
		AdminPortal.MODEL_SVC.loadUsers(request, new AsyncCallback<GPartialUserList>() {

			@Override
			public void onSuccess(GPartialUserList theResult) {
				initUi(theResult);
			}

			@Override
			public void onFailure(Throwable theCaught) {
				Model.handleFailure(theCaught);
			}
		});

	}

	private void initUi(GPartialUserList theUserList) {
		myUserList = theUserList;

		myLoadingSpinner.hideCompletely();

		myTopPanel.add(new Label("This page displays usage statistics for the selected user", true));

		myUserPicker = new ListBox(false);
		myUserPicker.setVisibleItemCount(10);
		myTopPanel.add(myUserPicker);

		// Stats Panel

		myGraphsPanel = new FlowPanel();
		add(myGraphsPanel);

		myGraphsPanel.setStylePrimaryName(CssConstants.MAIN_PANEL);

		Label titleLabel = new Label("Usage Graphs");
		titleLabel.setStyleName(CssConstants.MAIN_PANEL_TITLE);
		myGraphsPanel.add(titleLabel);

		myGraphsContentPanel = new FlowPanel();
		myGraphsContentPanel.addStyleName(CssConstants.CONTENT_INNER_PANEL);
		myGraphsPanel.add(myGraphsContentPanel);

		myUserPicker.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent theEvent) {
				updateGraphsPanel();
			}
		});

		myGraphsTimePicker = new TimeRangeSelectorPanel(true);
		myGraphsTimePicker.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent theEvent) {
				updateGraphsPanel();
			}
		});

		updateUserPicker();
		updateGraphsPanel();
	}

	private void updateGraphsPanel() {
		myGraphsContentPanel.clear();
		if (myUserPicker.getSelectedIndex() == -1) {
			return;
		}
		String selectedUserPid = myUserPicker.getValue(myUserPicker.getSelectedIndex());

		myGraphsContentPanel.add(myGraphsTimePicker);

		myGraphsContentPanel.add(new HtmlH1("User Method Usage"));
		Image img = new Image(ServiceVersionStatsPanel.GRAPH_FILENAME + "?ct=" + ChartTypeEnum.USERMETHODS.name() + "&pid=" + selectedUserPid + "&" + ChartParams.RANGE + "=" + myGraphsTimePicker.getSelectedRange().toUrlValue());
		ServiceVersionStatsPanel.addStatsImage(myGraphsContentPanel, img);

	}

	private void updateUserPicker() {
		List<GUser> userList = new ArrayList<GUser>(myUserList.toList());
		Collections.sort(userList, new Comparator<GUser>() {
			@Override
			public int compare(GUser theO1, GUser theO2) {
				return theO1.getUsername().compareTo(theO2.getUsername());
			}
		});
		myUserPicker.clear();
		for (GUser next : userList) {
			myUserPicker.addItem(next.getUsername(), Long.toString(next.getPid()));
			if (myInitialUserPid != null && next.getPid() == myInitialUserPid) {
				int itemCount = myUserPicker.getItemCount();
				myUserPicker.setSelectedIndex(itemCount-1);
			}
		}
		int selectedIndex = myUserPicker.getSelectedIndex();
		if (selectedIndex == -1 && myUserPicker.getItemCount() > 0) {
			myUserPicker.setSelectedIndex(0);
		}
	}

}
