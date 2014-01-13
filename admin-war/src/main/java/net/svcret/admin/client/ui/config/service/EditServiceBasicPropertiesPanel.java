package net.svcret.admin.client.ui.config.service;

import net.svcret.admin.client.ui.components.EditableField;
import net.svcret.admin.client.ui.components.LoadingSpinner;
import net.svcret.admin.client.ui.components.PButton;
import net.svcret.admin.client.ui.components.TwoColumnGrid;
import net.svcret.admin.shared.model.GService;
import net.svcret.admin.shared.util.StringUtil;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class EditServiceBasicPropertiesPanel extends FlowPanel {

	private HasValue<String> myIdTextBox;
	private HasValue<String> myNameTextBox;
	private LoadingSpinner mySpinner;
	private GService myService;
	private HasValue<String> myDescriptionTextBox;

	public EditServiceBasicPropertiesPanel(final GService theService, String theButtonText, ClickHandler theButtonHandler, ImageResource theButtonIcon, boolean theNewService) {
		myService = theService;

		TwoColumnGrid formGrid = new TwoColumnGrid();
		add(formGrid);

		/*
		 * Id
		 */
		myIdTextBox = theNewService ? new TextBox() : new EditableField();
		myIdTextBox.setValue(theService.getId());
		formGrid.addRow("ID", (Widget)myIdTextBox);

		/*
		 * Name
		 */
		myNameTextBox = theNewService ? new TextBox() : new EditableField();
		myNameTextBox.setValue(theService.getName());
		formGrid.addRow("Name", (Widget)myNameTextBox);

		/*
		 * Name
		 */
		myDescriptionTextBox = theNewService ? new TextBox() : new EditableField().setMultiline(true).setProcessHtml(true);
		myDescriptionTextBox.setValue(theService.getDescription());
		formGrid.addRow("Description", (Widget)myDescriptionTextBox);

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
			name = id;
		}

		myService.setName(myNameTextBox.getValue());
		myService.setId(myIdTextBox.getValue());
		myService.setDescription(myDescriptionTextBox.getValue());

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
