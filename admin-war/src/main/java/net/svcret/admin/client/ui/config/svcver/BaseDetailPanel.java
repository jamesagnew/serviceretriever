package net.svcret.admin.client.ui.config.svcver;

import static net.svcret.admin.client.AdminPortal.*;
import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.client.ui.components.HtmlBr;
import net.svcret.admin.client.ui.components.HtmlH1;
import net.svcret.admin.client.ui.components.HtmlLabel;
import net.svcret.admin.client.ui.components.PButton;
import net.svcret.admin.client.ui.components.TwoColumnGrid;
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
import net.svcret.admin.shared.model.ServerSecurityEnum;
import net.svcret.admin.shared.model.ServiceProtocolEnum;
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
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public abstract class BaseDetailPanel<T extends BaseGServiceVersion> extends TabPanel {

	private static final int COL_CLI_SECURITY_MODULE = 1;
	private static final int COL_METHOD_NAME = 1;
	private static final int COL_SVR_SECURITY_MODULE = 1;

	private AbstractServiceVersionPanel myParent;
	private T myServiceVersion;
	private Grid myClientSecurityGrid;
	private ListBox myHttpConfigList;
	private Grid myMethodGrid;
	private long myNextBackgroundSave;
	private Label myNoClientSercuritysLabel;
	private Label myNoMethodsLabel;
	private Label myNoServerSercuritysLabel;
	private Grid myServerSecurityGrid;
	private KeepRecentTransactionsPanel myKeepRecentTransactionsPanel;
	private CheckBox myExplicitProxyPathEnabledCheckbox;
	private TextBox myExplicitProxyPathTextbox;
	private UrlGrid myUrlGrid;

	public BaseDetailPanel(AbstractServiceVersionPanel theParent, T theServiceVersion) {
		myServiceVersion = theServiceVersion;
		myParent = theParent;

		addStyleName(CssConstants.CONTENT_OUTER_TAB_PANEL);

		addProtocolSpecificPanelsToTop(theParent.isAddPanel());

		FlowPanel methodPanel = new FlowPanel();
		add(methodPanel, "Methods");
		initMethodPanel(methodPanel);

		FlowPanel urlsPanel = new FlowPanel();
		add(urlsPanel, "URLs");
		initUrlPanel(urlsPanel);

		FlowPanel accessPanel = new FlowPanel();
		add(accessPanel, "Access");
		initAccessPanel(accessPanel);

		FlowPanel securityPanel = new FlowPanel();
		add(securityPanel, "Security");
		initServerSecurityPanel(securityPanel);
		FlowPanel csp = new FlowPanel();
		csp.addStyleName(CssConstants.CONTENT_INNER_SUBPANEL);
		securityPanel.add(csp);
		initClientSecurityPanel(csp);

		FlowPanel transactionFlowPanel = new FlowPanel();
		add(transactionFlowPanel, "Config");
		initJournalPanel(transactionFlowPanel);

		selectTab(0);

	}

	private void initAccessPanel(FlowPanel thePanel) {
		Label intro = new Label("If specified, the options below define the path at which " + "the service will be deployed. If not specified, a default will be used.");
		thePanel.add(intro);

		TwoColumnGrid grid = new TwoColumnGrid();
		thePanel.add(grid);

		myExplicitProxyPathEnabledCheckbox = new CheckBox("Use Explicit Proxy Path:");
		myExplicitProxyPathTextbox = new TextBox();
		grid.addRow(myExplicitProxyPathEnabledCheckbox, myExplicitProxyPathTextbox);
		
		myExplicitProxyPathEnabledCheckbox.setValue(myServiceVersion.getExplicitProxyPath() != null);
		myExplicitProxyPathTextbox.setValue(myServiceVersion.getExplicitProxyPath());

	}

	public abstract ServiceProtocolEnum getProtocol();

	protected abstract void addProtocolSpecificPanelsToTop(boolean theIsAddPanel);

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
		// thePanel.setStylePrimaryName(CssConstants.MAIN_PANEL);
		//
		// Label titleLabel = new Label("Client Security");
		// titleLabel.setStyleName(CssConstants.MAIN_PANEL_TITLE);
		// thePanel.add(titleLabel);
		//
		// FlowPanel contentPanel = new FlowPanel();
		// contentPanel.addStyleName(CssConstants.CONTENT_INNER_PANEL);
		// thePanel.add(contentPanel);

		thePanel.add(new HtmlH1("Client Security"));

		Label urlLabel = new Label("Client Security modules provide credentials to proxied service implementations. In other words, " + "if the service which is being proxied requires credentials in order to be invoked, a client "
				+ "security module can be used to provide those credentials.");
		thePanel.add(urlLabel);

		myClientSecurityGrid = new Grid(1, 2);
		myClientSecurityGrid.addStyleName(CssConstants.PROPERTY_TABLE);
		thePanel.add(myClientSecurityGrid);

		myClientSecurityGrid.setWidget(0, 0, new Label("Action"));
		myClientSecurityGrid.setWidget(0, COL_METHOD_NAME, new Label("Module"));

		myNoClientSercuritysLabel = new Label("No Client Sercurity Modules Configured");
		thePanel.add(myNoClientSercuritysLabel);

		thePanel.add(new HtmlBr());

		PButton addClientButton = new PButton(IMAGES.iconAdd(), MSGS.actions_Add());
		thePanel.add(addClientButton);
		final ListBox addClientList = new ListBox(false);
		for (ClientSecurityEnum next : ClientSecurityEnum.values()) {
			addClientList.addItem(next.getName(), next.name());
		}
		thePanel.add(addClientList);
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
		// thePanel.setStylePrimaryName(CssConstants.MAIN_PANEL);
		//
		// Label titleLabel = new Label("Transaction Flow");
		// titleLabel.setStyleName(CssConstants.MAIN_PANEL_TITLE);
		// thePanel.add(titleLabel);
		//
		// FlowPanel contentPanel = new FlowPanel();
		// contentPanel.addStyleName(CssConstants.CONTENT_INNER_PANEL);
		// thePanel.add(contentPanel);

		myKeepRecentTransactionsPanel = new KeepRecentTransactionsPanel(getServiceVersion());
		thePanel.add(myKeepRecentTransactionsPanel);
	}

	private void initMethodPanel(FlowPanel theMethodPanel) {

		myMethodGrid = new Grid(1, 2);
		myMethodGrid.addStyleName(CssConstants.PROPERTY_TABLE);
		theMethodPanel.add(myMethodGrid);

		myMethodGrid.setWidget(0, 0, new Label("Action"));
		myMethodGrid.setWidget(0, COL_METHOD_NAME, new Label("Name"));

		myNoMethodsLabel = new Label("No Methods Defined");
		theMethodPanel.add(myNoMethodsLabel);

		theMethodPanel.add(new HtmlBr());

		PButton addButton = new PButton(IMAGES.iconAdd(), MSGS.actions_Add());
		theMethodPanel.add(addButton);
		HtmlLabel addNameLabel = new HtmlLabel("Method Name:", "addMethodTb");
		theMethodPanel.add(addNameLabel);
		final TextBox addText = new TextBox();
		addText.getElement().setId("addMethodTb");
		theMethodPanel.add(addText);
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
				getServiceVersion().getMethodList().add(method);
				updateMethodPanel();

				addText.setValue("");
			}
		});

		updateMethodPanel();
	}

	private void initServerSecurityPanel(FlowPanel thePanel) {
		// thePanel.setStylePrimaryName(CssConstants.MAIN_PANEL);
		//
		// Label titleLabel = new Label("Server Security");
		// titleLabel.setStyleName(CssConstants.MAIN_PANEL_TITLE);
		// thePanel.add(titleLabel);
		//
		// FlowPanel contentPanel = new FlowPanel();
		// contentPanel.addStyleName(CssConstants.CONTENT_INNER_PANEL);
		// thePanel.add(contentPanel);

		thePanel.add(new HtmlH1("Server Security"));

		Label urlLabel = new Label("Server Security modules verify that the client which is making requests coming " + "in to the proxy are authorized to invoke the particular service they are attempting to "
				+ "invoke. If no server security modules are defined for this service version, all requests will be " + "allowed to proceed.");
		thePanel.add(urlLabel);

		myServerSecurityGrid = new Grid(1, 2);
		myServerSecurityGrid.addStyleName(CssConstants.PROPERTY_TABLE);
		thePanel.add(myServerSecurityGrid);

		myServerSecurityGrid.setWidget(0, 0, new Label("Action"));
		myServerSecurityGrid.setWidget(0, COL_METHOD_NAME, new Label("Module"));

		myNoServerSercuritysLabel = new Label("No Server Sercurity Modules Configured");
		thePanel.add(myNoServerSercuritysLabel);

		thePanel.add(new HtmlBr());

		PButton addServerButton = new PButton(IMAGES.iconAdd(), MSGS.actions_Add());
		thePanel.add(addServerButton);
		final ListBox addServerList = new ListBox(false);
		for (ServerSecurityEnum next : ServerSecurityEnum.values()) {
			if (next.appliesTo(myServiceVersion.getClass())) {
				addServerList.addItem(next.getName(), next.name());
			}
		}
		thePanel.add(addServerList);
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

	static long newUncommittedSessionId() {
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

			Widget nameWidget = new Label(next.getName());

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
		private GServiceMethod myMethod;

		private MethodEditButtonPanel(GServiceMethod theMethod) {
			myMethod = theMethod;

			myDeleteButton = new PButton(IMAGES.iconRemove());
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


	public boolean validateValuesAndApplyIfGood() {
		boolean retVal = myKeepRecentTransactionsPanel.validateAndShowErrorIfNotValid();
		if (!retVal) {
			return false;
		}

		myKeepRecentTransactionsPanel.populateDto(getServiceVersion());

		if (myExplicitProxyPathEnabledCheckbox.getValue()) {
			String newPath = myExplicitProxyPathTextbox.getValue();
			if (StringUtil.isBlank(newPath)) {
				Window.alert("Explicit proxy path is enabled, but no path is specified.");
				myExplicitProxyPathTextbox.setFocus(true);
				return false;
			}
			if (!newPath.startsWith("/") || newPath.length() < 2) {
				Window.alert("Explicit proxy path must be of the form /[path[/more path]]");
				myExplicitProxyPathTextbox.setFocus(true);
				return false;
			}
			myServiceVersion.setExplicitProxyPath(newPath);
		} else {
			myServiceVersion.setExplicitProxyPath(null);
		}

		return retVal;
	}


	private void initUrlPanel(FlowPanel thePanel) {

		myUrlGrid = new UrlGrid(myServiceVersion);
		thePanel.add(myUrlGrid);

		thePanel.add(new HtmlBr());

		FlowPanel clientConfigPanel = new FlowPanel();
		thePanel.add(clientConfigPanel);

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

	}

	public UrlGrid getUrlGrid() {
		return myUrlGrid;
	}

}
