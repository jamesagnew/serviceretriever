package net.svcret.admin.client.ui.config;

import static net.svcret.admin.client.AdminPortal.*;
import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.client.ui.components.HtmlBr;
import net.svcret.admin.client.ui.components.HtmlH1;
import net.svcret.admin.client.ui.components.LoadingSpinner;
import net.svcret.admin.client.ui.components.PButton;
import net.svcret.admin.client.ui.components.TwoColumnGrid;
import net.svcret.admin.shared.IAsyncLoadCallback;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.GHttpClientConfig;
import net.svcret.admin.shared.model.GHttpClientConfigList;
import net.svcret.admin.shared.model.UrlSelectionPolicy;
import net.svcret.admin.shared.util.StringUtil;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class HttpClientConfigsPanel extends FlowPanel {
	
	private PButton myAddButton;
	private IntegerBox myCircuitBreakerDelayBox;
	private CheckBox myCircuitBreakerEnabledCheck;
	private LoadingSpinner myConfigListLoadingSpinner;
	private GHttpClientConfigList myConfigs;
	private ListBox myConfigsListBox;
	private TextBox myIdTextBox;
	private LoadingSpinner myLoadingSpinner;
	private TextBox myNameTextBox;
	private PButton myRemoveButton;
	private IntegerBox myRetriesTextBox;
	private Long mySelectedPid;
	private IntegerBox myTcpConnectTimeoutTb;
	private IntegerBox myTcpReadTimeoutTb;
	private boolean myUpdatingConfigsListBox;
	private HTML myUrlSelectionPolicyDescriptionLabel;
	private ListBox myUrlSelectionPolicyListBox;
	private int ourNextUnsavedPid = -1;
	
	public HttpClientConfigsPanel() {
		initConfigListPanel();
		initDetailsPanel();
		
		Model.getInstance().loadHttpClientConfigs(new IAsyncLoadCallback<GHttpClientConfigList>() {
			@Override
			public void onSuccess(GHttpClientConfigList theResult) {
				setConfigList(theResult);
			}

		});

	}

	private void addConfig() {
		GHttpClientConfig newConfig = new GHttpClientConfig();
		newConfig.setId("NEW");
		newConfig.setName("New");
		newConfig.setPid(ourNextUnsavedPid--);
		myConfigs.add(newConfig);
		
		mySelectedPid = newConfig.getPid();
		updateConfigList();
		updateSelectedConfig();
	}

	private void doSave() {
		GHttpClientConfig config = myConfigs.getConfigByPid(mySelectedPid);
		config.setId(myIdTextBox.getValue());
		config.setName(myNameTextBox.getValue());

		Integer connectTimeout = myTcpConnectTimeoutTb.getValue();
		myTcpConnectTimeoutTb.removeStyleName(CssConstants.TEXTBOX_WITH_ERR);
		if (connectTimeout == null || connectTimeout <= 0) {
			myTcpConnectTimeoutTb.addStyleName(CssConstants.TEXTBOX_WITH_ERR);
			myLoadingSpinner.showMessage(MSGS.httpClientConfigsPanel_validateFailed_ConnectTimeout(), false);
			return;
		} else {
			config.setConnectTimeoutMillis(connectTimeout);
		}

		Integer readTimeout = myTcpReadTimeoutTb.getValue();
		myTcpReadTimeoutTb.removeStyleName(CssConstants.TEXTBOX_WITH_ERR);
		if (readTimeout == null || readTimeout <= 0) {
			myTcpReadTimeoutTb.addStyleName(CssConstants.TEXTBOX_WITH_ERR);
			myLoadingSpinner.showMessage(MSGS.httpClientConfigsPanel_validateFailed_ReadTimeout(), false);
			return;
		} else {
			config.setReadTimeoutMillis(readTimeout);
		}

		config.setCircuitBreakerEnabled(myCircuitBreakerEnabledCheck.getValue());

		Integer cbRetryTimeout = myCircuitBreakerDelayBox.getValue();
		myCircuitBreakerDelayBox.removeStyleName(CssConstants.TEXTBOX_WITH_ERR);
		if (cbRetryTimeout == null || cbRetryTimeout <= 0) {
			myCircuitBreakerDelayBox.addStyleName(CssConstants.TEXTBOX_WITH_ERR);
			myLoadingSpinner.showMessage(MSGS.httpClientConfigsPanel_validateFailed_CircuitBreakerDelay(), false);
			return;
		} else {
			config.setCircuitBreakerTimeBetweenResetAttempts(cbRetryTimeout);
		}

		Integer retries = myCircuitBreakerDelayBox.getValue();
		myRetriesTextBox.removeStyleName(CssConstants.TEXTBOX_WITH_ERR);
		if (retries == null || retries < 0) {
			myRetriesTextBox.addStyleName(CssConstants.TEXTBOX_WITH_ERR);
			myLoadingSpinner.showMessage(MSGS.httpClientConfigsPanel_validateFailed_Retries(), false);
			return;
		} else {
			config.setFailureRetriesBeforeAborting(retries);
		}
		
		UrlSelectionPolicy policy = UrlSelectionPolicy.values()[myUrlSelectionPolicyListBox.getSelectedIndex()];
		config.setUrlSelectionPolicy(policy);
		
		boolean create = config.getPid() < 0;

		myLoadingSpinner.show();
		AdminPortal.MODEL_SVC.saveHttpClientConfig(create, config, new AsyncCallback<GHttpClientConfig>() {

			@Override
			public void onFailure(Throwable theCaught) {
				Model.handleFailure(theCaught);
			}

			@Override
			public void onSuccess(GHttpClientConfig theResult) {
				myLoadingSpinner.showMessage("Saved", false);
				Model.getInstance().addHttpClientConfig(theResult);
				mySelectedPid = theResult.getPid();
				
				updateConfigList();
				updateSelectedConfig();
			}
		});
	}

	private void enableToolbar() {
		myAddButton.setEnabled(true);
		myRemoveButton.setEnabled(true);
	}

	private void initConfigListPanel() {
		FlowPanel listPanel = new FlowPanel();
		listPanel.setStylePrimaryName(CssConstants.MAIN_PANEL);
		add(listPanel);

		Label titleLabel = new Label(MSGS.httpClientConfigsPanel_ListTitle());
		titleLabel.setStyleName(CssConstants.MAIN_PANEL_TITLE);
		listPanel.add(titleLabel);

		FlowPanel contentPanel = new FlowPanel();
		contentPanel.addStyleName(CssConstants.CONTENT_INNER_PANEL);
		listPanel.add(contentPanel);

		contentPanel.add(new Label(AdminPortal.MSGS.httpClientConfigsPanel_IntroMessage()));

		myConfigListLoadingSpinner = new LoadingSpinner();
		contentPanel.add(myConfigListLoadingSpinner);

		HorizontalPanel hPanel = new HorizontalPanel();
		contentPanel.add(hPanel);
		
		VerticalPanel toolbar = new VerticalPanel();
		myAddButton = new PButton(AdminPortal.IMAGES.iconAdd(), AdminPortal.MSGS.actions_Add());
		myAddButton.setEnabled(false);
		myAddButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent theEvent) {
				addConfig();
			}
		});
		toolbar.add(myAddButton);
		myRemoveButton = new PButton(AdminPortal.IMAGES.iconRemove(), AdminPortal.MSGS.actions_Remove());
		myRemoveButton.setEnabled(false);
		myRemoveButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent theEvent) {
				removeConfig();
			}
		});
		toolbar.add(myRemoveButton);
		hPanel.add(toolbar);
		
		myConfigsListBox = new ListBox(false);
		myConfigsListBox.setVisibleItemCount(5);
		myConfigsListBox.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent theEvent) {
				if (myUpdatingConfigsListBox) {
					return;
				}
				mySelectedPid = Long.parseLong(myConfigsListBox.getValue(myConfigsListBox.getSelectedIndex()));
				updateSelectedConfig();
			}

		});
		hPanel.add(myConfigsListBox);

		HorizontalPanel buttonsBar = new HorizontalPanel();
		contentPanel.add(buttonsBar);

	}

	private void initDetailsPanel() {
		FlowPanel detailsOuterPanel = new FlowPanel();
		detailsOuterPanel.setStylePrimaryName("mainPanel");
		add(detailsOuterPanel);

		Label titleLabel = new Label(MSGS.httpClientConfigsPanel_EditDetailsTitle());
		titleLabel.setStyleName("mainPanelTitle");
		detailsOuterPanel.add(titleLabel);

		FlowPanel contentPanel = new FlowPanel();
		contentPanel.addStyleName("contentInnerPanel");
		detailsOuterPanel.add(contentPanel);

		/*
		 * Details
		 */

		PButton saveButton = new PButton(IMAGES.iconSave(), MSGS.actions_Save());
		saveButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent theEvent) {
				doSave();
			}
		});
		contentPanel.add(saveButton);
		myLoadingSpinner = new LoadingSpinner();
		myLoadingSpinner.hideCompletely();
		contentPanel.add(myLoadingSpinner);
		contentPanel.add(new HtmlBr());

		TwoColumnGrid idAndNameGrid = new TwoColumnGrid();
		contentPanel.add(idAndNameGrid);
		
		// ID
		myIdTextBox = new TextBox();
		idAndNameGrid.addRow(MSGS.propertyNameId(), myIdTextBox);

		// Name
		myNameTextBox = new TextBox();
		idAndNameGrid.addRow(MSGS.propertyNameName(), myNameTextBox);

		/*
		 * TCP Properties
		 */

		contentPanel.add(new HtmlH1(MSGS.httpClientConfigsPanel_TcpProperties()));
		contentPanel.add(new HTML(MSGS.httpClientConfigsPanel_TcpPropertiesDesc()));
		
		TwoColumnGrid tcpPropsGrid = new TwoColumnGrid();
		contentPanel.add(tcpPropsGrid);
		
		myTcpConnectTimeoutTb = new IntegerBox();
		tcpPropsGrid.addRow(MSGS.httpClientConfigsPanel_TcpConnectMillis(), myTcpConnectTimeoutTb);

		myTcpReadTimeoutTb = new IntegerBox();
		tcpPropsGrid.addRow(MSGS.httpClientConfigsPanel_TcpReadMillis(), myTcpReadTimeoutTb);
		
		// myTcpReadTimeoutTb.addKeyPressHandler(ValidatingTextBoxChangeHandlerPositiveInteger.INSTANCE);
		// myTcpReadTimeoutTb.addValueChangeHandler(ValidatingTextBoxChangeHandlerPositiveInteger.INSTANCE);

		/*
		 * URL Selection policy
		 */

		contentPanel.add(new HtmlH1(MSGS.httpClientConfigsPanel_UrlSelectionTitle()));
		contentPanel.add(new Label(MSGS.httpClientConfigsPanel_UrlSelectionDescription()));
		
		TwoColumnGrid urlSelGrid = new TwoColumnGrid();
		contentPanel.add(urlSelGrid);
		
		myUrlSelectionPolicyListBox = new ListBox(false);
		urlSelGrid.addRow(MSGS.httpClientConfigsPanel_UrlSelectionPolicyShortName(), myUrlSelectionPolicyListBox);
		
		myUrlSelectionPolicyDescriptionLabel = new HTML();
		urlSelGrid.addDescription(myUrlSelectionPolicyDescriptionLabel);

		for (UrlSelectionPolicy next : UrlSelectionPolicy.values()) {
			myUrlSelectionPolicyListBox.addItem(next.name());
		}
		myUrlSelectionPolicyListBox.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent theEvent) {
				updateSelectedUrlSelectionPolicy();
			}
		});

		/*
		 * Circuit Breaker
		 */

		contentPanel.add(new HtmlH1(MSGS.httpClientConfigsPanel_CircuitBreakerTitle()));
		contentPanel.add(new HTML(MSGS.httpClientConfigsPanel_CircuitBreakerDescription()));
		
		TwoColumnGrid circuitBreakerGrid = new TwoColumnGrid();
		contentPanel.add(circuitBreakerGrid);
		
		myCircuitBreakerEnabledCheck = new CheckBox();
		circuitBreakerGrid.addRow(MSGS.httpClientConfigsPanel_CircuitBreakerEnabled(), myCircuitBreakerEnabledCheck);

		myCircuitBreakerDelayBox = new IntegerBox();
		circuitBreakerGrid.addRow(MSGS.httpClientConfigsPanel_CircuitBreakerDelayBetweenReset(), myCircuitBreakerDelayBox);

		/*
		 * Retries
		 */
		
		contentPanel.add(new HtmlH1(MSGS.httpClientConfigsPanel_RetriesTitle()));
		contentPanel.add(new HTML(MSGS.httpClientConfigsPanel_RetriesDesc()));
		
		TwoColumnGrid retriesGrid = new TwoColumnGrid();
		contentPanel.add(retriesGrid);
		myRetriesTextBox = new IntegerBox();
		retriesGrid.addRow(MSGS.httpClientConfigsPanel_RetriesLabel(), myRetriesTextBox);
		// myRetriesTextBox.addValueChangeHandler(ValidatingTextBoxChangeHandlerPositiveInteger.INSTANCE_0_OR_ABOVE);
		// myRetriesTextBox.addKeyPressHandler(ValidatingTextBoxChangeHandlerPositiveInteger.INSTANCE_0_OR_ABOVE);
		contentPanel.add(myRetriesTextBox);
	}


	private void removeConfig() {
		GHttpClientConfig config = myConfigs.get(myConfigsListBox.getSelectedIndex());
		if (config.isDefault()) {
			Window.alert(MSGS.httpClientConfigsPanel_CantDeleteDefault());
			return;
		}
		if (!Window.confirm(MSGS.httpClientConfigsPanel_ConfirmDelete(config.getId()))) {
			return;
		}
		
		myConfigListLoadingSpinner.show();
		
		AdminPortal.MODEL_SVC.deleteHttpClientConfig(config.getPid(), new MyHttpClientConfigListHandler());
		
	}

	private void setConfigList(GHttpClientConfigList theConfigList) {
		assert theConfigList.size() > 0;
		myConfigListLoadingSpinner.hideCompletely();
		myConfigs = theConfigList;
		updateConfigList();
		enableToolbar();
	}

