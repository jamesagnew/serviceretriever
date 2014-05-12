package net.svcret.admin.client.ui.config.http;

import static net.svcret.admin.client.AdminPortal.*;
import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.client.ui.components.EditableField;
import net.svcret.admin.client.ui.components.HtmlBr;
import net.svcret.admin.client.ui.components.HtmlH1;
import net.svcret.admin.client.ui.components.HtmlLabel;
import net.svcret.admin.client.ui.components.LoadingSpinner;
import net.svcret.admin.client.ui.components.PButton;
import net.svcret.admin.client.ui.components.TwoColumnGrid;
import net.svcret.admin.shared.ConstantsHttpClientConfig;
import net.svcret.admin.shared.DateUtil;
import net.svcret.admin.shared.IAsyncLoadCallback;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.DtoKeystoreAnalysis;
import net.svcret.admin.shared.model.DtoHttpClientConfig;
import net.svcret.admin.shared.model.GHttpClientConfigList;
import net.svcret.admin.shared.model.UrlSelectionPolicy;
import net.svcret.admin.shared.util.StringUtil;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TabPanel;
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
	private HTML myKeyStoreLabel;
	private EditableField myKeystorePasswordBox;
	private LoadingSpinner myLoadingSpinner;
	private TextBox myNameTextBox;
	private DtoKeystoreAnalysis myNewKeystore;
	private DtoKeystoreAnalysis myNewTruststore;
	private PButton myRemoveButton;
	private IntegerBox myRetriesTextBox;
	private DtoHttpClientConfig mySelectedConfig;
	private IntegerBox myTcpConnectTimeoutTb;
	private IntegerBox myTcpReadTimeoutTb;
	private HTML myTrustStoreLabel;
	private EditableField myTruststorePasswordBox;
	private boolean myUpdatingConfigsListBox;
	private String myUploadFrameId;
	private HTML myUrlSelectionPolicyDescriptionLabel;
	private ListBox myUrlSelectionPolicyListBox;
	private int ourNextUnsavedPid = -1;
	private Long myDefaultPid;
	private EditableField myStickySessionCookieNameBox;
	private HtmlLabel myStickySessionCookieLabel;
	private boolean myClearKeystore;
	private boolean myClearTrustStore;

	public HttpClientConfigsPanel() {
		this(null);
	}

	public HttpClientConfigsPanel(Long theDefaultPid) {
		myDefaultPid = theDefaultPid;

		initConfigListPanel();
		initDetailsPanel();
		initBottomTabs();

		Model.getInstance().loadHttpClientConfigs(new IAsyncLoadCallback<GHttpClientConfigList>() {
			@Override
			public void onSuccess(GHttpClientConfigList theResult) {
				if (myDefaultPid != null) {
					mySelectedConfig = theResult.getConfigByPid(myDefaultPid);
				}
				setConfigList(theResult);
			}

		});

	}

	private void addConfig() {
		DtoHttpClientConfig newConfig = new DtoHttpClientConfig();
		newConfig.setId("NEW");
		newConfig.setName("New");
		newConfig.setPid(ourNextUnsavedPid--);
		myConfigs.add(newConfig);

		mySelectedConfig = newConfig;
		updateConfigList();
		updateSelectedConfig();
	}

	private TwoColumnGrid createKeystorePanel(FlowPanel contentPanel, final String titleLabel, String uploadFieldName, final EditableField theTruststorePasswordBox, final HTML theTrustStoreLabel,
			final boolean theIsKeystore) {
		contentPanel.add(new HtmlH1(titleLabel));

		TwoColumnGrid sslGrid = new TwoColumnGrid();
		contentPanel.add(sslGrid);

		sslGrid.addRow(titleLabel, theTrustStoreLabel);

		final FormPanel uploadForm = new FormPanel();
		uploadForm.setEncoding(FormPanel.ENCODING_MULTIPART);
		uploadForm.setMethod(FormPanel.METHOD_POST);
		sslGrid.addRow("Upload New", uploadForm);

		HorizontalPanel panel = new HorizontalPanel();
		uploadForm.add(panel);

		final LoadingSpinner trustStoreLoadingSpinner = new LoadingSpinner();

		final FileUpload fileUpload = new FileUpload();
		final Hidden passwordHidden = new Hidden();
		final Hidden pidHidden = new Hidden();
		final PButton submitButton = new PButton("Upload");

		fileUpload.setName(ConstantsHttpClientConfig.FIELD_FILEUPLOAD);
		panel.add(fileUpload);

		submitButton.setEnabled(false);
		fileUpload.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent theEvent) {
				submitButton.setEnabled(true);
			}
		});

		submitButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent theEvent) {
				GWT.log("Uploading");
				uploadForm.setAction(GWT.getModuleBaseURL() + "keystoreUpload");
				trustStoreLoadingSpinner.showMessage("Uploading...", true);
				pidHidden.setValue(Long.toString(mySelectedConfig.getPid()));
				passwordHidden.setValue(theTruststorePasswordBox.getValueOrBlank());
				uploadForm.submit();
			}
		});
		uploadForm.addSubmitCompleteHandler(new SubmitCompleteHandler() {
			@Override
			public void onSubmitComplete(SubmitCompleteEvent theEvent) {
				AsyncCallback<DtoKeystoreAnalysis> callback = new AsyncCallback<DtoKeystoreAnalysis>() {
					@Override
					public void onFailure(Throwable theCaught) {
						Model.handleFailure(theCaught);
					}

					@Override
					public void onSuccess(DtoKeystoreAnalysis theResult) {
						trustStoreLoadingSpinner.hide();
						if (theIsKeystore == false) {
							myNewTruststore = theResult;
						} else {
							myNewKeystore = theResult;
						}
						updateKeystoreLabels();
					}
				};

				if (theIsKeystore == false) {
					AdminPortal.SVC_HTTPCLIENTCONFIG.analyzeTransientTrustStore(mySelectedConfig.getPid(), callback);
				} else {
					AdminPortal.SVC_HTTPCLIENTCONFIG.analyzeTransientKeyStore(mySelectedConfig.getPid(), callback);
				}
			}
		});
		panel.add(submitButton);
		
		PButton clearButton = new PButton(AdminPortal.MSGS.actions_Clear());
		panel.add(clearButton);
		clearButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent theEvent) {
				if (theIsKeystore) {
					myClearKeystore=true;
					myNewKeystore=null;
				}else {
					myClearTrustStore=true;
					myNewTruststore=null;
				}
				updateKeystoreLabels();
			}
		});
		
		panel.add(trustStoreLoadingSpinner);

		Hidden typeHidden = new Hidden();
		typeHidden.setName(ConstantsHttpClientConfig.FIELD_TYPE);
		typeHidden.setValue(uploadFieldName);
		panel.add(typeHidden);

		pidHidden.setName(ConstantsHttpClientConfig.FIELD_CONFIG_PID);
		panel.add(pidHidden);

		passwordHidden.setName(ConstantsHttpClientConfig.FIELD_PASSWORD);
		panel.add(passwordHidden);

		sslGrid.addRow("Password", theTruststorePasswordBox);

		return sslGrid;
	}

	private void doSave() {
		DtoHttpClientConfig config = myConfigs.getConfigByPid(mySelectedConfig.getPid());
		config.setId(myIdTextBox.getValue());
		config.setName(myNameTextBox.getValue());
		config.setStickySessionCookieForSessionId(myStickySessionCookieNameBox.getValueOrNull());

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

		Integer retries = myRetriesTextBox.getValue();
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

		if (myNewKeystore != null) {
			config.setTlsKeystore(myNewKeystore);
		} else if (myClearKeystore) {
			config.setTlsKeystore(null);
		}
		if (myNewTruststore != null) {
			config.setTlsKeystore(myNewTruststore);
		} else if (myClearTrustStore) {
			config.setTlsTruststore(null);
		}

		myLoadingSpinner.show();
		boolean useNewTruststore = myNewTruststore != null;
		boolean useNewKeystore = myNewKeystore != null;
		AdminPortal.SVC_HTTPCLIENTCONFIG.saveHttpClientConfig(create, useNewTruststore, useNewKeystore, config, new AsyncCallback<DtoHttpClientConfig>() {

			@Override
			public void onFailure(Throwable theCaught) {
				Model.handleFailure(theCaught);
			}

			@Override
			public void onSuccess(DtoHttpClientConfig theResult) {
				myLoadingSpinner.showMessage("Saved", false);
				Model.getInstance().addHttpClientConfig(theResult);
				mySelectedConfig = theResult;

				updateConfigList();
				updateSelectedConfig();
			}
		});
	}

	private void enableToolbar() {
		myAddButton.setEnabled(true);
		myRemoveButton.setEnabled(true);
	}

	private DtoKeystoreAnalysis getKeystoreAnalysis() {
		if (myNewKeystore != null) {
			return myNewKeystore;
		}
		if (myClearKeystore) {
			return null;
		}
		return mySelectedConfig.getTlsKeystore();
	}

	private DtoKeystoreAnalysis getTruststoreAnalysis() {
		if (myNewTruststore != null) {
			return myNewTruststore;
		}
		if (myClearTrustStore) {
			return null;
		}
		return mySelectedConfig.getTlsTruststore();
	}

	private void initBottomTabs() {

		TabPanel bottomTabs = new TabPanel();
		bottomTabs.addStyleName(CssConstants.CONTENT_OUTER_TAB_PANEL);
		add(bottomTabs);

		{
			FlowPanel contentPanel = new FlowPanel();
			bottomTabs.add(contentPanel, "Failover");
			bottomTabs.selectTab(0);

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

			myStickySessionCookieNameBox = new EditableField();
			myStickySessionCookieLabel = urlSelGrid.addRow("Session Cookie", myStickySessionCookieNameBox);

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

		}

		{
			FlowPanel contentPanel = new FlowPanel();
			bottomTabs.add(contentPanel, "TCP");
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
		}
		{
			/*
			 * Retries
			 */
			FlowPanel contentPanel = new FlowPanel();
			bottomTabs.add(contentPanel, "Retry");

			contentPanel.add(new HtmlH1(MSGS.httpClientConfigsPanel_RetriesTitle()));
			contentPanel.add(new HTML(MSGS.httpClientConfigsPanel_RetriesDesc()));

			TwoColumnGrid retriesGrid = new TwoColumnGrid();
			contentPanel.add(retriesGrid);
			myRetriesTextBox = new IntegerBox();
			retriesGrid.addRow(MSGS.httpClientConfigsPanel_RetriesLabel(), myRetriesTextBox);
			// myRetriesTextBox.addValueChangeHandler(ValidatingTextBoxChangeHandlerPositiveInteger.INSTANCE_0_OR_ABOVE);
			// myRetriesTextBox.addKeyPressHandler(ValidatingTextBoxChangeHandlerPositiveInteger.INSTANCE_0_OR_ABOVE);
		}
		{
			/*
			 * SSL
			 */
			FlowPanel contentPanel = new FlowPanel();
			bottomTabs.add(contentPanel, "SSL/TLS");

			Frame uploadFrame = new Frame();
			uploadFrame.setVisible(false);
			myUploadFrameId = Document.get().createUniqueId();
			uploadFrame.getElement().setId(myUploadFrameId);
			;
			contentPanel.add(uploadFrame);

			// contentPanel.add(new
			// HtmlH1(MSGS.httpClientConfigsPanel_SSLTitle()));
			contentPanel.add(new HTML(MSGS.httpClientConfigsPanel_SSLDesc()));

			String uploadFieldName = ConstantsHttpClientConfig.TYPE_KEYSTORE;
			String titleLabel = "Keystore";
			myKeystorePasswordBox = new EditableField();
			myKeystorePasswordBox.setEmptyTextToDisplay("(No password)");
			myKeyStoreLabel = new HTML("", true);

			createKeystorePanel(contentPanel, titleLabel, uploadFieldName, myKeystorePasswordBox, myKeyStoreLabel, true);

			uploadFieldName = ConstantsHttpClientConfig.TYPE_TRUSTSTORE;
			titleLabel = "Truststore";
			myTruststorePasswordBox = new EditableField();
			myTruststorePasswordBox.setEmptyTextToDisplay("(No password)");
			myTrustStoreLabel = new HTML("", true);

			createKeystorePanel(contentPanel, titleLabel, uploadFieldName, myTruststorePasswordBox, myTrustStoreLabel, false);

		}
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
				mySelectedConfig = myConfigs.get(myConfigsListBox.getSelectedIndex());
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

	}

	private void removeConfig() {
		DtoHttpClientConfig config = myConfigs.get(myConfigsListBox.getSelectedIndex());
		if (config.isDefault()) {
			Window.alert(MSGS.httpClientConfigsPanel_CantDeleteDefault());
			return;
		}
		if (!Window.confirm(MSGS.httpClientConfigsPanel_ConfirmDelete(config.getId()))) {
			return;
		}

		myConfigListLoadingSpinner.show();

		AdminPortal.SVC_HTTPCLIENTCONFIG.deleteHttpClientConfig(config.getPid(), new MyHttpClientConfigListHandler());

	}

	private void setConfigList(GHttpClientConfigList theConfigList) {
		assert theConfigList.size() > 0;
		myConfigListLoadingSpinner.hideCompletely();
		myConfigs = theConfigList;
		updateConfigList();
		enableToolbar();
	}

	// private void updateConfigList() {
	// myUpdatingConfigsListBox = true;
	// myConfigsListBox.clear();
	//
	// int selectedIndex = 0;
	// for (GHttpClientConfig next : myConfigs) {
	// String desc = next.getId();
	// if (StringUtil.isNotBlank(next.getName())) {
	// desc = desc + " - " + next.getName();
	// }
	// if (mySelectedPid != null && mySelectedPid.equals(next.getPid())) {
	// selectedIndex = myConfigsListBox.getItemCount();
	// }
	// myConfigsListBox.addItem(desc, Long.toString(next.getPid()));
	// }
	//
	// myConfigsListBox.setSelectedIndex(selectedIndex);
	//
	// myUpdatingConfigsListBox = false;
	//
	// String value = myConfigsListBox.getValue(selectedIndex);
	// Long newSelectedId = Long.parseLong(value);
	// if (!newSelectedId.equals(mySelectedPid)) {
	// mySelectedPid = newSelectedId;
	// updateSelectedConfig();
	// }
	//
	// }

	private void updateConfigList() {
		myUpdatingConfigsListBox = true;
		myConfigsListBox.clear();

		int selectedIndex = 0;
		for (DtoHttpClientConfig next : myConfigs) {
			String desc = next.getId();
			if (StringUtil.isNotBlank(next.getName())) {
				desc = desc + " - " + next.getName();
			}
			if (mySelectedConfig != null && mySelectedConfig.getPid() == next.getPid()) {
				selectedIndex = myConfigsListBox.getItemCount();
			}
			myConfigsListBox.addItem(desc, Long.toString(next.getPid()));
		}

		myConfigsListBox.setSelectedIndex(selectedIndex);

		myUpdatingConfigsListBox = false;

		DtoHttpClientConfig newSelected = myConfigs.get(selectedIndex);
		if (!newSelected.equals(mySelectedConfig)) {
			mySelectedConfig = newSelected;
			updateSelectedConfig();
		}

	}

	private void updateSelectedConfig() {
		myIdTextBox.setValue(mySelectedConfig.getId());
		myNameTextBox.setValue(mySelectedConfig.getName());

		boolean nameAndIdEditable = !DtoHttpClientConfig.DEFAULT_ID.equals(mySelectedConfig.getId());
		myIdTextBox.setEnabled(nameAndIdEditable);
		myNameTextBox.setEnabled(nameAndIdEditable);

		int indexOf = UrlSelectionPolicy.indexOf(mySelectedConfig.getUrlSelectionPolicy());
		myUrlSelectionPolicyListBox.setSelectedIndex(indexOf);
		updateSelectedUrlSelectionPolicy();

		myCircuitBreakerEnabledCheck.setValue(mySelectedConfig.isCircuitBreakerEnabled());
		myCircuitBreakerDelayBox.setValue((mySelectedConfig.getCircuitBreakerTimeBetweenResetAttempts()));

		myTcpConnectTimeoutTb.setValue((mySelectedConfig.getConnectTimeoutMillis()));
		myTcpReadTimeoutTb.setValue((mySelectedConfig.getReadTimeoutMillis()));
		
		myRetriesTextBox.setValue((mySelectedConfig.getFailureRetriesBeforeAborting()));

		myStickySessionCookieNameBox.setValue(mySelectedConfig.getStickySessionCookieForSessionId());
		
		myNewKeystore=null;
		myClearKeystore=false;
		myNewTruststore=null;
		myClearTrustStore=false;
		
		updateKeystoreLabels();
	}

	private void updateKeystoreLabels() {

		// Truststore

		DtoKeystoreAnalysis trust = getTruststoreAnalysis();
		if (trust == null) {
			myTrustStoreLabel.setText("No Truststore defined for this config");
			myTruststorePasswordBox.setValue("");
		} else {
			if (myNewTruststore != null) {
				if (trust.isPasswordAccepted() == false) {
					myTrustStoreLabel.setText("Failed to process store because of error: " + trust.getProblemDescription());
				} else {
					SafeHtmlBuilder stringBuilder = new SafeHtmlBuilder();
					stringBuilder.appendHtmlConstant("Successfully uploaded new store, contains ");
					stringBuilder.append(trust.getKeyAliases().size());
					stringBuilder.appendHtmlConstant(" entries: ");
					stringBuilder.append(describeKeystore(trust));
					myTrustStoreLabel.setHTML(stringBuilder.toSafeHtml());
				}
			} else {
				SafeHtmlBuilder stringBuilder = new SafeHtmlBuilder();
				stringBuilder.appendHtmlConstant("Config has a truststore defined, contains ");
				stringBuilder.append(trust.getKeyAliases().size());
				stringBuilder.appendHtmlConstant(" entries: ");
				stringBuilder.append(describeKeystore(trust));
				myTrustStoreLabel.setHTML(stringBuilder.toSafeHtml());
			}
			myTruststorePasswordBox.setValue(trust.getPassword());
		}

		// Keystore

		DtoKeystoreAnalysis keystore = getKeystoreAnalysis();
		if (keystore == null) {
			myKeyStoreLabel.setText("No Keystore defined for this config");
			myKeystorePasswordBox.setValue("");
		} else {
			if (myNewKeystore != null) {
				if (keystore.isPasswordAccepted() == false) {
					myKeyStoreLabel.setText("Failed to process store because of error: " + keystore.getProblemDescription());
				} else {
					SafeHtmlBuilder stringBuilder = new SafeHtmlBuilder();
					stringBuilder.appendHtmlConstant("Successfully uploaded new store, contains ");
					stringBuilder.append(keystore.getKeyAliases().size());
					stringBuilder.appendHtmlConstant(" entries: ");
					stringBuilder.append(describeKeystore(keystore));
					myKeyStoreLabel.setHTML(stringBuilder.toSafeHtml());
				}
			} else {
				SafeHtmlBuilder stringBuilder = new SafeHtmlBuilder();
				stringBuilder.appendHtmlConstant("Config has a keystore defined, contains ");
				stringBuilder.append(keystore.getKeyAliases().size());
				stringBuilder.appendHtmlConstant(" entries: ");
				stringBuilder.append(describeKeystore(keystore));
				myKeyStoreLabel.setHTML(stringBuilder.toSafeHtml());
			}
			myKeystorePasswordBox.setValue(keystore.getPassword());
		}

	}

	private SafeHtml describeKeystore(DtoKeystoreAnalysis theKeystore) {
		SafeHtmlBuilder b = new SafeHtmlBuilder();

		b.appendHtmlConstant("<ul>");
		for (String nextAlias : theKeystore.getKeyAliases()) {
			b.appendHtmlConstant("<li>Alias: ");
			b.appendHtmlConstant("<b>");
			b.appendEscaped(nextAlias);
			b.appendHtmlConstant("</b>");
			b.appendHtmlConstant("<ul>");

			b.appendHtmlConstant("<li>");
			b.appendHtmlConstant("Subject: ");
			b.appendEscaped(theKeystore.getSubject().get(nextAlias));
			b.appendHtmlConstant("</li>");

			b.appendHtmlConstant("<li>");
			b.appendHtmlConstant("Issuer: ");
			b.appendEscaped(theKeystore.getIssuer().get(nextAlias));
			b.appendHtmlConstant("</li>");

			b.appendHtmlConstant("<li>");
			b.appendHtmlConstant("Expiry: ");
			b.appendEscaped(DateUtil.formatTime(theKeystore.getExpiryDate().get(nextAlias)));
			b.appendHtmlConstant("</li>");

			b.appendHtmlConstant("<li>");
			b.appendHtmlConstant("Private Key: ");
			b.appendEscaped(Boolean.TRUE.equals(theKeystore.getKeyEntry().get(nextAlias)) ? "Yes" : "No");
			b.appendHtmlConstant("</li>");

			b.appendHtmlConstant("</ul>");
			b.appendHtmlConstant("</li>");
		}
		b.appendHtmlConstant("</ul>");

		return b.toSafeHtml();
	}

	private void updateSelectedUrlSelectionPolicy() {
		int selectedIndex = myUrlSelectionPolicyListBox.getSelectedIndex();
		if (selectedIndex == -1) {
			return;
		}

		myStickySessionCookieLabel.setVisible(false);
		myStickySessionCookieNameBox.setVisible(false);

		UrlSelectionPolicy policy = UrlSelectionPolicy.values()[selectedIndex];
		switch (policy) {
		case PREFER_LOCAL:
			myUrlSelectionPolicyDescriptionLabel.setHTML(MSGS.urlSelectionPolicy_Desc_PreferLocal());
			break;
		case ROUND_ROBIN:
			myUrlSelectionPolicyDescriptionLabel.setHTML(MSGS.urlSelectionPolicy_Desc_RoundRobin());
			break;
		case RR_STICKY_SESSION:
			myUrlSelectionPolicyDescriptionLabel.setHTML(MSGS.urlSelectionPolicy_Desc_RrStickySessions());
			myStickySessionCookieNameBox.setValue(mySelectedConfig.getStickySessionCookieForSessionId(), false);
			myStickySessionCookieLabel.setVisible(true);
			myStickySessionCookieNameBox.setVisible(true);
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
