package net.svcret.admin.client.ui.config.service;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.nav.NavProcessor;
import net.svcret.admin.client.ui.alert.AlertGrid;
import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.client.ui.components.LoadingSpinner;
import net.svcret.admin.client.ui.config.KeepRecentTransactionsPanel;
import net.svcret.admin.client.ui.config.domain.EditDomainServicesPanel;
import net.svcret.admin.shared.IAsyncLoadCallback;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.DtoDomain;
import net.svcret.admin.shared.model.DtoDomainList;
import net.svcret.admin.shared.model.GService;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TabPanel;

public class EditServicePanel extends FlowPanel {
	private EditServiceBasicPropertiesPanel myEditServiceBasicPropertiesPanel;
	private FlowPanel myTopContentPanel;
	private KeepRecentTransactionsPanel myKeepRecentTransactionsPanel;
	private DtoDomain myDomain;

	public EditServicePanel(final long theDomainPid, final long theServicePid) {
		FlowPanel topPanel = new FlowPanel();
		add(topPanel);
		
		topPanel.setStylePrimaryName("mainPanel");

		Label titleLabel = new Label("Edit Service");
		titleLabel.setStyleName("mainPanelTitle");
		topPanel.add(titleLabel);

		myTopContentPanel = new FlowPanel();
		myTopContentPanel.addStyleName("contentInnerPanel");
		topPanel.add(myTopContentPanel);

		myTopContentPanel.add(new Label(EditDomainServicesPanel.SVC_DESC));

		final LoadingSpinner spinner = new LoadingSpinner();
		spinner.show();
		myTopContentPanel.add(spinner);

		IAsyncLoadCallback<DtoDomainList> callback = new IAsyncLoadCallback<DtoDomainList>() {


			@Override
			public void onSuccess(DtoDomainList theResult) {

				myDomain = theResult.getDomainByPid(theDomainPid);
				if (myDomain == null) {
					GWT.log("Unknown domain PID: " + theDomainPid);
					NavProcessor.goHome();
					return;
				}

				myService = myDomain.getServiceList().getServiceByPid(theServicePid);
				if (myService == null) {
					GWT.log("Unknown service PID: " + theDomainPid);
					NavProcessor.goHome();
					return;
				}

				spinner.hideCompletely();

				initUi();
			}

		};
		Model.getInstance().loadDomainList(callback);
	}

	private void initUi() {
		myEditServiceBasicPropertiesPanel = new EditServiceBasicPropertiesPanel(myService, "Save", new MySaveButtonHandler(myService), AdminPortal.IMAGES.iconSave(),false);
		myTopContentPanel.add(myEditServiceBasicPropertiesPanel);

		TabPanel domainTabs = new TabPanel();
		domainTabs.addStyleName(CssConstants.CONTENT_OUTER_TAB_PANEL);
		add(domainTabs);

		EditServiceVersionsPanel childrenPanel = new EditServiceVersionsPanel(myDomain, myService);
		domainTabs.add(childrenPanel, "Versions");

		FlowPanel configPanel = new FlowPanel();
		domainTabs.add(configPanel, "Config");
		myKeepRecentTransactionsPanel = new KeepRecentTransactionsPanel(myService);
		configPanel.add(myKeepRecentTransactionsPanel);

		final FlowPanel alertsPanel = new FlowPanel();
		domainTabs.add(alertsPanel, "Alerts");

		domainTabs.addSelectionHandler(new SelectionHandler<Integer>() {
			@Override
			public void onSelection(SelectionEvent<Integer> theEvent) {
				Integer selectedItem = theEvent.getSelectedItem();
				if (selectedItem == 2 && alertsPanel.getWidgetCount() == 0) {
					initAlertsPanel(alertsPanel);
				}
			}
		});
		domainTabs.selectTab(0);
	}

	private void initAlertsPanel(final FlowPanel theAlertsPanel) {
		final LoadingSpinner spinner = new LoadingSpinner();
		spinner.show();
		theAlertsPanel.add(spinner);

		Model.getInstance().loadDomainList(new IAsyncLoadCallback<DtoDomainList>() {

			@Override
			public void onSuccess(DtoDomainList theResult) {
				spinner.hideCompletely();

				final AlertGrid grid = new AlertGrid(theResult, null, myService.getPid(), null);
				theAlertsPanel.add(grid);
			}
		});
	}

	private GService myService;

	public class MySaveButtonHandler implements ClickHandler, AsyncCallback<DtoDomainList> {

		private GService myService;

		public MySaveButtonHandler(GService theService) {
			myService = theService;
		}

		@Override
		public void onClick(ClickEvent theEvent) {
			if (!myKeepRecentTransactionsPanel.validateAndShowErrorIfNotValid()) {
				return;
			}
			myKeepRecentTransactionsPanel.populateDto(myService);
			if (myEditServiceBasicPropertiesPanel.validateValues()) {
				myEditServiceBasicPropertiesPanel.showMessage("Saving Service...", true);
				AdminPortal.MODEL_SVC.saveService(myService, this);
			}
		}

		@Override
		public void onFailure(Throwable theCaught) {
			myEditServiceBasicPropertiesPanel.hideSpinner();
			myEditServiceBasicPropertiesPanel.showError(theCaught.getMessage());
		}

		@Override
		public void onSuccess(DtoDomainList theResult) {
			Model.getInstance().mergeDomainList(theResult);
			myEditServiceBasicPropertiesPanel.showMessage("Service has been saved", false);
		}

	}

}
