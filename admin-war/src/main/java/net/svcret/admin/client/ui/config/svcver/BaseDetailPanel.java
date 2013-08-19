package net.svcret.admin.client.ui.config.svcver;

import static net.svcret.admin.client.AdminPortal.*;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.client.ui.components.HtmlBr;
import net.svcret.admin.client.ui.components.HtmlH1;
import net.svcret.admin.client.ui.components.HtmlLabel;
import net.svcret.admin.client.ui.components.PButton;
import net.svcret.admin.client.ui.components.PButtonCell;
import net.svcret.admin.client.ui.components.PCellTable;
import net.svcret.admin.client.ui.components.PSelectionCell;
import net.svcret.admin.client.ui.components.Sparkline;
import net.svcret.admin.client.ui.components.TwoColumnGrid;
import net.svcret.admin.client.ui.config.KeepRecentTransactionsPanel;
import net.svcret.admin.client.ui.config.sec.IProvidesViewAndEdit;
import net.svcret.admin.client.ui.config.sec.IProvidesViewAndEdit.IValueChangeHandler;
import net.svcret.admin.client.ui.config.sec.ViewAndEditFactory;
import net.svcret.admin.shared.IAsyncLoadCallback;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.enm.MethodSecurityPolicyEnum;
import net.svcret.admin.shared.enm.ServerSecurityModeEnum;
import net.svcret.admin.shared.model.BaseGClientSecurity;
import net.svcret.admin.shared.model.BaseGServerSecurity;
import net.svcret.admin.shared.model.BaseGServerSecurityList;
import net.svcret.admin.shared.model.BaseGServiceVersion;
import net.svcret.admin.shared.model.ClientSecurityEnum;
import net.svcret.admin.shared.model.GHttpClientConfig;
import net.svcret.admin.shared.model.GHttpClientConfigList;
import net.svcret.admin.shared.model.GServiceMethod;
import net.svcret.admin.shared.model.GServiceVersionDetailedStats;
import net.svcret.admin.shared.model.ServerSecurityEnum;
import net.svcret.admin.shared.model.ServiceProtocolEnum;
import net.svcret.admin.shared.util.StringUtil;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;

public abstract class BaseDetailPanel<T extends BaseGServiceVersion> extends TabPanel {

	private static final int COL_CLI_SECURITY_MODULE = 1;
	private static final int COL_METHOD_NAME = 1;
	private static final int COL_SVR_SECURITY_MODULE = 1;
	private static final NumberFormat TRANSACTION_FORMAT = NumberFormat.getFormat("0.0#");

	private Grid myClientSecurityGrid;
	private CheckBox myExplicitProxyPathEnabledCheckbox;
	private TextBox myExplicitProxyPathTextbox;
	private ListBox myHttpConfigList;
	private KeepRecentTransactionsPanel myKeepRecentTransactionsPanel;
	private ListDataProvider<GServiceMethod> myMethodDataProvider;
	private long myNextBackgroundSave;
	private Label myNoClientSercuritysLabel;
	private Label myNoServerSercuritysLabel;
	private AbstractServiceVersionPanel myParent;
	private Grid myServerSecurityGrid;
	private T myServiceVersion;
	private UrlGrid myUrlGrid;
	private ListBox myServerSecurityModeBox;
	private HTML myServerSecurityModeDescription;

