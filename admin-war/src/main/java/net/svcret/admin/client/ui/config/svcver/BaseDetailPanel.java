package net.svcret.admin.client.ui.config.svcver;

import static net.svcret.admin.client.AdminPortal.*;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.MyResources;
import net.svcret.admin.client.nav.NavProcessor;
import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.client.ui.components.EditableField;
import net.svcret.admin.client.ui.components.HtmlBr;
import net.svcret.admin.client.ui.components.HtmlH1;
import net.svcret.admin.client.ui.components.HtmlLabel;
import net.svcret.admin.client.ui.components.PButton;
import net.svcret.admin.client.ui.components.PButtonCell;
import net.svcret.admin.client.ui.components.PCellTable;
import net.svcret.admin.client.ui.components.PSelectionCell;
import net.svcret.admin.client.ui.components.TwoColumnGrid;
import net.svcret.admin.client.ui.components.UsageSparkline;
import net.svcret.admin.client.ui.config.KeepRecentTransactionsPanel;
import net.svcret.admin.client.ui.config.sec.IProvidesViewAndEdit;
import net.svcret.admin.client.ui.config.sec.IProvidesViewAndEdit.IValueChangeHandler;
import net.svcret.admin.client.ui.config.sec.ViewAndEditFactory;
import net.svcret.admin.shared.DateUtil;
import net.svcret.admin.shared.IAsyncLoadCallback;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.enm.ClientSecurityEnum;
import net.svcret.admin.shared.enm.MethodSecurityPolicyEnum;
import net.svcret.admin.shared.enm.ServerSecurityModeEnum;
import net.svcret.admin.shared.model.BaseDtoClientSecurity;
import net.svcret.admin.shared.model.BaseDtoServerSecurity;
import net.svcret.admin.shared.model.DtoServerSecurityList;
import net.svcret.admin.shared.model.BaseDtoServiceVersion;
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
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
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
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SetSelectionModel;

public abstract class BaseDetailPanel<T extends BaseDtoServiceVersion> extends TabPanel {

	private static final int COL_CLI_SECURITY_MODULE = 1;
	private static final int COL_METHOD_NAME = 1;
	private static final int COL_SVR_SECURITY_MODULE = 1;
	private static final int TAB_DOESNT_EXIST = -2;

	private Grid myClientSecurityGrid;
	private Hyperlink myEditHttpClientConfigLink;
	private CheckBox myExplicitProxyPathEnabledCheckbox;
	private EditableField myExplicitProxyPathTextbox;
	private GHttpClientConfigList myHttpClientConfigList;
	private ListBox myHttpConfigList;
	private KeepRecentTransactionsPanel myKeepRecentTransactionsPanel;
	private ListDataProvider<GServiceMethod> myMethodDataProvider;
	private long myNextBackgroundSave;
	private Label myNoClientSercuritysLabel;
	private Label myNoServerSercuritysLabel;
	private AbstractServiceVersionPanel myParent;
	private Grid myServerSecurityGrid;
	private ListBox myServerSecurityModeBox;
	private HTML myServerSecurityModeDescription;
	private T myServiceVersion;
	private CheckBox myStandardProxyPathEnabledCheckbox;
	private Label myStandardProxyPathLabel;
	private ServiceVersionUrlGrid myUrlGrid;
	private int myAccessPanelTabIndex;

