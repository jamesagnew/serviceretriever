package net.svcret.admin.client.ui.config.auth;

import static net.svcret.admin.client.AdminPortal.*;
import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.ui.components.LoadingSpinner;
import net.svcret.admin.client.ui.components.PButton;
import net.svcret.admin.shared.IAsyncLoadCallback;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.AuthorizationHostTypeEnum;
import net.svcret.admin.shared.model.BaseGAuthHost;
import net.svcret.admin.shared.model.GAuthenticationHostList;
import net.svcret.admin.shared.model.GLocalDatabaseAuthHost;
import net.svcret.admin.shared.util.StringUtil;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class AuthenticationHostsPanel extends FlowPanel {

	private LoadingSpinner myConfigListLoadingSpinner;
	private PButton myAddButton;
	private PButton myRemoveButton;
	private ListBox myHostsListBox;
	private boolean myUpdatingConfigsListBox;
	private Long mySelectedPid;
	private GAuthenticationHostList myConfigs;
	private FlowPanel myDetailsContainer;
	private ListBox myAddListBox;
	private static long ourNextTemporaryPid = -1;

	public AuthenticationHostsPanel() {
		initListPanel();
		initDetailsPanel();
		
		Model.getInstance().loadAuthenticationHosts(new IAsyncLoadCallback<GAuthenticationHostList>() {
			@Override
			public void onSuccess(GAuthenticationHostList theResult) {
				setHostList(theResult);
			}
		});
	}

	private void initDetailsPanel() {
		myDetailsContainer = new FlowPanel();
		add(myDetailsContainer);
	}

	private void initListPanel() {
		FlowPanel listPanel = new FlowPanel();
		listPanel.setStylePrimaryName("mainPanel");
		add(listPanel);

		Label titleLabel = new Label(MSGS.authenticationHostsPanel_ListTitle());
		titleLabel.setStyleName("mainPanelTitle");
		listPanel.add(titleLabel);

		FlowPanel contentPanel = new FlowPanel();
		contentPanel.addStyleName("contentInnerPanel");
		listPanel.add(contentPanel);

		contentPanel.add(new HTML(AdminPortal.MSGS.authenticationHostsPanel_IntroMessage()));

		myConfigListLoadingSpinner = new LoadingSpinner();
		contentPanel.add(myConfigListLoadingSpinner);

		HorizontalPanel hPanel = new HorizontalPanel();
		contentPanel.add(hPanel);
		
		VerticalPanel toolbar = new VerticalPanel();
		
		HorizontalPanel addPanel = new HorizontalPanel();
		toolbar.add(addPanel);
		myAddButton = new PButton(AdminPortal.MSGS.actions_Add());
		myAddButton.setEnabled(false);
		myAddButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent theEvent) {
				addHost();
			}
		});
		addPanel.add(myAddButton);
		myAddListBox = new ListBox();
		for (AuthorizationHostTypeEnum next : AuthorizationHostTypeEnum.values()) {
			myAddListBox.addItem(next.description(), next.name());
		}
		myAddListBox.setSelectedIndex(0);
		addPanel.add(myAddListBox);
		
		myRemoveButton = new PButton(AdminPortal.MSGS.actions_Remove());
		myRemoveButton.setEnabled(false);
		myRemoveButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent theEvent) {
				removeHost();
			}
		});
		toolbar.add(myRemoveButton);
		hPanel.add(toolbar);
		
		myHostsListBox = new ListBox(false);
		myHostsListBox.setVisibleItemCount(5);
		myHostsListBox.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent theEvent) {
				if (myUpdatingConfigsListBox) {
					return;
				}
				mySelectedPid = Long.parseLong(myHostsListBox.getValue(myHostsListBox.getSelectedIndex()));
				updateSelectedHost();
			}

		});
		hPanel.add(myHostsListBox);

		HorizontalPanel buttonsBar = new HorizontalPanel();
		contentPanel.add(buttonsBar);

	}

	private void removeHost() {
		if (myConfigs.size() < 2) {
			String msg = AdminPortal.MSGS.baseAuthenticationHostEditPanel_ErrorCantRemoveConfigOnlyOne();
			Window.alert(msg);
			return;
		}
		
		/* 
		 * TODO: don't let the autho host which is used to log into the admin portal itself
		 * be deleted
		 */
	
		BaseGAuthHost config = myConfigs.get(myHostsListBox.getSelectedIndex());
		if (!Window.confirm(AdminPortal.MSGS.baseAuthenticationHostEditPanel_ConfirmDelete(config.getModuleId()))) {
			return;
		}
		
		if (config.getPid() <= 0) {
			myConfigs.remove(config);
			setHostList(myConfigs);
		} else {
		AdminPortal.MODEL_SVC.removeAuthenticationHost(config.getPid(), new AsyncCallback<GAuthenticationHostList>() {
			@Override
			public void onFailure(Throwable theCaught) {
				Model.handleFailure(theCaught);
			}
			@Override
			public void onSuccess(GAuthenticationHostList theResult) {
				setHostList(theResult);
			}
		});
		}
	}

	private void addHost() {
		switch (AuthorizationHostTypeEnum.values()[myAddListBox.getSelectedIndex()]) {
		case LOCAL_DATABASE:
			GLocalDatabaseAuthHost host = new GLocalDatabaseAuthHost();
			host.setPid(ourNextTemporaryPid--);
			host.setModuleId("Untitled");
			host.setModuleName("Untitled");
			myConfigs.add(host);
			mySelectedPid = host.getPid();
			updateHostList();
			updateSelectedHost();
		}
		
		
//		GHttpClientConfig newConfig = new Glo
//		newConfig.setId("NEW");
//		newConfig.setName("New");
//		newConfig.setPid(ourNextUnsavedPid--);
//		myConfigs.add(newConfig);
//		
//		mySelectedPid = newConfig.getPid();
//		updateConfigList();
//		updateSelectedConfig();
	}

	public void setHostList(GAuthenticationHostList theHostList) {
		assert theHostList.size() > 0;
		myConfigListLoadingSpinner.hideCompletely();
		myConfigs = theHostList;
		updateHostList();
		enableToolbar();
	}

	private void updateHostList() {
		myUpdatingConfigsListBox = true;
		myHostsListBox.clear();

		int selectedIndex = 0;
		for (BaseGAuthHost next : myConfigs) {
			String desc = next.getModuleId();
			if (StringUtil.isNotBlank(next.getModuleName())) {
				desc = desc + " - " + next.getModuleName();
			}
			if (mySelectedPid != null && mySelectedPid.equals(next.getPid())) {
				selectedIndex = myHostsListBox.getItemCount();
			}
			myHostsListBox.addItem(desc, Long.toString(next.getPid()));
		}

		myHostsListBox.setSelectedIndex(selectedIndex);

		myUpdatingConfigsListBox = false;

		String value = myHostsListBox.getValue(selectedIndex);
		Long newSelectedId = Long.parseLong(value);
		if (!newSelectedId.equals(mySelectedPid)) {
			mySelectedPid = newSelectedId;
			updateSelectedHost();
		}

	}

	private void updateSelectedHost() {
		BaseGAuthHost host = myConfigs.getAuthHostByPid(mySelectedPid);
		Widget panel=null;
		switch (host.getType()) {
		case LOCAL_DATABASE:
			panel = new  LocalDatabaseAuthenticationHostEditPanel(this, (GLocalDatabaseAuthHost) host);
		}
		
		if (panel != null) {
			myDetailsContainer.clear();
			myDetailsContainer.add(panel);
		}
	}

	private void enableToolbar() {
		myAddButton.setEnabled(true);
		myRemoveButton.setEnabled(true);
	}

}