	public BaseDetailPanel(AbstractServiceVersionPanel theParent, T theServiceVersion) {
		myServiceVersion = theServiceVersion;
		myParent = theParent;

		addStyleName(CssConstants.CONTENT_OUTER_TAB_PANEL);

		addProtocolSpecificPanelsToTop(theParent.isAddPanel());

		final int methodsIndex = getWidgetCount();
		final FlowPanel methodPanel = new FlowPanel();
		add(methodPanel, "Methods");

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

		addSelectionHandler(new SelectionHandler<Integer>() {
			@Override
			public void onSelection(SelectionEvent<Integer> theEvent) {
				if (theEvent.getSelectedItem() == methodsIndex && methodPanel.getWidgetCount() == 0) {
					initMethodPanel(methodPanel);
				}
			}
		});

		selectTab(0);

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

	public abstract ServiceProtocolEnum getProtocol();

	public UrlGrid getUrlGrid() {
		return myUrlGrid;
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
		final CellTable<GServiceMethod> grid = new PCellTable<GServiceMethod>();
		theMethodPanel.add(grid);
		grid.setEmptyTableWidget(new Label("No methods defined."));

		myMethodDataProvider = new ListDataProvider<GServiceMethod>();
		myMethodDataProvider.addDataDisplay(grid);

		// Action

		PButtonCell deleteCell = new PButtonCell(AdminPortal.IMAGES.iconRemove());

		Column<GServiceMethod, String> action = new NullColumn<GServiceMethod>(deleteCell);
		grid.addColumn(action, "");
		action.setFieldUpdater(new FieldUpdater<GServiceMethod, String>() {
			@Override
			public void update(int theIndex, GServiceMethod theObject, String theValue) {
				if (Window.confirm("Delete - Are you sure?")) {
					getServiceVersion().getMethodList().remove(theObject);
					updateMethodPanel();
					doBackgroundSave();
				}
			}
		});

		// Name

		Column<GServiceMethod, SafeHtml> nameColumn = new Column<GServiceMethod, SafeHtml>(new SafeHtmlCell()) {
			@Override
			public SafeHtml getValue(GServiceMethod theObject) {
				return SafeHtmlUtils.fromString(theObject.getName());
			}
		};
		grid.addColumn(nameColumn, "Name");
		grid.getColumn(grid.getColumnCount() - 1).setSortable(true);
		ListHandler<GServiceMethod> columnSortHandler = new ListHandler<GServiceMethod>(myMethodDataProvider.getList());
		columnSortHandler.setComparator(nameColumn, new Comparator<GServiceMethod>() {
			@Override
			public int compare(GServiceMethod theO1, GServiceMethod theO2) {
				return StringUtil.compare(theO1.getName(), theO2.getName());
			}
		});

		// Root Elements

		Column<GServiceMethod, SafeHtml> rootElementsColumn = new Column<GServiceMethod, SafeHtml>(new SafeHtmlCell()) {
			@Override
			public SafeHtml getValue(GServiceMethod theObject) {
				return SafeHtmlUtils.fromString(StringUtil.defaultString(theObject.getRootElements()));
			}

		};
		grid.addColumn(rootElementsColumn, "Root Elements");
		grid.getColumn(grid.getColumnCount() - 1).setSortable(true);
		ListHandler<GServiceMethod> rootElementsSortHandler = new ListHandler<GServiceMethod>(myMethodDataProvider.getList());
		rootElementsSortHandler.setComparator(rootElementsColumn, new Comparator<GServiceMethod>() {
			@Override
			public int compare(GServiceMethod theO1, GServiceMethod theO2) {
				return StringUtil.compare(theO1.getRootElements(), theO2.getRootElements());
			}
		});

		// Security Policy

		List<String> secValues = MethodSecurityPolicyEnum.valuesAsNameList();
		List<String> secTexts = MethodSecurityPolicyEnum.valuesAsFriendlyNameList();
		PSelectionCell secPolicyCell = new PSelectionCell(secValues, secTexts);
		secPolicyCell.setDisableWithMessageOnNullValue("No security defined");
		Column<GServiceMethod, String> secPolicyColumn = new Column<GServiceMethod, String>(secPolicyCell) {
			@Override
			public String getValue(GServiceMethod theObject) {
				if (myServiceVersion.isSecure()) {
					return theObject.getSecurityPolicy().name();
				} else {
					return null;
				}
			}

		};
		grid.addColumn(secPolicyColumn, "Security Policy");
		grid.getColumn(grid.getColumnCount() - 1).setSortable(true);
		ListHandler<GServiceMethod> secPolicySortHandler = new ListHandler<GServiceMethod>(myMethodDataProvider.getList());
		secPolicySortHandler.setComparator(rootElementsColumn, new Comparator<GServiceMethod>() {
			@Override
			public int compare(GServiceMethod theO1, GServiceMethod theO2) {
				return theO1.getSecurityPolicy().ordinal() - theO2.getSecurityPolicy().ordinal();
			}
		});

		// Usage

		Column<GServiceMethod, SafeHtml> usageColumn = new Column<GServiceMethod, SafeHtml>(new SafeHtmlCell()) {
			@Override
			public SafeHtml getValue(GServiceMethod theObject) {
				SafeHtmlBuilder b = new SafeHtmlBuilder();

				GServiceVersionDetailedStats detailedStats = myServiceVersion.getDetailedStats();
				if (detailedStats != null) {
					Map<Long, List<Integer>> methodPidToSuccessCount = detailedStats.getMethodPidToSuccessCount();
					List<Integer> success = methodPidToSuccessCount.get(theObject.getPid());
					List<Integer> fault = detailedStats.getMethodPidToFaultCount().get(theObject.getPid());
					List<Integer> fail = detailedStats.getMethodPidToFailCount().get(theObject.getPid());
					List<Integer> secFail = detailedStats.getMethodPidToSecurityFailCount().get(theObject.getPid());
					renderTransactionGraphsAsHtml(b, success, fault, fail, secFail, detailedStats.getStatsTimestamps());
				}
				return b.toSafeHtml();
			}

		};
		grid.addColumn(usageColumn, "Usage");
		grid.getColumn(grid.getColumnCount() - 1).setSortable(true);
		secPolicySortHandler.setComparator(usageColumn, new Comparator<GServiceMethod>() {
			@Override
			public int compare(GServiceMethod theO1, GServiceMethod theO2) {
				GServiceVersionDetailedStats detailedStats = myServiceVersion.getDetailedStats();
				if (detailedStats == null) {
					return 0;
				}
				long o1 = theO1.getPid();
				long o2 = theO2.getPid();

				Map<Long, List<Integer>> successCount = detailedStats.getMethodPidToSuccessCount();
				Map<Long, List<Integer>> faultCount = detailedStats.getMethodPidToFaultCount();
				Map<Long, List<Integer>> failCount = detailedStats.getMethodPidToFailCount();
				Map<Long, List<Integer>> secFailCount = detailedStats.getMethodPidToSecurityFailCount();
				@SuppressWarnings("unchecked")
				int total1 = addToTotal(successCount.get(o1), faultCount.get(o1), failCount.get(o1), secFailCount.get(o1));
				@SuppressWarnings("unchecked")
				int total2 = addToTotal(successCount.get(o2), faultCount.get(o2), failCount.get(o2), secFailCount.get(o2));
				return total2 - total1;
			}

			private int addToTotal(List<Integer>... theLists) {
				int retVal = 0;
				for (List<Integer> next : theLists) {
					if (next != null) {
						for (Integer nextInteger : next) {
							if (nextInteger != null) {
								retVal += nextInteger;
							}
						}
					}
				}
				return retVal;
			}
		});
		grid.addColumnSortHandler(secPolicySortHandler);

		grid.getColumnSortList().push(nameColumn);

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

				myServerSecurityModeBox.setEnabled(true);
				if (myServiceVersion.getServerSecurityMode() == ServerSecurityModeEnum.NONE) {
					myServerSecurityModeBox.setSelectedIndex(ServerSecurityModeEnum.indexOfDefault());
					handleServerSecurityModeChange();
				}
			}

		});

		thePanel.add(new HtmlH1("Server Security Mode"));

		TwoColumnGrid propsGrid = new TwoColumnGrid();
		thePanel.add(propsGrid);
		myServerSecurityModeBox = new ListBox(false);
		propsGrid.addRow("Mode", myServerSecurityModeBox);
		myServerSecurityModeDescription = propsGrid.addDescription("");
		for (ServerSecurityModeEnum next : ServerSecurityModeEnum.values()) {
			myServerSecurityModeBox.addItem(next.getFriendlyName(), next.name());
			if (next == ServerSecurityModeEnum.NONE && myServiceVersion.getServerSecurityList().size() == 0) {
				myServerSecurityModeBox.setSelectedIndex(myServerSecurityModeBox.getItemCount() - 1);
			} else if (next == myServiceVersion.getServerSecurityMode() && myServiceVersion.getServerSecurityList().size() > 0) {
				myServerSecurityModeBox.setSelectedIndex(myServerSecurityModeBox.getItemCount() - 1);
			}
		}
		myServerSecurityModeBox.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent theEvent) {
				handleServerSecurityModeChange();
			}
		});
		handleServerSecurityModeChange();
		if (myServiceVersion.getServerSecurityList().size() == 0) {
			myServerSecurityModeBox.setEnabled(false);
		}

		updateServerSercurityPanel();

	}

	private void handleServerSecurityModeChange() {
		int index = myServerSecurityModeBox.getSelectedIndex();
		if (index != -1) {
			ServerSecurityModeEnum mode = ServerSecurityModeEnum.values()[index];
			myServerSecurityModeDescription.setText(mode.getDescription());
			if (myServiceVersion != null) {
				myServiceVersion.setServerSecurityMode(mode);
			}
		}
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

	void updateMethodPanel() {
		if (myMethodDataProvider != null) {
			List<GServiceMethod> list = myMethodDataProvider.getList();
			list.clear();
			list.addAll(myServiceVersion.getMethodList().toList());
			myMethodDataProvider.refresh();
		}
	}

	public static void renderTransactionGraphsAsHtml(SafeHtmlBuilder b, List<Integer> success, List<Integer> fault, List<Integer> fail, List<Integer> secFail, List<Long> theStatsTimestamps) {
		boolean hasAnything = false;
		if (hasValues(success)) {
			hasAnything = true;
			b.appendHtmlConstant("<span class='" + CssConstants.TRANSACTION_GRAPH_HEADER + "'>");
			b.appendHtmlConstant("Success (" + toMethodStatDesc(success) + ")</span>");
			Sparkline sparkline = new Sparkline(success, theStatsTimestamps).withWidth("120px").asBar(true);
			b.appendHtmlConstant("<br/>");
			b.appendHtmlConstant("<span id='" + sparkline.getId() + "' onmouseover=\"" + sparkline.getNativeInvocation(sparkline.getId()) + "\">AAAA</span>");
			b.appendHtmlConstant("<img src='images/empty.png' onload=\"" + sparkline.getNativeInvocation(sparkline.getId()) + "\" />");
		}
		if (hasValues(fault)) {
			hasAnything = true;
			if (hasAnything) {
				b.appendHtmlConstant("<br/>");
			}
			b.appendHtmlConstant("<span class='" + CssConstants.TRANSACTION_GRAPH_HEADER + "'>");
			b.appendHtmlConstant("Fault (" + toMethodStatDesc(fault) + ")</span>");
			Sparkline sparkline = new Sparkline(fault, theStatsTimestamps).withWidth("120px").asBar(true);
			b.appendHtmlConstant("<br/>");
			b.appendHtmlConstant("<span id='" + sparkline.getId() + "'></span>");
			b.appendHtmlConstant("<img src='images/empty.png' onload=\"" + sparkline.getNativeInvocation(sparkline.getId()) + "\" />");
		}
		if (hasValues(fail)) {
			hasAnything = true;
			if (hasAnything) {
				b.appendHtmlConstant("<br/>");
			}
			b.appendHtmlConstant("<span class='" + CssConstants.TRANSACTION_GRAPH_HEADER + "'>");
			b.appendHtmlConstant("Fail (" + toMethodStatDesc(fail) + ")</span>");
			Sparkline sparkline = new Sparkline(fail, theStatsTimestamps).withWidth("120px").asBar(true);
			b.appendHtmlConstant("<br/>");
			b.appendHtmlConstant("<span id='" + sparkline.getId() + "'></span>");
			b.appendHtmlConstant("<img src='images/empty.png' onload=\"" + sparkline.getNativeInvocation(sparkline.getId()) + "\" />");
		}
		if (hasValues(secFail)) {
			hasAnything = true;
			if (hasAnything) {
				b.appendHtmlConstant("<br/>");
			}
			b.appendHtmlConstant("<span class='" + CssConstants.TRANSACTION_GRAPH_HEADER + "'>");
			b.appendHtmlConstant("Security Failure (" + toMethodStatDesc(secFail) + ")</span>");
			Sparkline sparkline = new Sparkline(secFail, theStatsTimestamps).withWidth("120px").asBar(true);
			b.appendHtmlConstant("<br/>");
			b.appendHtmlConstant("<span id='" + sparkline.getId() + "'></span>");
			b.appendHtmlConstant("<img src='images/empty.png' onload=\"" + sparkline.getNativeInvocation(sparkline.getId()) + "\" />");
		}
		if (!hasAnything) {
			b.appendHtmlConstant("No Usage");
		}
	}

	private static boolean hasValues(List<Integer> theSuccess) {
		if (theSuccess != null) {
			for (Integer integer : theSuccess) {
				if (integer != 0) {
					return true;
				}
			}
		}
		return false;
	}

	private static String toMethodStatDesc(List<Integer> theSecFail) {
		int total = 0;
		int max = 0;
		for (int next : theSecFail) {
			total += next;
			max = Math.max(max, next);
		}

		double avg = ((double) total) / (double) theSecFail.size();
		return "Avg:" + TRANSACTION_FORMAT.format(avg) + " Max:" + TRANSACTION_FORMAT.format(max) + "/min";
	}

	static long newUncommittedSessionId() {
		return (long) (Math.random() * Long.MAX_VALUE);
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
				myClientSecurityGrid.setWidget(myRow, COL_METHOD_NAME, editPanel);
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

			if (!theSec.isEditMode()) {
				myEditButton = new PButton(IMAGES.iconEdit(), MSGS.actions_Edit());
				myEditButton.addClickHandler(this);
				add(myEditButton);
			}

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
				myServerSecurityGrid.setWidget(myRow, COL_METHOD_NAME, editPanel);
			}

			source.setEnabled(false);
		}
	}

}