	public BaseDetailPanel(AbstractServiceVersionPanel theParent, T theServiceVersion) {
		myServiceVersion = theServiceVersion;
		myParent = theParent;

		addStyleName(CssConstants.CONTENT_OUTER_TAB_PANEL);

		addProtocolSpecificPanelsToTop(theParent.isAddPanel());

		final int methodsIndex = getWidgetCount();
		final FlowPanel methodPanel = new FlowPanel();
		add(methodPanel, "Methods");

		final int urlsIndex;
		if (isIncludeUrlsTab()) {
			urlsIndex = getWidgetCount();
			FlowPanel urlsPanel = new FlowPanel();
			add(urlsPanel, "URLs");
			initUrlPanel(urlsPanel);
		} else {
			urlsIndex = TAB_DOESNT_EXIST;
		}

		FlowPanel accessPanel = new FlowPanel();
		myAccessPanelTabIndex = getWidgetCount();
		add(accessPanel, "Access");
		initAccessPanel(accessPanel);

		FlowPanel securityPanel = new FlowPanel();
		add(securityPanel, "Security");
		initServerSecurityPanel(securityPanel);

		if (isIncludeClientSecurity()) {
			FlowPanel csp = new FlowPanel();
			csp.addStyleName(CssConstants.CONTENT_INNER_SUBPANEL);
			securityPanel.add(csp);
			initClientSecurityPanel(csp);
		}

		FlowPanel transactionFlowPanel = new FlowPanel();
		add(transactionFlowPanel, "Config");
		initJournalPanel(transactionFlowPanel);

		addSelectionHandler(new SelectionHandler<Integer>() {
			@Override
			public void onSelection(SelectionEvent<Integer> theEvent) {
				if (theEvent.getSelectedItem() == methodsIndex) {
					if (methodPanel.getWidgetCount() == 0) {
						initMethodPanel(methodPanel);
					} else {
						myMethodDataProvider.refresh();
					}
				} else if (theEvent.getSelectedItem() == urlsIndex) {
					/*
					 * Ok to do this even if it's already been done because the urls might have changed (ie for a WSDL reload)
					 */
					myUrlGrid.setServiceVersion(myServiceVersion);
				}
			}
		});

		selectTab(0);

	}

	protected abstract void addProtocolSpecificPanelsToTop(boolean theIsAddPanel);

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

	protected GHttpClientConfig getHttpClientConfig() {
		return myHttpClientConfigList.get(myHttpConfigList.getSelectedIndex());
	}

	/**
	 * @return the parent
	 */
	protected AbstractServiceVersionPanel getParentPanel() {
		return myParent;
	}

	public abstract ServiceProtocolEnum getProtocol();

	/**
	 * @return the serviceVersion
	 */
	protected T getServiceVersion() {
		return myServiceVersion;
	}

