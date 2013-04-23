package net.svcret.admin.client.ui.config.sec;

import net.svcret.admin.client.ui.components.HtmlBr;
import net.svcret.admin.client.ui.components.HtmlLabel;
import net.svcret.admin.shared.model.GWsSecUsernameTokenClientSecurity;
import net.svcret.admin.shared.util.StringUtil;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class WsSecClientSecurity implements IProvidesViewAndEdit<GWsSecUsernameTokenClientSecurity> {

	@Override
	public Widget provideView(int theRow, GWsSecUsernameTokenClientSecurity theObject) {
		FlowPanel retVal = new FlowPanel();
		
		retVal.add(new Label("WS-Security"));
//		retVal.add(new HtmlBr());
		retVal.add(new Label("Username: " + StringUtil.defaultString(theObject.getUsername())));
//		retVal.add(new HtmlBr());
		retVal.add(new Label("Password: " + StringUtil.obscure(theObject.getPassword())));
		
		return retVal;
	}

	@Override
	public Widget provideEdit(int theRow, final GWsSecUsernameTokenClientSecurity theObject, final IValueChangeHandler theValueChangeHandler) {
		FlowPanel retVal = new FlowPanel();
		
		retVal.add(new Label("WS-Security"));
		retVal.add(new HtmlBr());
		
		String id = "wsscsun" + theRow;
		retVal.add(new HtmlLabel("Username: ", id));
		final TextBox usernametextBox = new TextBox();
		usernametextBox.getElement().setId(id);
		usernametextBox.setValue(theObject.getUsername());
		usernametextBox.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> theEvent) {
				theObject.setUsername(usernametextBox.getValue());
				theValueChangeHandler.onValueChange();
			}});
		retVal.add(usernametextBox);
		retVal.add(new HtmlBr());

		id = "wsscspw" + theRow;
		retVal.add(new HtmlLabel("Password: ", id));
		final TextBox passwordTextBox = new TextBox();
		passwordTextBox.getElement().setId(id);
		passwordTextBox.setValue(theObject.getPassword());
		passwordTextBox.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> theEvent) {
				theObject.setPassword(passwordTextBox.getValue());
				theValueChangeHandler.onValueChange();
			}});
		retVal.add(passwordTextBox);
		
		return retVal;
	}

}
