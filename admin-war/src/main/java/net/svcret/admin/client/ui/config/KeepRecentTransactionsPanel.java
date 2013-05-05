package net.svcret.admin.client.ui.config;

import static net.svcret.admin.client.AdminPortal.*;
import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.client.ui.components.HtmlH1;
import net.svcret.admin.client.ui.components.LoadingSpinner;
import net.svcret.admin.client.ui.components.TwoColumnGrid;
import net.svcret.admin.shared.model.BaseGKeepsRecentMessages;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Label;

public class KeepRecentTransactionsPanel extends FlowPanel {

	private IntegerBox mySuccessTextbox;
	private IntegerBox myFailTextbox;
	private IntegerBox myFaultTextbox;
	private IntegerBox mySecurityFailTextbox;
	private LoadingSpinner myLoadingSpinner;

	public KeepRecentTransactionsPanel(BaseGKeepsRecentMessages<?> theKeepsRecentTransactions) {
		
		add(new HtmlH1(MSGS.keepRecentTransactionsPanel_Title()));
		add(new Label(MSGS.keepRecentTransactionsPanel_Description()));
		
		myLoadingSpinner = new LoadingSpinner();
		myLoadingSpinner.hideCompletely();
		add(myLoadingSpinner);
		
		TwoColumnGrid grid = new TwoColumnGrid();
		add(grid);
		
		mySuccessTextbox = new IntegerBox();
		mySuccessTextbox.setValue(theKeepsRecentTransactions.getKeepNumRecentTransactionsSuccess());
		grid.addRow(MSGS.keepRecentTransactionsPanel_OutcomeSuccess(), mySuccessTextbox);
		grid.addDescription(MSGS.keepRecentTransactionsPanel_OutcomeSuccessDesc());
		
		myFaultTextbox = new IntegerBox();
		myFaultTextbox.setValue(theKeepsRecentTransactions.getKeepNumRecentTransactionsFault());
		grid.addRow(MSGS.keepRecentTransactionsPanel_OutcomeFault(), myFaultTextbox);
		grid.addDescription(MSGS.keepRecentTransactionsPanel_OutcomeFaultDesc());
		
		myFailTextbox = new IntegerBox();
		myFailTextbox.setValue(theKeepsRecentTransactions.getKeepNumRecentTransactionsFail());
		grid.addRow(MSGS.keepRecentTransactionsPanel_OutcomeFail(), myFailTextbox);
		grid.addDescription(MSGS.keepRecentTransactionsPanel_OutcomeFailDesc());

		mySecurityFailTextbox = new IntegerBox();
		mySecurityFailTextbox.setValue(theKeepsRecentTransactions.getKeepNumRecentTransactionsSecurityFail());
		grid.addRow(MSGS.keepRecentTransactionsPanel_OutcomeSecurityFail(), mySecurityFailTextbox);
		grid.addDescription(MSGS.keepRecentTransactionsPanel_OutcomeSecurityFailDesc());

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
