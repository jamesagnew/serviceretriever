package net.svcret.admin.client.ui.config.monitor;

import net.svcret.admin.client.ui.log.BaseViewSavedTransactionPanel;
import net.svcret.admin.shared.model.DtoMonitorRuleActiveCheckOutcome;

public class ViewActiveCheckOutcomeDetailPanel extends BaseViewSavedTransactionPanel {

	private DtoMonitorRuleActiveCheckOutcome myResult;

	public void setMessage(final DtoMonitorRuleActiveCheckOutcome theResult) {
		myResult = theResult;
		setSavedTransaction(theResult, null);
	}

	@Override
	protected void addToStartOfTopGrid() {
		// nothing
	}

	@Override
	protected void addToTransactionSectionOfGrid() {
		// nothing
	}

	@Override
	protected String getPanelTitle() {
		return "Outcome";
	}


}
