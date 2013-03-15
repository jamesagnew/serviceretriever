package ca.uhn.sail.proxy.admin.client.ui.config;

import ca.uhn.sail.proxy.admin.client.ui.components.HtmlLabel;
import ca.uhn.sail.proxy.admin.client.ui.components.LoadingSpinner;
import ca.uhn.sail.proxy.admin.shared.util.StringUtil;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

public class EditServiceBasicPropertiesPanel extends FlowPanel {

	private TextBox myIdTextBox;
	private HtmlLabel myIdLabel;
	private HtmlLabel myNameLabel;
	private TextBox myNameTextBox;
	private Label myErrorLabel;
	private LoadingSpinner mySpinner;
	private Grid myFormGrid;
	private CheckBox myActiveCheckbox;

	public EditServiceBasicPropertiesPanel(String theId, String theName, boolean theActive, String theButtonText, ClickHandler theButtonHandler) {
		this(theId, theName, theActive);
		
		mySpinner = new LoadingSpinner();
		mySpinner.hideCompletely();
		add(mySpinner);

		Button addButton = new Button(theButtonText);
		addButton.addClickHandler(theButtonHandler);
		add(addButton);
	}

	public EditServiceBasicPropertiesPanel(String theId, String theName, boolean theActive) {
		myFormGrid = new Grid(3, 2);
		add(myFormGrid);

		/*
		 * Id
		 */
		myIdLabel = new HtmlLabel("ID", "elem_id");
		myFormGrid.setWidget(0, 0, myIdLabel);

		myIdTextBox = new TextBox();
		myIdTextBox.setValue(theId);
		myIdTextBox.getElement().setId("elem_id");
		myFormGrid.setWidget(0, 1, myIdTextBox);

		/*
		 * Name
		 */
		myNameLabel = new HtmlLabel("Name", "elem_name");
		myFormGrid.setWidget(1, 0, myNameLabel);
		myNameTextBox = new TextBox();
		myNameTextBox.setValue(theName);
		myNameTextBox.getElement().setId("elem_name");
		myFormGrid.setWidget(1, 1, myNameTextBox);
		
		/*
		 * Active
		 */
		myActiveCheckbox = new CheckBox("Active");
		myActiveCheckbox.setValue(theActive);
		myFormGrid.setWidget(2, 1, myActiveCheckbox);
		
		myErrorLabel = new Label();
		myErrorLabel.setStyleName("hidden");
		add(myErrorLabel);

	}

	public boolean isActive() {
		return myActiveCheckbox.getValue();
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
}
