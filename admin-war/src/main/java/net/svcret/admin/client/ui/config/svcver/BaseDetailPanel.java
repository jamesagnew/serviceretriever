package net.svcret.admin.client.ui.config.svcver;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.client.ui.components.HtmlBr;
import net.svcret.admin.client.ui.components.HtmlLabel;
import net.svcret.admin.client.ui.components.PButton;
import net.svcret.admin.client.ui.config.KeepRecentTransactionsPanel;
import net.svcret.admin.client.ui.config.sec.IProvidesViewAndEdit;
import net.svcret.admin.client.ui.config.sec.IProvidesViewAndEdit.IValueChangeHandler;
import net.svcret.admin.client.ui.config.sec.ViewAndEditFactory;
import net.svcret.admin.shared.IAsyncLoadCallback;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.BaseGClientSecurity;
import net.svcret.admin.shared.model.BaseGServerSecurity;
import net.svcret.admin.shared.model.BaseGServerSecurityList;
import net.svcret.admin.shared.model.BaseGServiceVersion;
import net.svcret.admin.shared.model.ClientSecurityEnum;
import net.svcret.admin.shared.model.GHttpClientConfig;
import net.svcret.admin.shared.model.GHttpClientConfigList;
import net.svcret.admin.shared.model.GServiceMethod;
import net.svcret.admin.shared.model.GServiceVersionUrl;
import net.svcret.admin.shared.model.ServerSecurityEnum;
import net.svcret.admin.shared.util.StringUtil;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import static net.svcret.admin.client.AdminPortal.*;


public abstract class BaseDetailPanel<T extends BaseGServiceVersion> extends FlowPanel {

	private static final int COL_CLI_SECURITY_MODULE = 1;
	private static final int COL_METHOD_NAME = 1;
	private static final int COL_SVR_SECURITY_MODULE = 1;
	private static final int COL_URL_ID = 1;
	private static final int COL_URL_URL = 2;

	private AbstractServiceVersionPanel myParent;
	private T myServiceVersion;
	private Grid myClientSecurityGrid;
	private ListBox myHttpConfigList;
	private Grid myMethodGrid;
	private long myNextBackgroundSave;
	private Label myNoClientSercuritysLabel;
	private Label myNoMethodsLabel;
	private Label myNoServerSercuritysLabel;
	private Label myNoUrlsLabel;
	private Grid myServerSecurityGrid;
	private Grid myUrlGrid;
	private KeepRecentTransactionsPanel myKeepRecentTransactionsPanel;

	public BaseDetailPanel(AbstractServiceVersionPanel theParent, T theServiceVersion) {
		myServiceVersion = theServiceVersion;
		myParent = theParent;

		addProtocolSpecificPanelsToTop();

		FlowPanel journalPanel = new FlowPanel();
		add(journalPanel);
		initJournalPanel(journalPanel);

		FlowPanel urlPanel = new FlowPanel();
		add(urlPanel);
		initUrlPanel(urlPanel);

		FlowPanel methodPanel = new FlowPanel();
		add(methodPanel);
		initMethodPanel(methodPanel);

		FlowPanel clientSecurityPanel = new FlowPanel();
		add(clientSecurityPanel);
		initClientSecurityPanel(clientSecurityPanel);

		FlowPanel serverSecurityPanel = new FlowPanel();
		add(serverSecurityPanel);
		initServerSecurityPanel(serverSecurityPanel);

	}

	protected abstract void addProtocolSpecificPanelsToTop();

	/**
	 * @return the parent
	 */
	protected AbstractServiceVersionPanel getParentPanel() {
		return myParent;
	}

	/**
	 * @return the serviceVersion
	 */
	protected T getServiceVersion() {
		return myServiceVersion;
	}

	public void doBackgroundSave() {
		myNextBackgroundSave = System.currentTimeMillis() + 1000;
		new Timer() {

			@Override
			public void run() {
				if (System.currentTimeMillis() > myNextBackgroundSave) {
					AsyncCallback<Void> callback = new AsyncCallback<Void>() {

						@Override
						public void onFailure(Throwable theCaught) {
							Model.handleFailure(theCaught);
						}

						@Override
						public void onSuccess(Void theResult) {
							GWT.log("Autosave complete");
						}
					};
					AdminPortal.MODEL_SVC.saveServiceVersionToSession(getServiceVersion(), callback);
				}
			}
		}.schedule(1200);

	}

