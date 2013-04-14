package net.svcret.admin.client.ui.config.sec;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.ui.components.HtmlBr;
import net.svcret.admin.client.ui.components.TwoColumnGrid;
import net.svcret.admin.shared.IAsyncLoadCallback;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.BaseGAuthHost;
import net.svcret.admin.shared.model.GAuthenticationHostList;
import net.svcret.admin.shared.model.GWsSecServerSecurity;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

public class WsSecServerSecurity implements IProvidesViewAndEdit<GWsSecServerSecurity> {

	@Override
	public Widget provideView(int theRow, final GWsSecServerSecurity theObject) {
		final FlowPanel flowPanel = new FlowPanel();
		flowPanel.add(new Label(AdminPortal.MSGS.wsSecServerSecurity_Name()));
		flowPanel.add(new HtmlBr());
		
		Model.getInstance().loadAuthenticationHosts(new IAsyncLoadCallback<GAuthenticationHostList>() {
			@Override
			public void onSuccess(GAuthenticationHostList theResult) {
				BaseGAuthHost authHost = theResult.getAuthHostByPid(theObject.getAuthHostPid());
				if (authHost!=null) {
					flowPanel.add(new Label(AdminPortal.MSGS.wsSecServerSecurity_UsesAuthenticationHost(authHost.getModuleId())));
				}
			}
		});
		
		return flowPanel;
	}

	@Override
	public Widget provideEdit(int theRow, final GWsSecServerSecurity theObject, IValueChangeHandler theValueChangeHandler) {
		final FlowPanel flowPanel = new FlowPanel();
		flowPanel.add(new Label(AdminPortal.MSGS.wsSecServerSecurity_Name()));
		
		TwoColumnGrid propertyGrid = new TwoColumnGrid();
		flowPanel.add(propertyGrid);
		
		final ListBox authHostList = new ListBox(false);
		propertyGrid.addRow(AdminPortal.MSGS.wsSecServerSecurity_AuthenticationHost(), authHostList);
		
		Model.getInstance().loadAuthenticationHosts(new IAsyncLoadCallback<GAuthenticationHostList>() {
			@Override
			public void onSuccess(final GAuthenticationHostList theResult) {
				int index=0;
				for (BaseGAuthHost next : theResult) {
					authHostList.addItem(next.getModuleId() + " (" + next.getModuleName() + ")");
					if (theObject.getAuthHostPid()==next.getPid()) {
						authHostList.setSelectedIndex(index);
					}
					index++;
				}
				if (authHostList.getSelectedIndex()==-1) {
					authHostList.setSelectedIndex(0);
					theObject.setAuthHostPid(theResult.get(0).getPid());
				}
				authHostList.addChangeHandler(new ChangeHandler() {
					@Override
					public void onChange(ChangeEvent theEvent) {
						theObject.setAuthHostPid(theResult.get(authHostList.getSelectedIndex()).getPid());
					}});
			}
		});
		
		return flowPanel;
	}

}
