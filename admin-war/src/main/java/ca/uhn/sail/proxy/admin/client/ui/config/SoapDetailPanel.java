package ca.uhn.sail.proxy.admin.client.ui.config;

import ca.uhn.sail.proxy.admin.client.AdminPortal;
import ca.uhn.sail.proxy.admin.client.ui.components.HtmlBr;
import ca.uhn.sail.proxy.admin.client.ui.components.HtmlLabel;
import ca.uhn.sail.proxy.admin.client.ui.components.LoadingSpinner;
import ca.uhn.sail.proxy.admin.shared.model.GServiceMethod;
import ca.uhn.sail.proxy.admin.shared.model.GServiceVersionUrl;
import ca.uhn.sail.proxy.admin.shared.model.GSoap11ServiceVersion;

import com.google.gwt.dom.client.Style.Float;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

public class SoapDetailPanel extends FlowPanel {

	private Grid myMethodGrid;
	private GSoap11ServiceVersion myServiceVersion;
	private Grid myUrlGrid;
	private TextBox myUrlTextBox;
	private Label myNoMethodsLabel;
	private Label myNoUrlsLabel;
	private LoadingSpinner myLoadWsdlSpinner;
	
	public SoapDetailPanel(GSoap11ServiceVersion theServiceVersion) {
		myServiceVersion = theServiceVersion;
		
		FlowPanel wsdlPanel = new FlowPanel();
		add(wsdlPanel);
		initWsdlPanel(wsdlPanel);
		
		FlowPanel proxyPanel = new FlowPanel();
		add(proxyPanel);
		initProxyPanel(proxyPanel);
		
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

	private void initServerSecurityPanel(FlowPanel thePanel) {
		thePanel.setStylePrimaryName("mainPanel");

		Label titleLabel = new Label("Server Security");
		titleLabel.setStyleName("mainPanelTitle");
		thePanel.add(titleLabel);

		FlowPanel contentPanel = new FlowPanel();
		contentPanel.addStyleName("contentInnerPanel");
		thePanel.add(contentPanel);

		Label urlLabel = new Label("Server Security modules verify that the client which is making requests coming " +
				"in to the proxy are authorized to invoke the particular service they are attempting to " +
				"invoke. If no server security modules are defined for this service version, all requests will be " +
				"allowed to proceed.");
		contentPanel.add(urlLabel);
	}

	private void initClientSecurityPanel(FlowPanel thePanel) {
		thePanel.setStylePrimaryName("mainPanel");

		Label titleLabel = new Label("Client Security");
		titleLabel.setStyleName("mainPanelTitle");
		thePanel.add(titleLabel);

		FlowPanel contentPanel = new FlowPanel();
		contentPanel.addStyleName("contentInnerPanel");
		thePanel.add(contentPanel);

		Label urlLabel = new Label("Client Security modules provide credentials to ");
		contentPanel.add(urlLabel);
	}

	private void initWsdlPanel(FlowPanel wsdlPanel) {
		wsdlPanel.setStylePrimaryName("mainPanel");

		Label titleLabel = new Label("WSDL Location");
		titleLabel.setStyleName("mainPanelTitle");
		wsdlPanel.add(titleLabel);

		FlowPanel contentPanel = new FlowPanel();
		contentPanel.addStyleName("contentInnerPanel");
		wsdlPanel.add(contentPanel);

		HtmlLabel urlLabel = new HtmlLabel("URL:", "urlTb");
		contentPanel.add(urlLabel);
		
		myUrlTextBox = new TextBox();
		myUrlTextBox.getElement().setId("urlTb");
		myUrlTextBox.getElement().getStyle().setWidth(500, Unit.PX);
		contentPanel.add(myUrlTextBox);
		
		contentPanel.add(new HtmlBr());
		
		Button loadButton = new Button("Load WSDL");
		loadButton.getElement().getStyle().setFloat(Float.LEFT);
		contentPanel.add(loadButton);
		
		myLoadWsdlSpinner = new LoadingSpinner();
		myLoadWsdlSpinner.hideCompletely();
		contentPanel.add(myLoadWsdlSpinner);
		
		loadButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent theEvent) {
				handleLoadWsdl();
			}
		});
	}

	private void handleLoadWsdl() {
		AsyncCallback<GSoap11ServiceVersion> callback = new AsyncCallback<GSoap11ServiceVersion>() {
			public void onFailure(Throwable theCaught) {
				myLoadWsdlSpinner.showMessage(theCaught.getMessage(), false);
			}

			public void onSuccess(GSoap11ServiceVersion theResult) {
				myServiceVersion = theResult;
				updateMethodPanel();
				updateProxyPanel();
			}
		};
		AdminPortal.MODEL_SVC.loadWsdl(myUrlTextBox.getValue(), callback);
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
		myMethodGrid.setWidget(0, 1, new Label("Name"));
		
		myNoMethodsLabel = new Label("No Methods Defined");
		contentPanel.add(myNoMethodsLabel);
		
		updateMethodPanel();
	}

	private void initProxyPanel(FlowPanel theProxyPanel) {
		theProxyPanel.setStylePrimaryName("mainPanel");

		Label titleLabel = new Label("Implementation URLs");
		titleLabel.setStyleName("mainPanelTitle");
		theProxyPanel.add(titleLabel);

		FlowPanel contentPanel = new FlowPanel();
		contentPanel.addStyleName("contentInnerPanel");
		theProxyPanel.add(contentPanel);

		myUrlGrid = new Grid(1, 3);
		myUrlGrid.addStyleName("dashboardTable");
		contentPanel.add(myUrlGrid);
		
		myUrlGrid.setWidget(0, 0, new Label("Action"));
		myUrlGrid.setWidget(0, 1, new Label("ID"));
		myUrlGrid.setWidget(0, 2, new Label("URL"));
		
		myNoUrlsLabel = new Label("No URLs Defined");
		contentPanel.add(myNoUrlsLabel);

		updateProxyPanel();
	}

	private void updateMethodPanel() {
		myMethodGrid.resize(myServiceVersion.getMethodList().size(), 2);
		
		int row = 0;
		for (GServiceMethod next : myServiceVersion.getMethodList()) {
			row++;
			
			myUrlGrid.setWidget(row, 0, new UrlEditButtonPanel(row));
			myUrlGrid.setWidget(row, 1, new Label(next.getName()));
			
		}
		
		myNoMethodsLabel.setVisible(myServiceVersion.getMethodList().size() == 0);
	}

	private void updateProxyPanel() {
		myUrlGrid.resize(myServiceVersion.getUrlList().size(), 3);
		
		int row = 0;
		for (GServiceVersionUrl next : myServiceVersion.getUrlList()) {
			row++;
			
			myUrlGrid.setWidget(row, 0, new UrlEditButtonPanel(row));
			myUrlGrid.setWidget(row, 1, new Label(next.getId()));
			myUrlGrid.setWidget(row, 2, new Label(next.getUrl()));
			
		}

		myNoUrlsLabel.setVisible(myServiceVersion.getUrlList().size() == 0);
	}
	
	private final class MethodEditButtonPanel extends FlowPanel implements ClickHandler {
		private int myRow;
		private Button myEditButton;
		private Button myDeleteButton;

		private MethodEditButtonPanel(int theRow) {
			myRow = theRow;

			myEditButton = new Button("Edit");
			myEditButton.addClickHandler(this);
			add(myEditButton);

			myDeleteButton = new Button("Delete");
			myDeleteButton.addClickHandler(this);
			add(myDeleteButton);
		}

		public void onClick(ClickEvent theEvent) {
			Button source = (Button) theEvent.getSource();
			source.setEnabled(false);
		}
	}
	
	private final class UrlEditButtonPanel extends FlowPanel implements ClickHandler {
		private int myRow;
		private Button myEditButton;
		private Button myDeleteButton;

		private UrlEditButtonPanel(int theRow) {
			myRow = theRow;

			myEditButton = new Button("Edit");
			myEditButton.addClickHandler(this);
			add(myEditButton);

			myDeleteButton = new Button("Delete");
			myDeleteButton.addClickHandler(this);
			add(myDeleteButton);
		}

		public void onClick(ClickEvent theEvent) {
			Button source = (Button) theEvent.getSource();
			source.setEnabled(false);
		}
	}
	
}
