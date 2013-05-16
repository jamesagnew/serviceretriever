package net.svcret.admin.client.ui.config.sec;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.ui.components.TwoColumnGrid;
import net.svcret.admin.shared.model.GAuthenticationHostList;
import net.svcret.admin.shared.model.GNamedParameterJsonRpcServerAuth;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

public class JsonRpcNamedParameterServerSecurity extends BaseServerSecurityViewAndEdit<GNamedParameterJsonRpcServerAuth> {

	@Override
	protected String provideName() {
		return AdminPortal.MSGS.jsonRpcNamedParameterServerSecurity_Name();
	}

	@Override
	protected void initViewPanel(int theRow, GNamedParameterJsonRpcServerAuth theObject, TwoColumnGrid thePanelToPopulate, GAuthenticationHostList theAuthenticationHostList) {
		thePanelToPopulate.addRow("Username Parameter Name", new Label(theObject.getUsernameParameterName()));
		thePanelToPopulate.addRow("Password Parameter Name", new Label(theObject.getPasswordParameterName()));
	}

	@Override
	protected void initEditPanel(int theRow, final GNamedParameterJsonRpcServerAuth theObject, TwoColumnGrid thePanelToPopulate, GAuthenticationHostList theAuthenticationHostList) {
		TextBox usernameTextBox = new TextBox();
		usernameTextBox.setValue(theObject.getUsernameParameterName());
		usernameTextBox.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> theEvent) {
				theObject.setUsernameParameterName(theEvent.getValue());
			}
		});
		thePanelToPopulate.addRow("Username Parameter Name", usernameTextBox);
		
		TextBox passwordTextBox = new TextBox();
		passwordTextBox.setValue(theObject.getPasswordParameterName());
		passwordTextBox.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> theEvent) {
				theObject.setPasswordParameterName(theEvent.getValue());
			}
		});
		thePanelToPopulate.addRow("Password Parameter Name", passwordTextBox);
	}


	
}
