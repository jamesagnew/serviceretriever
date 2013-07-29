package net.svcret.admin.client.ui.config.domain;

import net.svcret.admin.client.ui.components.EditableField;
import net.svcret.admin.client.ui.components.LoadingSpinner;
import net.svcret.admin.client.ui.components.PButton;
import net.svcret.admin.client.ui.components.TwoColumnGrid;
import net.svcret.admin.client.ui.config.KeepRecentTransactionsPanel;
import net.svcret.admin.shared.model.GDomain;
import net.svcret.admin.shared.util.StringUtil;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class EditDomainBasicPropertiesPanel extends FlowPanel {

	private HasValue<String> myIdTextBox;
	private HasValue<String> myNameTextBox;
	private LoadingSpinner mySpinner;
//	private KeepRecentTransactionsPanel myKeepRecentTransactionsPanel;
	private GDomain myDomain;

	public EditDomainBasicPropertiesPanel(final GDomain theDomain, String theButtonText, ClickHandler theButtonHandler, ImageResource theButtonIcon, boolean theNewDomain) {
		myDomain = theDomain;
		
		TwoColumnGrid formGrid = new TwoColumnGrid();
		add(formGrid);

		/*
		 * Id
		 */
		myIdTextBox = theNewDomain ? new TextBox() : new EditableField();
		myIdTextBox.setValue(theDomain.getId());
		formGrid.addRow("ID", (Widget)myIdTextBox);

		/*
		 * Name
		 */
		myNameTextBox = theNewDomain ? new TextBox() : new EditableField();
		myNameTextBox.setValue(theDomain.getName());
		formGrid.addRow("Name",(Widget)myNameTextBox);

		mySpinner = new LoadingSpinner();
		mySpinner.hideCompletely();
		add(mySpinner);

		Button addButton = new PButton(theButtonIcon, theButtonText);
		addButton.addClickHandler(theButtonHandler);
		add(addButton);
		
//		myKeepRecentTransactionsPanel = new KeepRecentTransactionsPanel(theDomain);
//		add(myKeepRecentTransactionsPanel);
		
		
	}

	public boolean validateValuesAndApplyValues() {
//		if (!myKeepRecentTransactionsPanel.validateAndShowErrorIfNotValid()) {
//			return false;
//		}
		
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

		myDomain.setId(myIdTextBox.getValue());
		myDomain.setName(myNameTextBox.getValue());
//		myKeepRecentTransactionsPanel.populateDto(myDomain);

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
