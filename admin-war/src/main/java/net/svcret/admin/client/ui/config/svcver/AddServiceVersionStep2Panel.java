package net.svcret.admin.client.ui.config.svcver;

import net.svcret.admin.client.nav.NavProcessor;
import net.svcret.admin.client.nav.PagesEnum;
import net.svcret.admin.client.ui.components.HtmlBr;
import net.svcret.admin.client.ui.components.LoadingSpinner;
import net.svcret.admin.client.ui.components.PButton;
import net.svcret.admin.shared.IAsyncLoadCallback;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.BaseGServiceVersion;
import net.svcret.admin.shared.model.GService;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

public class AddServiceVersionStep2Panel extends FlowPanel {

	public AddServiceVersionStep2Panel(final long theDomainPid, final long theServicePid, long theVersionPid) {
		setStylePrimaryName("mainPanel");

		Label titleLabel = new Label("Add Domain");
		titleLabel.setStyleName("mainPanelTitle");
		add(titleLabel);

		final FlowPanel contentPanel = new FlowPanel();
		contentPanel.addStyleName("contentInnerPanel");
		add(contentPanel);

		final LoadingSpinner spinner = new LoadingSpinner();
		spinner.show();
		contentPanel.add(spinner);
		
		IAsyncLoadCallback<BaseGServiceVersion> callback = new IAsyncLoadCallback<BaseGServiceVersion>() {
			@Override
			public void onSuccess(final BaseGServiceVersion theResult) {
				spinner.hideCompletely();
				
				final Label messageLabel = new Label();
				contentPanel.add(messageLabel);
				
				Model.getInstance().loadService(theDomainPid, theServicePid, new IAsyncLoadCallback<GService>() {
					@Override
					public void onSuccess(GService theServiceResult) {
						messageLabel.setText("Successfully added Version \"" + theResult.getName() + "\" to Service \"" + theServiceResult.getId() + "\"");
					}
				});
				
				contentPanel.add(new HtmlBr());
				
				contentPanel.add(new PButton("Close", new ClickHandler() {
					@Override
					public void onClick(ClickEvent theEvent) {
						History.newItem(NavProcessor.getLastTokenBefore(PagesEnum.ASV, PagesEnum.AV2));
					}
				}));
			}
		};
		Model.getInstance().loadServiceVersion(theDomainPid, theServicePid, theVersionPid, false, callback);
	}
	
}
