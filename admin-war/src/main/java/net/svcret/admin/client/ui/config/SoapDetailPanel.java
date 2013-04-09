package net.svcret.admin.client.ui.config;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.nav.NavProcessor;
import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.client.ui.components.HtmlBr;
import net.svcret.admin.client.ui.components.HtmlLabel;
import net.svcret.admin.client.ui.components.LoadingSpinner;
import net.svcret.admin.client.ui.components.PButton;
import net.svcret.admin.client.ui.config.sec.IProvidesViewAndEdit;
import net.svcret.admin.client.ui.config.sec.ViewAndEditFactory;
import net.svcret.admin.client.ui.config.sec.IProvidesViewAndEdit.IValueChangeHandler;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.BaseGClientSecurity;
import net.svcret.admin.shared.model.BaseGServerSecurity;
import net.svcret.admin.shared.model.BaseGServerSecurityList;
import net.svcret.admin.shared.model.ClientSecurityEnum;
import net.svcret.admin.shared.model.GServiceMethod;
import net.svcret.admin.shared.model.GServiceVersionUrl;
import net.svcret.admin.shared.model.GSoap11ServiceVersion;
import net.svcret.admin.shared.model.ServerSecurityEnum;
import net.svcret.admin.shared.util.StringUtil;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Style.Float;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class SoapDetailPanel extends FlowPanel {

	private static final int COL_CLI_SECURITY_MODULE = 1;
	private static final int COL_METHOD_NAME = 1;
	private static final int COL_SVR_SECURITY_MODULE = 1;

	private static final int COL_URL_ID = 1;
	private static final int COL_URL_URL = 2;

	private Grid myClientSecurityGrid;
	private LoadingSpinner myLoadWsdlSpinner;
	private Grid myMethodGrid;
	private long myNextBackgroundSave;
	private Label myNoClientSercuritysLabel;
	private Label myNoMethodsLabel;
	private Label myNoServerSercuritysLabel;
	private Label myNoUrlsLabel;
	private Grid myServerSecurityGrid;
	private GSoap11ServiceVersion myServiceVersion;
	private Grid myUrlGrid;
	private TextBox myUrlTextBox;

	public SoapDetailPanel(GSoap11ServiceVersion theServiceVersion) {
		myServiceVersion = theServiceVersion;

		FlowPanel wsdlPanel = new FlowPanel();
		add(wsdlPanel);
		initWsdlPanel(wsdlPanel);

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
					AdminPortal.MODEL_SVC.saveServiceVersionToSession(myServiceVersion, callback);
				}
			}
		}.schedule(1200);

	}

	private void handleLoadWsdl() {
		myLoadWsdlSpinner.show();
		final long start = System.currentTimeMillis();
		
		AsyncCallback<GSoap11ServiceVersion> callback = new AsyncCallback<GSoap11ServiceVersion>() {
			@Override
			public void onFailure(Throwable theCaught) {
				myLoadWsdlSpinner.showMessage(theCaught.getMessage(), false);
			}

			@Override
			public void onSuccess(GSoap11ServiceVersion theResult) {
				long time = System.currentTimeMillis() - start;
				myLoadWsdlSpinner.showMessage("Loaded WSDL in " + time + "ms", false);
				
				myServiceVersion = theResult;
				updateMethodPanel();
				updateUrlPanel();

				String navToken = NavProcessor.getTokenAddServiceVersion(true, myServiceVersion.getUncommittedSessionId());
				History.newItem(navToken, false);
			}
		};
		AdminPortal.MODEL_SVC.loadWsdl(myServiceVersion, myUrlTextBox.getValue(), callback);
	}

	private void initClientSecurityPanel(FlowPanel thePanel) {
		thePanel.setStylePrimaryName("mainPanel");

		Label titleLabel = new Label("Client Security");
		titleLabel.setStyleName("mainPanelTitle");
		thePanel.add(titleLabel);

		FlowPanel contentPanel = new FlowPanel();
		contentPanel.addStyleName("contentInnerPanel");
		thePanel.add(contentPanel);

		Label urlLabel = new Label("Client Security modules provide credentials to proxied service implementations. In other words, " + "if the service which is being proxied requires credentials in order to be invoked, a client "
				+ "security module can be used to provide those credentials.");
		contentPanel.add(urlLabel);

		myClientSecurityGrid = new Grid(1, 2);
		myClientSecurityGrid.addStyleName("dashboardTable");
		contentPanel.add(myClientSecurityGrid);

		myClientSecurityGrid.setWidget(0, 0, new Label("Action"));
		myClientSecurityGrid.setWidget(0, COL_METHOD_NAME, new Label("Module"));

		myNoClientSercuritysLabel = new Label("No Client Sercurity Modules Configured");
		contentPanel.add(myNoClientSercuritysLabel);

		contentPanel.add(new HtmlBr());

		PButton addClientButton = new PButton("Add");
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
				myServiceVersion.getClientSecurityList().add(module);
				updateClientSercurityPanel();
			}
		});

		updateClientSercurityPanel();

	}

	private void initMethodPanel(FlowPanel theMethodPanel) {
		theMethodPanel.setStylePrimaryName("mainPanel");

		Label titleLabel = new Label("Methods");
		titleLabel.setStyleName("mainPanelTitle");
		theMethodPanel.add(titleLabel);

		FlowPanel contentPanel = new FlowPanel();
		contentPanel.addStyleName("contentInnerPanel");
		theMethodPanel.add(contentPanel);

		myMethodGrid = new Grid(1, 2);
		myMethodGrid.addStyleName("dashboardTable");
		contentPanel.add(myMethodGrid);

		myMethodGrid.setWidget(0, 0, new Label("Action"));
		myMethodGrid.setWidget(0, COL_METHOD_NAME, new Label("Name"));

		myNoMethodsLabel = new Label("No Methods Defined");
		contentPanel.add(myNoMethodsLabel);

		contentPanel.add(new HtmlBr());

		PButton addButton = new PButton("Add");
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
				if (myServiceVersion.hasMethodWithName(name)) {
					Window.alert("A method already exists with the name: " + name);
					return;
				}

				GServiceMethod method = new GServiceMethod();
				method.setUncommittedSessionId(newUncommittedSessionId());
				method.setName(name);
				method.setEditMode(true);
				myServiceVersion.getMethodList().add(method);
				updateMethodPanel();

			}
		});

		updateMethodPanel();
	}

	private void initServerSecurityPanel(FlowPanel thePanel) {
		thePanel.setStylePrimaryName("mainPanel");

		Label titleLabel = new Label("Server Security");
		titleLabel.setStyleName("mainPanelTitle");
		thePanel.add(titleLabel);

		FlowPanel contentPanel = new FlowPanel();
		contentPanel.addStyleName("contentInnerPanel");
		thePanel.add(contentPanel);

		Label urlLabel = new Label("Server Security modules verify that the client which is making requests coming " + "in to the proxy are authorized to invoke the particular service they are attempting to "
				+ "invoke. If no server security modules are defined for this service version, all requests will be " + "allowed to proceed.");
		contentPanel.add(urlLabel);

		myServerSecurityGrid = new Grid(1, 2);
		myServerSecurityGrid.addStyleName("dashboardTable");
		contentPanel.add(myServerSecurityGrid);

		myServerSecurityGrid.setWidget(0, 0, new Label("Action"));
		myServerSecurityGrid.setWidget(0, COL_METHOD_NAME, new Label("Module"));

		myNoServerSercuritysLabel = new Label("No Server Sercurity Modules Configured");
		contentPanel.add(myNoServerSercuritysLabel);

		contentPanel.add(new HtmlBr());

		PButton addServerButton = new PButton("Add");
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
				myServiceVersion.getServerSecurityList().add(module);
				updateServerSercurityPanel();
			}

		});

		updateServerSercurityPanel();

	}

	private long newUncommittedSessionId() {
		return (long) (Math.random() * Long.MAX_VALUE);
	}

	private void initUrlPanel(FlowPanel theProxyPanel) {
		theProxyPanel.setStylePrimaryName("mainPanel");

		Label titleLabel = new Label("Implementation URLs");
		titleLabel.setStyleName("mainPanelTitle");
		theProxyPanel.add(titleLabel);

		FlowPanel contentPanel = new FlowPanel();
		contentPanel.addStyleName("contentInnerPanel");
		theProxyPanel.add(contentPanel);

		contentPanel.add(new Label("Each proxied service will have one or more implementation URLs. " +
				"When a client attempts to invoke a service that has been proxied, the ServiceProxy will " +
				"forward this request to one of these implementations. Specifying more than one " +
				"implementation URL means that if one is unavailable, another can be tried (i.e. redundancy)."));
		
		myUrlGrid = new Grid(1, 3);
		myUrlGrid.addStyleName(CssConstants.DASHBOARD_TABLE);
		contentPanel.add(myUrlGrid);

		myUrlGrid.setWidget(0, 0, new Label("Action"));
		myUrlGrid.setWidget(0, COL_URL_ID, new Label("ID"));
		myUrlGrid.setWidget(0, COL_URL_URL, new Label("URL"));

		myNoUrlsLabel = new Label("No URLs Defined");
		contentPanel.add(myNoUrlsLabel);

		contentPanel.add(new HtmlBr());

		PButton addButton = new PButton("Add");
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
				if (myServiceVersion.hasUrlWithName(urlText)) {
					Window.alert("Duplicate URL: " + urlText);
					return;
				}

				GServiceVersionUrl url = new GServiceVersionUrl();
				url.setUncommittedSessionId(newUncommittedSessionId());
				url.setEditMode(true);
				url.setUrl(urlText);
				myServiceVersion.getUrlList().add(url);
				updateMethodPanel();

			}
		});
		
		contentPanel.add(new HtmlBr());
		contentPanel.add(new Label(""));
		contentPanel.add(new Label("The HTTP client configuration provides the connection details for " +
				"how the proxy will attempt to invoke proxied service implementations. This includes " +
				"settings for timeouts, round-robin policies, etc."));
		
		
		updateUrlPanel();
	}

	private void initWsdlPanel(FlowPanel wsdlPanel) {
		wsdlPanel.setStylePrimaryName("mainPanel");

		Label titleLabel = new Label("WSDL Location");
		titleLabel.setStyleName("mainPanelTitle");
		wsdlPanel.add(titleLabel);

		FlowPanel contentPanel = new FlowPanel();
		contentPanel.addStyleName("contentInnerPanel");
		wsdlPanel.add(contentPanel);

		contentPanel.add(new Label("Every SOAP based service must have a backing WSDL, which provides " +
				"clients with the service contract being implemented. Enter a URL to a remote WSDL " +
				"here, and click the \"Load\" button below, and ServiceRetriever will fetch the " +
				"WSDL and initialize your service."));
		
		
		HtmlLabel urlLabel = new HtmlLabel("URL:", "urlTb");
		contentPanel.add(urlLabel);

		myUrlTextBox = new TextBox();
		myUrlTextBox.setValue(myServiceVersion.getWsdlLocation());
		myUrlTextBox.getElement().setId("urlTb");
		myUrlTextBox.getElement().getStyle().setWidth(500, Unit.PX);
		contentPanel.add(myUrlTextBox);

		contentPanel.add(new HtmlBr());

		PButton loadButton = new PButton("Load WSDL");
		loadButton.getElement().getStyle().setFloat(Float.LEFT);
		contentPanel.add(loadButton);

		myLoadWsdlSpinner = new LoadingSpinner();
		myLoadWsdlSpinner.hideCompletely();
		contentPanel.add(myLoadWsdlSpinner);

		loadButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent theEvent) {
				handleLoadWsdl();
			}
		});

		contentPanel.add(new HtmlBr());
	}

	private void updateClientSercurityPanel() {
		myClientSecurityGrid.resize(myServiceVersion.getClientSecurityList().size() + 1, 2);

		int row = 0;
		for (BaseGClientSecurity next : myServiceVersion.getClientSecurityList()) {
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

		myNoClientSercuritysLabel.setVisible(myServiceVersion.getClientSecurityList().size() == 0);
	}

	private void updateMethodPanel() {
		myMethodGrid.resize(myServiceVersion.getMethodList().size() + 1, 2);

		int row = 0;
		for (final GServiceMethod next : myServiceVersion.getMethodList()) {
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

		myNoMethodsLabel.setVisible(myServiceVersion.getMethodList().size() == 0);
	}

	private void updateServerSercurityPanel() {
		BaseGServerSecurityList serverSecurityList = myServiceVersion.getServerSecurityList();
		myServerSecurityGrid.resize(serverSecurityList.size() + 1, 2);

		int row = 0;
		for (BaseGServerSecurity next : myServiceVersion.getServerSecurityList()) {
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

		myNoServerSercuritysLabel.setVisible(myServiceVersion.getServerSecurityList().size() == 0);
	}

	private void updateUrlPanel() {
		myUrlGrid.resize(myServiceVersion.getUrlList().size() + 1, 3);

		int row = 0;
		for (final GServiceVersionUrl next : myServiceVersion.getUrlList()) {
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
			}else {
				myUrlGrid.setWidget(row, COL_URL_ID, new Label(next.getId()));
				myUrlGrid.setWidget(row, COL_URL_URL, new Label(next.getUrl()));
			}
		}

		myNoUrlsLabel.setVisible(myServiceVersion.getUrlList().size() == 0);
	}

	private final class ClientSecurityEditButtonPanel extends FlowPanel implements ClickHandler {
		private PButton myDeleteButton;
		private PButton myEditButton;
		private int myRow;
		private BaseGClientSecurity mySec;

		private ClientSecurityEditButtonPanel(BaseGClientSecurity theSec, int theRow) {
			mySec = theSec;
			myRow = theRow;

			myEditButton = new PButton("Edit");
			myEditButton.addClickHandler(this);
			add(myEditButton);

			myDeleteButton = new PButton("Delete");
			myDeleteButton.addClickHandler(this);
			add(myDeleteButton);
		}

		@Override
		public void onClick(ClickEvent theEvent) {
			PButton source = (PButton) theEvent.getSource();
			if (source == myDeleteButton) {
				if (Window.confirm("Delete - Are you sure?")) {
					myServiceVersion.getClientSecurityList().remove(mySec);
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
			myEditButton = new PButton("Edit");
			myEditButton.addClickHandler(this);
			add(myEditButton);

			myDeleteButton = new PButton("Delete");
			myDeleteButton.addClickHandler(this);
			add(myDeleteButton);
		}

		@Override
		public void onClick(ClickEvent theEvent) {
			PButton source = (PButton) theEvent.getSource();
			if (source == myDeleteButton) {
				if (Window.confirm("Delete - Are you sure?")) {
					myServiceVersion.getMethodList().remove(myMethod);
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

	private final class AutosaveValueChangeHandler implements IValueChangeHandler {
		@Override
		public void onValueChange() {
			doBackgroundSave();
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

			myEditButton = new PButton("Edit");
			myEditButton.addClickHandler(this);
			add(myEditButton);

			myDeleteButton = new PButton("Delete");
			myDeleteButton.addClickHandler(this);
			add(myDeleteButton);
		}

		@Override
		public void onClick(ClickEvent theEvent) {
			PButton source = (PButton) theEvent.getSource();
			if (source == myDeleteButton) {
				if (Window.confirm("Delete - Are you sure?")) {
					myServiceVersion.getServerSecurityList().remove(mySec);
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

			myEditButton = new PButton("Edit");
			myEditButton.addClickHandler(this);
			add(myEditButton);

			myDeleteButton = new PButton("Delete");
			myDeleteButton.addClickHandler(this);
			add(myDeleteButton);
		}

		@Override
		public void onClick(ClickEvent theEvent) {
			PButton source = (PButton) theEvent.getSource();
			if (source == myDeleteButton) {
				if (Window.confirm("Delete - Are you sure?")) {
					myServiceVersion.getUrlList().remove(myUrl);
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

}
