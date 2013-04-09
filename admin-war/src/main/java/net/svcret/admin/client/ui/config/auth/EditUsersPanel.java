package net.svcret.admin.client.ui.config.auth;

import static net.svcret.admin.client.AdminPortal.*;
import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.nav.NavProcessor;
import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.client.ui.components.LoadingSpinner;
import net.svcret.admin.client.ui.components.PButton;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.GPartialUserList;
import net.svcret.admin.shared.model.GUser;
import net.svcret.admin.shared.model.PartialUserListRequest;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;

public class EditUsersPanel extends FlowPanel {

	private static final int COL_USERNAME = 0;
	private static final int COL_ACTIONS = 1;
	
	private FlexTable myTable;
	private GPartialUserList myUserList;
	private LoadingSpinner myLoadingSpinner;

	public EditUsersPanel() {
		FlowPanel listPanel = new FlowPanel();
		listPanel.setStylePrimaryName(CssConstants.MAIN_PANEL);
		add(listPanel);

		Label titleLabel = new Label(MSGS.editUsersPanel_Title());
		titleLabel.setStyleName(CssConstants.MAIN_PANEL_TITLE);
		listPanel.add(titleLabel);

		FlowPanel contentPanel = new FlowPanel();
		contentPanel.addStyleName(CssConstants.CONTENT_INNER_PANEL);
		listPanel.add(contentPanel);

		contentPanel.add(new Label(MSGS.editUsersPanel_ListDescription()));
		
		myLoadingSpinner = new LoadingSpinner();
		contentPanel.add(myLoadingSpinner);
		
		myTable = new FlexTable(); 
		contentPanel.add(myTable);
		
		myTable.addStyleName(CssConstants.PROPERTY_TABLE);
		myTable.setText(0, COL_USERNAME, MSGS.editUsersPanel_ColumnUsername());
		myTable.setText(0, COL_ACTIONS, MSGS.editUsersPanel_ColumnActions());
		
		loadUserList();
		
	}
	
	private void loadUserList() {
		myLoadingSpinner.show();
		PartialUserListRequest request=new PartialUserListRequest();
		AsyncCallback<GPartialUserList> callback=new AsyncCallback<GPartialUserList>() {
			@Override
			public void onFailure(Throwable theCaught) {
				Model.handleFailure(theCaught);
			}
			@Override
			public void onSuccess(GPartialUserList theResult) {
				setUserlist(theResult);
				myLoadingSpinner.hide();
			}
		};
		AdminPortal.MODEL_SVC.loadUsers(request, callback);
	}

	private void setUserlist(GPartialUserList theUserList) {
		myUserList = theUserList;
		
		for (int i = 0; i < myUserList.size(); i++) {
			
			final GUser nextUser = myUserList.get(i);
			int row = i + 1;
			
			myTable.setText(row, COL_USERNAME, nextUser.getUsername());
			
			Panel actionPanel = new FlowPanel();
			myTable.setWidget(row, COL_ACTIONS, actionPanel);
			
			actionPanel.add(new PButton(MSGS.actions_Edit(), new ClickHandler() {
				@Override
				public void onClick(ClickEvent theEvent) {
					editUser(nextUser);
				}
			}));
		}
	}

	private void editUser(GUser theNextUser) {
		String token = NavProcessor.getTokenEditUser(true, theNextUser.getPid());
		History.newItem(token);
	}
	
}
