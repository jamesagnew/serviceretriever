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
import net.svcret.admin.shared.IAsyncLoadCallback;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.DtoKeystoreAnalysis;
import net.svcret.admin.shared.model.GHttpClientConfig;
import net.svcret.admin.shared.model.GHttpClientConfigList;
import net.svcret.admin.shared.model.UrlSelectionPolicy;
import net.svcret.admin.shared.util.StringUtil;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
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

public class SvcRetHttpClientConfigsPanel extends FlowPanel {

	private PButton _myAddButton;
	private IntegerBox myCircuitBreakerDelayBox;
	private CheckBox myCircuitBreakerEnabledCheck;
	private LoadingSpinner _myConfigListLoadingSpinner;
	private GHttpClientConfigList _myConfigs;
	private ListBox _myConfigsListBox;
	private TextBox _myIdTextBox;
	private HTML myKeyStoreLabel;
	private EditableField myKeystorePasswordBox;
	private LoadingSpinner _myLoadingSpinner;
	private TextBox _myNameTextBox;
	private DtoKeystoreAnalysis myNewKeystore;
	private DtoKeystoreAnalysis myNewTruststore;
	private PButton _myRemoveButton;
	private IntegerBox myRetriesTextBox;
	private GHttpClientConfig _mySelectedConfig;
	private IntegerBox myTcpConnectTimeoutTb;
	private IntegerBox myTcpReadTimeoutTb;
	private HTML myTrustStoreLabel;
	private EditableField myTruststorePasswordBox;
	private boolean _myUpdatingConfigsListBox;
	private String myUploadFrameId;
	private HTML myUrlSelectionPolicyDescriptionLabel;
	private ListBox myUrlSelectionPolicyListBox;
	private int ourNextUnsavedPid = -1;
	private EditableField myStickySessionCookieNameBox;
	private HtmlLabel myStickySessionCookieLabel;

