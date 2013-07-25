package net.svcret.admin.client.ui.config.monitor;

import com.google.gwt.user.client.rpc.AsyncCallback;

import net.svcret.admin.shared.IAsyncLoadCallback;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.GMonitorRule;
import net.svcret.admin.shared.model.ModelUpdateRequest;

public class EditMonitorRulePanel extends BaseMonitorRulePanel {

	public EditMonitorRulePanel(long theRulePid) {
		Model.getInstance().loadMonitorRule(theRulePid, new IAsyncLoadCallback<GMonitorRule>(){
			@Override
			public void onSuccess(GMonitorRule theResult) {
				setRule(theResult);
			}});
		
	}

	@Override
	protected String getPanelTitle() {
		return "Edit Monitor Rule";
	}

}
