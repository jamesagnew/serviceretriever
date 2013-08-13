package net.svcret.admin.client.ui.config.monitor;

import net.svcret.admin.shared.IAsyncLoadCallback;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.BaseGMonitorRule;

public class EditMonitorRulePanel extends BaseMonitorRulePanel {

	public EditMonitorRulePanel(long theRulePid) {
		Model.getInstance().loadMonitorRule(theRulePid, new IAsyncLoadCallback<BaseGMonitorRule>(){
			@Override
			public void onSuccess(BaseGMonitorRule theResult) {
				// TODO: clone this so that changes don't affect the cached version in memory until we save
				setRule(theResult);
			}});
		
	}

	@Override
	protected String getPanelTitle() {
		return "Edit Monitor Rule";
	}

}
