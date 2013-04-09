package net.svcret.admin.client.ui.config;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.nav.NavProcessor;
import net.svcret.admin.client.ui.components.HtmlLabel;
import net.svcret.admin.client.ui.components.LoadingSpinner;
import net.svcret.admin.client.ui.components.PButton;
import net.svcret.admin.shared.IAsyncLoadCallback;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.AddServiceVersionResponse;
import net.svcret.admin.shared.model.GDomain;
import net.svcret.admin.shared.model.GDomainList;
import net.svcret.admin.shared.model.GService;
import net.svcret.admin.shared.model.GServiceList;
import net.svcret.admin.shared.model.GSoap11ServiceVersion;
import net.svcret.admin.shared.model.ProtocolEnum;
import net.svcret.admin.shared.util.StringUtil;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class AddServiceVersionPanel extends FlowPanel {

	private Widget myBottomContents;
	private FlowPanel myBottomPanel;
	private ListBox myDomainListBox;
	private Long myDomainPid;
	private LoadingSpinner myLoadingSpinner;
	private HtmlLabel myNewDomainLabel;
	private TextBox myNewDomainNameTextBox;
	private HtmlLabel myNewServiceLabel;
	private TextBox myNewServiceNameTextBox;
	private Grid myParentsGrid;
	private ListBox myServiceListBox;
	private Long myServicePid;
	private FlowPanel myTopPanel;
	private ListBox myTypeComboBox;
	private boolean myUpdating;
	private GSoap11ServiceVersion myVersion;
	private TextBox myVersionTextBox;

	public AddServiceVersionPanel(Long theDomainPid, Long theServicePid) {
		myDomainPid = theDomainPid;
		myServicePid = theServicePid;

		myTopPanel = new FlowPanel();
		add(myTopPanel);

		myTopPanel.setStylePrimaryName("mainPanel");

		Label titleLabel = new Label("Add Service Version");
		titleLabel.setStyleName("mainPanelTitle");
		myTopPanel.add(titleLabel);

		FlowPanel contentPanel = new FlowPanel();
		contentPanel.addStyleName("contentInnerPanel");
		myTopPanel.add(contentPanel);

		Label intro = new Label(AdminPortal.MSGS.addServiceVersion_Description());
		contentPanel.add(intro);

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
			@Override
			public void onChange(ChangeEvent theEvent) {
				handleDomainListChange();
			}
		});
		myParentsGrid.setWidget(0, 1, myDomainListBox);

		myNewDomainLabel = new HtmlLabel(" Name:", "cbNewDomainName");
		myParentsGrid.setWidget(0, 2, myNewDomainLabel);
		myNewDomainNameTextBox = new TextBox();
		myNewDomainNameTextBox.getElement().setId("cbNewDomainName");
		myNewDomainNameTextBox.setValue("Untitled Domain");
		myParentsGrid.setWidget(0, 3, myNewDomainNameTextBox);

		HtmlLabel svcLbl = new HtmlLabel("Service", "cbSvc");
		myParentsGrid.setWidget(1, 0, svcLbl);
		myServiceListBox = new ListBox(false);
		myServiceListBox.getElement().setId("cbSvc");
		myServiceListBox.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent theEvent) {
				handleServiceListChange();
			}
		});
		myParentsGrid.setWidget(1, 1, myServiceListBox);

		myNewServiceLabel = new HtmlLabel(" Name:", "cbNewServiceName");
		myParentsGrid.setWidget(1, 2, myNewServiceLabel);
		myNewServiceNameTextBox = new TextBox();
		myNewServiceNameTextBox.getElement().setId("cbNewServiceName");
		myNewServiceNameTextBox.setValue("Untitled Service");
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

		HtmlLabel typeLabel = new HtmlLabel("Protocol", "cbType");
		myParentsGrid.setWidget(3, 0, typeLabel);
		myTypeComboBox = new ListBox(false);
		myTypeComboBox.getElement().setId("cbType");
		myParentsGrid.setWidget(3, 1, myTypeComboBox);

		for (ProtocolEnum next : ProtocolEnum.values()) {
			myTypeComboBox.addItem(next.getNiceName(), next.name());
			myTypeComboBox.addChangeHandler(new ChangeHandler() {
				@Override
				public void onChange(ChangeEvent theEvent) {
					handleTypeChange();
				}
			});
		}

		PButton saveButton = new PButton("Save");
		contentPanel.add(saveButton);
		saveButton.addClickHandler(new SaveClickHandler());

		myLoadingSpinner = new LoadingSpinner();
		contentPanel.add(myLoadingSpinner);

		/*
		 * The following panel contains the rest of the screen (i.e. no
		 * background, so that it can have lots of contents
		 */

		myBottomPanel = new FlowPanel();
		add(myBottomPanel);

		Model.getInstance().loadDomainList(new IAsyncLoadCallback<GDomainList>() {
			@Override
			public void onSuccess(GDomainList theResult) {
				initParents(theResult);
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

		String domainPidString = myDomainListBox.getValue(myDomainListBox.getSelectedIndex());
		if (StringUtil.isBlank(domainPidString)) {
			myDomainPid = null;
		} else {
			myDomainPid = Long.parseLong(domainPidString);
		}

		IAsyncLoadCallback<GServiceList> callback = new IAsyncLoadCallback<GServiceList>() {
			@Override
			public void onSuccess(GServiceList theResult) {
				processServiceList(theResult);
			}

		};

		if (myDomainPid != null) {
			Model.getInstance().loadServiceList(myDomainPid, callback);
		} else {
			processServiceList(new GServiceList());
		}
	}

	private void processServiceList(GServiceList theResult) {
		myServiceListBox.clear();
		myServiceListBox.addItem("New...", "");
		for (GService nextService : theResult) {
			Long value = nextService.getPid();
			myServiceListBox.addItem(nextService.getName(), value.toString());
			if (value.equals(myServicePid)) {
				myServiceListBox.setSelectedIndex(myServiceListBox.getSelectedIndex() - 1);
			}
		}

		if (myServicePid == null && myServiceListBox.getItemCount() > 1) {
			myServiceListBox.setSelectedIndex(1);
		}

		handleServiceListChange();
	}

	private void handleServiceListChange() {
		boolean showEdit = myServiceListBox.getSelectedIndex() == 0;

		myNewServiceLabel.setVisible(showEdit);
		myNewServiceNameTextBox.setVisible(showEdit);

		String pidString = myServiceListBox.getValue(myServiceListBox.getSelectedIndex());
		if (StringUtil.isBlank(pidString)) {
			myServicePid = null;
		} else {
			myServicePid = Long.parseLong(pidString);
		}

	}

	private void handleTypeChange() {
		switch (ProtocolEnum.valueOf(myTypeComboBox.getValue(myTypeComboBox.getSelectedIndex()))) {
		case SOAP11:
			if (!(myBottomContents instanceof SoapDetailPanel)) {

				myLoadingSpinner.show();
				myBottomPanel.clear();

				AsyncCallback<GSoap11ServiceVersion> callback = new AsyncCallback<GSoap11ServiceVersion>() {
					@Override
					public void onFailure(Throwable theCaught) {
						Model.handleFailure(theCaught);
					}

					@Override
					public void onSuccess(GSoap11ServiceVersion theResult) {
						myLoadingSpinner.hide();

						myVersion = theResult;

						myBottomContents = new SoapDetailPanel(myVersion);
						myBottomPanel.add(myBottomContents);

						String navToken = NavProcessor.getTokenAddServiceVersion(true, theResult.getUncommittedSessionId());
						History.newItem(navToken, false);
					}
				};

				Long uncommittedId = NavProcessor.getParamAddServiceVersionUncommittedId();
				AdminPortal.MODEL_SVC.createNewSoap11ServiceVersion(uncommittedId, callback);
			}
			break;
		}
	}

	private void initParents(GDomainList theDomainList) {
		myUpdating = true;

		myDomainListBox.clear();
		myDomainListBox.addItem("New...", "");
		for (GDomain nextDomain : theDomainList) {
			Long value = nextDomain.getPid();
			myDomainListBox.addItem(nextDomain.getName(), value.toString());
			if (value.equals(myDomainPid)) {
				myDomainListBox.setSelectedIndex(myDomainListBox.getItemCount() - 1);
			}
		}

		if (myDomainPid == null && myDomainListBox.getItemCount() > 1) {
			myDomainListBox.setSelectedIndex(1);
		}

		myUpdating = false;

		handleDomainListChange();

	}

	public class SaveClickHandler implements ClickHandler {

		@Override
		public void onClick(ClickEvent theEvent) {
			final String domainId;
			if (myDomainListBox.getSelectedIndex() == 0) {
				domainId = myNewDomainNameTextBox.getValue();
				if (StringUtil.isBlank(domainId)) {
					myLoadingSpinner.showMessage("Please select an existing Domain, or enter a name for a new one to be created.", false);
					myNewDomainNameTextBox.setFocus(true);
					return;
				}
			} else {
				domainId = null;
			}

			String serviceId;
			if (myServiceListBox.getSelectedIndex() == 0) {
				serviceId = myNewServiceNameTextBox.getValue();
				if (StringUtil.isBlank(serviceId)) {
					myLoadingSpinner.showMessage("Please select an existing Service, or enter a name for a new one to be created.", false);
					myNewDomainNameTextBox.setFocus(true);
					return;
				}
			} else {
				serviceId = null;
			}

			AsyncCallback<AddServiceVersionResponse> callback = new AsyncCallback<AddServiceVersionResponse>() {
				@Override
				public void onFailure(Throwable theCaught) {
					Model.handleFailure(theCaught);
				}

				@Override
				public void onSuccess(AddServiceVersionResponse theResult) {
					if (theResult.getNewDomain() != null) {
						Model.getInstance().addDomain(theResult.getNewDomain());
						myDomainPid = theResult.getNewDomain().getPid();
					}
					if (theResult.getNewService() != null) {
						Model.getInstance().addService(myDomainPid, theResult.getNewService());
						myServicePid = theResult.getNewService().getPid();
					}
					Model.getInstance().addServiceVersion(myDomainPid, myServicePid, theResult.getNewServiceVersion());
					String token = NavProcessor.getTokenAddServiceVersionStep2(myDomainPid, myServicePid, theResult.getNewServiceVersion().getPid());
					History.newItem(token);
				}
			};
			AdminPortal.MODEL_SVC.addServiceVersion(myDomainPid, domainId, myServicePid, serviceId, myVersion, callback);
		}

	}
}
