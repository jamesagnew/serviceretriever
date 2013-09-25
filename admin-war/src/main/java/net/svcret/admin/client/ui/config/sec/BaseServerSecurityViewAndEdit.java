package net.svcret.admin.client.ui.config.sec;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.client.ui.components.HtmlBr;
import net.svcret.admin.client.ui.components.TwoColumnGrid;
import net.svcret.admin.shared.IAsyncLoadCallback;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.BaseDtoAuthHost;
import net.svcret.admin.shared.model.BaseDtoServerSecurity;
import net.svcret.admin.shared.model.GAuthenticationHostList;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

public abstract class BaseServerSecurityViewAndEdit<T extends BaseDtoServerSecurity> implements IProvidesViewAndEdit<T> {

	@Override
	public Widget provideView(final int theRow, final T theObject) {
		final FlowPanel flowPanel = new FlowPanel();
		flowPanel.add(new Label(provideName()));
		flowPanel.add(new HtmlBr());
		
		Model.getInstance().loadAuthenticationHosts(new IAsyncLoadCallback<GAuthenticationHostList>() {
			@Override
			public void onSuccess(GAuthenticationHostList theResult) {
				BaseDtoAuthHost authHost = theResult.getAuthHostByPid(theObject.getAuthHostPid());
				if (authHost!=null) {
					flowPanel.add(new Label(AdminPortal.MSGS.wsSecServerSecurity_UsesAuthenticationHost(authHost.getModuleId())));
				}
				
				TwoColumnGrid grid = new TwoColumnGrid();
				grid.addStyleName(CssConstants.PROPERTY_TABLE_CHILDTABLE);
				flowPanel.add(grid);
				
				initViewPanel(theRow, theObject, grid, theResult);
			}
		});
		
		return flowPanel;
	}

	protected abstract void initViewPanel(int theRow, T theObject, TwoColumnGrid thePanelToPopulate, GAuthenticationHostList theAuthenticationHostList);

	protected abstract void initEditPanel(int theRow, T theObject, TwoColumnGrid thePanelToPopulate, GAuthenticationHostList theAuthenticationHostList);

	protected abstract String provideName();

	@Override
	public Widget provideEdit(final int theRow, final T theObject, IValueChangeHandler theValueChangeHandler) {
		final FlowPanel flowPanel = new FlowPanel();
		flowPanel.add(new Label(provideName()));
		
		final TwoColumnGrid propertyGrid = new TwoColumnGrid();
		propertyGrid.addStyleName(CssConstants.PROPERTY_TABLE_CHILDTABLE);
		flowPanel.add(propertyGrid);
		
		final ListBox authHostList = new ListBox(false);
		propertyGrid.addRow(AdminPortal.MSGS.wsSecServerSecurity_AuthenticationHost(), authHostList);
		
		Model.getInstance().loadAuthenticationHosts(new IAsyncLoadCallback<GAuthenticationHostList>() {
			@Override
			public void onSuccess(final GAuthenticationHostList theResult) {
				int index=0;
				for (BaseDtoAuthHost next : theResult) {
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
				if (theObject.getAuthHostPid() == 0) {
					theObject.setAuthHostPid(theResult.get(authHostList.getSelectedIndex()).getPid());
				}
				authHostList.addChangeHandler(new ChangeHandler() {
					@Override
					public void onChange(ChangeEvent theEvent) {
						theObject.setAuthHostPid(theResult.get(authHostList.getSelectedIndex()).getPid());
					}});
				
				initEditPanel(theRow, theObject, propertyGrid, theResult);

			}
		});
		
		return flowPanel;
	}

}
