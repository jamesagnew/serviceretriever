package net.svcret.admin.client.ui.config.svcver;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.nav.NavProcessor;
import net.svcret.admin.client.nav.PagesEnum;
import net.svcret.admin.client.ui.components.HtmlBr;
import net.svcret.admin.client.ui.components.LoadingSpinner;
import net.svcret.admin.client.ui.components.PButton;
import net.svcret.admin.shared.IAsyncLoadCallback;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.BaseDtoServiceVersion;
import net.svcret.admin.shared.model.GDomainList;
import net.svcret.admin.shared.model.GService;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

public class AddServiceVersionStep2Panel extends FlowPanel {

	public AddServiceVersionStep2Panel(final long theVersionPid) {
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

		Model.getInstance().loadDomainList(new IAsyncLoadCallback<GDomainList>() {
			@Override
			public void onSuccess(final GDomainList theDomainList) {
				spinner.hideCompletely();
				
				BaseDtoServiceVersion version = theDomainList.getServiceVersionByPid(theVersionPid);
				GService service = theDomainList.getServiceWithServiceVersion(theVersionPid);
				
				final Label messageLabel = new Label();
				contentPanel.add(messageLabel);
				messageLabel.setText("Successfully added Version \"" + version.getId() + "\" to Service \"" + service.getName() + "\"");
				
				contentPanel.add(new HtmlBr());
				
				contentPanel.add(new PButton(AdminPortal.MSGS.actions_Back(), new ClickHandler() {
					@Override
					public void onClick(ClickEvent theEvent) {
						History.newItem(NavProcessor.getBackToken());
					}
				}));
				contentPanel.add(new PButton(AdminPortal.MSGS.actions_Close(), new ClickHandler() {
					@Override
					public void onClick(ClickEvent theEvent) {
						History.newItem(NavProcessor.getLastTokenBefore(PagesEnum.ASV, PagesEnum.AV2));
					}
				}));
			}
		});
	}
	
}
