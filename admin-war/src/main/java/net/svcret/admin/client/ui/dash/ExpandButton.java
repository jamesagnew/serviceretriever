package net.svcret.admin.client.ui.dash;

import net.svcret.admin.client.ui.dash.model.IDashModel;
import net.svcret.admin.shared.IAsyncLoadCallback;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.GDomainList;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Image;

public class ExpandButton extends Image implements ClickHandler {

	private ServiceDashboardPanel myPanel;
	private IDashModel myModel;

	public ExpandButton(ServiceDashboardPanel theServiceDashboardPanel, IDashModel theModel) {
		myPanel = theServiceDashboardPanel;
		myModel = theModel;

		if (theModel.hasChildren()) {
			addStyleName("buttonImage");
			addClickHandler(this);
		}
		
		update();
	}

	private void update() {
		if (!myModel.hasChildren()) {
			setUrl("images/table_plusdisabled_16.png");
		} else if (myModel.getModel().isExpandedOnDashboard()) {
			setUrl("images/table_minus_16.png");
		} else {
			setUrl("images/table_plus_16.png");
		}
	}

	@Override
	public void onClick(ClickEvent theEvent) {
		boolean value = !(myModel.getModel().isExpandedOnDashboard() == Boolean.TRUE);
		myModel.getModel().setExpandedOnDashboard(value);
		GWT.log("Setting expansion to " + value);
		update();
		myPanel.updateView();
		if (value) {
			Model.getInstance().loadDomainListAndStats(new IAsyncLoadCallback<GDomainList>() {
				@Override
				public void onSuccess(GDomainList theResult) {
					myPanel.updateView(theResult);
				}
			});
		}
	}

}