	public SvcRetHttpClientConfigsPanel() {
		initDetailsPanel();
		initBottomTabs();

//		Model.getInstance().loadHttpClientConfigs(new IAsyncLoadCallback<GHttpClientConfigList>() {
//			@Override
//			public void onSuccess(GHttpClientConfigList theResult) {
//				if (myDefaultPid != null) {
//					mySelectedConfig = theResult.getConfigByPid(myDefaultPid);
//				}
//				setConfigList(theResult);
//			}
//
//		});

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
				pidHidden.setValue(Long.toString(_mySelectedConfig.getPid()));
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
					AdminPortal.SVC_HTTPCLIENTCONFIG.analyzeTransientTrustStore(_mySelectedConfig.getPid(), callback);
				} else {
					AdminPortal.SVC_HTTPCLIENTCONFIG.analyzeTransientKeyStore(_mySelectedConfig.getPid(), callback);
				}
			}
		});
		panel.add(submitButton);
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
		GHttpClientConfig config = _myConfigs.getConfigByPid(_mySelectedConfig.getPid());
		config.setId(_myIdTextBox.getValue());
		config.setName(_myNameTextBox.getValue());

		Integer connectTimeout = myTcpConnectTimeoutTb.getValue();
		myTcpConnectTimeoutTb.removeStyleName(CssConstants.TEXTBOX_WITH_ERR);
		if (connectTimeout == null || connectTimeout <= 0) {
			myTcpConnectTimeoutTb.addStyleName(CssConstants.TEXTBOX_WITH_ERR);
			_myLoadingSpinner.showMessage(MSGS.httpClientConfigsPanel_validateFailed_ConnectTimeout(), false);
			return;
		} else {
			config.setConnectTimeoutMillis(connectTimeout);
		}

		Integer readTimeout = myTcpReadTimeoutTb.getValue();
		myTcpReadTimeoutTb.removeStyleName(CssConstants.TEXTBOX_WITH_ERR);
		if (readTimeout == null || readTimeout <= 0) {
			myTcpReadTimeoutTb.addStyleName(CssConstants.TEXTBOX_WITH_ERR);
			_myLoadingSpinner.showMessage(MSGS.httpClientConfigsPanel_validateFailed_ReadTimeout(), false);
			return;
		} else {
			config.setReadTimeoutMillis(readTimeout);
		}

		config.setCircuitBreakerEnabled(myCircuitBreakerEnabledCheck.getValue());

		Integer cbRetryTimeout = myCircuitBreakerDelayBox.getValue();
		myCircuitBreakerDelayBox.removeStyleName(CssConstants.TEXTBOX_WITH_ERR);
		if (cbRetryTimeout == null || cbRetryTimeout <= 0) {
			myCircuitBreakerDelayBox.addStyleName(CssConstants.TEXTBOX_WITH_ERR);
			_myLoadingSpinner.showMessage(MSGS.httpClientConfigsPanel_validateFailed_CircuitBreakerDelay(), false);
			return;
		} else {
			config.setCircuitBreakerTimeBetweenResetAttempts(cbRetryTimeout);
		}

		Integer retries = myRetriesTextBox.getValue();
		myRetriesTextBox.removeStyleName(CssConstants.TEXTBOX_WITH_ERR);
		if (retries == null || retries < 0) {
			myRetriesTextBox.addStyleName(CssConstants.TEXTBOX_WITH_ERR);
			_myLoadingSpinner.showMessage(MSGS.httpClientConfigsPanel_validateFailed_Retries(), false);
			return;
		} else {
			config.setFailureRetriesBeforeAborting(retries);
		}

		UrlSelectionPolicy policy = UrlSelectionPolicy.values()[myUrlSelectionPolicyListBox.getSelectedIndex()];
		config.setUrlSelectionPolicy(policy);

		boolean create = config.getPid() < 0;

		if (myNewKeystore != null) {
			config.setTlsKeystore(myNewKeystore);
		}
		if (myNewTruststore != null) {
			config.setTlsKeystore(myNewTruststore);
		}

		_myLoadingSpinner.show();
		boolean useNewTruststore = myNewTruststore != null;
		boolean useNewKeystore = myNewKeystore != null;
		AdminPortal.SVC_HTTPCLIENTCONFIG.saveHttpClientConfig(create, useNewTruststore, useNewKeystore, config, new AsyncCallback<GHttpClientConfig>() {

			@Override
			public void onFailure(Throwable theCaught) {
				Model.handleFailure(theCaught);
			}

			@Override
			public void onSuccess(GHttpClientConfig theResult) {
				_myLoadingSpinner.showMessage("Saved", false);
				Model.getInstance().addHttpClientConfig(theResult);
				_mySelectedConfig = theResult;

				updateConfigList();
				updateSelectedConfig();
			}
		});
	}

	private void enableToolbar() {
		_myAddButton.setEnabled(true);
		_myRemoveButton.setEnabled(true);
	}

	private DtoKeystoreAnalysis getKeystoreAnalysis() {
		if (myNewKeystore != null) {
			return myNewKeystore;
		}
		return _mySelectedConfig.getTlsKeystore();
	}

	private DtoKeystoreAnalysis getTruststoreAnalysis() {
		if (myNewTruststore != null) {
			return myNewTruststore;
		}
		return _mySelectedConfig.getTlsTruststore();
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

	

	



	private void updateSelectedConfig() {
		_myIdTextBox.setValue(_mySelectedConfig.getId());
		_myNameTextBox.setValue(_mySelectedConfig.getName());

		boolean nameAndIdEditable = !GHttpClientConfig.DEFAULT_ID.equals(_mySelectedConfig.getId());
		_myIdTextBox.setEnabled(nameAndIdEditable);
		_myNameTextBox.setEnabled(nameAndIdEditable);

		int indexOf = UrlSelectionPolicy.indexOf(_mySelectedConfig.getUrlSelectionPolicy());
		myUrlSelectionPolicyListBox.setSelectedIndex(indexOf);
		updateSelectedUrlSelectionPolicy();

		myCircuitBreakerEnabledCheck.setValue(_mySelectedConfig.isCircuitBreakerEnabled());
		myCircuitBreakerDelayBox.setValue((_mySelectedConfig.getCircuitBreakerTimeBetweenResetAttempts()));

		myTcpConnectTimeoutTb.setValue((_mySelectedConfig.getConnectTimeoutMillis()));
		myTcpReadTimeoutTb.setValue((_mySelectedConfig.getReadTimeoutMillis()));

		myRetriesTextBox.setValue((_mySelectedConfig.getFailureRetriesBeforeAborting()));

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
					myTrustStoreLabel.setText("Successfully uploaded new store, contains " + trust.getKeyAliases().size() + " entries");
				}
			} else {
				myTrustStoreLabel.setText("Config has a truststore defined, contains " + trust.getKeyAliases().size() + " entries");
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
					myKeyStoreLabel.setText("Successfully uploaded new store, contains " + keystore.getKeyAliases().size() + " entries");
				}
			} else {
				myKeyStoreLabel.setText("Config has a truststore defined, contains " + keystore.getKeyAliases().size() + " entries");
			}
			myKeystorePasswordBox.setValue(keystore.getPassword());
		}

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
			myStickySessionCookieNameBox.setValue(_mySelectedConfig.getStickySessionCookieForSessionId(), false);
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
