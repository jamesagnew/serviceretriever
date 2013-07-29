package net.svcret.admin.client.ui.config.domain;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.nav.NavProcessor;
import net.svcret.admin.client.ui.alert.AlertGrid;
import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.client.ui.components.LoadingSpinner;
import net.svcret.admin.client.ui.config.KeepRecentTransactionsPanel;
import net.svcret.admin.shared.IAsyncLoadCallback;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.GDomain;
import net.svcret.admin.shared.model.GDomainList;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TabPanel;

public class EditDomainPanel extends FlowPanel {

	private GDomain myDomain;
	private long myDomainPid;
	private LoadingSpinner myInitialSpinner;
	private FlowPanel myTopContentPanel;
	private EditDomainBasicPropertiesPanel myEditDomainBasicPropertiesPanel;
	private KeepRecentTransactionsPanel myKeepRecentTransactionsPanel;

	public EditDomainPanel(long theDomainPid) {
		myDomainPid = theDomainPid;

		FlowPanel topPanel = new FlowPanel();
		add(topPanel);
		topPanel.setStylePrimaryName("mainPanel");

		Label titleLabel = new Label("Domain");
		titleLabel.setStyleName("mainPanelTitle");
		topPanel.add(titleLabel);

		myTopContentPanel = new FlowPanel();
		myTopContentPanel.addStyleName("contentInnerPanel");
		topPanel.add(myTopContentPanel);

		myInitialSpinner = new LoadingSpinner();
		myInitialSpinner.show();
		myTopContentPanel.add(myInitialSpinner);
		
		IAsyncLoadCallback<GDomainList> callback=new IAsyncLoadCallback<GDomainList>() {
			@Override
			public void onSuccess(GDomainList theResult) {
				myInitialSpinner.hideCompletely();
				myDomain = theResult.getDomainByPid(myDomainPid);
				if (myDomain == null) {
					NavProcessor.goHome();
				}

				initUi();
			}
		};
		Model.getInstance().loadDomainList(callback);
	}

	private void initUi() {
		myTopContentPanel.add(new Label(AddDomainPanel.DOMAIN_DESC));

		myEditDomainBasicPropertiesPanel = new EditDomainBasicPropertiesPanel(myDomain, "Save", new MySaveButtonHandler(), AdminPortal.IMAGES.iconSave(),false);
		myTopContentPanel.add(myEditDomainBasicPropertiesPanel);

		TabPanel domainTabs = new TabPanel();
		domainTabs.addStyleName(CssConstants.CONTENT_OUTER_TAB_PANEL);
		add(domainTabs);
				
		EditDomainServicesPanel childrenPanel = new EditDomainServicesPanel(myDomain);
		domainTabs.add(childrenPanel, "Services");
		
		FlowPanel configPanel = new FlowPanel();
		domainTabs.add(configPanel, "Config");
		myKeepRecentTransactionsPanel = new KeepRecentTransactionsPanel(myDomain);
		configPanel.add(myKeepRecentTransactionsPanel);

		final FlowPanel alertsPanel = new FlowPanel();
		domainTabs.add(alertsPanel, "Alerts");
		
		domainTabs.addSelectionHandler(new SelectionHandler<Integer>() {
			@Override
			public void onSelection(SelectionEvent<Integer> theEvent) {
				Integer selectedItem = theEvent.getSelectedItem();
				if (selectedItem==2 && alertsPanel.getWidgetCount()==0) {
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
		
		Model.getInstance().loadDomainList(new IAsyncLoadCallback<GDomainList>() {

			@Override
			public void onSuccess(GDomainList theResult) {
				spinner.hideCompletely();
				
				final AlertGrid grid = new AlertGrid(theResult, myDomainPid, null, null);
				theAlertsPanel.add(grid);
			}
		});
	}

	public class MySaveButtonHandler implements ClickHandler, AsyncCallback<GDomain> {

		@Override
		public void onClick(ClickEvent theEvent) {
			if (!myKeepRecentTransactionsPanel.validateAndShowErrorIfNotValid()) {
				return;
			}
			myKeepRecentTransactionsPanel.populateDto(myDomain);
			if (myEditDomainBasicPropertiesPanel.validateValuesAndApplyValues()) {
				myEditDomainBasicPropertiesPanel.showMessage("Saving Domain...", true);
				AdminPortal.MODEL_SVC.saveDomain(myDomain, this);
			}
		}

		@Override
		public void onFailure(Throwable theCaught) {
			myEditDomainBasicPropertiesPanel.hideSpinner();
			myEditDomainBasicPropertiesPanel.showError(theCaught.getMessage());
		}

		@Override
		public void onSuccess(GDomain theResult) {
			Model.getInstance().addDomain(theResult);
			myEditDomainBasicPropertiesPanel.showMessage("Domain has been saved", false);
		}

	}

}