	public BaseUrlGrid getUrlGrid() {
		return myUrlGrid;
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

	private void initAccessPanel(FlowPanel thePanel) {
		Label intro = new Label("By default, ServiceRetriever publishes your services at a simple " + "default path. If needed, an alternate path may be used instead, or as well as the "
				+ "default path.");
		thePanel.add(intro);

		TwoColumnGrid grid = new TwoColumnGrid();
		thePanel.add(grid);

		myStandardProxyPathEnabledCheckbox = new CheckBox("Use Default Proxy Path:");
		myStandardProxyPathEnabledCheckbox.setValue(myServiceVersion.isUseDefaultProxyPath());
		myStandardProxyPathEnabledCheckbox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> theEvent) {
				myServiceVersion.setUseDefaultProxyPath(myStandardProxyPathEnabledCheckbox.getValue());
			}
		});
		myStandardProxyPathLabel = new Label(myServiceVersion.getDefaultProxyPath());
		grid.addRow(myStandardProxyPathEnabledCheckbox, myStandardProxyPathLabel);

		myExplicitProxyPathEnabledCheckbox = new CheckBox("Use Alternate Proxy Path:");
		myExplicitProxyPathTextbox = new EditableField();
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

		Label urlLabel = new Label("Client Security modules provide credentials to proxied service implementations. In other words, "
				+ "if the service which is being proxied requires credentials in order to be invoked, a client " + "security module can be used to provide those credentials.");
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
				BaseDtoClientSecurity module = type.newInstance();
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

		ListHandler<GServiceMethod> sortHandler = new ListHandler<GServiceMethod>(myMethodDataProvider.getList());
		grid.addColumnSortHandler(sortHandler);

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
		sortHandler.setComparator(nameColumn, new Comparator<GServiceMethod>() {
			@Override
			public int compare(GServiceMethod theO1, GServiceMethod theO2) {
				return StringUtil.compare(theO1.getName(), theO2.getName());
			}
		});

		// Root Elements

		Column<GServiceMethod, SafeHtml> rootElementsColumn = new Column<GServiceMethod, SafeHtml>(new SafeHtmlCell()) {
			@Override
			public SafeHtml getValue(GServiceMethod theObject) {
				String value = StringUtil.defaultString(theObject.getRootElements());
				if (StringUtil.isNotBlank(value)) {
					int index = value.lastIndexOf(':');
					String namespace = value.substring(0, index);
					String element = value.substring(index + 1);
					StringBuilder b = new StringBuilder();
					b.append("<div onmouseover=\"return overlib('<b>Namespace:</b>&nbsp;");
					b.append(namespace);
					b.append("<br/><b>Element:</b>&nbsp;");
					b.append(element);
					b.append("');\" onmouseout=\"return nd();\" atitle=\"Namespace: ");
					b.append(namespace);
					b.append("<br/>Element: ");
					b.append(element);
					b.append("\" class=\"");
					b.append(MyResources.CSS.methodRootElementBlock());
					b.append("\">");
					b.append(SafeHtmlUtils.htmlEscape(namespace));
					b.append("<br/>");
					b.append(SafeHtmlUtils.htmlEscape(element));
					b.append("</div>");
					return SafeHtmlUtils.fromTrustedString(b.toString());
				} else {
					return SafeHtmlUtils.fromString("");
				}
			}

		};
		grid.addColumn(rootElementsColumn, "Root Elements");
		grid.getColumn(grid.getColumnCount() - 1).setSortable(true);
		sortHandler.setComparator(rootElementsColumn, new Comparator<GServiceMethod>() {
			@Override
			public int compare(GServiceMethod theO1, GServiceMethod theO2) {
				return StringUtil.compare(theO1.getRootElements(), theO2.getRootElements());
			}
		});

		// Security Policy

		List<String> secValues = MethodSecurityPolicyEnum.valuesAsNameList();
		List<String> secTexts = MethodSecurityPolicyEnum.valuesAsFriendlyNameList();
		PSelectionCell secPolicyCell = new PSelectionCell(secValues, secTexts);
		secPolicyCell.setDisableWithMessageOnNullValue("No server security");
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
		sortHandler.setComparator(rootElementsColumn, new Comparator<GServiceMethod>() {
			@Override
			public int compare(GServiceMethod theO1, GServiceMethod theO2) {
				return theO1.getSecurityPolicy().ordinal() - theO2.getSecurityPolicy().ordinal();
			}
		});
		secPolicyColumn.setFieldUpdater(new FieldUpdater<GServiceMethod, String>() {
			@Override
			public void update(int theIndex, GServiceMethod theObject, String theValue) {
				if (StringUtil.isNotBlank(theValue)) {
					theObject.setSecurityPolicy(MethodSecurityPolicyEnum.valueOf(theValue));
				}
			}
		});

		// Usage

		Column<GServiceMethod, SafeHtml> usageColumn = new Column<GServiceMethod, SafeHtml>(new SafeHtmlCell()) {
			@Override
			public SafeHtml getValue(GServiceMethod theObject) {
				SafeHtmlBuilder b = new SafeHtmlBuilder();

				GServiceVersionDetailedStats detailedStats = myServiceVersion.getDetailedStats();
				if (detailedStats != null) {
					Map<Long, int[]> methodPidToSuccessCount = detailedStats.getMethodPidToSuccessCount();
					int[] success = methodPidToSuccessCount.get(theObject.getPid());
					int[] fault = detailedStats.getMethodPidToFaultCount().get(theObject.getPid());
					int[] fail = detailedStats.getMethodPidToFailCount().get(theObject.getPid());
					int[] secFail = detailedStats.getMethodPidToSecurityFailCount().get(theObject.getPid());

					renderTransactionGraphsAsHtml(b, success, fault, fail, secFail, false, null);
				}
				return b.toSafeHtml();
			}

		};
		grid.addColumn(usageColumn, "Usage");
		grid.getColumn(grid.getColumnCount() - 1).setSortable(true);
		sortHandler.setComparator(usageColumn, new Comparator<GServiceMethod>() {
			private int addToTotal(int[]... theLists) {
				int retVal = 0;
				for (int[] next : theLists) {
					if (next != null) {
						for (int nextInteger : next) {
							retVal += nextInteger;
						}
					}
				}
				return retVal;
			}

			@Override
			public int compare(GServiceMethod theO1, GServiceMethod theO2) {
				GServiceVersionDetailedStats detailedStats = myServiceVersion.getDetailedStats();
				if (detailedStats == null) {
					return 0;
				}
				long o1 = theO1.getPid();
				long o2 = theO2.getPid();

				Map<Long, int[]> successCount = detailedStats.getMethodPidToSuccessCount();
				Map<Long, int[]> faultCount = detailedStats.getMethodPidToFaultCount();
				Map<Long, int[]> failCount = detailedStats.getMethodPidToFailCount();
				Map<Long, int[]> secFailCount = detailedStats.getMethodPidToSecurityFailCount();
				int total1 = addToTotal(successCount.get(o1), faultCount.get(o1), failCount.get(o1), secFailCount.get(o1));
				int total2 = addToTotal(successCount.get(o2), faultCount.get(o2), failCount.get(o2), secFailCount.get(o2));
				return total2 - total1;
			}
		});

		grid.getColumnSortList().push(nameColumn);

		// Add Method
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
				method.setSecurityPolicy(MethodSecurityPolicyEnum.getDefault());
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

		Label urlLabel = new Label("Server Security modules verify that the client which is making requests coming "
				+ "in to the proxy are authorized to invoke the particular service they are attempting to "
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
				BaseDtoServerSecurity module = type.newInstance();
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

	private void initUrlPanel(FlowPanel thePanel) {

		myUrlGrid = new ServiceVersionUrlGrid();
		thePanel.add(myUrlGrid);

		thePanel.add(new HtmlBr());

		FlowPanel clientConfigPanel = new FlowPanel();
		thePanel.add(clientConfigPanel);

		clientConfigPanel.addStyleName(CssConstants.CONTENT_INNER_SUBPANEL);
		clientConfigPanel.add(new Label("The HTTP client configuration provides the connection details for " + "how the proxy will attempt to invoke proxied service implementations. This includes "
				+ "settings for timeouts, round-robin policies, etc."));

		myHttpConfigList = new ListBox();

		HorizontalPanel listPanel = new HorizontalPanel();
		listPanel.add(myHttpConfigList);
		clientConfigPanel.add(listPanel);

		myEditHttpClientConfigLink = new Hyperlink();
		myEditHttpClientConfigLink.setText("Edit this config");
		listPanel.add(myEditHttpClientConfigLink);

		Model.getInstance().loadHttpClientConfigs(new IAsyncLoadCallback<GHttpClientConfigList>() {

			@Override
			public void onSuccess(final GHttpClientConfigList theResult) {
				myHttpClientConfigList = theResult;

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
						updateEditHttpClientConfigLink();
					}

				});

				updateEditHttpClientConfigLink();

			}

		});

	}

	protected boolean isIncludeClientSecurity() {
		return true;
	}

	protected boolean isIncludeUrlsTab() {
		return true;
	}

	private void updateClientSercurityPanel() {
		myClientSecurityGrid.resize(getServiceVersion().getClientSecurityList().size() + 1, 2);

		int row = 0;
		for (BaseDtoClientSecurity next : getServiceVersion().getClientSecurityList()) {
			row++;

			IProvidesViewAndEdit<BaseDtoClientSecurity> provider = ViewAndEditFactory.provideClientSecurity(next);
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

	private void updateEditHttpClientConfigLink() {
		myEditHttpClientConfigLink.setTargetHistoryToken(NavProcessor.getTokenEditHttpClientConfig(getServiceVersion().getHttpClientConfigPid()));
	}

	protected void updateMethodPanel() {
		if (myMethodDataProvider != null) {
			List<GServiceMethod> list = myMethodDataProvider.getList();
			list.clear();
			list.addAll(myServiceVersion.getMethodList().toList());
			myMethodDataProvider.refresh();
		}
	}

	private void updateServerSercurityPanel() {
		DtoServerSecurityList serverSecurityList = getServiceVersion().getServerSecurityList();
		myServerSecurityGrid.resize(serverSecurityList.size() + 1, 2);

		int row = 0;
		for (BaseDtoServerSecurity next : getServiceVersion().getServerSecurityList()) {
			row++;

			IProvidesViewAndEdit<BaseDtoServerSecurity> provider = ViewAndEditFactory.provideServerSecurity(next);
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

	public boolean validateValuesAndApplyIfGood() {
		boolean retVal = myKeepRecentTransactionsPanel.validateAndShowErrorIfNotValid();
		if (!retVal) {
			return false;
		}

		myKeepRecentTransactionsPanel.populateDto(getServiceVersion());

		if (myExplicitProxyPathEnabledCheckbox.getValue()) {
			String newPath = myExplicitProxyPathTextbox.getValue();
			if (StringUtil.isBlank(newPath)) {
				selectTab(myAccessPanelTabIndex);
				Window.alert("Explicit proxy path is enabled, but no path is specified.");
				myExplicitProxyPathTextbox.setEditorMode();
				return false;
			}
			if (!newPath.startsWith("/") || newPath.length() < 2) {
				selectTab(myAccessPanelTabIndex);
				Window.alert("Explicit proxy path must be of the form /[path[/more path]]");
				myExplicitProxyPathTextbox.setEditorMode();
				return false;
			}
			myServiceVersion.setExplicitProxyPath(newPath);
		} else {
			myServiceVersion.setExplicitProxyPath(null);
		}

		return retVal;
	}

	private static boolean hasValues(int[] theSuccess) {
		if (theSuccess != null) {
			for (int integer : theSuccess) {
				if (integer != 0) {
					return true;
				}
			}
		}
		return false;
	}

	static long newUncommittedSessionId() {
		return (long) (Math.random() * Long.MAX_VALUE);
	}

	public static void renderTransactionGraphsAsHtml(SafeHtmlBuilder b, int[] success, int[] fault, int[] fail, int[] secFail, boolean theLastUsageProvidedIfKnown, Date theLastUsage) {
		if (hasValues(success) || hasValues(fault) || hasValues(fail) || hasValues(secFail)) {
			UsageSparkline sparkline = new UsageSparkline(success, fault, fail, secFail, "");
			b.appendHtmlConstant("<span id='" + sparkline.getId() + "'></span>");
			b.appendHtmlConstant("<img src='images/empty.png' onload=\"" + sparkline.getNativeInvocation(sparkline.getId()) + "\" />");
		} else {
			if (!theLastUsageProvidedIfKnown) {
				b.appendHtmlConstant("No usage within timeframe");
			} else if (theLastUsage == null) {
				b.appendHtmlConstant("Never used");
			} else {
				b.appendHtmlConstant("No Usage since " + DateUtil.formatTime(theLastUsage));
			}
		}
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
		private BaseDtoClientSecurity mySec;

		private ClientSecurityEditButtonPanel(BaseDtoClientSecurity theSec, int theRow) {
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
		private BaseDtoServerSecurity mySec;

		private ServerSecurityEditButtonPanel(BaseDtoServerSecurity theSec, int theRow) {
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
