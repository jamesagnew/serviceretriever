package net.svcret.admin.client.ui.config.service;

import net.svcret.admin.client.nav.NavProcessor;
import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.client.ui.components.LoadingSpinner;
import net.svcret.admin.client.ui.components.PButton;
import net.svcret.admin.shared.IAsyncLoadCallback;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.DtoDomain;
import net.svcret.admin.shared.model.DtoDomainList;
import net.svcret.admin.shared.model.GService;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import static net.svcret.admin.client.AdminPortal.*;

public class DeleteServicePanel extends FlowPanel {

	private LoadingSpinner mySpinner;
	private PButton myDeleteButton;

	public DeleteServicePanel(final long theDomainPid, final long theServicePid) {
		setStylePrimaryName(CssConstants.MAIN_PANEL);

		Label titleLabel = new Label(MSGS.deleteServicePanel_Title());
		titleLabel.setStyleName(CssConstants.MAIN_PANEL_TITLE);
		add(titleLabel);

		final FlowPanel contentPanel = new FlowPanel();
		contentPanel.addStyleName(CssConstants.CONTENT_INNER_PANEL);
		add(contentPanel);

		mySpinner = new LoadingSpinner();
		mySpinner.show();
		contentPanel.add(mySpinner);

		Model.getInstance().loadDomainList(new IAsyncLoadCallback<DtoDomainList>() {
			@Override
			public void onSuccess(DtoDomainList theResult) {
				mySpinner.hide();
				populate(theDomainPid, theServicePid, contentPanel, theResult);
			}

		});

	}

	private void populate(final long theDomainPid, final long theServicePid, final FlowPanel contentPanel, DtoDomainList theResult) {
		DtoDomain domain = theResult.getDomainByPid(theDomainPid);
		if (domain == null) {
			GWT.log("Unknown domain PID: " + theDomainPid);
			NavProcessor.goHome();
			return;
		}
		
		GService service = domain.getServiceList().getServiceByPid(theServicePid);
		if (service	 == null) {
			GWT.log("Unknown service PID: " + theDomainPid);
			NavProcessor.goHome();
			return;
		}

		Label intro = new Label(MSGS.deleteServicePanel_Confirm(domain.getId(), service.getId()));
		contentPanel.add(intro);

		HorizontalPanel buttonPanel = new HorizontalPanel();
		contentPanel.add(buttonPanel);
		
		myDeleteButton = new PButton(MSGS.actions_Remove(), new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent theEvent) {
				mySpinner.show();
				MODEL_SVC.removeService(theDomainPid, theServicePid, new AsyncCallback<DtoDomainList>() {
					
					@Override
					public void onSuccess(DtoDomainList theDomainResult) {
						Model.getInstance().mergeDomainList(theDomainResult);
						mySpinner.showMessage("Service Deleted Successfully", false);
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
