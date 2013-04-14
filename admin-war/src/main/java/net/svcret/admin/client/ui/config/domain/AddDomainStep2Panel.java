package net.svcret.admin.client.ui.config.domain;

import net.svcret.admin.client.nav.NavProcessor;
import net.svcret.admin.client.ui.components.HtmlBr;
import net.svcret.admin.client.ui.components.LoadingSpinner;
import net.svcret.admin.client.ui.components.PButton;
import net.svcret.admin.shared.IAsyncLoadCallback;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.GDomain;
import net.svcret.admin.shared.model.GDomainList;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

public class AddDomainStep2Panel  extends FlowPanel implements ClickHandler {

	private long myDomainPid;

	public AddDomainStep2Panel(long theDomainPid) {
		myDomainPid = theDomainPid;
		
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
		
		IAsyncLoadCallback<GDomainList> callback = new IAsyncLoadCallback<GDomainList>() {
			@Override
			public void onSuccess(GDomainList theResult) {
				spinner.hideCompletely();
				GDomain domain = theResult.getDomainByPid(myDomainPid);
				
				Label text = new Label("Domain \"" + domain.getName() + "\" has been successfully added.");
				contentPanel.add(text);
				
				add(new HtmlBr());
				add(new PButton("Edit It", AddDomainStep2Panel.this));
			}
		};
		Model.getInstance().loadDomainList(callback);
		
		
	}

	@Override
	public void onClick(ClickEvent theEvent) {
		History.newItem(NavProcessor.getTokenEditDomain(false, myDomainPid));
	}
	
}
