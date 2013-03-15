package ca.uhn.sail.proxy.admin.client.ui.config;

import ca.uhn.sail.proxy.admin.client.ui.components.HtmlLabel;
import ca.uhn.sail.proxy.admin.client.ui.components.LoadingSpinner;
import ca.uhn.sail.proxy.admin.shared.model.GDomain;
import ca.uhn.sail.proxy.admin.shared.model.GDomainList;
import ca.uhn.sail.proxy.admin.shared.model.GService;
import ca.uhn.sail.proxy.admin.shared.model.GSoap11ServiceVersion;
import ca.uhn.sail.proxy.admin.shared.model.Model;
import ca.uhn.sail.proxy.admin.shared.util.IAsyncLoadCallback;
import ca.uhn.sail.proxy.admin.shared.util.StringUtil;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class AddServiceVersionPanel extends FlowPanel {

	private static final String TYPE_VAL_SOAP11 = "soap11";
	private static final String VERSION_DESC = "Each service can have one or more versions, which are " + "exposed using a separate URL.";
	private Widget myBottomContents;
	private FlowPanel myBottomPanel;
	private ListBox myDomainListBox;
	private String myDomainPid;
	private LoadingSpinner myLoadingSpinner;
	private HtmlLabel myNewDomainLabel;
	private TextBox myNewDomainNameTextBox;
	private HtmlLabel myNewServiceLabel;
	private TextBox myNewServiceNameTextBox;
	private Grid myParentsGrid;
	private ListBox myServiceListBox;
	private String myServicePid;
	private FlowPanel myTopPanel;
	private ListBox myTypeComboBox;
	private boolean myUpdating;
	private GSoap11ServiceVersion myVersion;
	private TextBox myVersionTextBox;

	public AddServiceVersionPanel(String theDomainPid, String theServicePid) {
		myDomainPid = theDomainPid;
		myServicePid = theServicePid;

		myTopPanel = new FlowPanel();
		add(myTopPanel);

		myTopPanel.setStylePrimaryName("mainPanel");

		Label titleLabel = new Label("Add Domain");
		titleLabel.setStyleName("mainPanelTitle");
		myTopPanel.add(titleLabel);

		FlowPanel contentPanel = new FlowPanel();
		contentPanel.addStyleName("contentInnerPanel");
		myTopPanel.add(contentPanel);

		Label intro = new Label(VERSION_DESC);
		contentPanel.add(intro);

		myLoadingSpinner = new LoadingSpinner();
		contentPanel.add(myLoadingSpinner);

		myParentsGrid = new Grid(4, 4);
		contentPanel.add(myParentsGrid);

		/*
		 * Parents
		 */
		HtmlLabel domainLbl = new HtmlLabel("Domain", "cbDomain");
		myParentsGrid.setWidget(0, 0, domainLbl);
		myDomainListBox = new ListBox(false);
		myDomainListBox.getElement().setId("cbDomain");
		myDomainListBox.addChangeHandler(new ChangeHandler() {
			public void onChange(ChangeEvent theEvent) {
				handleDomainListChange();
			}
		});
		myParentsGrid.setWidget(0, 1, myDomainListBox);

		myNewDomainLabel = new HtmlLabel(" Name:", "cbNewDomainName");
		myParentsGrid.setWidget(0, 2, myNewDomainLabel);
		myNewDomainNameTextBox = new TextBox();
		myNewDomainNameTextBox.getElement().setId("cbNewDomainName");
		myParentsGrid.setWidget(0, 3, myNewDomainNameTextBox);

		HtmlLabel svcLbl = new HtmlLabel("Service", "cbSvc");
		myParentsGrid.setWidget(1, 0, svcLbl);
		myServiceListBox = new ListBox(false);
		myServiceListBox.getElement().setId("cbSvc");
		myServiceListBox.addChangeHandler(new ChangeHandler() {
			public void onChange(ChangeEvent theEvent) {
				handleServiceListChange();
			}
		});
		myParentsGrid.setWidget(1, 1, myServiceListBox);

		myNewServiceLabel = new HtmlLabel(" Name:", "cbNewServiceName");
		myParentsGrid.setWidget(1, 2, myNewServiceLabel);
		myNewServiceNameTextBox = new TextBox();
		myNewServiceNameTextBox.getElement().setId("cbNewServiceName");
		myParentsGrid.setWidget(1, 3, myNewServiceNameTextBox);

		/*
		 * Version
		 */

		HtmlLabel versionLabel = new HtmlLabel("Version", "tbVer");
		myParentsGrid.setWidget(2, 0, versionLabel);
		myVersionTextBox = new TextBox();
		myVersionTextBox.setValue("1.0");
		myVersionTextBox.getElement().setId("tbVer");
		myParentsGrid.setWidget(2, 1, myVersionTextBox);

		/*
		 * Type
		 */

		HtmlLabel typeLabel = new HtmlLabel("Version", "cbType");
		myParentsGrid.setWidget(3, 0, typeLabel);
		myTypeComboBox = new ListBox(false);
		myTypeComboBox.getElement().setId("cbType");
		myParentsGrid.setWidget(3, 1, myTypeComboBox);

		myTypeComboBox.addItem("SOAP 1.1", TYPE_VAL_SOAP11);
		myTypeComboBox.addChangeHandler(new ChangeHandler() {
			public void onChange(ChangeEvent theEvent) {
				handleTypeChange();
			}
		});

		myBottomPanel = new FlowPanel();
		add(myBottomPanel);

		Model.getInstance().loadAllDomainsAndServices(new IAsyncLoadCallback<GDomainList>() {
			public void onSuccess(GDomainList theResult) {
				initParents();
				handleTypeChange();
			}
		});

	}

	private void handleDomainListChange() {
		if (myUpdating) {
			return;
		}

		if (myDomainListBox.getSelectedIndex() == 0) {
			myNewDomainLabel.setVisible(true);
			myNewDomainNameTextBox.setVisible(true);
		} else {
			myNewDomainLabel.setVisible(false);
			myNewDomainNameTextBox.setVisible(false);
		}

		myDomainPid = myDomainListBox.getValue(myDomainListBox.getSelectedIndex());

		myServiceListBox.clear();
		myServiceListBox.addItem("New...", "");
		GDomain domain = Model.getInstance().getDomainList().getDomainByPid(myDomainPid);
		if (domain != null) {
			for (GService nextService : domain.getServiceList()) {
				String value = "" + nextService.getPid();
				myServiceListBox.addItem(nextService.getName(), value);
				if (value.equals(myServicePid)) {
					myServiceListBox.setSelectedIndex(myServiceListBox.getSelectedIndex() - 1);
				}
			}
		}

		if (StringUtil.isBlank(myServicePid) && myServiceListBox.getItemCount() > 1) {
			myServiceListBox.setSelectedIndex(1);
		}
		myServicePid = myServiceListBox.getValue(myServiceListBox.getSelectedIndex());

		handleServiceListChange();
	}

	private void handleServiceListChange() {
		boolean showEdit = myServiceListBox.getSelectedIndex() == 0;

		myNewDomainLabel.setVisible(showEdit);
		myNewDomainNameTextBox.setVisible(showEdit);
	}

	private void handleTypeChange() {
		if (!(myBottomContents instanceof SoapDetailPanel)) {

			myVersion = new GSoap11ServiceVersion();
			myVersion.initChildList();

			myBottomContents = new SoapDetailPanel(myVersion);
			myBottomPanel.clear();
			myBottomPanel.add(myBottomContents);
		}
	}

	private void initParents() {
		myUpdating = true;

		myDomainListBox.clear();
		myDomainListBox.addItem("New...", "");
		for (GDomain nextDomain : Model.getInstance().getDomainList()) {
			String value = Long.toString(nextDomain.getPid());
			myDomainListBox.addItem(nextDomain.getName(), value);
			if (value.equals(myDomainPid)) {
				myDomainListBox.setSelectedIndex(myDomainListBox.getItemCount() - 1);
			}
		}

		if (StringUtil.isBlank(myDomainPid) && myDomainListBox.getItemCount() > 1) {
			myDomainListBox.setSelectedIndex(1);
		}

		myUpdating = false;

		handleDomainListChange();

	}
}
