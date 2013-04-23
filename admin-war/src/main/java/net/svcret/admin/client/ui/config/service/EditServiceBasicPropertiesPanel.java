package net.svcret.admin.client.ui.config.service;

import net.svcret.admin.client.ui.components.LoadingSpinner;
import net.svcret.admin.client.ui.components.PButton;
import net.svcret.admin.client.ui.components.TwoColumnGrid;
import net.svcret.admin.shared.model.GService;
import net.svcret.admin.shared.util.StringUtil;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextBox;

public class EditServiceBasicPropertiesPanel extends FlowPanel {

	private TextBox myIdTextBox;
	private TextBox myNameTextBox;
	private LoadingSpinner mySpinner;

	public EditServiceBasicPropertiesPanel(final GService theService, String theButtonText, ClickHandler theButtonHandler, ImageResource theButtonIcon) {
		TwoColumnGrid formGrid = new TwoColumnGrid();
		add(formGrid);

		/*
		 * Id
		 */
		myIdTextBox = new TextBox();
		myIdTextBox.setValue(theService.getId());
		myIdTextBox.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> theEvent) {
				theService.setId(myIdTextBox.getValue());
			}
		});
		formGrid.addRow("ID", myIdTextBox);

		/*
		 * Name
		 */
		myNameTextBox = new TextBox();
		myNameTextBox.setValue(theService.getName());
		myNameTextBox.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> theEvent) {
				theService.setName(myNameTextBox.getValue());
			}
		});
		formGrid.addRow("Name",myNameTextBox);

		mySpinner = new LoadingSpinner();
		mySpinner.hideCompletely();
		add(mySpinner);

		Button addButton = new PButton(theButtonIcon, theButtonText);
		addButton.addClickHandler(theButtonHandler);
		add(addButton);
	}

	public boolean validateValues() {
		String id = myIdTextBox.getValue();
		if (StringUtil.isBlank(id)) {
			showError("You must supply an ID");
			return false;
		}

		String name = myNameTextBox.getValue();
		if (StringUtil.isBlank(name)) {
			showError("You must supply a name");
			return false;
		}

		return true;
	}

	public void showSpinner() {
		mySpinner.show();
	}

	public void hideSpinner() {
		mySpinner.hideCompletely();
	}

	public void showError(String theMessage) {
		mySpinner.showMessage(theMessage, false);
	}

	public void showMessage(String theMessage, boolean theShowSpinner) {
		mySpinner.showMessage(theMessage, theShowSpinner);		
	}
	
	
	
}
