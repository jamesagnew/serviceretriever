package net.svcret.admin.client.ui.config.svcver;

import net.svcret.admin.client.nav.NavProcessor;
import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.client.ui.components.LoadingSpinner;
import net.svcret.admin.client.ui.components.PButton;
import net.svcret.admin.client.ui.components.TwoColumnGrid;
import net.svcret.admin.shared.IAsyncLoadCallback;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.DtoDomain;
import net.svcret.admin.shared.model.GDomainList;
import net.svcret.admin.shared.model.GService;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;

import static net.svcret.admin.client.AdminPortal.*;

public class DeleteServiceVersionPanel extends FlowPanel {

	private LoadingSpinner mySpinner;
	private PButton myDeleteButton;

	public DeleteServiceVersionPanel(final long theServiceVersionPid) {
		setStylePrimaryName(CssConstants.MAIN_PANEL);

		Label titleLabel = new Label(MSGS.deleteServiceVersion_Title());
		titleLabel.setStyleName(CssConstants.MAIN_PANEL_TITLE);
		add(titleLabel);

		final FlowPanel contentPanel = new FlowPanel();
		contentPanel.addStyleName(CssConstants.CONTENT_INNER_PANEL);
		add(contentPanel);

		mySpinner = new LoadingSpinner();
		mySpinner.show();
		contentPanel.add(mySpinner);

		Model.getInstance().loadDomainList(new IAsyncLoadCallback<GDomainList>() {
			@Override
			public void onSuccess(GDomainList theResult) {
				mySpinner.hide();
				populate(theServiceVersionPid, contentPanel, theResult);
			}

		});

	}

	private void populate(final long theServiceVersionPid, final FlowPanel contentPanel, GDomainList theDomainList) {
		DtoDomain domain = theDomainList.getDomainWithServiceVersion(theServiceVersionPid);
		if (domain == null) {
			GWT.log("Unknown svcVer PID: " + theServiceVersionPid);
			NavProcessor.goHome();
			return;
		}
		
		GService service = theDomainList.getServiceWithServiceVersion(theServiceVersionPid);
		if (service	 == null) {
			GWT.log("Unknown svcVer PID: " + theServiceVersionPid);
			NavProcessor.goHome();
			return;
		}

		TwoColumnGrid grid = new TwoColumnGrid();
		grid.addRow(MSGS.name_Domain(), domain.getName());
		grid.addRow(MSGS.name_Service(), service.getName());
		grid.addRow(MSGS.name_ServiceVersion(), domain.getId());
		contentPanel.add(grid);
		
		Label intro = new Label("Delete this version?");
		contentPanel.add(intro);

		HorizontalPanel buttonPanel = new HorizontalPanel();
		contentPanel.add(buttonPanel);
		
		myDeleteButton = new PButton(MSGS.actions_Remove(), new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent theEvent) {
				mySpinner.show();
				MODEL_SVC.removeServiceVersion(theServiceVersionPid, new AsyncCallback<GDomainList>() {
					
					@Override
					public void onSuccess(GDomainList theDomainResult) {
						Model.getInstance().mergeDomainList(theDomainResult);
						mySpinner.showMessage("Version Deleted Successfully", false);
						myDeleteButton.setEnabled(false);
					}
					
					@Override
					public void onFailure(Throwable theCaught) {
						Model.handleFailure(theCaught);
					}
				});
				
			}
		});
		buttonPanel.add(myDeleteButton);
		
		PButton backButton = new PButton(MSGS.actions_Back(), NavProcessor.getBackHandler());
		buttonPanel.add(backButton);
		
	}

}
