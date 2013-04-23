package net.svcret.admin.client.ui.config.domain;

import net.svcret.admin.client.nav.NavProcessor;
import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.client.ui.components.LoadingSpinner;
import net.svcret.admin.client.ui.components.PButton;
import net.svcret.admin.shared.IAsyncLoadCallback;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.GDomain;
import net.svcret.admin.shared.model.GDomainList;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import static net.svcret.admin.client.AdminPortal.*;

public class DeleteDomainPanel extends FlowPanel {

	private LoadingSpinner mySpinner;
	private PButton myDeleteButton;

	public DeleteDomainPanel(final long thePid) {
		setStylePrimaryName(CssConstants.MAIN_PANEL);

		Label titleLabel = new Label(MSGS.deleteDomainPanel_Title());
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
				populate(thePid, contentPanel, theResult);
			}

		});

	}

	private void populate(final long thePid, final FlowPanel contentPanel, GDomainList theResult) {
		GDomain domain = theResult.getDomainByPid(thePid);
		if (domain == null) {
			GWT.log("Unknown domain PID: " + thePid);
			NavProcessor.goHome();
			return;
		}

		Label intro = new Label(MSGS.deleteDomainPanel_Confirm(domain.getId()));
		contentPanel.add(intro);

		HorizontalPanel buttonPanel = new HorizontalPanel();
		contentPanel.add(buttonPanel);
		
		myDeleteButton = new PButton(MSGS.actions_Remove(), new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent theEvent) {
				mySpinner.show();
				MODEL_SVC.removeDomain(thePid, new AsyncCallback<GDomainList>() {
					
					@Override
					public void onSuccess(GDomainList theDomainResult) {
						Model.getInstance().mergeDomainList(theDomainResult);
						mySpinner.showMessage("Domain Deleted Successfully", false);
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
