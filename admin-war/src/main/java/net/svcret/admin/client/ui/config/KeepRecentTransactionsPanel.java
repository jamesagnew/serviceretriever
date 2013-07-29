package net.svcret.admin.client.ui.config;

import static net.svcret.admin.client.AdminPortal.*;
import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.client.ui.components.HtmlH1;
import net.svcret.admin.client.ui.components.LoadingSpinner;
import net.svcret.admin.client.ui.components.TwoColumnGrid;
import net.svcret.admin.shared.model.BaseGKeepsRecentMessages;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Label;

public class KeepRecentTransactionsPanel extends FlowPanel {

	private IntegerBox mySuccessTextbox;
	private IntegerBox myFailTextbox;
	private IntegerBox myFaultTextbox;
	private IntegerBox mySecurityFailTextbox;
	private LoadingSpinner myLoadingSpinner;
	private TwoColumnGrid myGrid;

	public KeepRecentTransactionsPanel(BaseGKeepsRecentMessages<?> theKeepsRecentTransactions) {

		boolean canInherit = theKeepsRecentTransactions.isCanInheritKeepNumRecentTransactions();
		int descRows = canInherit ? 2 : 1;

		add(new HtmlH1(MSGS.keepRecentTransactionsPanel_Title()));
		add(new Label(MSGS.keepRecentTransactionsPanel_Description()));

		myLoadingSpinner = new LoadingSpinner();
		myLoadingSpinner.hideCompletely();
		add(myLoadingSpinner);

		myGrid = new TwoColumnGrid();
		add(myGrid);

		{
			mySuccessTextbox = new IntegerBox();
			mySuccessTextbox.setValue(theKeepsRecentTransactions.getKeepNumRecentTransactionsSuccess());
			myGrid.addRow(MSGS.keepRecentTransactionsPanel_OutcomeSuccess(), mySuccessTextbox);
			myGrid.addDescriptionToRight(MSGS.keepRecentTransactionsPanel_OutcomeSuccessDesc(), descRows);
			if (canInherit) {
				Integer inherited = theKeepsRecentTransactions.getInheritedKeepNumRecentTransactionsSuccess();
				Integer value = theKeepsRecentTransactions.getKeepNumRecentTransactionsSuccess();
				addInheritControl(mySuccessTextbox, inherited, value);
			}
		}

		{
			myFaultTextbox = new IntegerBox();
			myFaultTextbox.setValue(theKeepsRecentTransactions.getKeepNumRecentTransactionsFault());
			myGrid.addRow(MSGS.keepRecentTransactionsPanel_OutcomeFault(), myFaultTextbox);
			myGrid.addDescriptionToRight(MSGS.keepRecentTransactionsPanel_OutcomeFaultDesc(), descRows);
			if (canInherit) {
				Integer inherited = theKeepsRecentTransactions.getInheritedKeepNumRecentTransactionsFault();
				Integer value = theKeepsRecentTransactions.getKeepNumRecentTransactionsFault();
				addInheritControl(myFaultTextbox, inherited, value);
			}
		}

		{
			myFailTextbox = new IntegerBox();
			myFailTextbox.setValue(theKeepsRecentTransactions.getKeepNumRecentTransactionsFail());
			myGrid.addRow(MSGS.keepRecentTransactionsPanel_OutcomeFail(), myFailTextbox);
			myGrid.addDescriptionToRight(MSGS.keepRecentTransactionsPanel_OutcomeFailDesc(), descRows);
			if (canInherit) {
				Integer inherited = theKeepsRecentTransactions.getInheritedKeepNumRecentTransactionsFail();
				Integer value = theKeepsRecentTransactions.getKeepNumRecentTransactionsFail();
				addInheritControl(myFailTextbox, inherited, value);
			}
		}

		{
			mySecurityFailTextbox = new IntegerBox();
			mySecurityFailTextbox.setValue(theKeepsRecentTransactions.getKeepNumRecentTransactionsSecurityFail());
			myGrid.addRow(MSGS.keepRecentTransactionsPanel_OutcomeSecurityFail(), mySecurityFailTextbox);
			myGrid.addDescriptionToRight(MSGS.keepRecentTransactionsPanel_OutcomeSecurityFailDesc(), descRows);
			if (canInherit) {
				Integer inherited = theKeepsRecentTransactions.getInheritedKeepNumRecentTransactionsSecurityFail();
				Integer value = theKeepsRecentTransactions.getKeepNumRecentTransactionsSecurityFail();
				addInheritControl(mySecurityFailTextbox, inherited, value);
			}
		}
	}