//	private void updateConfigList() {
//		myUpdatingConfigsListBox = true;
//		myConfigsListBox.clear();
//
//		int selectedIndex = 0;
//		for (GHttpClientConfig next : myConfigs) {
//			String desc = next.getId();
//			if (StringUtil.isNotBlank(next.getName())) {
//				desc = desc + " - " + next.getName();
//			}
//			if (mySelectedPid != null && mySelectedPid.equals(next.getPid())) {
//				selectedIndex = myConfigsListBox.getItemCount();
//			}
//			myConfigsListBox.addItem(desc, Long.toString(next.getPid()));
//		}
//
//		myConfigsListBox.setSelectedIndex(selectedIndex);
//
//		myUpdatingConfigsListBox = false;
//
//		String value = myConfigsListBox.getValue(selectedIndex);
//		Long newSelectedId = Long.parseLong(value);
//		if (!newSelectedId.equals(mySelectedPid)) {
//			mySelectedPid = newSelectedId;
//			updateSelectedConfig();
//		}
//
//	}
	
	private void updateConfigList() {
		myUpdatingConfigsListBox = true;
		myConfigsListBox.clear();

		int selectedIndex = 0;
		for (GHttpClientConfig next : myConfigs) {
			String desc = next.getId();
			if (StringUtil.isNotBlank(next.getName())) {
				desc = desc + " - " + next.getName();
			}
			if (mySelectedPid != null && mySelectedPid.equals(next.getPid())) {
				selectedIndex = myConfigsListBox.getItemCount();
			}
			myConfigsListBox.addItem(desc, Long.toString(next.getPid()));
		}

		myConfigsListBox.setSelectedIndex(selectedIndex);

		myUpdatingConfigsListBox = false;

		String value = myConfigsListBox.getValue(selectedIndex);
		Long newSelectedId = Long.parseLong(value);
		if (!newSelectedId.equals(mySelectedPid)) {
			mySelectedPid = newSelectedId;
			updateSelectedConfig();
		}

	}

	private void updateSelectedConfig() {
		GHttpClientConfig config = myConfigs.getConfigByPid(mySelectedPid);
		if (config == null) {
			throw new IllegalStateException("No config with PID " + mySelectedPid + " found. Valid are: " + myConfigs.listConfigIds());
		}
		myIdTextBox.setValue(config.getId());
		myNameTextBox.setValue(config.getName());

		boolean nameAndIdEditable = !GHttpClientConfig.DEFAULT_ID.equals(config.getId());
		myIdTextBox.setEnabled(nameAndIdEditable);
		myNameTextBox.setEnabled(nameAndIdEditable);

		int indexOf = UrlSelectionPolicy.indexOf(config.getUrlSelectionPolicy());
		myUrlSelectionPolicyListBox.setSelectedIndex(indexOf);
		updateSelectedUrlSelectionPolicy();

		myCircuitBreakerEnabledCheck.setValue(config.isCircuitBreakerEnabled());
		myCircuitBreakerDelayBox.setValue((config.getCircuitBreakerTimeBetweenResetAttempts()));

		myTcpConnectTimeoutTb.setValue((config.getConnectTimeoutMillis()));
		myTcpReadTimeoutTb.setValue((config.getReadTimeoutMillis()));

		myRetriesTextBox.setValue((config.getFailureRetriesBeforeAborting()));
	}

	private void updateSelectedUrlSelectionPolicy() {
		int selectedIndex = myUrlSelectionPolicyListBox.getSelectedIndex();
		if (selectedIndex == -1) {
			return;
		}
		UrlSelectionPolicy policy = UrlSelectionPolicy.values()[selectedIndex];
		switch (policy) {
		case PREFER_LOCAL:
			myUrlSelectionPolicyDescriptionLabel.setHTML(MSGS.urlSelectionPolicy_Desc_PreferLocal());
			break;
		case ROUND_ROBIN:
			myUrlSelectionPolicyDescriptionLabel.setHTML(MSGS.urlSelectionPolicy_Desc_RoundRobin());
			break;
		}
	}

	private final class MyHttpClientConfigListHandler implements AsyncCallback<GHttpClientConfigList> {
		@Override
		public void onFailure(Throwable theCaught) {
			Model.handleFailure(theCaught);
		}

		@Override
		public void onSuccess(GHttpClientConfigList theResult) {
			setConfigList(theResult);
		}
	}

}
