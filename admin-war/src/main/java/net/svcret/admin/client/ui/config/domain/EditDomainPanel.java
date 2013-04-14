package net.svcret.admin.client.ui.config.domain;

import net.svcret.admin.client.nav.NavProcessor;
import net.svcret.admin.client.ui.components.LoadingSpinner;
import net.svcret.admin.shared.IAsyncLoadCallback;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.GDomain;
import net.svcret.admin.shared.model.GDomainList;

import com.google.gwt.user.client.ui.FlowPanel;

public class EditDomainPanel extends FlowPanel {

	private GDomain myDomain;
	private long myDomainPid;

	public EditDomainPanel(long theDomainPid) {
		myDomainPid = theDomainPid;
		
		final LoadingSpinner spinner = new LoadingSpinner();
		spinner.show();
		add(spinner);
		
		IAsyncLoadCallback<GDomainList> callback=new IAsyncLoadCallback<GDomainList>() {
			@Override
			public void onSuccess(GDomainList theResult) {
				spinner.hideCompletely();
				myDomain = theResult.getDomainByPid(myDomainPid);
				if (myDomain == null) {
					NavProcessor.goHome();
				}

				add(new EditDomainPropertiesPanel(myDomain));
				add(new EditDomainServicesPanel(myDomain));
			}
		};
		Model.getInstance().loadDomainList(callback);
	}

}
