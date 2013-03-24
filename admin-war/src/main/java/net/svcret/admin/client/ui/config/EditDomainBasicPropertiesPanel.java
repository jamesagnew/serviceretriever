package net.svcret.admin.client.ui.config;

import net.svcret.admin.client.ui.components.HtmlLabel;
import net.svcret.admin.client.ui.components.LoadingSpinner;
import net.svcret.admin.client.ui.components.PButton;
import net.svcret.admin.shared.util.StringUtil;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

public class EditDomainBasicPropertiesPanel extends FlowPanel {

	private TextBox myIdTextBox;
	private HtmlLabel myIdLabel;
	private HtmlLabel myNameLabel;
	private TextBox myNameTextBox;
	private Label myErrorLabel;
	private LoadingSpinner mySpinner;

	public EditDomainBasicPropertiesPanel(String theId, String theName, String theButtonText, ClickHandler theButtonHandler) {
		Grid formGrid = new Grid(2, 2);
		add(formGrid);

		/*
		 * Id
		 */
		myIdLabel = new HtmlLabel("ID", "elem_id");
		formGrid.setWidget(0, 0, myIdLabel);

		myIdTextBox = new TextBox();
		myIdTextBox.setValue(theId);
		myIdTextBox.getElement().setId("elem_id");
		formGrid.setWidget(0, 1, myIdTextBox);

		/*
		 * Name
		 */
		myNameLabel = new HtmlLabel("Name", "elem_name");
		formGrid.setWidget(1, 0, myNameLabel);
		myNameTextBox = new TextBox();
		myNameTextBox.setValue(theName);
		myNameTextBox.getElement().setId("elem_name");
		formGrid.setWidget(1, 1, myNameTextBox);

		myErrorLabel = new Label();
		myErrorLabel.setStyleName("hidden");
		add(myErrorLabel);

		mySpinner = new LoadingSpinner();
		mySpinner.hideCompletely();
		add(mySpinner);

		Button addButton = new PButton(theButtonText);
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

	public String getId() {
		return myIdTextBox.getValue();
	}

	public String getName() {
		return myNameTextBox.getValue();
	}

	public void showError(String theMessage) {
		myErrorLabel.setStyleName("errorLabel");
		myErrorLabel.setText(theMessage);
	}

	public void showMessage(String theMessage, boolean theShowSpinner) {
		mySpinner.showMessage(theMessage, theShowSpinner);		
	}
}
