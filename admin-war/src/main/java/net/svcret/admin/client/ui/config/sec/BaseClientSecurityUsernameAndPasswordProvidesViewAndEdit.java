package net.svcret.admin.client.ui.config.sec;

import net.svcret.admin.client.ui.components.HtmlBr;
import net.svcret.admin.client.ui.components.TwoColumnGrid;
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

		TwoColumnGrid grid = new TwoColumnGrid();
		retVal.add(grid);
		
		final TextBox usernametextBox = new TextBox();
		usernametextBox.setValue(getUsername(theObject));
		usernametextBox.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> theEvent) {
				setUsername(theObject, usernametextBox.getValue());
				theValueChangeHandler.onValueChange();
			}
		});
		grid.addRow(getUsernamePrompt(), usernametextBox);

		addToEditGridAfterUsername(grid, theObject, theValueChangeHandler);
		
		final TextBox passwordTextBox = new TextBox();
		passwordTextBox.setValue(getPassword(theObject));
		passwordTextBox.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> theEvent) {
				setPassword(theObject, passwordTextBox.getValue());
				theValueChangeHandler.onValueChange();
			}
		});
		grid.addRow(getPasswordPrompt(), passwordTextBox);

		addToEditGridAfterPassword(grid, theObject, theValueChangeHandler);
		
		return retVal;
	}

	@SuppressWarnings("unused") 
	protected void addToEditGridAfterUsername(TwoColumnGrid theGrid, T theObject, net.svcret.admin.client.ui.config.sec.IProvidesViewAndEdit.IValueChangeHandler theValueChangeHandler) {
		// nothing
	}

	@SuppressWarnings("unused") 
	protected void addToEditGridAfterPassword(TwoColumnGrid theGrid, T theObject, net.svcret.admin.client.ui.config.sec.IProvidesViewAndEdit.IValueChangeHandler theValueChangeHandler) {
		// nothing
	}

	@SuppressWarnings("unused") 
	protected void addToViewGridAfterUsername(TwoColumnGrid theGrid, T theObject) {
		// nothing
	}

	@SuppressWarnings("unused") 
	protected void addToViewGridAfterPassword(TwoColumnGrid theGrid, T theObject) {
		// nothing
	}

	
	@Override
	public Widget provideView(int theRow, T theObject) {
		FlowPanel retVal = new FlowPanel();

		retVal.add(new Label(getModuleName()));
		
		TwoColumnGrid grid = new TwoColumnGrid();
		retVal.add(grid);

		grid.addRow(getUsernamePrompt() ,new Label(StringUtil.defaultString(getUsername(theObject))));
		addToViewGridAfterUsername(grid, theObject);
		
		grid.addRow(getPasswordPrompt(), new Label(StringUtil.obscure(getPassword(theObject))));
		addToViewGridAfterPassword(grid, theObject);

		return retVal;
	}

	protected abstract void setPassword(T theObject, String theValue);

	protected abstract void setUsername(T theObject, String theValue);
}
