package net.svcret.admin.client.ui.config.sec;

import net.svcret.admin.client.ui.components.HtmlBr;
import net.svcret.admin.client.ui.components.HtmlLabel;
import net.svcret.admin.shared.util.StringUtil;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public abstract class BaseClientSecurityUsernameAndPasswordProvidesViewAndEdit<T> implements IProvidesViewAndEdit<T> {

	protected abstract String getModuleName();

	protected abstract String getPassword(T theObject);

	protected String getPasswordPrompt() {
		return "Password: ";
	}

	protected abstract String getUsername(T theObject);

	protected String getUsernamePrompt() {
		return "Username: ";
	}

	@Override
	public Widget provideEdit(int theRow, final T theObject, final IValueChangeHandler theValueChangeHandler) {
		FlowPanel retVal = new FlowPanel();

		retVal.add(new Label(getModuleName()));
		retVal.add(new HtmlBr());

		String id = "wsscsun" + theRow;
		retVal.add(new HtmlLabel(getUsernamePrompt(), id));
		final TextBox usernametextBox = new TextBox();
		usernametextBox.getElement().setId(id);
		usernametextBox.setValue(getUsername(theObject));
		usernametextBox.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> theEvent) {
				setUsername(theObject, usernametextBox.getValue());
				theValueChangeHandler.onValueChange();
			}
		});
		retVal.add(usernametextBox);
		retVal.add(new HtmlBr());

		id = "wsscspw" + theRow;
		retVal.add(new HtmlLabel(getPasswordPrompt(), id));
		final TextBox passwordTextBox = new TextBox();
		passwordTextBox.getElement().setId(id);
		passwordTextBox.setValue(getPassword(theObject));
		passwordTextBox.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> theEvent) {
				setPassword(theObject, passwordTextBox.getValue());
				theValueChangeHandler.onValueChange();
			}
		});
		retVal.add(passwordTextBox);

		return retVal;
	}

	@Override
	public Widget provideView(int theRow, T theObject) {
		FlowPanel retVal = new FlowPanel();

		retVal.add(new Label(getModuleName()));
		retVal.add(new Label(getUsernamePrompt() + StringUtil.defaultString(getUsername(theObject))));
		retVal.add(new Label(getPasswordPrompt() + StringUtil.obscure(getPassword(theObject))));

		return retVal;
	}

	protected abstract void setPassword(T theObject, String theValue);

	protected abstract void setUsername(T theObject, String theValue);
}
