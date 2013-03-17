package ca.uhn.sail.proxy.admin.client.ui.config.sec;

import ca.uhn.sail.proxy.admin.shared.model.GWsSecServerSecurity;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class WsSecServerSecurity implements IProvidesViewAndEdit<GWsSecServerSecurity> {

	@Override
	public Widget provideView(int theRow, GWsSecServerSecurity theObject) {
		return new Label("WS-Security");
	}

	@Override
	public Widget provideEdit(int theRow, GWsSecServerSecurity theObject, IValueChangeHandler theValueChangeHandler) {
		FlowPanel retVal = new FlowPanel();
		
		
		
		return retVal;
	}

}
