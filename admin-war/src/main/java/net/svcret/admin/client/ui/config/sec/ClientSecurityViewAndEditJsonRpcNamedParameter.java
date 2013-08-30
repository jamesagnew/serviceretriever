package net.svcret.admin.client.ui.config.sec;

import net.svcret.admin.client.ui.components.TwoColumnGrid;
import net.svcret.admin.shared.model.DtoClientSecurityJsonRpcNamedParameter;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.TextBox;

public class ClientSecurityViewAndEditJsonRpcNamedParameter extends BaseClientSecurityUsernameAndPasswordProvidesViewAndEdit<DtoClientSecurityJsonRpcNamedParameter> {

	@Override
	protected String getModuleName() {
		return "JSON-RPC Named Parameter";
	}

	@Override
	protected String getPassword(DtoClientSecurityJsonRpcNamedParameter theObject) {
		return theObject.getPassword();
	}

	@Override
	protected String getUsername(DtoClientSecurityJsonRpcNamedParameter theObject) {
		return theObject.getUsername();
	}

	@Override
	protected void addToViewGridAfterUsername(TwoColumnGrid theGrid, DtoClientSecurityJsonRpcNamedParameter theObject) {
		theGrid.addRow("Username Parameter", theObject.getUsernameParameterName());
	}

	@Override
	protected void addToViewGridAfterPassword(TwoColumnGrid theGrid, DtoClientSecurityJsonRpcNamedParameter theObject) {
		theGrid.addRow("Password Parameter", theObject.getPasswordParameterName());
	}

	@Override
	protected void addToEditGridAfterUsername(TwoColumnGrid theGrid, final DtoClientSecurityJsonRpcNamedParameter theObject,
			final net.svcret.admin.client.ui.config.sec.IProvidesViewAndEdit.IValueChangeHandler theValueChangeHandler) {
		super.addToEditGridAfterUsername(theGrid, theObject, theValueChangeHandler);

		final TextBox usernametextBox = new TextBox();
		usernametextBox.setValue(theObject.getUsernameParameterName());
		usernametextBox.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> theEvent) {
				theObject.setUsernameParameterName(usernametextBox.getValue());
				theValueChangeHandler.onValueChange();
			}
		});
		theGrid.addRow("Username Parameter", usernametextBox);
	}

	@Override
	protected void addToEditGridAfterPassword(TwoColumnGrid theGrid, final DtoClientSecurityJsonRpcNamedParameter theObject,
			final net.svcret.admin.client.ui.config.sec.IProvidesViewAndEdit.IValueChangeHandler theValueChangeHandler) {
		super.addToEditGridAfterPassword(theGrid, theObject, theValueChangeHandler);

		final TextBox PasswordtextBox = new TextBox();
		PasswordtextBox.setValue(theObject.getPasswordParameterName());
		PasswordtextBox.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> theEvent) {
				theObject.setPasswordParameterName(PasswordtextBox.getValue());
				theValueChangeHandler.onValueChange();
			}
		});
		theGrid.addRow("Password Parameter", PasswordtextBox);
	}

	@Override
	protected void setPassword(DtoClientSecurityJsonRpcNamedParameter theObject, String theValue) {
		theObject.setPassword(theValue);
	}

	@Override
	protected void setUsername(DtoClientSecurityJsonRpcNamedParameter theObject, String theValue) {
		theObject.setUsername(theValue);
	}

}