	private void addInheritControl(final IntegerBox textBox, Integer theInheritedValue, Integer theValue) {
		if (theInheritedValue != null) {
			final CheckBox inheritCheckbox = new CheckBox("Override inherited value: " + theInheritedValue);
			inheritCheckbox.addStyleName(CssConstants.RECENT_TRANSACTIONS_INHERIT_CHECKBOX);
			
			inheritCheckbox.setValue(theValue != null);
			myGrid.addRow("", inheritCheckbox);
			inheritCheckbox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> theEvent) {
					if (theEvent.getValue()) {
						textBox.setValue(0, false);
					} else {
						textBox.setValue(null, false);
					}
				}
			});
			textBox.addValueChangeHandler(new ValueChangeHandler<Integer>() {
				@Override
				public void onValueChange(ValueChangeEvent<Integer> theEvent) {
					if (theEvent.getValue() != null) {
						inheritCheckbox.setValue(false, false);
					} else {
						inheritCheckbox.setValue(true, false);
					}
				}
			});
		} else {
			myGrid.addRow("", "");
		}
	}

	private static final int MAX = 100;

	public boolean validateAndShowErrorIfNotValid() {

		mySuccessTextbox.removeStyleName(CssConstants.TEXTBOX_WITH_ERR);
		myFailTextbox.removeStyleName(CssConstants.TEXTBOX_WITH_ERR);
		mySecurityFailTextbox.removeStyleName(CssConstants.TEXTBOX_WITH_ERR);
		myFaultTextbox.removeStyleName(CssConstants.TEXTBOX_WITH_ERR);

		Integer sval = mySuccessTextbox.getValue();
		if (sval != null && (sval < 0 || sval > MAX)) {
			myLoadingSpinner.showMessage(MSGS.keepRecentTransactionsPanel_AlertInvalidValue(sval.toString(), Integer.toString(MAX)), false);
			mySuccessTextbox.addStyleName(CssConstants.TEXTBOX_WITH_ERR);
			mySuccessTextbox.setFocus(true);
			return false;
		}

		sval = myFailTextbox.getValue();
		if (sval != null && (sval < 0 || sval > MAX)) {
			myLoadingSpinner.showMessage(MSGS.keepRecentTransactionsPanel_AlertInvalidValue(sval.toString(), Integer.toString(MAX)), false);
			myFailTextbox.addStyleName(CssConstants.TEXTBOX_WITH_ERR);
			myFailTextbox.setFocus(true);
			return false;
		}

		sval = mySecurityFailTextbox.getValue();
		if (sval != null && (sval < 0 || sval > MAX)) {
			myLoadingSpinner.showMessage(MSGS.keepRecentTransactionsPanel_AlertInvalidValue(sval.toString(), Integer.toString(MAX)), false);
			mySecurityFailTextbox.addStyleName(CssConstants.TEXTBOX_WITH_ERR);
			mySecurityFailTextbox.setFocus(true);
			return false;
		}

		sval = myFaultTextbox.getValue();
		if (sval != null && (sval < 0 || sval > MAX)) {
			myLoadingSpinner.showMessage(MSGS.keepRecentTransactionsPanel_AlertInvalidValue(sval.toString(), Integer.toString(MAX)), false);
			myFaultTextbox.addStyleName(CssConstants.TEXTBOX_WITH_ERR);
			myFaultTextbox.setFocus(true);
			return false;
		}

		return true;
	}

	public void populateDto(BaseGKeepsRecentMessages<?> theDto) {

		theDto.setKeepNumRecentTransactionsSuccess(mySuccessTextbox.getValue());
		theDto.setKeepNumRecentTransactionsFail(myFailTextbox.getValue());
		theDto.setKeepNumRecentTransactionsSecurityFail(mySecurityFailTextbox.getValue());
		theDto.setKeepNumRecentTransactionsFault(myFaultTextbox.getValue());

	}

}