	private void initClientSecurityPanel(FlowPanel thePanel) {
		thePanel.setStylePrimaryName(CssConstants.MAIN_PANEL);

		Label titleLabel = new Label("Client Security");
		titleLabel.setStyleName(CssConstants.MAIN_PANEL_TITLE);
		thePanel.add(titleLabel);

		FlowPanel contentPanel = new FlowPanel();
		contentPanel.addStyleName(CssConstants.CONTENT_INNER_PANEL);
		thePanel.add(contentPanel);

		Label urlLabel = new Label("Client Security modules provide credentials to proxied service implementations. In other words, " + "if the service which is being proxied requires credentials in order to be invoked, a client "
				+ "security module can be used to provide those credentials.");
		contentPanel.add(urlLabel);

		myClientSecurityGrid = new Grid(1, 2);
		myClientSecurityGrid.addStyleName(CssConstants.PROPERTY_TABLE);
		contentPanel.add(myClientSecurityGrid);

		myClientSecurityGrid.setWidget(0, 0, new Label("Action"));
		myClientSecurityGrid.setWidget(0, COL_METHOD_NAME, new Label("Module"));

		myNoClientSercuritysLabel = new Label("No Client Sercurity Modules Configured");
		contentPanel.add(myNoClientSercuritysLabel);

		contentPanel.add(new HtmlBr());

		PButton addClientButton = new PButton(IMAGES.iconAdd(), MSGS.actions_Add());
		contentPanel.add(addClientButton);
		final ListBox addClientList = new ListBox(false);
		for (ClientSecurityEnum next : ClientSecurityEnum.values()) {
			addClientList.addItem(next.getName(), next.name());
		}
		contentPanel.add(addClientList);
		addClientButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent theEvent) {
				ClientSecurityEnum type = ClientSecurityEnum.valueOf(addClientList.getValue(addClientList.getSelectedIndex()));
				BaseGClientSecurity module = type.newInstance();
				module.setUncommittedSessionId(newUncommittedSessionId());
				module.setEditMode(true);
				getServiceVersion().getClientSecurityList().add(module);
				updateClientSercurityPanel();
			}
		});

		updateClientSercurityPanel();

	}

	private void initJournalPanel(FlowPanel thePanel) {
		thePanel.setStylePrimaryName(CssConstants.MAIN_PANEL);

		Label titleLabel = new Label("Transaction Flow");
		titleLabel.setStyleName(CssConstants.MAIN_PANEL_TITLE);
		thePanel.add(titleLabel);

		FlowPanel contentPanel = new FlowPanel();
		contentPanel.addStyleName(CssConstants.CONTENT_INNER_PANEL);
		thePanel.add(contentPanel);

		myKeepRecentTransactionsPanel = new KeepRecentTransactionsPanel(getServiceVersion());
		contentPanel.add(myKeepRecentTransactionsPanel);
	}

	private void initMethodPanel(FlowPanel theMethodPanel) {
		theMethodPanel.setStylePrimaryName(CssConstants.MAIN_PANEL);

		Label titleLabel = new Label("Methods");
		titleLabel.setStyleName(CssConstants.MAIN_PANEL_TITLE);
		theMethodPanel.add(titleLabel);

		FlowPanel contentPanel = new FlowPanel();
		contentPanel.addStyleName(CssConstants.CONTENT_INNER_PANEL);
		theMethodPanel.add(contentPanel);

		myMethodGrid = new Grid(1, 2);
		myMethodGrid.addStyleName(CssConstants.PROPERTY_TABLE);
		contentPanel.add(myMethodGrid);

		myMethodGrid.setWidget(0, 0, new Label("Action"));
		myMethodGrid.setWidget(0, COL_METHOD_NAME, new Label("Name"));

		myNoMethodsLabel = new Label("No Methods Defined");
		contentPanel.add(myNoMethodsLabel);

		contentPanel.add(new HtmlBr());

		PButton addButton = new PButton(IMAGES.iconAdd(), MSGS.actions_Add());
		contentPanel.add(addButton);
		HtmlLabel addNameLabel = new HtmlLabel("Method Name:", "addMethodTb");
		contentPanel.add(addNameLabel);
		final TextBox addText = new TextBox();
		addText.getElement().setId("addMethodTb");
		contentPanel.add(addText);
		addButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent theEvent) {
				String name = addText.getValue();
				if (StringUtil.isBlank(name)) {
					Window.alert("Please enter a name for the new method");
					addText.setFocus(true);
					return;
				}
				if (getServiceVersion().hasMethodWithName(name)) {
					Window.alert("A method already exists with the name: " + name);
					return;
				}

				GServiceMethod method = new GServiceMethod();
				method.setUncommittedSessionId(newUncommittedSessionId());
				method.setName(name);
				method.setEditMode(true);
				getServiceVersion().getMethodList().add(method);
				updateMethodPanel();

				addText.setValue("");
			}
		});

		updateMethodPanel();
	}

	private void initServerSecurityPanel(FlowPanel thePanel) {
		thePanel.setStylePrimaryName(CssConstants.MAIN_PANEL);

		Label titleLabel = new Label("Server Security");
		titleLabel.setStyleName(CssConstants.MAIN_PANEL_TITLE);
		thePanel.add(titleLabel);

		FlowPanel contentPanel = new FlowPanel();
		contentPanel.addStyleName(CssConstants.CONTENT_INNER_PANEL);
		thePanel.add(contentPanel);

		Label urlLabel = new Label("Server Security modules verify that the client which is making requests coming " + "in to the proxy are authorized to invoke the particular service they are attempting to "
				+ "invoke. If no server security modules are defined for this service version, all requests will be " + "allowed to proceed.");
		contentPanel.add(urlLabel);

		myServerSecurityGrid = new Grid(1, 2);
		myServerSecurityGrid.addStyleName(CssConstants.PROPERTY_TABLE);
		contentPanel.add(myServerSecurityGrid);

		myServerSecurityGrid.setWidget(0, 0, new Label("Action"));
		myServerSecurityGrid.setWidget(0, COL_METHOD_NAME, new Label("Module"));

		myNoServerSercuritysLabel = new Label("No Server Sercurity Modules Configured");
		contentPanel.add(myNoServerSercuritysLabel);

		contentPanel.add(new HtmlBr());

		PButton addServerButton = new PButton(IMAGES.iconAdd(), MSGS.actions_Add());
		contentPanel.add(addServerButton);
		final ListBox addServerList = new ListBox(false);
		for (ServerSecurityEnum next : ServerSecurityEnum.values()) {
			addServerList.addItem(next.getName(), next.name());
		}
		contentPanel.add(addServerList);
		addServerButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent theEvent) {
				ServerSecurityEnum type = ServerSecurityEnum.valueOf(addServerList.getValue(addServerList.getSelectedIndex()));
				BaseGServerSecurity module = type.newInstance();
				module.setEditMode(true);
				module.setUncommittedSessionId(newUncommittedSessionId());
				getServiceVersion().getServerSecurityList().add(module);
				updateServerSercurityPanel();
			}

		});

		updateServerSercurityPanel();

	}

	private long newUncommittedSessionId() {
		return (long) (Math.random() * Long.MAX_VALUE);
	}

	private void updateClientSercurityPanel() {
		myClientSecurityGrid.resize(getServiceVersion().getClientSecurityList().size() + 1, 2);

		int row = 0;
		for (BaseGClientSecurity next : getServiceVersion().getClientSecurityList()) {
			row++;

			IProvidesViewAndEdit<BaseGClientSecurity> provider = ViewAndEditFactory.provideClientSecurity(next);
			Widget view;
			if (next.isEditMode()) {
				view = provider.provideEdit(row, next, new AutosaveValueChangeHandler());
			} else {
				view = provider.provideView(row, next);
			}

			myClientSecurityGrid.setWidget(row, 0, new ClientSecurityEditButtonPanel(next, row));
			myClientSecurityGrid.setWidget(row, COL_CLI_SECURITY_MODULE, view);

		}

		myNoClientSercuritysLabel.setVisible(getServiceVersion().getClientSecurityList().size() == 0);
	}

	protected void updateMethodPanel() {
		myMethodGrid.resize(getServiceVersion().getMethodList().size() + 1, 2);

		int row = 0;
		for (final GServiceMethod next : getServiceVersion().getMethodList()) {
			row++;

			myMethodGrid.setWidget(row, 0, new MethodEditButtonPanel(next));

			Widget nameWidget;
			if (next.isEditMode()) {
				final TextBox textBox = new TextBox();
				textBox.setValue(next.getName());
				textBox.addValueChangeHandler(new ValueChangeHandler<String>() {
					@Override
					public void onValueChange(ValueChangeEvent<String> theEvent) {
						next.setName(textBox.getValue());
						doBackgroundSave();
					}
				});
				nameWidget = textBox;
			} else {
				nameWidget = new Label(next.getName());
			}

			myMethodGrid.setWidget(row, COL_METHOD_NAME, nameWidget);

		}

		myNoMethodsLabel.setVisible(getServiceVersion().getMethodList().size() == 0);
	}

	private void updateServerSercurityPanel() {
		BaseGServerSecurityList serverSecurityList = getServiceVersion().getServerSecurityList();
		myServerSecurityGrid.resize(serverSecurityList.size() + 1, 2);

		int row = 0;
		for (BaseGServerSecurity next : getServiceVersion().getServerSecurityList()) {
			row++;

			IProvidesViewAndEdit<BaseGServerSecurity> provider = ViewAndEditFactory.provideServerSecurity(next);
			Widget view;
			if (next.isEditMode()) {
				view = provider.provideEdit(row, next, new AutosaveValueChangeHandler());
			} else {
				view = provider.provideView(row, next);
			}

			myServerSecurityGrid.setWidget(row, 0, new ServerSecurityEditButtonPanel(next, row));
			myServerSecurityGrid.setWidget(row, COL_SVR_SECURITY_MODULE, view);

		}

		myNoServerSercuritysLabel.setVisible(getServiceVersion().getServerSecurityList().size() == 0);
	}

	private final class AutosaveValueChangeHandler implements IValueChangeHandler {
		@Override
		public void onValueChange() {
			doBackgroundSave();
		}
	}

	private final class ClientSecurityEditButtonPanel extends FlowPanel implements ClickHandler {
		private PButton myDeleteButton;
		private PButton myEditButton;
		private int myRow;
		private BaseGClientSecurity mySec;

		private ClientSecurityEditButtonPanel(BaseGClientSecurity theSec, int theRow) {
			mySec = theSec;
			myRow = theRow;

			myEditButton = new PButton(IMAGES.iconEdit(), MSGS.actions_Edit());
			myEditButton.addClickHandler(this);
			add(myEditButton);

			myDeleteButton = new PButton(IMAGES.iconRemove(), MSGS.actions_Remove());
			myDeleteButton.addClickHandler(this);
			add(myDeleteButton);
		}

		@Override
		public void onClick(ClickEvent theEvent) {
			PButton source = (PButton) theEvent.getSource();
			if (source == myDeleteButton) {
				if (Window.confirm("Delete - Are you sure?")) {
					getServiceVersion().getClientSecurityList().remove(mySec);
					updateClientSercurityPanel();
					doBackgroundSave();
				}
			} else if (source == myEditButton) {
				IValueChangeHandler vcf = new IValueChangeHandler() {
					@Override
					public void onValueChange() {
						doBackgroundSave();
					}
				};
				final Widget editPanel = ViewAndEditFactory.provideClientSecurity(mySec).provideEdit(myRow, mySec, vcf);
				myMethodGrid.setWidget(myRow, COL_METHOD_NAME, editPanel);
			}

			source.setEnabled(false);
		}
	}

	private final class MethodEditButtonPanel extends FlowPanel implements ClickHandler {
		private PButton myDeleteButton;
		private PButton myEditButton;
		private GServiceMethod myMethod;

		private MethodEditButtonPanel(GServiceMethod theMethod) {
			myMethod = theMethod;
			myEditButton = new PButton(IMAGES.iconEdit(), MSGS.actions_Edit());
			myEditButton.addClickHandler(this);
			add(myEditButton);

			myDeleteButton = new PButton(IMAGES.iconRemove(), MSGS.actions_Remove());
			myDeleteButton.addClickHandler(this);
			add(myDeleteButton);
		}

		@Override
		public void onClick(ClickEvent theEvent) {
			PButton source = (PButton) theEvent.getSource();
			if (source == myDeleteButton) {
				if (Window.confirm("Delete - Are you sure?")) {
					getServiceVersion().getMethodList().remove(myMethod);
					updateMethodPanel();
					doBackgroundSave();
				}
			} else if (source == myEditButton) {
				myMethod.setEditMode(true);
				updateMethodPanel();
			}

			source.setEnabled(false);
		}
	}

	private final class ServerSecurityEditButtonPanel extends FlowPanel implements ClickHandler {

		private PButton myDeleteButton;
		private PButton myEditButton;
		private int myRow;
		private BaseGServerSecurity mySec;

		private ServerSecurityEditButtonPanel(BaseGServerSecurity theSec, int theRow) {
			mySec = theSec;
			myRow = theRow;

			myEditButton = new PButton(IMAGES.iconEdit(), MSGS.actions_Edit());
			myEditButton.addClickHandler(this);
			add(myEditButton);

			myDeleteButton = new PButton(IMAGES.iconRemove(), MSGS.actions_Remove());
			myDeleteButton.addClickHandler(this);
			add(myDeleteButton);
		}

		@Override
		public void onClick(ClickEvent theEvent) {
			PButton source = (PButton) theEvent.getSource();
			if (source == myDeleteButton) {
				if (Window.confirm("Delete - Are you sure?")) {
					getServiceVersion().getServerSecurityList().remove(mySec);
					updateServerSercurityPanel();
					doBackgroundSave();
				}
			} else if (source == myEditButton) {
				IValueChangeHandler vcf = new AutosaveValueChangeHandler();
				final Widget editPanel = ViewAndEditFactory.provideServerSecurity(mySec).provideEdit(myRow, mySec, vcf);
				myMethodGrid.setWidget(myRow, COL_METHOD_NAME, editPanel);
			}

			source.setEnabled(false);
		}
	}

	private final class UrlEditButtonPanel extends FlowPanel implements ClickHandler {
		private PButton myDeleteButton;
		private PButton myEditButton;
		private GServiceVersionUrl myUrl;

		private UrlEditButtonPanel(GServiceVersionUrl theUrl) {
			myUrl = theUrl;

			myEditButton = new PButton(IMAGES.iconEdit(), MSGS.actions_Edit());
			myEditButton.addClickHandler(this);
			add(myEditButton);

			myDeleteButton = new PButton(IMAGES.iconRemove(), MSGS.actions_Remove());
			myDeleteButton.addClickHandler(this);
			add(myDeleteButton);
		}

		@Override
		public void onClick(ClickEvent theEvent) {
			PButton source = (PButton) theEvent.getSource();
			if (source == myDeleteButton) {
				if (Window.confirm("Delete - Are you sure?")) {
					getServiceVersion().getUrlList().remove(myUrl);
					updateMethodPanel();
					doBackgroundSave();
				}
			} else if (source == myEditButton) {
				myUrl.setEditMode(true);
				updateUrlPanel();
			}

			source.setEnabled(false);
		}
	}

	public boolean validateValuesAndApplyIfGood() {
		boolean retVal = myKeepRecentTransactionsPanel.validateAndShowErrorIfNotValid();
		if (retVal) {
			myKeepRecentTransactionsPanel.populateDto(getServiceVersion());
		}
		return retVal;
	}

	protected void updateUrlPanel() {
		myUrlGrid.resize(getServiceVersion().getUrlList().size() + 1, 3);

		int row = 0;
		for (final GServiceVersionUrl next : getServiceVersion().getUrlList()) {
			row++;

			myUrlGrid.setWidget(row, 0, new UrlEditButtonPanel(next));

			if (next.isEditMode()) {
				final TextBox idTextBox = new TextBox();
				idTextBox.setValue(next.getId());
				idTextBox.addValueChangeHandler(new ValueChangeHandler<String>() {
					@Override
					public void onValueChange(ValueChangeEvent<String> theEvent) {
						next.setId(idTextBox.getValue());
						doBackgroundSave();
					}
				});
				myUrlGrid.setWidget(row, COL_URL_ID, idTextBox);

				final TextBox urlTextBox = new TextBox();
				urlTextBox.setValue(next.getUrl());
				urlTextBox.addValueChangeHandler(new ValueChangeHandler<String>() {
					@Override
					public void onValueChange(ValueChangeEvent<String> theEvent) {
						next.setUrl(urlTextBox.getValue());
						doBackgroundSave();
					}
				});
				myUrlGrid.setWidget(row, COL_URL_URL, urlTextBox);
			} else {
				myUrlGrid.setWidget(row, COL_URL_ID, new Label(next.getId()));
				myUrlGrid.setWidget(row, COL_URL_URL, new Label(next.getUrl()));
			}
		}

		myNoUrlsLabel.setVisible(getServiceVersion().getUrlList().size() == 0);
	}

	private void initUrlPanel(FlowPanel theProxyPanel) {
		theProxyPanel.setStylePrimaryName(CssConstants.MAIN_PANEL);

		Label titleLabel = new Label("Implementation URLs");
		titleLabel.setStyleName(CssConstants.MAIN_PANEL_TITLE);
		theProxyPanel.add(titleLabel);

		FlowPanel contentPanel = new FlowPanel();
		contentPanel.addStyleName(CssConstants.CONTENT_INNER_PANEL);
		theProxyPanel.add(contentPanel);

		contentPanel.add(new Label("Each proxied service will have one or more implementation URLs. " + "When a client attempts to invoke a service that has been proxied, the ServiceProxy will " + "forward this request to one of these implementations. Specifying more than one "
				+ "implementation URL means that if one is unavailable, another can be tried (i.e. redundancy)."));

		myUrlGrid = new Grid(1, 3);
		myUrlGrid.addStyleName(CssConstants.PROPERTY_TABLE);
		contentPanel.add(myUrlGrid);

		myUrlGrid.setWidget(0, 0, new Label("Action"));
		myUrlGrid.setWidget(0, COL_URL_ID, new Label("ID"));
		myUrlGrid.setWidget(0, COL_URL_URL, new Label("URL"));

		myNoUrlsLabel = new Label("No URLs Defined");
		contentPanel.add(myNoUrlsLabel);

		contentPanel.add(new HtmlBr());

		PButton addButton = new PButton(IMAGES.iconAdd(), MSGS.actions_Add());
		contentPanel.add(addButton);
		HtmlLabel addNameLabel = new HtmlLabel("URL:", "addUrlTb");
		contentPanel.add(addNameLabel);
		final TextBox addText = new TextBox();
		addText.getElement().setId("addUrlTb");
		contentPanel.add(addText);
		addButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent theEvent) {
				String urlText = addText.getValue();
				if (StringUtil.isBlank(urlText)) {
					Window.alert("Please enter a URL");
					addText.setFocus(true);
					return;
				}
				if (getServiceVersion().hasUrlWithName(urlText)) {
					Window.alert("Duplicate URL: " + urlText);
					return;
				}

				GServiceVersionUrl url = new GServiceVersionUrl();
				url.setUncommittedSessionId(newUncommittedSessionId());
				url.setEditMode(true);
				url.setUrl(urlText);

				getServiceVersion().getUrlList().add(url);

				for (int urlNum = getServiceVersion().getUrlList().size();; urlNum++) {
					String name = "url" + urlNum;
					if (getServiceVersion().getUrlList().getUrlWithId(name) == null) {
						url.setId(name);
						break;
					}
				}

				updateUrlPanel();

				addText.setValue("");
			}
		});

		contentPanel.add(new HtmlBr());

		FlowPanel clientConfigPanel = new FlowPanel();
		contentPanel.add(clientConfigPanel);

		clientConfigPanel.addStyleName(CssConstants.CONTENT_INNER_SUBPANEL);
		clientConfigPanel.add(new Label("The HTTP client configuration provides the connection details for " + "how the proxy will attempt to invoke proxied service implementations. This includes " + "settings for timeouts, round-robin policies, etc."));

		myHttpConfigList = new ListBox();
		clientConfigPanel.add(myHttpConfigList);

		Model.getInstance().loadHttpClientConfigs(new IAsyncLoadCallback<GHttpClientConfigList>() {

			@Override
			public void onSuccess(final GHttpClientConfigList theResult) {
				for (GHttpClientConfig next : theResult) {
					myHttpConfigList.addItem(next.getId() + " (" + next.getName() + ")");
					if (next.getPid() == getServiceVersion().getHttpClientConfigPid()) {
						myHttpConfigList.setSelectedIndex(myHttpConfigList.getItemCount() - 1);
					}
				}
				if (getServiceVersion().getHttpClientConfigPid() == 0) {
					myHttpConfigList.setSelectedIndex(0);
					getServiceVersion().setHttpClientConfigPid(theResult.get(0).getPid());
				}
				myHttpConfigList.addChangeHandler(new ChangeHandler() {
					@Override
					public void onChange(ChangeEvent theEvent) {
						getServiceVersion().setHttpClientConfigPid(theResult.get(myHttpConfigList.getSelectedIndex()).getPid());
					}
				});
			}
		});

		updateUrlPanel();
	}

}
