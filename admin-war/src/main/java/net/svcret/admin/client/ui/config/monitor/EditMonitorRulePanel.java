package net.svcret.admin.client.ui.config.monitor;

import net.svcret.admin.shared.IAsyncLoadCallback;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.BaseGMonitorRule;

public class EditMonitorRulePanel extends BaseMonitorRulePanel {

	public EditMonitorRulePanel(long theRulePid) {
		Model.getInstance().loadMonitorRule(theRulePid, new IAsyncLoadCallback<BaseGMonitorRule>(){
			@Override
			public void onSuccess(BaseGMonitorRule theResult) {
				setRule(theResult);
			}});
		
	}

	@Override
	protected String getPanelTitle() {
		return "Edit Monitor Rule";
	}

}
